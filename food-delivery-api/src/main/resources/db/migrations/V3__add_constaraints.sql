BEGIN;

-- customers
ALTER TABLE customers
    ADD CONSTRAINT add_customers_primary_key PRIMARY KEY (customer_id),
    ALTER COLUMN customer_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN customer_id SET NOT NULL,
    ALTER COLUMN full_name SET NOT NULL,
    ALTER COLUMN phone SET NOT NULL,
    ADD CONSTRAINT customers_phone_unique UNIQUE (phone),
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT customers_email_unique UNIQUE (email),
    ADD CONSTRAINT check_positives_bonuses CHECK (bonuses >= 0),
    ALTER COLUMN bonuses SET DEFAULT 0,
    ALTER COLUMN password_hash SET NOT NULL,
    ALTER COLUMN is_active SET DEFAULT TRUE,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;


-- customer_addresses
ALTER TABLE customer_addresses
    ADD CONSTRAINT add_customer_addresses_primary_key PRIMARY KEY (address_id),
    ALTER COLUMN address_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN address_id SET NOT NULL,
    ADD CONSTRAINT address_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customers (customer_ID) ON DELETE CASCADE,
    ALTER COLUMN customer_id SET NOT NULL,
    ALTER COLUMN city SET NOT NULL,
    ALTER COLUMN street SET NOT NULL,
    ALTER COLUMN house SET NOT NULL,
    ALTER COLUMN is_default SET DEFAULT false,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- couriers
ALTER TABLE couriers
    ADD CONSTRAINT add_couriers_primary_key PRIMARY KEY (courier_id),
    ALTER COLUMN courier_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN courier_id SET NOT NULL,
    ALTER COLUMN full_name SET NOT NULL,
    ALTER COLUMN phone SET NOT NULL,
    ADD CONSTRAINT couriers_phone_unique UNIQUE (phone),
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT couriers_email_unique UNIQUE (email),
    ALTER COLUMN password_hash SET NOT NULL,
    ALTER COLUMN employee_date SET NOT NULL,
    ALTER COLUMN vehicle_type SET NOT NULL,
    ADD CONSTRAINT check_rating CHECK (rating >= 0 AND rating <= 5),
    ALTER COLUMN rating SET DEFAULT 0,
    ALTER COLUMN is_active SET DEFAULT true,
    ALTER COLUMN is_available SET DEFAULT true,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- restaurants
ALTER TABLE restaurants
    ADD CONSTRAINT add_restaurants_primary_key PRIMARY KEY (restaurant_id),
    ALTER COLUMN restaurant_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN restaurant_id SET NOT NULL,
    ALTER COLUMN name SET NOT NULL,
    ADD CONSTRAINT check_rating CHECK (rating >= 0 AND rating <= 5),
    ALTER COLUMN rating SET DEFAULT 0.00,
    ALTER COLUMN address SET NOT NULL,
    ALTER COLUMN phone SET NOT NULL,
    ADD CONSTRAINT restaurants_phone_unique UNIQUE (phone),
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT restaurants_email_unique UNIQUE (email),
    ALTER COLUMN is_active SET DEFAULT true,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- delivery_zones
ALTER TABLE delivery_zones
    ADD CONSTRAINT add_delivery_zones_pk PRIMARY KEY (zone_id),
    ALTER COLUMN zone_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN zone_id SET NOT NULL,
    ADD CONSTRAINT restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE CASCADE,
    ALTER COLUMN restaurant_id SET NOT NULL,
    ALTER COLUMN zone_name SET NOT NULL,
    ALTER COLUMN postal_code SET NOT NULL,
    ADD CONSTRAINT check_delivery_fee_positive CHECK (delivery_fee >= 0),
    ALTER COLUMN delivery_fee SET DEFAULT 0,
    ADD CONSTRAINT check_near_threshold_positive CHECK (near_threshold >= 0),
    ALTER COLUMN near_threshold SET DEFAULT 3,
    ADD CONSTRAINT check_far_threshold_positive CHECK (far_threshold >= 0),
    ALTER COLUMN far_threshold SET DEFAULT 5,
    ADD CONSTRAINT check_far_greater_than_near CHECK (far_threshold >= near_threshold),
    ADD CONSTRAINT check_far_zone_multiplier_positive CHECK (far_zone_multiplier > 0),
    ALTER COLUMN far_zone_multiplier SET DEFAULT 1.5,
    ADD CONSTRAINT check_fee_per_km_positive CHECK (fee_per_km >= 0),
    ALTER COLUMN fee_per_km SET DEFAULT 0,
    ADD CONSTRAINT check_peak_surcharge_positive CHECK (peak_surcharge >= 0),
    ALTER COLUMN peak_surcharge SET DEFAULT 0,
    ADD CONSTRAINT check_weekend_surcharge_positive CHECK (weekend_surcharge >= 0),
    ALTER COLUMN weekend_surcharge SET DEFAULT 0,
    ADD CONSTRAINT check_min_order_amount_positive CHECK (min_order_amount >= 0),
    ALTER COLUMN min_order_amount SET DEFAULT 0,
    ADD CONSTRAINT check_delivery_time_positive CHECK (delivery_time >= 0);

-- dish categories
ALTER TABLE dish_categories
    ADD CONSTRAINT add_dish_categories_primary_key PRIMARY KEY (category_id),
    ALTER COLUMN category_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN category_id SET NOT NULL,
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN display_order SET DEFAULT 0,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- dishes
ALTER TABLE dishes
    ADD CONSTRAINT add_dishes_primary_key PRIMARY KEY (dish_id),
    ALTER COLUMN dish_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN dish_id SET NOT NULL,
    ADD CONSTRAINT dishes_restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE CASCADE,
    ALTER COLUMN restaurant_id SET NOT NULL,
    ALTER COLUMN name SET NOT NULL,
    ALTER COLUMN price SET NOT NULL,
    ADD CONSTRAINT check_price_positive CHECK (price > 0),
    ADD CONSTRAINT check_prep_time_positive CHECK (preparation_time >= 0),
    ADD CONSTRAINT check_calories_positive CHECK (calories >= 0),
    ADD CONSTRAINT check_weight_positive CHECK (weight_grams >= 0),
    ALTER COLUMN is_available SET DEFAULT true,
    ALTER COLUMN is_spicy SET DEFAULT false,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE dish_category_mapping
    ADD CONSTRAINT dish_id_fkey FOREIGN KEY (dish_id) REFERENCES dishes (dish_id),
    ADD CONSTRAINT category_id_fkey FOREIGN KEY (category_id) REFERENCES dish_categories (category_id),
    ADD CONSTRAINT add_dish_category_mapping_pk PRIMARY KEY (dish_id, category_id);

-- orders
ALTER TABLE orders
    ADD CONSTRAINT add_orders_primary_key PRIMARY KEY (order_id),
    ALTER COLUMN order_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN order_id SET NOT NULL,
    ADD CONSTRAINT orders_order_number_unique UNIQUE (order_number),
    ALTER COLUMN order_number SET NOT NULL,
    ADD CONSTRAINT orders_customer_id_fkey FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE RESTRICT,
    ALTER COLUMN customer_id SET NOT NULL,
    ADD CONSTRAINT orders_restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE RESTRICT,
    ALTER COLUMN restaurant_id SET NOT NULL,
    ADD CONSTRAINT orders_courier_id_fkey FOREIGN KEY (courier_id) REFERENCES couriers (courier_id) ON DELETE SET NULL,
    ALTER COLUMN subtotal SET NOT NULL,
    ADD CONSTRAINT check_subtotal_positive CHECK (subtotal >= 0),
    ALTER COLUMN delivery_fee SET DEFAULT 0,
    ADD CONSTRAINT check_delivery_fee_positive CHECK (delivery_fee >= 0),
    ALTER COLUMN discount SET DEFAULT 0,
    ADD CONSTRAINT check_discount_positive CHECK (discount >= 0),
    ALTER COLUMN total_amount SET NOT NULL,
    ADD CONSTRAINT check_total_amount_positive CHECK (total_amount >= 0),
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'pending',
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- order items
ALTER TABLE order_items
    ADD CONSTRAINT add_order_items_primary_key PRIMARY KEY (order_item_id),
    ALTER COLUMN order_item_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN order_item_id SET NOT NULL,
    ADD CONSTRAINT order_items_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
    ALTER COLUMN order_id SET NOT NULL,
    ADD CONSTRAINT order_items_dish_id_fkey FOREIGN KEY (dish_id) REFERENCES dishes (dish_id) ON DELETE RESTRICT,
    ALTER COLUMN dish_id SET NOT NULL,
    ALTER COLUMN unit_price SET NOT NULL,
    ADD CONSTRAINT check_unit_price_positive CHECK (unit_price > 0),
    ALTER COLUMN quantity SET NOT NULL,
    ADD CONSTRAINT check_quantity_positive CHECK (quantity > 0),
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- payments
ALTER TABLE payments
    ADD CONSTRAINT add_payments_primary_key PRIMARY KEY (payment_id),
    ALTER COLUMN payment_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN payment_id SET NOT NULL,
    ADD CONSTRAINT payments_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE RESTRICT,
    ALTER COLUMN order_id SET NOT NULL,
    ADD CONSTRAINT payments_order_id_unique UNIQUE (order_id),
    ALTER COLUMN amount SET NOT NULL,
    ADD CONSTRAINT check_amount_positive CHECK (amount > 0),
    ALTER COLUMN payment_method SET NOT NULL,
    ALTER COLUMN status SET DEFAULT 'pending',
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

-- reviews
ALTER TABLE reviews
    ADD CONSTRAINT add_reviews_primary_key PRIMARY KEY (review_id),
    ALTER COLUMN review_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN review_id SET NOT NULL,
    ADD CONSTRAINT reviews_order_id_fkey FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
    ALTER COLUMN order_id SET NOT NULL,
    ADD CONSTRAINT reviews_order_id_unique UNIQUE (order_id),
    ADD CONSTRAINT check_restaurant_rating CHECK (restaurant_rating >= 1 AND restaurant_rating <= 5),
    ADD CONSTRAINT check_courier_rating CHECK (courier_rating >= 1 AND courier_rating <= 5),
    ADD CONSTRAINT check_delivery_speed CHECK (delivery_speed >= 1 AND delivery_speed <= 5),
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- admins
ALTER TABLE admins
    ADD CONSTRAINT add_admins_primary_key PRIMARY KEY (admin_id),
    ALTER COLUMN admin_id SET DEFAULT gen_random_uuid(),
    ALTER COLUMN admin_id SET NOT NULL,
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT admins_email_unique UNIQUE (email),
    ADD CONSTRAINT check_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    ALTER COLUMN password_hash SET NOT NULL,
    ADD CONSTRAINT check_password_hash_length CHECK (LENGTH(password_hash) >= 60),
    ALTER COLUMN role SET NOT NULL,
    ALTER COLUMN is_active SET DEFAULT TRUE,
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

-- admin_restaurants junction table
ALTER TABLE admin_restaurants
    ADD CONSTRAINT admin_restaurants_admin_id_fkey FOREIGN KEY (admin_id) REFERENCES admins (admin_id) ON DELETE CASCADE,
    ADD CONSTRAINT admin_restaurants_restaurant_id_fkey FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id) ON DELETE CASCADE;

COMMIT;