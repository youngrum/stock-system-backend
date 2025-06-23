## API test <swagger-ui>

http://localhost:8080/swagger-ui/index.html#/

## H2 DB

http://localhost:8080/h2-console/

### 開発

<jdbc:h2:file:Z:/inventory-db/inventory>

### ローカル

http://localhost:8080/h2-console/
<jdbc:h2:file:./data/inventory>

## データベース設計

```mermaid
erDiagram
    users {
        varchar(64) username PK "NOT NULL"
        varchar(255) password "NOT NULL"
        boolean enabled "NOT NULL"
    }

    stock_master {
        bigint id PK "AUTO_INCREMENT"
        varchar(32) item_code UK "UNIQUE"
        varchar(128) item_name "NOT NULL"
        varchar(64) model_number
        varchar(32) category "NOT NULL"
        varchar(64) manufacturer
        decimal current_stock "NOT NULL, DEFAULT 0"
        varchar(64) location
        timestamp last_updated
    }

    inventory_transaction {
        bigint id PK "AUTO_INCREMENT"
        varchar transaction_id "NOT NULL"
        varchar(32) item_code FK "NOT NULL"
        varchar order_no FK
        varchar(32) transaction_type "NOT NULL"
        decimal quantity "NOT NULL"
        varchar(64) operator "NOT NULL"
        timestamp transaction_time "DEFAULT CURRENT_TIMESTAMP, NOT NULL"
        varchar(64) manufacturer
        varchar(64) supplier
        decimal(10,2) purchase_price
        varchar(255) remarks
    }

    purchase_order {
        bigint id PK "AUTO_INCREMENT"
        varchar order_no UK "UNIQUE, NOT NULL"
        varchar(64) supplier "NOT NULL"
        decimal(14,2) order_subtotal "NOT NULL, DEFAULT 0"
        date order_date "NOT NULL"
        decimal(10,2) shipping_fee "NOT NULL, DEFAULT 0"
        varchar(64) operator "NOT NULL"
        varchar status "NOT NULL, DEFAULT 未完了"
        varchar(255) remarks
        date created_at "DEFAULT CURRENT_DATE"
    }

    purchase_order_detail {
        bigint id PK "AUTO_INCREMENT"
        varchar order_no FK "NOT NULL"
        varchar item_code
        varchar item_name
        varchar model_number
        varchar category
        decimal quantity "NOT NULL"
        decimal purchase_price "NOT NULL"
        decimal received_quantity "DEFAULT 0"
        varchar status "DEFAULT 未入庫"
        varchar remarks
    }

    numbering_master {
        bigint id PK "AUTO_INCREMENT"
        varchar(10) department_code "NOT NULL"
        varchar(20) numbering_type "NOT NULL"
        int fiscal_year "NOT NULL"
        bigint current_decimal_number "NOT NULL"
        timestamp created_at "NOT NULL, NOT UPDATABLE"
        timestamp updated_at "NOT NULL"
        int version "楽観的排他制御"
    }

    %% Relationships
    stock_master ||--o{ inventory_transaction : "item_code"
    purchase_order ||--o{ inventory_transaction : "order_no"
    purchase_order ||--o{ purchase_order_detail : "order_no"
    numbering_master ||--o{ purchase_order : "generates order_no"
