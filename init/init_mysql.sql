-- =========================
-- DATABASE: poc_demo
-- =========================

-- =========================
-- Table: users
-- =========================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================
-- Table: roles
-- =========================
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- =========================
-- Table: user_roles (N:M users ↔ roles)
-- =========================
CREATE TABLE user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =========================
-- Table: products
-- =========================
CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =========================
-- Table: orders
-- =========================
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =========================
-- Table: order_items (1:N orders → products)
-- =========================
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

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
