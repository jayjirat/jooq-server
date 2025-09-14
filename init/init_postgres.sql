-- =========================
-- DATABASE: poc_demo
-- =========================

-- =========================
-- Table: users
-- =========================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- Table: roles
-- =========================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- =========================
-- Table: user_roles (N:M users ↔ roles)
-- =========================
CREATE TABLE user_roles (
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- =========================
-- Table: products
-- =========================
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =========================
-- Table: orders
-- =========================
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING'
);

-- =========================
-- Table: order_items (1:N orders → products)
-- =========================
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id INT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL DEFAULT 1,
    unit_price NUMERIC(10,2) NOT NULL
);

-- =========================
-- Sample Data
-- =========================

-- Roles
INSERT INTO roles (name) VALUES
('ADMIN'),
('USER'),
('GUEST');

-- Users
INSERT INTO users (username, email, password) VALUES
('alice', 'alice@example.com', 'pass123'),
('bob', 'bob@example.com', 'pass123'),
('charlie', 'charlie@example.com', 'pass123');

-- User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- alice → ADMIN
(1, 2), -- alice → USER
(2, 2), -- bob → USER
(3, 3); -- charlie → GUEST

-- Products
INSERT INTO products (name, description, price) VALUES
('Laptop', 'High-end gaming laptop', 1500.00),
('Mouse', 'Wireless mouse', 25.50),
('Keyboard', 'Mechanical keyboard', 70.00),
('Monitor', '27-inch 4K monitor', 350.00);

-- Orders
INSERT INTO orders (user_id, status) VALUES
(1, 'COMPLETED'),
(2, 'PENDING'),
(1, 'CANCELLED');

-- Order Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 1500.00),
(1, 2, 2, 25.50),
(2, 3, 1, 70.00),
(3, 4, 1, 350.00);
