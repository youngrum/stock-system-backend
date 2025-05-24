package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "purchase_order_detail")
@Data
@IdClass(PurchaseOrderDetailId.class)
public class PurchaseOrderDetail {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_no", insertable = false)
  private PurchaseOrder purchaseOrder;

  @Id
  @Column(name = "item_code")
  private String itemCode;

  @Column(name = "item_name")
  private String itemName;

  @Column(name = "model_number")
  private String modelNumber;

  private String category;

  @Column(nullable = false)
  private java.math.BigDecimal quantity;

  @Column(nullable = false)
  private java.math.BigDecimal purchasePrice;

  @Column(nullable = true)
  private java.math.BigDecimal receivedQuantity = java.math.BigDecimal.ZERO;

  @Column(nullable = true)
  private String status = "未入庫";

  @Column(nullable = true)
  private String remarks;
}
