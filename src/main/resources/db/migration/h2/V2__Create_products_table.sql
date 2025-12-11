-- Create products table
-- Product entity extends BaseEntity with audit fields
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT,
    updated_by_id BIGINT,
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (updated_by_id) REFERENCES users(id)
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_deleted ON products(deleted);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_created_by ON products(created_by_id);
CREATE INDEX idx_products_updated_by ON products(updated_by_id);
