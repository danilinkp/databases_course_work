BEGIN;

CREATE TABLE IF NOT EXISTS customers
(
    customer_id   UUID,

    full_name     VARCHAR(150),
    email         VARCHAR(255),
    phone         VARCHAR(20),

    password_hash VARCHAR(255),
    bonuses       INTEGER,
    is_active     BOOLEAN,

    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer_addresses
(
    address_id      UUID,

    customer_id     UUID,

    region          VARCHAR(100),
    city            VARCHAR(100),
    street          VARCHAR(200),
    house           VARCHAR(20),
    apartment       VARCHAR(20),
    address_details TEXT,
    postal_code     VARCHAR(20),
    latitude        DECIMAL(12, 8),
    longitude       DECIMAL(12, 8),

    is_default      BOOLEAN,

    created_at      TIMESTAMP
);


CREATE TABLE IF NOT EXISTS couriers
(
    courier_id     UUID,

    full_name      VARCHAR(75),
    phone          VARCHAR(20),
    email          VARCHAR(255),
    password_hash  VARCHAR(255),
    employee_date  DATE,

    area_of_work   VARCHAR(100),
    vehicle_type   vehicle_type,
    rating         DECIMAL(3, 2),

    is_available   BOOLEAN,
    is_active      BOOLEAN,

    created_at     TIMESTAMP,
    updated_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS restaurants
(
    restaurant_id UUID,

    name          VARCHAR(75),
    cuisine_type  VARCHAR(25),
    rating        DECIMAL(4, 2),
    review_count  INTEGER,

    address       TEXT,
    phone         VARCHAR(20),
    email         VARCHAR(255),
    latitude      DECIMAL(12, 8),
    longitude     DECIMAL(12, 8),

    opening_time  TIME,
    closing_time  TIME,

    is_active     BOOLEAN,

    created_at    TIMESTAMP,
    updated_at    TIMESTAMP
);


CREATE TABLE IF NOT EXISTS delivery_zones
(
    zone_id           UUID,

    restaurant_id     UUID,

    zone_name         VARCHAR,
    postal_code       VARCHAR,
    delivery_fee      DECIMAL(10, 2),
    near_threshold    DECIMAL(10, 2),
    far_threshold     DECIMAL(10, 2),
    fee_per_km        DECIMAL(10, 2),
    peak_surcharge    DECIMAL(10, 2),
    weekend_surcharge DECIMAL(10, 2),
    min_order_amount  DECIMAL(10, 2),
    delivery_time     INTEGER
);

CREATE TABLE IF NOT EXISTS dish_categories
(
    category_id   UUID,

    name          VARCHAR(100),
    description   TEXT,
    display_order INTEGER,

    created_at    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dishes
(
    dish_id          UUID,

    restaurant_id    UUID,
    category_id      UUID,

    name             VARCHAR(150),
    description      TEXT,
    price            DECIMAL(10, 2),

    image_url        TEXT,
    is_available     BOOLEAN,
    is_spicy         BOOLEAN,
    preparation_time INTEGER,
    calories         INTEGER,
    weight_grams     INTEGER,

    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders
(
    order_id         UUID,
    order_number     VARCHAR(20),

    customer_id      UUID,
    restaurant_id    UUID,
    courier_id       UUID,

    customer_name    VARCHAR(150),
    customer_phone   VARCHAR(20),
    restaurant_name  VARCHAR(150),
    delivery_address TEXT,
    delivery_latitude DECIMAL(12, 8),
    delivery_longitude DECIMAL(12, 8),

    subtotal         DECIMAL(10, 2),
    delivery_fee     DECIMAL(10, 2),
    discount         DECIMAL(10, 2),
    total_amount     DECIMAL(10, 2),

    status           order_status,

    created_at       TIMESTAMP,
    confirmed_at     TIMESTAMP,
    delivered_at     TIMESTAMP,
    cancelled_at     TIMESTAMP,
    updated_at       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items
(
    order_item_id    UUID,
    order_id         UUID,
    dish_id          UUID,

    dish_name        VARCHAR(150),
    unit_price       DECIMAL(10, 2),
    quantity         INTEGER,

    special_requests TEXT,
    created_at       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments
(
    payment_id              UUID,

    order_id                UUID,

    amount                  DECIMAL(10, 2),
    payment_method          payment_method,
    status                  payment_status,

    external_transaction_id VARCHAR(255),
    payment_gateway         VARCHAR(50),
    error_message           TEXT,
    metadata                JSONB,

    created_at              TIMESTAMP,
    processed_at            TIMESTAMP,
    completed_at            TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reviews
(
    review_id         UUID,
    order_id          UUID,

    restaurant_rating INTEGER,
    courier_rating    INTEGER,
    delivery_speed    INTEGER,

    comment           TEXT,

    created_at        TIMESTAMP,
    updated_at        TIMESTAMP
);

COMMIT;