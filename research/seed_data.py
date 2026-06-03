"""Утилиты для создания схемы и генерации тестовых данных."""

import os
import random
from datetime import datetime, timedelta

import psycopg2
from psycopg2.extras import execute_values
from faker import Faker

fake = Faker('ru_RU')
random.seed(42)

DB_CONFIG = dict(
    host='localhost', port=5434,
    dbname='food_delivery_test',
    user='root', password='postgres',
)

MIGRATIONS_DIR = os.path.join(
    os.path.dirname(__file__),
    '..', 'food-delivery-api', 'src', 'main', 'resources', 'db', 'migrations',
)

ORDER_STATUSES  = ['pending', 'confirmed', 'preparing', 'ready', 'delivering', 'delivered', 'cancelled']
VEHICLE_TYPES   = ['car', 'bicycle', 'walking']
CUISINE_TYPES   = ['Итальянская', 'Японская', 'Китайская', 'Русская', 'Американская', 'Грузинская', 'Французская']
PASSWORD_HASH   = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj/RK.s5uFDi'


def get_connection():
    return psycopg2.connect(**DB_CONFIG)


def setup_schema(conn):
    conn.autocommit = True
    with conn.cursor() as cur:
        cur.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO public;")
    conn.autocommit = False

    migrations = [
        'V1__create_types.sql',
        'V2__create_tables.sql',
        'V3__add_constaraints.sql',
        'V4__create_indexes.sql',
        'V5__create_function.sql',
        'V6__create_triggers.sql',
    ]
    with conn.cursor() as cur:
        for name in migrations:
            path = os.path.join(MIGRATIONS_DIR, name)
            with open(path) as f:
                cur.execute(f.read())
            print(f'  Применена миграция: {name}')
    conn.commit()


def seed_base_data(conn, n_customers=500, n_couriers=50, n_restaurants=50):
    """Наполняет базовые таблицы: клиенты, курьеры, рестораны, категории, блюда."""
    with conn.cursor() as cur:
        # Категории блюд
        cats = [
            ('Бургеры', 1), ('Пицца', 2), ('Суши', 3), ('Салаты', 4), ('Супы', 5),
            ('Десерты', 6), ('Напитки', 7), ('Закуски', 8), ('Паста', 9), ('Стейки', 10),
        ]
        execute_values(cur,
            "INSERT INTO dish_categories (name, description, display_order) VALUES %s RETURNING category_id",
            [(n, f'Категория {n}', o) for n, o in cats])
        category_ids = [r[0] for r in cur.fetchall()]

        # Клиенты
        execute_values(cur,
            "INSERT INTO customers (full_name, email, phone, password_hash) VALUES %s RETURNING customer_id",
            [(fake.name(), f'customer{i}@test.com', f'+79001{i:06d}', PASSWORD_HASH)
             for i in range(n_customers)])
        customer_ids = [r[0] for r in cur.fetchall()]

        # Курьеры
        execute_values(cur,
            """INSERT INTO couriers
               (full_name, email, phone, password_hash, employee_date, area_of_work, vehicle_type, rating)
               VALUES %s RETURNING courier_id""",
            [(fake.name(), f'courier{i}@test.com', f'+79111{i:06d}', PASSWORD_HASH,
              datetime(2020, 1, 1) + timedelta(days=random.randint(0, 1000)),
              random.choice(['Центральный', 'Северный', 'Южный']),
              random.choice(VEHICLE_TYPES),
              round(random.uniform(3.5, 5.0), 2))
             for i in range(n_couriers)])
        courier_ids = [r[0] for r in cur.fetchall()]

        # Рестораны
        execute_values(cur,
            """INSERT INTO restaurants
               (name, cuisine_type, address, phone, email, latitude, longitude, opening_time, closing_time)
               VALUES %s RETURNING restaurant_id""",
            [(f'Ресторан {i + 1}', random.choice(CUISINE_TYPES),
              f'ул. Тестовая, д. {i + 1}', f'+74951{i:06d}', f'rest{i}@test.com',
              round(random.uniform(55.50, 55.90), 6), round(random.uniform(37.30, 37.90), 6),
              '10:00', '23:00')
             for i in range(n_restaurants)])
        restaurant_ids = [r[0] for r in cur.fetchall()]

        # Блюда (4 на ресторан)
        execute_values(cur,
            """INSERT INTO dishes
               (restaurant_id, name, price, is_available, is_spicy, preparation_time, calories, weight_grams)
               VALUES %s RETURNING dish_id""",
            [(rid, f'Блюдо {j + 1}', round(random.uniform(200.0, 1500.0), 2),
              True, random.random() < 0.3, random.randint(15, 60),
              random.randint(200, 1200), random.randint(150, 800))
             for rid in restaurant_ids for j in range(4)])
        dish_ids = [r[0] for r in cur.fetchall()]

        # Привязка блюд к категориям
        execute_values(cur,
            "INSERT INTO dish_category_mapping (dish_id, category_id) VALUES %s",
            [(did, random.choice(category_ids)) for did in dish_ids])

    conn.commit()
    return customer_ids, restaurant_ids, courier_ids, dish_ids


def seed_orders(conn, n_orders, customer_ids, restaurant_ids, courier_ids, dish_ids,
                batch_size=10_000):
    """Добавляет N заказов порциями."""
    base_dt = datetime.now() - timedelta(days=730)

    with conn.cursor() as cur:
        cur.execute("SELECT dish_id, restaurant_id FROM dishes")
        by_rest: dict = {}
        for did, rid in cur.fetchall():
            by_rest.setdefault(rid, []).append(did)

    inserted = 0
    counter  = 0
    with conn.cursor() as cur:
        while inserted < n_orders:
            n = min(batch_size, n_orders - inserted)

            orders_rows = []
            for _ in range(n):
                cid      = random.choice(customer_ids)
                rid      = random.choice(restaurant_ids)
                kur      = random.choice(courier_ids)
                subtotal = round(random.uniform(300.0, 3000.0), 2)
                fee      = round(random.uniform(0.0,   300.0),  2)
                disc     = round(random.uniform(0.0,   100.0),  2)
                orders_rows.append((
                    f'ORD{counter:010d}', cid, rid, kur,
                    fake.name(), f'+79{random.randint(100000000, 999999999)}',
                    'Ресторан', 'ул. Тестовая, 1',
                    round(random.uniform(55.50, 55.90), 6),
                    round(random.uniform(37.30, 37.90), 6),
                    subtotal, fee, disc,
                    round(subtotal + fee - disc, 2),
                    random.choice(ORDER_STATUSES),
                    base_dt + timedelta(seconds=random.randint(0, 730 * 86400)),
                ))
                counter += 1

            execute_values(cur,
                """INSERT INTO orders
                   (order_number, customer_id, restaurant_id, courier_id,
                    customer_name, customer_phone, restaurant_name, delivery_address,
                    delivery_latitude, delivery_longitude,
                    subtotal, delivery_fee, discount, total_amount, status, created_at)
                   VALUES %s RETURNING order_id, restaurant_id""",
                orders_rows)
            pairs = cur.fetchall()

            items_rows = []
            for oid, rid in pairs:
                pool = by_rest.get(rid, dish_ids[:4])
                for did in random.sample(pool, min(2, len(pool))):
                    items_rows.append((oid, did, round(random.uniform(200.0, 1500.0), 2), random.randint(1, 3)))
            execute_values(cur,
                "INSERT INTO order_items (order_id, dish_id, unit_price, quantity) VALUES %s",
                items_rows)

            conn.commit()
            inserted += n
            print(f'  Заказов вставлено: {inserted}/{n_orders}', end='\r', flush=True)
    print()


def truncate_orders(conn):
    with conn.cursor() as cur:
        cur.execute("TRUNCATE order_items, orders CASCADE")
    conn.commit()


def seed_restaurants_for_cache(conn, n):
    """Подготавливает ровно N активных ресторанов для теста кэша."""
    with conn.cursor() as cur:
        cur.execute("TRUNCATE restaurants CASCADE")

        execute_values(cur,
            """INSERT INTO restaurants
               (name, cuisine_type, address, phone, email, latitude, longitude,
                opening_time, closing_time, is_active)
               VALUES %s RETURNING restaurant_id""",
            [(f'Ресторан {i + 1}', random.choice(CUISINE_TYPES),
              f'ул. Ленина, д. {i + 1}', f'+74952{i:06d}', f'cr{i}@test.com',
              round(random.uniform(55.50, 55.90), 6), round(random.uniform(37.30, 37.90), 6),
              '10:00', '23:00', True)
             for i in range(n)])
        restaurant_ids = [r[0] for r in cur.fetchall()]
    conn.commit()
    return restaurant_ids


def seed_dishes_for_cache(conn, n_dishes):
    """Один ресторан с N доступными блюдами для теста кэша."""
    with conn.cursor() as cur:
        cur.execute("TRUNCATE restaurants CASCADE")

        cur.execute(
            """INSERT INTO restaurants
               (name, cuisine_type, address, phone, email, latitude, longitude, opening_time, closing_time)
               VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s) RETURNING restaurant_id""",
            ('Тестовый', 'Итальянская', 'ул. Тест, 1', '+74950000001',
             'trest@test.com', 55.75, 37.62, '10:00', '23:00'))
        rest_id = cur.fetchone()[0]

        cur.execute("SELECT category_id FROM dish_categories LIMIT 1")
        row = cur.fetchone()
        if row:
            cat_id = row[0]
        else:
            cur.execute(
                "INSERT INTO dish_categories (name, display_order) VALUES (%s, %s) RETURNING category_id",
                ('Основное', 1))
            cat_id = cur.fetchone()[0]

        execute_values(cur,
            """INSERT INTO dishes
               (restaurant_id, name, price, is_available, is_spicy, preparation_time, calories, weight_grams)
               VALUES %s RETURNING dish_id""",
            [(rest_id, f'Блюдо {j + 1}', round(random.uniform(200.0, 1500.0), 2),
              True, False, 20, 400, 300)
             for j in range(n_dishes)])
        dish_ids = [r[0] for r in cur.fetchall()]

        execute_values(cur,
            "INSERT INTO dish_category_mapping (dish_id, category_id) VALUES %s",
            [(did, cat_id) for did in dish_ids])

    conn.commit()
    return rest_id
