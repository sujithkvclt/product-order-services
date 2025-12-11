-- Insert sample users (password is 'password123' encrypted with BCrypt)
INSERT INTO users (username, email, password, role, enabled, created_at, updated_at) VALUES
('admin', 'admin@example.com', '$2a$10$0VaYGLymDS/FCaK7oQz/IeAnMysW5vvE2T5sMWSrhWY7BaIMo7BD6', 'ADMIN', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('premiumuser', 'premium@example.com', '$2a$10$0VaYGLymDS/FCaK7oQz/IeAnMysW5vvE2T5sMWSrhWY7BaIMo7BD6', 'PREMIUM_USER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('regularuser', 'user@example.com', '$2a$10$0VaYGLymDS/FCaK7oQz/IeAnMysW5vvE2T5sMWSrhWY7BaIMo7BD6', 'USER', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample products with audit fields (created_by and updated_by set to admin user ID 1)
INSERT INTO products (name, description, price, quantity, deleted, created_at, updated_at, created_by_id, updated_by_id) VALUES
('Laptop', 'High-performance laptop with 16GB RAM', 1200.00, 50, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('Smartphone', 'Latest smartphone with 5G support', 800.00, 100, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('Wireless Mouse', 'Ergonomic wireless mouse', 25.00, 200, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('Mechanical Keyboard', 'RGB mechanical gaming keyboard', 150.00, 75, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('USB-C Cable', 'High-speed USB-C charging cable', 15.00, 500, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('Monitor', '27-inch 4K UHD monitor', 450.00, 30, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('Headphones', 'Noise-cancelling wireless headphones', 200.00, 60, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('Webcam', '1080p HD webcam with microphone', 80.00, 120, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('External SSD', '1TB portable external SSD', 120.00, 90, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1),
('Desk Lamp', 'LED desk lamp with adjustable brightness', 35.00, 150, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 1);
