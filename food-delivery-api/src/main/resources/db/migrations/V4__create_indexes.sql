BEGIN;

-- customers
CREATE INDEX IF NOT EXISTS idx_customers_email ON customers (email);
CREATE INDEX IF NOT EXISTS idx_customers_phone ON customers (phone);
CREATE INDEX IF NOT EXISTS idx_customers_active ON customers (is_active) WHERE is_active = true;

-- customer addresses
CREATE INDEX IF NOT EXISTS idx_customer_addresses_customer ON customer_addresses (customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_customer_addresses_default ON customer_addresses (customer_id, is_default) WHERE is_default = true;

-- couriers
CREATE INDEX IF NOT EXISTS idx_couriers_email ON couriers(email);
CREATE INDEX IF NOT EXISTS idx_couriers_phone ON couriers(phone);
CREATE INDEX IF NOT EXISTS idx_couriers_available ON couriers(is_available, is_active)
    WHERE is_available = true AND is_active = true;

-- restaurants
CREATE INDEX IF NOT EXISTS idx_restaurants_cuisine ON restaurants(cuisine_type);
CREATE INDEX IF NOT EXISTS idx_restaurants_rating ON restaurants(rating DESC);
CREATE INDEX IF NOT EXISTS idx_restaurants_active ON restaurants(is_active) WHERE is_active = true;

-- delivery_zones
CREATE INDEX idx_delivery_zones_restaurant_postal ON delivery_zones(restaurant_id, postal_code);

-- dish categories
CREATE INDEX IF NOT EXISTS idx_dish_categories_display ON dish_categories(display_order);

-- dishes
CREATE INDEX IF NOT EXISTS idx_dishes_restaurant ON dishes(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_dishes_category ON dishes(category_id);
CREATE INDEX IF NOT EXISTS idx_dishes_available ON dishes(restaurant_id, is_available)
    WHERE is_available = true;
CREATE INDEX IF NOT EXISTS idx_dishes_price ON dishes(price);

-- orders
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant ON orders(restaurant_id);
CREATE INDEX IF NOT EXISTS idx_orders_courier ON orders(courier_id);

CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE INDEX IF NOT EXISTS idx_orders_created ON orders(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_orders_customer_status ON orders(customer_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_restaurant_status ON orders(restaurant_id, status);

CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders(order_number);

--order items
CREATE INDEX IF NOT EXISTS idx_order_items_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_dish ON order_items(dish_id);

-- payments
CREATE INDEX IF NOT EXISTS idx_payments_order ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created ON payments(created_at DESC);

-- reviews
CREATE INDEX IF NOT EXISTS idx_reviews_order ON reviews(order_id);
CREATE INDEX IF NOT EXISTS idx_reviews_created ON reviews(created_at DESC);

COMMIT;