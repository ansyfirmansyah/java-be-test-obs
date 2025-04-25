-- Initial Items Data
INSERT INTO item (id, name, price) VALUES (1, 'Pensil 2B', 2500.0);
INSERT INTO item (id, name, price) VALUES (2, 'Buku Tulis A5', 5000.0);
INSERT INTO item (id, name, price) VALUES (3, 'Pulpen Hitam', 3500.0);
INSERT INTO item (id, name, price) VALUES (4, 'Penggaris 30cm', 4000.0);
INSERT INTO item (id, name, price) VALUES (5, 'Penghapus', 1500.0);
INSERT INTO item (id, name, price) VALUES (6, 'Tip-X', 7500.0);
INSERT INTO item (id, name, price) VALUES (7, 'Kertas HVS A4', 500.0);
INSERT INTO item (id, name, price) VALUES (8, 'Map Plastik', 3000.0);
INSERT INTO item (id, name, price) VALUES (9, 'Stabilo', 8500.0);
INSERT INTO item (id, name, price) VALUES (10, 'Sticky Notes', 10000.0);

-- Initial Inventory Data (Top Up)
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (1, 1, 100, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (2, 2, 80, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (3, 3, 120, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (4, 4, 50, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (5, 5, 200, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (6, 6, 75, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (7, 7, 500, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (8, 8, 60, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (9, 9, 45, 'T', NULL);
INSERT INTO inventory (id, item_id, qty, type, order_id) VALUES (10, 10, 30, 'T', NULL);

-- Reset sequences to continue from our initial data
ALTER TABLE item ALTER COLUMN id RESTART WITH 11;
ALTER TABLE inventory ALTER COLUMN id RESTART WITH 11;