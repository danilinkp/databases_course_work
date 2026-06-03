import json
import random
import uuid
from datetime import datetime, timedelta
from decimal import Decimal

import psycopg2
from faker import Faker
from psycopg2.extras import register_uuid

fake = Faker('ru_RU')

DB_CONFIG = {
    'host': 'localhost',
    'port': 5433,
    'database': 'food_delivery',
    'user': 'root',
    'password': 'postgres'
}

register_uuid()

POSTAL_CODES = [str(random.randint(100000, 199999)) for _ in range(20)]


def get_connection():
    return psycopg2.connect(**DB_CONFIG)


def generate_customers(conn, count=5000):
    cursor = conn.cursor()
    customers = []

    for _ in range(count):
        customer_id = uuid.uuid4()
        created_at = fake.date_time_between(start_date='-2y', end_date='now')
        customers.append({
            'id': customer_id,
            'full_name': fake.name(),
            'email': fake.unique.email(),
            'phone': fake.unique.phone_number()[:20],
            'password_hash': '$2a$12$' + fake.sha256()[:53],
            'bonuses': random.randint(0, 5000),
            'is_active': random.choice([True, True, True, False]),
            'created_at': created_at,
        })

    for c in customers:
        cursor.execute("""
                       INSERT INTO customers (customer_id, full_name, email, phone, password_hash,
                                              bonuses, is_active, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (c['id'], c['full_name'], c['email'], c['phone'], c['password_hash'],
                             c['bonuses'], c['is_active'], c['created_at'], c['created_at']))

    conn.commit()
    cursor.close()
    return customers


def generate_customer_addresses(conn, customers):
    cursor = conn.cursor()
    addresses = []

    for customer in customers:
        num_addresses = random.randint(1, 3)
        for i in range(num_addresses):
            address_id = uuid.uuid4()
            addresses.append({
                'id': address_id,
                'customer_id': customer['id'],
                'region': fake.region(),
                'city': fake.city(),
                'street': fake.street_name(),
                'house': str(random.randint(1, 200)),
                'apartment': str(random.randint(1, 200)) if random.random() > 0.3 else None,
                'address_details': fake.text(max_nb_chars=100) if random.random() > 0.7 else None,
                'postal_code': random.choice(POSTAL_CODES),
                'latitude': Decimal(str(random.uniform(55.5, 56.0))),
                'longitude': Decimal(str(random.uniform(37.3, 37.9))),
                'is_default': i == 0,
                'created_at': customer['created_at'] + timedelta(days=random.randint(0, 30))
            })

    for a in addresses:
        cursor.execute("""
                       INSERT INTO customer_addresses (address_id, customer_id, region, city, street, house,
                                                       apartment, address_details, postal_code, latitude,
                                                       longitude, is_default, created_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (a['id'], a['customer_id'], a['region'], a['city'], a['street'], a['house'],
                             a['apartment'], a['address_details'], a['postal_code'],
                             a['latitude'], a['longitude'], a['is_default'], a['created_at']))

    conn.commit()
    cursor.close()
    return addresses


def generate_couriers(conn, count=1000):
    cursor = conn.cursor()
    couriers = []

    vehicle_types = ['car', 'bicycle', 'walking']

    for _ in range(count):
        courier_id = uuid.uuid4()
        created_at = fake.date_time_between(start_date='-3y', end_date='-1y')
        couriers.append({
            'id': courier_id,
            'full_name': fake.name(),
            'phone': fake.unique.phone_number()[:20],
            'email': fake.unique.email(),
            'password_hash': '$2a$12$' + fake.sha256()[:53],
            'employee_date': created_at.date(),
            'area_of_work': fake.city(),
            'vehicle_type': random.choice(vehicle_types),
            'rating': Decimal(str(random.uniform(3.5, 5.0))).quantize(Decimal('0.01')),
            'is_available': random.choice([True, False]),
            'is_active': random.choice([True, True, True, False]),
            'created_at': created_at
        })

    for c in couriers:
        cursor.execute("""
                       INSERT INTO couriers (courier_id, full_name, phone, email, password_hash,
                                             employee_date, area_of_work, vehicle_type, rating,
                                             is_available, is_active, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (c['id'], c['full_name'], c['phone'], c['email'], c['password_hash'],
                             c['employee_date'], c['area_of_work'], c['vehicle_type'], c['rating'],
                             c['is_available'], c['is_active'], c['created_at'], c['created_at']))

    conn.commit()
    cursor.close()
    return couriers


def generate_restaurants(conn, count=200):
    cursor = conn.cursor()
    restaurants = []

    cuisine_types = ['Итальянская', 'Японская', 'Русская', 'Американская', 'Китайская',
                     'Грузинская', 'Узбекская', 'Индийская', 'Французская', 'Мексиканская']

    for _ in range(count):
        restaurant_id = uuid.uuid4()
        created_at = fake.date_time_between(start_date='-2y', end_date='-6M')
        restaurants.append({
            'id': restaurant_id,
            'name': fake.company(),
            'cuisine_type': random.choice(cuisine_types),
            'rating': Decimal('0.00'),
            'address': fake.address(),
            'phone': fake.unique.phone_number()[:20],
            'email': fake.unique.company_email(),
            'latitude': Decimal(str(random.uniform(55.5, 56.0))),
            'longitude': Decimal(str(random.uniform(37.3, 37.9))),
            'opening_time': datetime.strptime(f"{random.randint(7, 10)}:00", "%H:%M").time(),
            'closing_time': datetime.strptime(f"{random.randint(21, 23)}:00", "%H:%M").time(),
            'is_active': random.choice([True, True, True, False]),
            'created_at': created_at
        })

    for r in restaurants:
        cursor.execute("""
                       INSERT INTO restaurants (restaurant_id, name, cuisine_type, rating, address, phone,
                                                email, latitude, longitude, opening_time, closing_time,
                                                is_active, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (r['id'], r['name'], r['cuisine_type'], r['rating'], r['address'],
                             r['phone'], r['email'], r['latitude'], r['longitude'],
                             r['opening_time'], r['closing_time'], r['is_active'],
                             r['created_at'], r['created_at']))

    conn.commit()
    cursor.close()
    return restaurants


def generate_delivery_zones(conn, restaurants):
    cursor = conn.cursor()
    zones = []

    for restaurant in restaurants:
        num_zones = random.randint(3, 8)
        selected_codes = random.sample(POSTAL_CODES, min(num_zones, len(POSTAL_CODES)))

        for postal_code in selected_codes:
            zone_id = uuid.uuid4()
            near_threshold = Decimal(str(random.choice([2, 3, 4])))
            far_threshold = near_threshold + Decimal(str(random.choice([2, 3, 4])))

            zones.append({
                'id': zone_id,
                'restaurant_id': restaurant['id'],
                'zone_name': f"Зона {postal_code}",
                'postal_code': postal_code,
                'delivery_fee': Decimal(str(random.choice([0, 99, 149, 199, 249]))),
                'near_threshold': near_threshold,
                'far_threshold': far_threshold,
                'far_zone_multiplier': Decimal(str(random.choice([1.3, 1.5, 1.8, 2.0]))),
                'fee_per_km': Decimal(str(random.choice([20, 25, 30, 35]))),
                'peak_surcharge': Decimal(str(random.choice([0, 30, 50, 70]))),
                'weekend_surcharge': Decimal(str(random.choice([0, 20, 30, 50]))),
                'min_order_amount': Decimal(str(random.choice([0, 300, 500, 800, 1000]))),
                'delivery_time': random.choice([30, 40, 45, 60, 75, 90]),
            })

    for z in zones:
        cursor.execute("""
                       INSERT INTO delivery_zones (zone_id, restaurant_id, zone_name, postal_code,
                                                   delivery_fee, near_threshold, far_threshold,
                                                   far_zone_multiplier, fee_per_km, peak_surcharge,
                                                   weekend_surcharge, min_order_amount, delivery_time)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (z['id'], z['restaurant_id'], z['zone_name'], z['postal_code'],
                             z['delivery_fee'], z['near_threshold'], z['far_threshold'],
                             z['far_zone_multiplier'], z['fee_per_km'], z['peak_surcharge'],
                             z['weekend_surcharge'], z['min_order_amount'], z['delivery_time']))

    conn.commit()
    cursor.close()
    return zones


def generate_dish_categories(conn):
    cursor = conn.cursor()
    categories = []

    category_names = [
        'Закуски', 'Салаты', 'Супы', 'Горячие блюда', 'Гарниры',
        'Роллы', 'Суши', 'Пицца', 'Паста', 'Бургеры',
        'Десерты', 'Напитки', 'Выпечка', 'Завтраки', 'Вегетарианское'
    ]

    for i, name in enumerate(category_names):
        category_id = uuid.uuid4()
        categories.append({
            'id': category_id,
            'name': name,
            'description': fake.text(max_nb_chars=150),
            'display_order': i,
            'created_at': fake.date_time_between(start_date='-2y', end_date='-1y')
        })

    for c in categories:
        cursor.execute("""
                       INSERT INTO dish_categories (category_id, name, description, display_order, created_at)
                       VALUES (%s, %s, %s, %s, %s)
                       """, (c['id'], c['name'], c['description'], c['display_order'], c['created_at']))

    conn.commit()
    cursor.close()
    return categories


def generate_dishes(conn, restaurants, categories, avg_per_restaurant=30):
    cursor = conn.cursor()
    dishes = []
    mappings = []

    dish_names = [
        'Цезарь', 'Греческий салат', 'Оливье', 'Борщ', 'Солянка',
        'Калифорния', 'Филадельфия', 'Маргарита', 'Пепперони', 'Карбонара',
        'Чизбургер', 'Тирамису', 'Чизкейк', 'Эспрессо', 'Капучино',
        'Том Ям', 'Паэлья', 'Лазанья', 'Стейк', 'Шаурма'
    ]

    for restaurant in restaurants:
        num_dishes = random.randint(20, avg_per_restaurant + 15)
        created_at_base = restaurant['created_at'] + timedelta(days=random.randint(1, 30))

        for _ in range(num_dishes):
            dish_id = uuid.uuid4()
            dish = {
                'id': dish_id,
                'restaurant_id': restaurant['id'],
                'name': random.choice(dish_names) + ' ' + fake.word().capitalize(),
                'description': fake.text(max_nb_chars=300),
                'price': Decimal(str(random.randint(150, 2500))),
                'image_url': f"https://example.com/dishes/{dish_id}.jpg",
                'is_available': random.choice([True, True, True, False]),
                'is_spicy': random.choice([True, False, False, False]),
                'preparation_time': random.choice([15, 20, 25, 30, 35, 40, 45]),
                'calories': random.randint(200, 1500),
                'weight_grams': random.randint(100, 800),
                'created_at': created_at_base + timedelta(days=random.randint(0, 30))
            }
            dishes.append(dish)

            # Каждое блюдо — от 1 до 3 категорий
            num_cats = random.randint(1, 3)
            selected_cats = random.sample(categories, num_cats)
            for cat in selected_cats:
                mappings.append({'dish_id': dish_id, 'category_id': cat['id']})

    for d in dishes:
        cursor.execute("""
                       INSERT INTO dishes (dish_id, restaurant_id, name, description, price, image_url,
                                           is_available, is_spicy, preparation_time, calories,
                                           weight_grams, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (d['id'], d['restaurant_id'], d['name'], d['description'], d['price'],
                             d['image_url'], d['is_available'], d['is_spicy'], d['preparation_time'],
                             d['calories'], d['weight_grams'], d['created_at'], d['created_at']))

    for m in mappings:
        cursor.execute("""
                       INSERT INTO dish_category_mapping (dish_id, category_id)
                       VALUES (%s, %s)
                       ON CONFLICT DO NOTHING
                       """, (m['dish_id'], m['category_id']))

    conn.commit()
    cursor.close()
    return dishes


def generate_orders(conn, customers, restaurants, couriers, addresses, count=15000):
    cursor = conn.cursor()
    orders = []

    statuses = ['pending', 'confirmed', 'preparing', 'ready', 'delivering', 'delivered', 'cancelled']
    weights = [5, 8, 8, 5, 10, 52, 12]
    payment_methods = ['cash', 'card', 'online', 'bonuses']

    active_customers = [c for c in customers if c['is_active']]
    active_restaurants = [r for r in restaurants if r['is_active']]

    addresses_by_customer = {}
    for a in addresses:
        addresses_by_customer.setdefault(a['customer_id'], []).append(a)

    for _ in range(count):
        customer = random.choice(active_customers)
        restaurant = random.choice(active_restaurants)
        courier = random.choice(couriers) if random.random() > 0.1 else None

        customer_addrs = addresses_by_customer.get(customer['id'], [])
        addr = random.choice(customer_addrs) if customer_addrs else None

        created_at = fake.date_time_between(start_date='-1y', end_date='now')
        status = random.choices(statuses, weights=weights)[0]

        subtotal = Decimal(str(random.randint(500, 5000)))
        delivery_fee = Decimal(str(random.choice([0, 99, 149, 199])))
        discount = Decimal(str(random.choice([0, 0, 0, 50, 100, 150])))
        total_amount = subtotal + delivery_fee - discount

        order_id = uuid.uuid4()
        order_number = f"ORD-{created_at.strftime('%Y%m%d')}-{random.randint(100000, 999999)}"

        confirmed_at = None
        delivered_at = None
        cancelled_at = None

        if status in ['confirmed', 'preparing', 'ready', 'delivering', 'delivered']:
            confirmed_at = created_at + timedelta(minutes=random.randint(2, 10))
        if status == 'delivered':
            delivered_at = created_at + timedelta(minutes=random.randint(30, 90))
        if status == 'cancelled':
            cancelled_at = created_at + timedelta(minutes=random.randint(5, 60))

        delivery_address = f"{addr['street']}, {addr['house']}" if addr else fake.street_address()
        delivery_lat = addr['latitude'] if addr else Decimal(str(random.uniform(55.5, 56.0)))
        delivery_lon = addr['longitude'] if addr else Decimal(str(random.uniform(37.3, 37.9)))

        orders.append({
            'id': order_id,
            'order_number': order_number,
            'customer_id': customer['id'],
            'customer_name': customer['full_name'],
            'customer_phone': customer['phone'],
            'restaurant_id': restaurant['id'],
            'restaurant_name': restaurant['name'],
            'courier_id': courier['id'] if courier else None,
            'delivery_address': delivery_address,
            'delivery_latitude': delivery_lat,
            'delivery_longitude': delivery_lon,
            'subtotal': subtotal,
            'delivery_fee': delivery_fee,
            'discount': discount,
            'total_amount': total_amount,
            'payment_method': random.choice(payment_methods),
            'status': status,
            'created_at': created_at,
            'confirmed_at': confirmed_at,
            'delivered_at': delivered_at,
            'cancelled_at': cancelled_at
        })

    for o in orders:
        cursor.execute("""
                       INSERT INTO orders (order_id, order_number, customer_id, customer_name, customer_phone,
                                           restaurant_id, restaurant_name, courier_id, delivery_address,
                                           delivery_latitude, delivery_longitude, subtotal, delivery_fee,
                                           discount, total_amount, status, created_at, confirmed_at,
                                           delivered_at, cancelled_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (o['id'], o['order_number'], o['customer_id'], o['customer_name'], o['customer_phone'],
                             o['restaurant_id'], o['restaurant_name'], o['courier_id'], o['delivery_address'],
                             o['delivery_latitude'], o['delivery_longitude'], o['subtotal'], o['delivery_fee'],
                             o['discount'], o['total_amount'], o['status'], o['created_at'], o['confirmed_at'],
                             o['delivered_at'], o['cancelled_at'], o['created_at']))

    conn.commit()
    cursor.close()
    return orders


def generate_order_items(conn, orders, dishes):
    cursor = conn.cursor()
    order_items = []

    dishes_by_restaurant = {}
    for dish in dishes:
        dishes_by_restaurant.setdefault(dish['restaurant_id'], []).append(dish)

    for order in orders:
        restaurant_dishes = dishes_by_restaurant.get(order['restaurant_id'], [])
        if not restaurant_dishes:
            continue

        num_items = random.randint(1, 6)
        selected_dishes = random.sample(restaurant_dishes, min(num_items, len(restaurant_dishes)))

        for dish in selected_dishes:
            quantity = random.randint(1, 4)
            order_item_id = uuid.uuid4()
            order_items.append({
                'id': order_item_id,
                'order_id': order['id'],
                'dish_id': dish['id'],
                'unit_price': dish['price'],
                'quantity': quantity,
                'special_requests': fake.text(max_nb_chars=100) if random.random() > 0.7 else None,
                'created_at': order['created_at']
            })

    for item in order_items:
        cursor.execute("""
                       INSERT INTO order_items (order_item_id, order_id, dish_id, unit_price,
                                                quantity, special_requests, created_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s)
                       """, (item['id'], item['order_id'], item['dish_id'], item['unit_price'],
                             item['quantity'], item['special_requests'], item['created_at']))

    conn.commit()
    cursor.close()
    return order_items


def generate_payments(conn, orders):
    cursor = conn.cursor()
    payments = []

    payment_gateways = ['yookassa', 'sberbank', 'tinkoff', 'sbp']

    for order in orders:
        if random.random() > 0.95:
            continue

        payment_id = uuid.uuid4()

        if order['status'] == 'cancelled':
            status = random.choice(['failed', 'refunded'])
        elif order['status'] == 'delivered':
            status = 'completed'
        elif order['status'] in ['pending', 'confirmed']:
            status = random.choice(['pending', 'processing'])
        else:
            status = random.choice(['processing', 'completed'])

        created_at = order['created_at'] + timedelta(seconds=random.randint(10, 300))
        processed_at = created_at + timedelta(seconds=random.randint(5, 60)) if status != 'pending' else None
        completed_at = processed_at + timedelta(seconds=random.randint(5, 30)) if status == 'completed' else None

        metadata = json.dumps({
            'ip': fake.ipv4(),
            'user_agent': fake.user_agent()
        }) if random.random() > 0.5 else None

        payments.append({
            'id': payment_id,
            'order_id': order['id'],
            'amount': order['total_amount'],
            'payment_method': order['payment_method'],
            'status': status,
            'external_transaction_id': f"txn_{uuid.uuid4().hex[:16]}",
            'payment_gateway': random.choice(payment_gateways),
            'error_message': fake.text(max_nb_chars=100) if status == 'failed' else None,
            'metadata': metadata,
            'created_at': created_at,
            'processed_at': processed_at,
            'completed_at': completed_at
        })

    for p in payments:
        cursor.execute("""
                       INSERT INTO payments (payment_id, order_id, amount, payment_method, status,
                                             external_transaction_id, payment_gateway, error_message,
                                             metadata, created_at, processed_at, completed_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (p['id'], p['order_id'], p['amount'], p['payment_method'], p['status'],
                             p['external_transaction_id'], p['payment_gateway'], p['error_message'],
                             p['metadata'], p['created_at'], p['processed_at'], p['completed_at']))

    conn.commit()
    cursor.close()
    return payments


def generate_reviews(conn, orders):
    cursor = conn.cursor()
    reviews = []

    delivered_orders = [o for o in orders if o['status'] == 'delivered']

    for order in delivered_orders:
        if random.random() > 0.5:
            continue

        review_id = uuid.uuid4()
        created_at = order['delivered_at'] + timedelta(hours=random.randint(1, 48))

        reviews.append({
            'id': review_id,
            'order_id': order['id'],
            'restaurant_rating': random.randint(1, 5),
            'courier_rating': random.randint(1, 5) if order['courier_id'] else None,
            'delivery_speed': random.randint(1, 5),
            'comment': fake.text(max_nb_chars=500) if random.random() > 0.4 else None,
            'created_at': created_at
        })

    for r in reviews:
        cursor.execute("""
                       INSERT INTO reviews (review_id, order_id, restaurant_rating, courier_rating,
                                            delivery_speed, comment, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                       """, (r['id'], r['order_id'], r['restaurant_rating'], r['courier_rating'],
                             r['delivery_speed'], r['comment'], r['created_at'], r['created_at']))

    conn.commit()
    cursor.close()
    return reviews


def generate_admins(conn, restaurants, count=50):
    cursor = conn.cursor()
    admins = []
    admin_restaurant_links = []

    # Системные администраторы
    for _ in range(5):
        admin_id = uuid.uuid4()
        admins.append({
            'id': admin_id,
            'email': fake.unique.email(),
            'password_hash': '$2a$12$' + fake.sha256()[:53],
            'role': 'system_admin',
            'is_active': True,
            'created_at': fake.date_time_between(start_date='-2y', end_date='-1y')
        })

    # Администраторы ресторанов
    for restaurant in restaurants:
        num_admins = random.randint(1, 3)
        for _ in range(num_admins):
            admin_id = uuid.uuid4()
            admins.append({
                'id': admin_id,
                'email': fake.unique.email(),
                'password_hash': '$2a$12$' + fake.sha256()[:53],
                'role': 'restaurant_admin',
                'is_active': random.choice([True, True, False]),
                'created_at': restaurant['created_at'] + timedelta(days=random.randint(0, 30))
            })
            admin_restaurant_links.append({
                'admin_id': admin_id,
                'restaurant_id': restaurant['id']
            })

    for a in admins:
        cursor.execute("""
                       INSERT INTO admins (admin_id, email, password_hash, role, is_active, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s)
                       """, (a['id'], a['email'], a['password_hash'], a['role'],
                             a['is_active'], a['created_at'], a['created_at']))

    for link in admin_restaurant_links:
        cursor.execute("""
                       INSERT INTO admin_restaurants (admin_id, restaurant_id)
                       VALUES (%s, %s)
                       ON CONFLICT DO NOTHING
                       """, (link['admin_id'], link['restaurant_id']))

    conn.commit()
    cursor.close()
    return admins, admin_restaurant_links


def main():
    print("Connecting to database...")
    conn = get_connection()

    print("Generating customers...")
    customers = generate_customers(conn, 5000)
    print(f"  -> {len(customers)} customers")

    print("Generating customer addresses...")
    addresses = generate_customer_addresses(conn, customers)
    print(f"  -> {len(addresses)} addresses")

    print("Generating couriers...")
    couriers = generate_couriers(conn, 1000)
    print(f"  -> {len(couriers)} couriers")

    print("Generating restaurants...")
    restaurants = generate_restaurants(conn, 200)
    print(f"  -> {len(restaurants)} restaurants")

    print("Generating delivery zones...")
    zones = generate_delivery_zones(conn, restaurants)
    print(f"  -> {len(zones)} delivery zones")

    print("Generating dish categories...")
    categories = generate_dish_categories(conn)
    print(f"  -> {len(categories)} categories")

    print("Generating dishes + category mappings...")
    dishes = generate_dishes(conn, restaurants, categories)
    print(f"  -> {len(dishes)} dishes")

    print("Generating orders...")
    orders = generate_orders(conn, customers, restaurants, couriers, addresses, 15000)
    print(f"  -> {len(orders)} orders")

    print("Generating order items...")
    order_items = generate_order_items(conn, orders, dishes)
    print(f"  -> {len(order_items)} order items")

    print("Generating payments...")
    payments = generate_payments(conn, orders)
    print(f"  -> {len(payments)} payments")

    print("Generating reviews...")
    reviews = generate_reviews(conn, orders)
    print(f"  -> {len(reviews)} reviews")

    print("Generating admins...")
    admins, admin_links = generate_admins(conn, restaurants)
    print(f"  -> {len(admins)} admins, {len(admin_links)} admin-restaurant links")

    conn.close()
    print("\nData generation completed!")
    print("\nSummary:")
    print(f"  customers:             {len(customers)}")
    print(f"  customer_addresses:    {len(addresses)}")
    print(f"  couriers:              {len(couriers)}")
    print(f"  restaurants:           {len(restaurants)}")
    print(f"  delivery_zones:        {len(zones)}")
    print(f"  dish_categories:       {len(categories)}")
    print(f"  dishes:                {len(dishes)}")
    print(f"  orders:                {len(orders)}")
    print(f"  order_items:           {len(order_items)}")
    print(f"  payments:              {len(payments)}")
    print(f"  reviews:               {len(reviews)}")
    print(f"  admins:                {len(admins)}")
    print(f"  admin_restaurants:     {len(admin_links)}")


if __name__ == "__main__":
    main()