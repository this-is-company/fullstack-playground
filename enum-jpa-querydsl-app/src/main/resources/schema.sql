CREATE TABLE IF NOT EXISTS qdsl_demo_orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_name   VARCHAR(100) NOT NULL,
    status          VARCHAR(20),
    pay_method      VARCHAR(20),
    user_grade      VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS qdsl_demo_order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES qdsl_demo_orders(id) ON DELETE CASCADE,
    product_name    VARCHAR(100) NOT NULL,
    sku             VARCHAR(50) NOT NULL,
    quantity        INT NOT NULL
);
