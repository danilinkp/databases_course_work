BEGIN;

CREATE OR REPLACE FUNCTION update_restaurant_rating()
    RETURNS TRIGGER AS
$$
DECLARE
    v_restaurant_id UUID;
    v_new_rating    NUMERIC;
BEGIN
    SELECT o.restaurant_id
    INTO v_restaurant_id
    FROM orders o
    WHERE o.order_id = NEW.order_id;

    IF v_restaurant_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT ROUND(AVG(r.restaurant_rating)::NUMERIC, 2)
    INTO v_new_rating
    FROM reviews r
             JOIN orders o ON o.order_id = r.order_id
    WHERE o.restaurant_id = v_restaurant_id;

    UPDATE restaurants
    SET rating     = v_new_rating,
        updated_at = NOW()
    WHERE restaurant_id = v_restaurant_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_restaurant_rating
    AFTER INSERT OR UPDATE
    ON reviews
    FOR EACH ROW
EXECUTE FUNCTION update_restaurant_rating();


CREATE OR REPLACE FUNCTION update_courier_rating()
    RETURNS TRIGGER AS
$$
DECLARE
    v_courier_id UUID;
    v_new_rating NUMERIC;
BEGIN
    SELECT o.courier_id
    INTO v_courier_id
    FROM orders o
    WHERE o.order_id = NEW.order_id;

    IF v_courier_id IS NULL THEN
        RETURN NEW;
    END IF;

    SELECT ROUND(AVG(r.courier_rating)::NUMERIC, 2)
    INTO v_new_rating
    FROM reviews r
             JOIN orders o ON o.order_id = r.order_id
    WHERE o.courier_id = v_courier_id;

    UPDATE couriers
    SET rating     = v_new_rating,
        updated_at = NOW()
    WHERE courier_id = v_courier_id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_courier_rating
    AFTER INSERT OR UPDATE
    ON reviews
    FOR EACH ROW
EXECUTE FUNCTION update_courier_rating();

COMMIT;