BEGIN;

-- customers
ALTER TABLE customers
    ADD CONSTRAINT add_customers_primary_key PRIMARY KEY (customer_id);
ALTER TABLE customers
    ALTER COLUMN customer_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE customers
    ALTER COLUMN full_name SET NOT NULL;
ALTER TABLE customers
    ALTER COLUMN phone SET NOT NULL,
    ADD CONSTRAINT customers_phone_unique UNIQUE (phone);
ALTER TABLE customers
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT customers_email_unique UNIQUE (email);
ALTER TABLE customers
    ADD CONSTRAINT check_positives_bonuses CHECK (bonuses >= 0),
    ALTER COLUMN bonuses SET DEFAULT 0;
ALTER TABLE customers
    ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE customers
    ALTER COLUMN is_active SET DEFAULT TRUE;
ALTER TABLE customers
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE customers
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;


-- customer_addresses
ALTER TABLE customer_addresses
    ADD CONSTRAINT add_customer_addresses_primary_key PRIMARY KEY (address_id);
ALTER TABLE customer_addresses
    ALTER COLUMN address_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN address_id SET NOT NULL;
ALTER TABLE customer_addresses
    ADD CONSTRAINT address_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customers (customer_ID) ON DELETE CASCADE;
ALTER TABLE customer_addresses
    ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE customer_addresses
    ALTER COLUMN city SET NOT NULL;
ALTER TABLE customer_addresses
    ALTER COLUMN street SET NOT NULL;
ALTER TABLE customer_addresses
    ALTER COLUMN house SET NOT NULL;
ALTER TABLE customer_addresses
    ALTER COLUMN is_default SET DEFAULT false;
ALTER TABLE customer_addresses
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- couriers
ALTER TABLE couriers
    ADD CONSTRAINT add_couriers_primary_key PRIMARY KEY (courier_id);
ALTER TABLE couriers
    ALTER COLUMN courier_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN courier_id SET NOT NULL;
ALTER TABLE couriers
    ALTER COLUMN full_name SET NOT NULL;
ALTER TABLE couriers
    ALTER COLUMN phone SET NOT NULL;
ALTER TABLE couriers
    ADD CONSTRAINT couriers_phone_unique UNIQUE (phone);
ALTER TABLE couriers
    ALTER COLUMN email SET NOT NULL;
ALTER TABLE couriers
    ADD CONSTRAINT couriers_email_unique UNIQUE (email);
ALTER TABLE couriers
    ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE couriers
    ALTER COLUMN employee_date SET NOT NULL;
ALTER TABLE couriers
    ALTER COLUMN vehicle_type SET NOT NULL;
ALTER TABLE couriers
    ADD CONSTRAINT check_rating CHECK (rating >= 0 AND rating <= 5),
    ALTER COLUMN rating SET DEFAULT 0;
ALTER TABLE couriers
    ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE couriers
    ALTER COLUMN is_available SET DEFAULT true;
ALTER TABLE couriers
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE couriers
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- restaurants
ALTER TABLE restaurants
    ADD CONSTRAINT add_restaurants_primary_key PRIMARY KEY (restaurant_id);
ALTER TABLE restaurants
    ALTER COLUMN restaurant_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN restaurant_id SET NOT NULL;
ALTER TABLE restaurants
    ALTER COLUMN name SET NOT NULL;
ALTER TABLE restaurants
    ADD CONSTRAINT check_rating CHECK (rating >= 0 AND rating <= 5),
    ALTER COLUMN rating SET DEFAULT 0.00;
ALTER TABLE restaurants
    ADD CONSTRAINT check_review_count CHECK (review_count >= 0),
    ALTER COLUMN review_count SET DEFAULT 0;
ALTER TABLE restaurants
    ALTER COLUMN address SET NOT NULL;
ALTER TABLE restaurants
    ALTER COLUMN phone SET NOT NULL,
    ADD CONSTRAINT restaurants_phone_unique UNIQUE (phone);
ALTER TABLE restaurants
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT restaurants_email_unique UNIQUE (email);
ALTER TABLE restaurants
    ALTER COLUMN is_active SET DEFAULT true;
ALTER TABLE restaurants
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE restaurants
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- delivery_zones
ALTER TABLE delivery_zones
    ADD CONSTRAINT add_delivery_zones_pk PRIMARY KEY (zone_id),
    ALTER COLUMN zone_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN zone_id SET NOT NULL;
ALTER TABLE delivery_zones
    ADD CONSTRAINT restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE CASCADE,
    ALTER COLUMN restaurant_id SET NOT NULL;
ALTER TABLE delivery_zones
    ALTER COLUMN zone_name SET NOT NULL;
ALTER TABLE delivery_zones
    ALTER COLUMN postal_code SET NOT NULL;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_delivery_fee_positive CHECK (delivery_fee >= 0),
    ALTER COLUMN delivery_fee SET DEFAULT 0;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_near_threshold_positive CHECK (near_threshold >= 0),
    ALTER COLUMN near_threshold SET DEFAULT 3;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_far_threshold_positive CHECK (far_threshold >= 0),
    ALTER COLUMN far_threshold SET DEFAULT 5;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_fee_per_km_positive CHECK (fee_per_km >= 0),
    ALTER COLUMN fee_per_km SET DEFAULT 0;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_peak_surcharge_positive CHECK (peak_surcharge >= 0),
    ALTER COLUMN peak_surcharge SET DEFAULT 0;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_weekend_surcharge_positive CHECK (weekend_surcharge >= 0),
    ALTER COLUMN weekend_surcharge SET DEFAULT 0;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_min_order_amount_positive CHECK (min_order_amount >= 0),
    ALTER COLUMN min_order_amount SET DEFAULT 0;
ALTER TABLE delivery_zones
    ADD CONSTRAINT check_delivery_time_positive CHECK (delivery_time >= 0);

-- dish categories
ALTER TABLE dish_categories
    ADD CONSTRAINT add_dish_categories_primary_key PRIMARY KEY (category_id);
ALTER TABLE dish_categories
    ALTER COLUMN category_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE dish_categories
    ALTER COLUMN name SET NOT NULL;
ALTER TABLE dish_categories
    ALTER COLUMN display_order SET DEFAULT 0;
ALTER TABLE dish_categories
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- dishes
ALTER TABLE dishes
    ADD CONSTRAINT add_dishes_primary_key PRIMARY KEY (dish_id);
ALTER TABLE dishes
    ALTER COLUMN dish_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN dish_id SET NOT NULL;
ALTER TABLE dishes
    ADD CONSTRAINT dishes_restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE CASCADE,
    ALTER COLUMN restaurant_id SET NOT NULL;
ALTER TABLE dishes
    ADD CONSTRAINT dishes_category_id_fkey FOREIGN KEY (category_id) REFERENCES dish_categories (category_id) ON DELETE SET NULL;
ALTER TABLE dishes
    ALTER COLUMN name SET NOT NULL;
ALTER TABLE dishes
    ALTER COLUMN price SET NOT NULL,
    ADD CONSTRAINT check_price_positive CHECK (price > 0);
ALTER TABLE dishes
    ADD CONSTRAINT check_prep_time_positive CHECK (preparation_time >= 0);
ALTER TABLE dishes
    ADD CONSTRAINT check_calories_positive CHECK (calories >= 0);
ALTER TABLE dishes
    ADD CONSTRAINT check_weight_positive CHECK (weight_grams >= 0);
ALTER TABLE dishes
    ALTER COLUMN is_available SET DEFAULT true;
ALTER TABLE dishes
    ALTER COLUMN is_spicy SET DEFAULT false;
ALTER TABLE dishes
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE dishes
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- orders
ALTER TABLE orders
    ADD CONSTRAINT add_orders_primary_key PRIMARY KEY (order_id);
ALTER TABLE orders
    ALTER COLUMN order_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE orders
    ADD CONSTRAINT orders_order_number_unique UNIQUE (order_number),
    ALTER COLUMN order_number SET NOT NULL;
ALTER TABLE orders
    ADD CONSTRAINT orders_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE RESTRICT;
ALTER TABLE orders
    ALTER COLUMN customer_id SET NOT NULL;
ALTER TABLE orders
    ADD CONSTRAINT orders_restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE RESTRICT;
ALTER TABLE orders
    ALTER COLUMN restaurant_id SET NOT NULL;
ALTER TABLE orders
    ADD CONSTRAINT orders_courier_id_fkey FOREIGN KEY (courier_id) REFERENCES couriers (courier_id) ON DELETE SET NULL;
ALTER TABLE orders
    ALTER COLUMN subtotal SET NOT NULL,
    ADD CONSTRAINT check_subtotal_positive CHECK (subtotal >= 0);
ALTER TABLE orders
    ALTER COLUMN delivery_fee SET DEFAULT 0,
    ADD CONSTRAINT check_delivery_fee_positive CHECK (delivery_fee >= 0);
ALTER TABLE orders
    ALTER COLUMN discount SET DEFAULT 0,
    ADD CONSTRAINT check_discount_positive CHECK (discount >= 0);
ALTER TABLE orders
    ALTER COLUMN total_amount SET NOT NULL,
    ADD CONSTRAINT check_total_amount_positive CHECK (total_amount >= 0);
ALTER TABLE orders
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'pending';
ALTER TABLE orders
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE orders
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- order items
ALTER TABLE order_items
    ADD CONSTRAINT add_order_items_primary_key PRIMARY KEY (order_item_id);
ALTER TABLE order_items
    ALTER COLUMN order_item_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN order_item_id SET NOT NULL;
ALTER TABLE order_items
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE;
ALTER TABLE order_items
    ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE order_items
    ADD CONSTRAINT order_items_dish_id_fkey FOREIGN KEY (dish_id) REFERENCES dishes (dish_id) ON DELETE RESTRICT;
ALTER TABLE order_items
    ALTER COLUMN dish_id SET NOT NULL;
ALTER TABLE order_items
    ALTER COLUMN dish_name SET NOT NULL;
ALTER TABLE order_items
    ALTER COLUMN unit_price SET NOT NULL,
    ADD CONSTRAINT check_unit_price_positive CHECK (unit_price > 0);
ALTER TABLE order_items
    ALTER COLUMN quantity SET NOT NULL,
    ADD CONSTRAINT check_quantity_positive CHECK (quantity > 0);
ALTER TABLE order_items
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- payments
ALTER TABLE payments
    ADD CONSTRAINT add_payments_primary_key PRIMARY KEY (payment_id);
ALTER TABLE payments
    ALTER COLUMN payment_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN payment_id SET NOT NULL;
ALTER TABLE payments
    ADD CONSTRAINT payments_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE RESTRICT;
ALTER TABLE payments
    ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE payments
    ADD CONSTRAINT payments_order_id_unique UNIQUE (order_id);
ALTER TABLE payments
    ALTER COLUMN amount SET NOT NULL,
    ADD CONSTRAINT check_amount_positive CHECK (amount > 0);
ALTER TABLE payments
    ALTER COLUMN payment_method SET NOT NULL;
ALTER TABLE payments
    ALTER COLUMN status SET DEFAULT 'pending',
    ALTER COLUMN status SET NOT NULL;
ALTER TABLE payments
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- reviews
ALTER TABLE reviews
    ADD CONSTRAINT add_reviews_primary_key PRIMARY KEY (review_id);
ALTER TABLE reviews
    ALTER COLUMN review_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN review_id SET NOT NULL;
ALTER TABLE reviews
    ADD CONSTRAINT reviews_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE;
ALTER TABLE reviews
    ALTER COLUMN order_id SET NOT NULL;
ALTER TABLE reviews
    ADD CONSTRAINT reviews_order_id_unique UNIQUE (order_id);
ALTER TABLE reviews
    ADD CONSTRAINT check_restaurant_rating CHECK (restaurant_rating >= 1 AND restaurant_rating <= 5);
ALTER TABLE reviews
    ADD CONSTRAINT check_courier_rating CHECK (courier_rating >= 1 AND courier_rating <= 5);
ALTER TABLE reviews
    ADD CONSTRAINT check_delivery_speed CHECK (delivery_speed >= 1 AND delivery_speed <= 5);
ALTER TABLE reviews
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE reviews
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

COMMIT;