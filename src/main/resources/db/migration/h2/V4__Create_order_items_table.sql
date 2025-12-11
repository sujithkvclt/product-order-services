-- Create order_items table
-- OrderItem entity extends BaseEntity with audit fields
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    discount_applied DECIMAL(10, 2),
    total_price DECIMAL(10, 2) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by_id BIGINT,
    updated_by_id BIGINT,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (created_by_id) REFERENCES users(id),
    FOREIGN KEY (updated_by_id) REFERENCES users(id)
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_deleted ON order_items(deleted);
CREATE INDEX idx_order_items_created_by ON order_items(created_by_id);
CREATE INDEX idx_order_items_updated_by ON order_items(updated_by_id);
