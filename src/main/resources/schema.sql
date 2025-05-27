CREATE TABLE users (
  username VARCHAR(64) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  department VARCHAR(64),
  enabled BOOLEAN NOT NULL
);
CREATE TABLE stock_master (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_code VARCHAR(64) NOT NULL UNIQUE,
  item_name VARCHAR(128) NOT NULL,
  category VARCHAR(64),
  model_number VARCHAR(64),
  manufacturer VARCHAR(64);
  current_stock INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE inventory_transaction (
  transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_code VARCHAR(64) NOT NULL,
  transaction_type VARCHAR(10) NOT NULL,
  quantity DECIMAL(14, 2) NOT NULL,
  operator VARCHAR(64) NOT NULL,
  transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  manufacturer VARCHAR(64),
  supplier VARCHAR(64),
  purchase_price DECIMAL(10, 2),
  order_no VARCHAR(64),
  remarks VARCHAR(255),
  CONSTRAINT fk_inventory_transaction_order_no
    FOREIGN KEY (order_no) REFERENCES purchase_order(order_no),
  CONSTRAINT fk_inventory_transaction_item_code
    FOREIGN KEY (item_code) REFERENCES stock_master(item_code)
);

CREATE TABLE purchase_order (
  order_no VARCHAR(64) NOT NULL PRIMARY KEY,
  supplier VARCHAR(64) NOT NULL,
  order_subtotal DECIMAL(14,2) NOT NULL DEFAULT 0,
  order_date DATE NOT NULL,
  shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
  operator VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT '未完納',
  remarks VARCHAR(255),
  created_at DATE DEFAULT CURRENT_DATE
);

CREATE TABLE purchase_order_detail (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL,
  item_code VARCHAR(64),
  item_name VARCHAR(128),
  model_number VARCHAR(64),
  category VARCHAR(64),
  quantity DECIMAL(14,2) NOT NULL,
  CONSTRAINT fk_purchase_order_detail_order_no
    FOREIGN KEY (order_no)
    REFERENCES purchase_order(order_no)
);
