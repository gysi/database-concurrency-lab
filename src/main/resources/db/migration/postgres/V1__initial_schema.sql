CREATE TABLE users
(
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)              NOT NULL,
    email         VARCHAR(100)             NOT NULL UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    balance       NUMERIC(15, 2) DEFAULT 0 NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100)   NOT NULL,
    description TEXT,
    price       NUMERIC(10, 2) NOT NULL,
    stock       INTEGER        NOT NULL CHECK (stock >= 0),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders
(
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER        NOT NULL,
    product_id  INTEGER        NOT NULL,
    quantity    INTEGER        NOT NULL CHECK (quantity > 0),
    total_price NUMERIC(10, 2) NOT NULL,
    order_date  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE
);
