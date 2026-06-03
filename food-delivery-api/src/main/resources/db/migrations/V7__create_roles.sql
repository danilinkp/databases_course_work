BEGIN;

CREATE ROLE customer_role;
CREATE ROLE restaurant_admin_role;
CREATE ROLE courier_role;
CREATE ROLE system_admin_role;

GRANT SELECT ON restaurants, dish_categories, dishes TO customer_role;
GRANT SELECT, UPDATE ON customers TO customer_role;
GRANT SELECT, INSERT, UPDATE ON customer_addresses TO customer_role;
GRANT SELECT, INSERT ON orders TO customer_role;
GRANT SELECT, INSERT ON order_items TO customer_role;
GRANT SELECT ON payments TO customer_role;
GRANT SELECT ON delivery_zones TO customer_role;
GRANT SELECT, INSERT, UPDATE ON reviews TO customer_role;

GRANT SELECT, UPDATE ON couriers TO courier_role;
GRANT SELECT, UPDATE ON orders TO courier_role;
GRANT SELECT ON order_items TO courier_role;
GRANT SELECT ON restaurants TO courier_role;
GRANT SELECT ON customers TO courier_role;
GRANT SELECT ON customer_addresses TO courier_role;
GRANT SELECT ON delivery_zones TO courier_role;
GRANT SELECT ON reviews TO courier_role;

GRANT SELECT, UPDATE ON restaurants TO restaurant_admin_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON dish_categories TO restaurant_admin_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON dishes TO restaurant_admin_role;
GRANT SELECT, UPDATE ON orders TO restaurant_admin_role;
GRANT SELECT ON order_items TO restaurant_admin_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON delivery_zones TO restaurant_admin_role;
GRANT SELECT ON reviews TO restaurant_admin_role;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO system_admin_role;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO system_admin_role;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO system_admin_role;

COMMIT;