// PurchaseOrderDetail.java
package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "purchase_order_detail")
@Data
public class PurchaseOrderDetail {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_no", referencedColumnName = "order_no", nullable = false)
  @JsonBackReference
  private PurchaseOrder purchaseOrder;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

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