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
        varchar(64) department
        boolean enabled "NOT NULL"
    }

    stock_master {
        bigint id PK "AUTO_INCREMENT"
        varchar(64) item_code UK "NOT NULL, UNIQUE"
        varchar(128) item_name "NOT NULL"
        varchar(64) category
        varchar(64) model_number
        varchar(64) manufacturer
        int current_stock "NOT NULL"
        timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
        timestamp updated_at "DEFAULT CURRENT_TIMESTAMP ON UPDATE"
    }

    inventory_transaction {
        bigint transaction_id PK "AUTO_INCREMENT"
        varchar(64) item_code FK "NOT NULL"
        varchar(32) transaction_type "NOT NULL"
        decimal(14,2) quantity "NOT NULL"
        varchar(64) operator "NOT NULL"
        timestamp transaction_time "DEFAULT CURRENT_TIMESTAMP, NOT NULL"
        varchar(64) manufacturer
        varchar(64) supplier
        decimal(10,2) purchase_price
        varchar(64) order_no FK
        varchar(255) remarks
    }

    purchase_order {
        varchar(64) order_no PK "NOT NULL"
        varchar(64) supplier "NOT NULL"
        decimal(14,2) order_subtotal "NOT NULL, DEFAULT 0"
        date order_date "NOT NULL"
        decimal(10,2) shipping_fee "NOT NULL, DEFAULT 0"
        varchar(64) operator "NOT NULL"
        varchar(32) status "NOT NULL, DEFAULT '未完了'"
        varchar(255) remarks
        date created_at "DEFAULT CURRENT_DATE"
    }

    purchase_order_detail {
        bigint id PK "AUTO_INCREMENT"
        varchar(64) order_no FK "NOT NULL"
        varchar(64) item_code
        varchar(128) item_name
        varchar(64) model_number
        varchar(64) category
        decimal(14,2) quantity "NOT NULL"
        decimal(10,2) purchasePrice "NOT NULL"
        decimal(10,2) receivedQuantity
        varchar(32) status
        varchar(255) remarks
    }

    %% Relationships
    stock_master ||--o{ inventory_transaction : "item_code"
    purchase_order ||--o{ inventory_transaction : "order_no"
    purchase_order ||--o{ purchase_order_detail : "order_no"