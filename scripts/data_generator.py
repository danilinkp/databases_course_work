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


def get_connection():
    return psycopg2.connect(**DB_CONFIG)


def generate_customers(conn, count=5000):
    cursor = conn.cursor()
    customers = []

    for _ in range(count):
        customer_id = uuid.uuid4()
        customers.append({
            'id': customer_id,
            'full_name': fake.name(),
            'email': fake.unique.email(),
            'phone': fake.unique.phone_number()[:20],
            'password_hash': fake.sha256(),
            'bonuses': random.randint(0, 5000),
            'is_active': random.choice([True, True, True, False]),
            'created_at': fake.date_time_between(start_date='-2y', end_date='now'),
        })

    for customer in customers:
        cursor.execute("""
                       INSERT INTO customers (customer_id, full_name, email, phone, password_hash, bonuses, is_active,
                                              created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           customer['id'],
                           customer['full_name'],
                           customer['email'],
                           customer['phone'],
                           customer['password_hash'],
                           customer['bonuses'],
                           customer['is_active'],
                           customer['created_at'],
                           customer['created_at']
                       ))

    conn.commit()
    cursor.close()
    return customers


def generate_customer_addresses(conn, customers, avg_per_customer=2):
    cursor = conn.cursor()
    addresses = []

    for customer in customers:
        num_addresses = random.randint(1, avg_per_customer + 2)
        for i in range(num_addresses):
            address_id = uuid.uuid4()
            addresses.append({
                'id': address_id,
                'customer_id': customer['id'],
                'address_line': fake.street_address(),
                'city': fake.city(),
                'postal_code': fake.postcode(),
                'latitude': Decimal(str(random.uniform(55.5, 56.0))),
                'longitude': Decimal(str(random.uniform(37.3, 37.9))),
                'is_default': i == 0,
                'created_at': customer['created_at'] + timedelta(days=random.randint(0, 30))
            })

    for addr in addresses:
        cursor.execute("""
                       INSERT INTO customer_addresses (address_id, customer_id, address_line, city, postal_code,
                                                       latitude, longitude, is_default, created_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           addr['id'],
                           addr['customer_id'],
                           addr['address_line'],
                           addr['city'],
                           addr['postal_code'],
                           addr['latitude'],
                           addr['longitude'],
                           addr['is_default'],
                           addr['created_at']
                       ))

    conn.commit()
    cursor.close()
    return addresses


def generate_couriers(conn, count=1000):
    cursor = conn.cursor()
    couriers = []

    vehicle_types = ['car', 'scooter', 'bike', 'foot']

    for _ in range(count):
        courier_id = uuid.uuid4()
        created_at = fake.date_time_between(start_date='-3y', end_date='-1y')
        couriers.append({
            'id': courier_id,
            'full_name': fake.name(),
            'phone': fake.unique.phone_number()[:20],
            'email': fake.unique.email(),
            'password_hash': fake.sha256(),
            'employee_date': created_at.date(),
            'area_of_work': fake.city(),
            'vehicle_type': random.choice(vehicle_types),
            'rating': Decimal(str(random.uniform(3.5, 5.0))).quantize(Decimal('0.01')),
            'delivery_count': random.randint(0, 5000),
            'is_available': random.choice([True, False]),
            'is_active': random.choice([True, True, True, False]),
            'created_at': created_at
        })

    for courier in couriers:
        cursor.execute("""
                       INSERT INTO couriers (courier_id, full_name, phone, email, password_hash, employee_date,
                                             area_of_work, vehicle_type, rating, delivery_count, is_available,
                                             is_active, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           courier['id'],
                           courier['full_name'],
                           courier['phone'],
                           courier['email'],
                           courier['password_hash'],
                           courier['employee_date'],
                           courier['area_of_work'],
                           courier['vehicle_type'],
                           courier['rating'],
                           courier['delivery_count'],
                           courier['is_available'],
                           courier['is_active'],
                           courier['created_at'],
                           courier['created_at']
                       ))

    conn.commit()
    cursor.close()
    return couriers


def generate_restaurants(conn, count=500):
    cursor = conn.cursor()
    restaurants = []

    cuisine_types = ['Итальянская', 'Японская', 'Русская', 'Американская', 'Китайская', 'Грузинская', 'Узбекская',
                     'Индийская', 'Французская', 'Мексиканская']

    for _ in range(count):
        restaurant_id = uuid.uuid4()
        restaurants.append({
            'id': restaurant_id,
            'name': fake.company(),
            'cuisine_type': random.choice(cuisine_types),
            'rating': Decimal(str(random.uniform(3.0, 5.0))).quantize(Decimal('0.01')),
            'review_count': random.randint(0, 1000),
            'address': fake.address(),
            'phone': fake.phone_number()[:20],
            'email': fake.company_email(),
            'latitude': Decimal(str(random.uniform(55.5, 56.0))),
            'longitude': Decimal(str(random.uniform(37.3, 37.9))),
            'opening_time': datetime.strptime(f"{random.randint(7, 10)}:00", "%H:%M").time(),
            'closing_time': datetime.strptime(f"{random.randint(21, 23)}:00", "%H:%M").time(),
            'min_order_amount': Decimal(str(random.choice([0, 300, 500, 800]))),
            'delivery_fee': Decimal(str(random.choice([0, 100, 150, 200, 250]))),
            'is_active': random.choice([True, True, True, False]),
            'created_at': fake.date_time_between(start_date='-2y', end_date='-6M')
        })

    for restaurant in restaurants:
        cursor.execute("""
                       INSERT INTO restaurants (restaurant_id, name, cuisine_type, rating, review_count, address, phone,
                                                email, latitude, longitude, opening_time, closing_time,
                                                min_order_amount, delivery_fee, is_active, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           restaurant['id'],
                           restaurant['name'],
                           restaurant['cuisine_type'],
                           restaurant['rating'],
                           restaurant['review_count'],
                           restaurant['address'],
                           restaurant['phone'],
                           restaurant['email'],
                           restaurant['latitude'],
                           restaurant['longitude'],
                           restaurant['opening_time'],
                           restaurant['closing_time'],
                           restaurant['min_order_amount'],
                           restaurant['delivery_fee'],
                           restaurant['is_active'],
                           restaurant['created_at'],
                           restaurant['created_at']
                       ))

    conn.commit()
    cursor.close()
    return restaurants


def generate_dish_categories(conn, restaurants):
    cursor = conn.cursor()
    categories = []

    category_names = [
        'Закуски', 'Салаты', 'Супы', 'Горячие блюда', 'Гарниры',
        'Роллы', 'Суши', 'Сашими', 'Пицца', 'Паста',
        'Бургеры', 'Десерты', 'Напитки', 'Выпечка', 'Завтраки'
    ]

    for restaurant in restaurants:
        num_categories = random.randint(5, 12)
        selected_categories = random.sample(category_names, min(num_categories, len(category_names)))

        for i, cat_name in enumerate(selected_categories):
            category_id = uuid.uuid4()
            categories.append({
                'id': category_id,
                'restaurant_id': restaurant['id'],
                'name': cat_name,
                'description': fake.text(max_nb_chars=200),
                'display_order': i,
                'created_at': restaurant['created_at'] + timedelta(days=random.randint(1, 7))
            })

    for category in categories:
        cursor.execute("""
                       INSERT INTO dish_categories (category_id, restaurant_id, name, description, display_order,
                                                    created_at)
                       VALUES (%s, %s, %s, %s, %s, %s)
                       """, (
                           category['id'],
                           category['restaurant_id'],
                           category['name'],
                           category['description'],
                           category['display_order'],
                           category['created_at']
                       ))

    conn.commit()
    cursor.close()
    return categories


def generate_dishes(conn, categories, avg_per_category=15):
    cursor = conn.cursor()
    dishes = []

    dish_names = [
        'Цезарь', 'Греческий салат', 'Оливье', 'Борщ', 'Солянка',
        'Калифорния', 'Филадельфия', 'Маргарита', 'Пепперони', 'Карбонара',
        'Чизбургер', 'Воппер', 'Тирамису', 'Чизкейк', 'Эспрессо'
    ]

    for category in categories:
        num_dishes = random.randint(10, avg_per_category + 10)

        for _ in range(num_dishes):
            dish_id = uuid.uuid4()
            dishes.append({
                'id': dish_id,
                'restaurant_id': category['restaurant_id'],
                'category_id': category['id'],
                'name': random.choice(dish_names) + ' ' + fake.word().capitalize(),
                'description': fake.text(max_nb_chars=300),
                'price': Decimal(str(random.randint(150, 2500))),
                'image_url': f"https://example.com/dishes/{dish_id}.jpg",
                'is_available': random.choice([True, True, True, False]),
                'is_spicy': random.choice([True, False, False, False]),
                'preparation_time': random.choice([15, 20, 25, 30, 35, 40, 45]),
                'calories': random.randint(200, 1500),
                'weight_grams': random.randint(100, 800),
                'created_at': category['created_at'] + timedelta(days=random.randint(1, 30))
            })

    for dish in dishes:
        cursor.execute("""
                       INSERT INTO dishes (dish_id, restaurant_id, category_id, name, description, price, image_url,
                                           is_available, is_spicy, preparation_time, calories, weight_grams, created_at,
                                           updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           dish['id'],
                           dish['restaurant_id'],
                           dish['category_id'],
                           dish['name'],
                           dish['description'],
                           dish['price'],
                           dish['image_url'],
                           dish['is_available'],
                           dish['is_spicy'],
                           dish['preparation_time'],
                           dish['calories'],
                           dish['weight_grams'],
                           dish['created_at'],
                           dish['created_at']
                       ))

    conn.commit()
    cursor.close()
    return dishes


def generate_orders(conn, customers, restaurants, couriers, addresses, count=15000):
    cursor = conn.cursor()
    orders = []

    statuses = ['pending', 'confirmed', 'preparing', 'ready', 'picked_up', 'delivering', 'delivered', 'cancelled']
    payment_methods = ['cash', 'card', 'online', 'bonuses']

    active_customers = [c for c in customers if c['is_active']]
    active_restaurants = [r for r in restaurants if r['is_active']]

    for _ in range(count):
        customer = random.choice(active_customers)
        restaurant = random.choice(active_restaurants)
        courier = random.choice(couriers) if random.random() > 0.1 else None

        customer_addresses = [a for a in addresses if a['customer_id'] == customer['id']]
        address = random.choice(customer_addresses) if customer_addresses else {
            'address_line': fake.street_address(),
            'city': fake.city()
        }

        created_at = fake.date_time_between(start_date='-1y', end_date='now')
        status = random.choices(statuses, weights=[5, 10, 8, 5, 5, 10, 50, 7])[0]

        subtotal = Decimal(str(random.randint(500, 5000)))
        delivery_fee = restaurant['delivery_fee']
        discount = Decimal(str(random.choice([0, 0, 0, 50, 100, 150, 200])))
        total_amount = subtotal + delivery_fee - discount

        order_id = uuid.uuid4()
        order_number = f"ORD-{created_at.strftime('%Y%m%d')}-{random.randint(100000, 999999)}"

        confirmed_at = None
        delivered_at = None
        cancelled_at = None

        if status in ['confirmed', 'preparing', 'ready', 'picked_up', 'delivering', 'delivered']:
            confirmed_at = created_at + timedelta(minutes=random.randint(2, 10))
        if status == 'delivered':
            delivered_at = created_at + timedelta(minutes=random.randint(30, 90))
        if status == 'cancelled':
            cancelled_at = created_at + timedelta(minutes=random.randint(5, 60))

        orders.append({
            'id': order_id,
            'order_number': order_number,
            'customer_id': customer['id'],
            'customer_name': customer['full_name'],
            'customer_phone': customer['phone'],
            'restaurant_id': restaurant['id'],
            'restaurant_name': restaurant['name'],
            'courier_id': courier['id'] if courier else None,
            'delivery_address': address['address_line'],
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

    for order in orders:
        cursor.execute("""
                       INSERT INTO orders (order_id, order_number, customer_id, customer_name, customer_phone,
                                           restaurant_id, restaurant_name, courier_id, delivery_address, subtotal,
                                           delivery_fee, discount, total_amount, payment_method, status, created_at,
                                           confirmed_at, delivered_at, cancelled_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           order['id'],
                           order['order_number'],
                           order['customer_id'],
                           order['customer_name'],
                           order['customer_phone'],
                           order['restaurant_id'],
                           order['restaurant_name'],
                           order['courier_id'],
                           order['delivery_address'],
                           order['subtotal'],
                           order['delivery_fee'],
                           order['discount'],
                           order['total_amount'],
                           order['payment_method'],
                           order['status'],
                           order['created_at'],
                           order['confirmed_at'],
                           order['delivered_at'],
                           order['cancelled_at'],
                           order['created_at']
                       ))

    conn.commit()
    cursor.close()
    return orders


def generate_order_items(conn, orders, dishes):
    cursor = conn.cursor()
    order_items = []

    dishes_by_restaurant = {}
    for dish in dishes:
        if dish['restaurant_id'] not in dishes_by_restaurant:
            dishes_by_restaurant[dish['restaurant_id']] = []
        dishes_by_restaurant[dish['restaurant_id']].append(dish)

    for order in orders:
        restaurant_dishes = dishes_by_restaurant.get(order['restaurant_id'], [])
        if not restaurant_dishes:
            continue

        num_items = random.randint(1, 6)
        selected_dishes = random.sample(restaurant_dishes, min(num_items, len(restaurant_dishes)))

        for dish in selected_dishes:
            quantity = random.randint(1, 4)
            subtotal = dish['price'] * quantity

            order_item_id = uuid.uuid4()
            order_items.append({
                'id': order_item_id,
                'order_id': order['id'],
                'dish_id': dish['id'],
                'dish_name': dish['name'],
                'unit_price': dish['price'],
                'quantity': quantity,
                'subtotal': subtotal,
                'special_requests': fake.text(max_nb_chars=100) if random.random() > 0.7 else None,
                'created_at': order['created_at']
            })

    for item in order_items:
        cursor.execute("""
                       INSERT INTO order_items (order_item_id, order_id, dish_id, dish_name, unit_price, quantity,
                                                subtotal, special_requests, created_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           item['id'],
                           item['order_id'],
                           item['dish_id'],
                           item['dish_name'],
                           item['unit_price'],
                           item['quantity'],
                           item['subtotal'],
                           item['special_requests'],
                           item['created_at']
                       ))

    conn.commit()
    cursor.close()
    return order_items


def generate_payments(conn, orders):
    cursor = conn.cursor()
    payments = []

    payment_statuses = ['pending', 'processing', 'completed', 'failed', 'refunded']
    payment_gateways = ['stripe', 'paypal', 'yookassa', 'sberbank']

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

    for payment in payments:
        cursor.execute("""
                       INSERT INTO payments (payment_id, order_id, amount, payment_method, status,
                                             external_transaction_id, payment_gateway, error_message, metadata,
                                             created_at, processed_at, completed_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           payment['id'],
                           payment['order_id'],
                           payment['amount'],
                           payment['payment_method'],
                           payment['status'],
                           payment['external_transaction_id'],
                           payment['payment_gateway'],
                           payment['error_message'],
                           payment['metadata'],
                           payment['created_at'],
                           payment['processed_at'],
                           payment['completed_at']
                       ))

    conn.commit()
    cursor.close()
    return payments


def generate_reviews(conn, orders):
    cursor = conn.cursor()
    reviews = []

    delivered_orders = [o for o in orders if o['status'] == 'delivered']

    for order in delivered_orders:
        if random.random() > 0.4:
            continue

        review_id = uuid.uuid4()

        restaurant_rating = random.randint(1, 5)
        courier_rating = random.randint(1, 5) if order['courier_id'] else None
        food_quality = random.randint(1, 5)
        delivery_speed = random.randint(1, 5)

        comment = fake.text(max_nb_chars=500) if random.random() > 0.3 else None

        created_at = order['delivered_at'] + timedelta(hours=random.randint(1, 48))

        reviews.append({
            'id': review_id,
            'order_id': order['id'],
            'restaurant_rating': restaurant_rating,
            'courier_rating': courier_rating,
            'food_quality': food_quality,
            'delivery_speed': delivery_speed,
            'comment': comment,
            'created_at': created_at
        })

    for review in reviews:
        cursor.execute("""
                       INSERT INTO reviews (review_id, order_id, restaurant_rating, courier_rating, food_quality,
                                            delivery_speed, comment, created_at, updated_at)
                       VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                       """, (
                           review['id'],
                           review['order_id'],
                           review['restaurant_rating'],
                           review['courier_rating'],
                           review['food_quality'],
                           review['delivery_speed'],
                           review['comment'],
                           review['created_at'],
                           review['created_at']
                       ))

    conn.commit()
    cursor.close()
    return reviews


def main():
    print("Connecting to database...")
    conn = get_connection()

    print("Generating customers...")
    customers = generate_customers(conn, 5000)
    print(f"Generated {len(customers)} customers")

    print("Generating customer addresses...")
    addresses = generate_customer_addresses(conn, customers)
    print(f"Generated {len(addresses)} addresses")

    print("Generating couriers...")
    couriers = generate_couriers(conn, 1000)
    print(f"Generated {len(couriers)} couriers")

    print("Generating restaurants...")
    restaurants = generate_restaurants(conn, 500)
    print(f"Generated {len(restaurants)} restaurants")

    print("Generating dish categories...")
    categories = generate_dish_categories(conn, restaurants)
    print(f"Generated {len(categories)} categories")

    print("Generating dishes...")
    dishes = generate_dishes(conn, categories)
    print(f"Generated {len(dishes)} dishes")

    print("Generating orders...")
    orders = generate_orders(conn, customers, restaurants, couriers, addresses, 15000)
    print(f"Generated {len(orders)} orders")

    print("Generating order items...")
    order_items = generate_order_items(conn, orders, dishes)
    print(f"Generated {len(order_items)} order items")

    print("Generating payments...")
    payments = generate_payments(conn, orders)
    print(f"Generated {len(payments)} payments")

    print("Generating reviews...")
    reviews = generate_reviews(conn, orders)
    print(f"Generated {len(reviews)} reviews")

    conn.close()
    print("\nData generation completed successfully!")
    print("\nSummary:")
    print(f"  Customers: {len(customers)}")
    print(f"  Addresses: {len(addresses)}")
    print(f"  Couriers: {len(couriers)}")
    print(f"  Restaurants: {len(restaurants)}")
    print(f"  Categories: {len(categories)}")
    print(f"  Dishes: {len(dishes)}")
    print(f"  Orders: {len(orders)}")
    print(f"  Order Items: {len(order_items)}")
    print(f"  Payments: {len(payments)}")
    print(f"  Reviews: {len(reviews)}")


if __name__ == "__main__":
    main()
