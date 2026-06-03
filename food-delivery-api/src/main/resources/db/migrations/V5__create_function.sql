BEGIN;

CREATE OR REPLACE FUNCTION calculate_order_total(
    p_order_id UUID,
    p_discount_rate NUMERIC DEFAULT 0.2
)
    RETURNS TABLE
            (
                subtotal     NUMERIC,
                delivery_fee NUMERIC,
                discount     NUMERIC,
                total_amount NUMERIC
            )
    LANGUAGE plpgsql
AS
$$
DECLARE
    v_order          orders%ROWTYPE;
    v_restaurant     restaurants%ROWTYPE;
    v_zone           delivery_zones%ROWTYPE;
    v_subtotal       NUMERIC;
    v_delivery_fee   NUMERIC;
    v_additional_fee NUMERIC;
    v_discount       NUMERIC := 0;
    v_distance       NUMERIC;
    v_hour           INTEGER;
    v_day_of_week    TEXT;
    v_orders_count   INTEGER;
    v_postal_code    TEXT;
BEGIN
    SELECT *
    INTO v_order
    FROM orders
    WHERE order_id = p_order_id;

    SELECT *
    INTO v_restaurant
    FROM restaurants
    WHERE restaurant_id = v_order.restaurant_id;

    SELECT ca.postal_code
    INTO v_postal_code
    FROM customer_addresses ca
    WHERE ca.customer_id = v_order.customer_id
      AND ca.is_default = TRUE
    LIMIT 1;

    SELECT *
    INTO v_zone
    FROM delivery_zones
    WHERE restaurant_id = v_order.restaurant_id
      AND postal_code = v_postal_code
    LIMIT 1;

    SELECT COALESCE(SUM(unit_price * quantity), 0)
    INTO v_subtotal
    FROM order_items
    WHERE order_id = p_order_id;

    IF v_subtotal >= v_zone.min_order_amount THEN
        v_delivery_fee := 0;
    ELSE
        v_distance := (
            2 * 6371 * ASIN(SQRT(
                    POWER(SIN(RADIANS((v_order.delivery_latitude - v_restaurant.latitude) / 2)), 2) +
                    COS(RADIANS(v_restaurant.latitude)) *
                    COS(RADIANS(v_order.delivery_latitude)) *
                    POWER(SIN(RADIANS((v_order.delivery_longitude - v_restaurant.longitude) / 2)), 2)
                            ))
            );

        v_delivery_fee := v_zone.delivery_fee;

        IF v_distance > v_zone.near_threshold THEN
            v_additional_fee := (v_distance - v_zone.near_threshold) * v_zone.fee_per_km;

            IF v_distance > v_zone.far_threshold THEN
                v_additional_fee := v_additional_fee * v_zone.far_zone_multiplier;
            END IF;

            v_delivery_fee := v_delivery_fee + v_additional_fee;
        END IF;

        v_hour := EXTRACT(HOUR FROM v_order.created_at);

        IF (v_hour >= 12 AND v_hour < 14) OR (v_hour >= 18 AND v_hour < 21) THEN
            v_delivery_fee := v_delivery_fee + v_zone.peak_surcharge;
        END IF;

        v_day_of_week := TO_CHAR(v_order.created_at, 'Day');

        IF TRIM(v_day_of_week) = 'Saturday' OR TRIM(v_day_of_week) = 'Sunday' THEN
            v_delivery_fee := v_delivery_fee + v_zone.weekend_surcharge;
        END IF;
    END IF;

    SELECT COUNT(*)
    INTO v_orders_count
    FROM orders
    WHERE customer_id = v_order.customer_id
      AND order_id != p_order_id;

    v_discount := 0;

    IF v_orders_count = 0 THEN
        v_discount := v_subtotal * p_discount_rate;
    END IF;

    RETURN QUERY SELECT ROUND(v_subtotal, 2),
                        ROUND(v_delivery_fee, 2),
                        ROUND(v_discount, 2),
                        ROUND(v_subtotal + v_delivery_fee - v_discount, 2);
END;
$$;


COMMIT;