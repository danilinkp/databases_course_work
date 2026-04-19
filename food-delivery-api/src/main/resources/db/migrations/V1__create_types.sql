BEGIN;

CREATE TYPE order_status AS ENUM (
    'pending',
    'confirmed',
    'preparing',
    'ready',
    'delivering',
    'delivered',
    'cancelled'
    );

CREATE TYPE payment_method AS ENUM (
    'cash',
    'card',
    'online',
    'bonuses'
    );

CREATE TYPE payment_status AS ENUM (
    'pending',
    'processing',
    'completed',
    'failed',
    'refunded'
    );

COMMIT;