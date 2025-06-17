CREATE TABLE users
(
  username VARCHAR(64) NOT NULL PRIMARY KEY,
  password VARCHAR(255) NOT NULL,
  department VARCHAR(64),
  enabled BOOLEAN NOT NULL
);

CREATE TABLE stock_master
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_code VARCHAR(64) NOT NULL UNIQUE,
  item_name VARCHAR(128) NOT NULL,
  category VARCHAR(64) NOT NULL,
  model_number VARCHAR(64),
  manufacturer VARCHAR(64),
  current_stock DECIMAL(14,2) NOT NULL DEFAULT 0,
  location VARCHAR(64) DEFAULT '-',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE purchase_order
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL UNIQUE,
  supplier VARCHAR(64) NOT NULL,
  order_subtotal DECIMAL(14,2) NOT NULL DEFAULT 0,
  order_date DATE NOT NULL,
  shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
  operator VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT '未完了',
  remarks VARCHAR(255),
  created_at DATE DEFAULT (CURRENT_DATE)
);

CREATE TABLE purchase_order_detail
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_no VARCHAR(64) NOT NULL,
  item_code VARCHAR(64),
  item_name VARCHAR(128),
  model_number VARCHAR(64),
  category VARCHAR(64),
  quantity DECIMAL(14,2) NOT NULL,
  purchase_price DECIMAL(10,2) NOT NULL,
  received_quantity DECIMAL(14,2) DEFAULT 0,
  status VARCHAR(32) DEFAULT '未入庫',
  remarks VARCHAR(255),
  CONSTRAINT fk_purchase_order_detail_order_no
    FOREIGN KEY (order_no)
    REFERENCES purchase_order (order_no)
);

CREATE TABLE inventory_transaction
(
  transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  item_code VARCHAR(64) NOT NULL,
  transaction_type VARCHAR(32) NOT NULL,
  quantity DECIMAL(14, 2) NOT NULL,
  operator VARCHAR(64) NOT NULL,
  transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  manufacturer VARCHAR(64),
  supplier VARCHAR(64),
  purchase_price DECIMAL(10, 2),
  order_no VARCHAR(64),
  remarks VARCHAR(255),
  CONSTRAINT fk_inventory_transaction_order_no
    FOREIGN KEY (order_no) REFERENCES purchase_order (order_no),
  CONSTRAINT fk_inventory_transaction_item_code
    FOREIGN KEY (item_code) REFERENCES stock_master (item_code)
);

CREATE TABLE numbering_master
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  department_code VARCHAR(10) NOT NULL,
  numbering_type VARCHAR(20) NOT NULL,
  fiscal_year INTEGER NOT NULL,
  current_decimal_number BIGINT NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  version INTEGER DEFAULT 0
);