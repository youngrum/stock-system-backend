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
        varchar username PK "NOT NULL"
        varchar password "NOT NULL"
        varchar department
        boolean enabled "NOT NULL"
    }

    stock_master {
        bigint id PK "AUTO_INCREMENT"
        varchar item_code UK "NOT NULL, UNIQUE"
        varchar item_name "NOT NULL"
        varchar category
        varchar model_number
        varchar manufacturer
        int current_stock "NOT NULL"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
        timestamp updated_at "DEFAULT CURRENT_TIMESTAMP ON UPDATE"
    }

    inventory_transaction {
        bigint transaction_id PK "AUTO_INCREMENT"
        varchar id "NOT NULL"
        varchar item_code FK "NOT NULL"
        varchar transaction_type "NOT NULL"
        decimal quantity "NOT NULL"
        varchar operator "NOT NULL"
        timestamp transaction_time "DEFAULT CURRENT_TIMESTAMP, NOT NULL"
        varchar manufacturer
        varchar supplier
        decimal purchase_price
        varchar order_no FK
        varchar remarks
    }

    purchase_order {
        varchar order_no PK "NOT NULL"
        varchar supplier "NOT NULL"
        decimal order_subtotal "NOT NULL, DEFAULT 0"
        date order_date "NOT NULL"
        decimal shipping_fee "NOT NULL, DEFAULT 0"
        varchar operator "NOT NULL"
        varchar status "NOT NULL, DEFAULT 未完了"
        varchar remarks
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
        decimal received_quantity
        varchar status
        varchar remarks
    }

    numbering_master {
        bigint id PK "AUTO_INCREMENT"
        varchar department_code "NOT NULL, LENGTH 10"
        varchar numbering_type "NOT NULL, LENGTH 20"
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