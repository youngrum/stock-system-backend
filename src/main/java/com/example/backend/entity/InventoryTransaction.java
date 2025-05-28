package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transaction")
@Data
public class InventoryTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "transaction_id")
  private Long transactionId;

  // NULL許容: 出庫にはorderNoがない可能性あり
  @ManyToOne
  @JoinColumn(name = "order_no", referencedColumnName = "order_no")
  private PurchaseOrder purchaseOrder;

  @ManyToOne // 他のエンティティ(StockMaster)の参照を宣言
  // InventoryTransactionテーブルのitem_codeを使ってSTOCK_MASTERのitem_codeを外部参照
  @JoinColumn(name = "item_code", referencedColumnName = "item_code", nullable = false)
  private StockMaster stockItem;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 32)
  private TransactionType transactionType;

  @Column(name = "quantity", nullable = false)
  private BigDecimal quantity;

  @Column(name = "operator", nullable = false, length = 64)
  private String operator;

  @Column(name = "transaction_time", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime transactionTime;

  @Column(name = "manufacturer", length = 64)
  private String manufacturer;

  @Column(name = "supplier", length = 64)
  private String supplier;

  @Column(name = "purchase_price", precision = 10, scale = 2)
  private BigDecimal purchasePrice;

  @Column(name = "remarks", length = 255)
  private String remarks;
}
