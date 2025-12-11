-- Create orders table
-- Order entity extends BaseEntity with audit fields
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_total DECIMAL(10, 2) NOT NULL,
    total_discount DECIMAL(10, 2),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT,
    updated_by_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (updated_by_id) REFERENCES users(id)
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_deleted ON orders(deleted);
CREATE INDEX idx_orders_created_by ON orders(created_by_id);
CREATE INDEX idx_orders_updated_by ON orders(updated_by_id);
