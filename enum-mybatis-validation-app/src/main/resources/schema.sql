CREATE TABLE IF NOT EXISTS demo_orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_name   VARCHAR(100) NOT NULL,
    status          VARCHAR(20),
    pay_method      VARCHAR(20),
    user_grade      VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS demo_order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT NOT NULL REFERENCES demo_orders(id) ON DELETE CASCADE,
    product_name    VARCHAR(100) NOT NULL,
    sku             VARCHAR(50) NOT NULL,
    quantity        INT NOT NULL
);

-- 기존 DB 볼륨에 테이블이 이미 있을 수 있어 컬럼 추가를 허용
ALTER TABLE demo_orders ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT NOW();
