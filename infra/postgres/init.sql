CREATE TABLE IF NOT EXISTS users (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    department  VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO users (name, email, department) VALUES
    ('Kim Minsoo', 'minsoo.kim@example.com', 'Engineering'),
    ('Lee Jihye', 'jihye.lee@example.com', 'Product'),
    ('Park Junho', 'junho.park@example.com', 'Engineering'),
    ('Choi Sora', 'sora.choi@example.com', 'Design'),
    ('Jung Haeun', 'haeun.jung@example.com', 'Operations')
ON CONFLICT (email) DO NOTHING;
