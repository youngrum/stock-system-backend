package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_order")
@Data
public class PurchaseOrder {

  @Id
  @Column(name = "order_no", length = 64)
  private String orderNo;

  @Column(name = "supplier", nullable = false, length = 64)
  private String supplier;

  @Column(name = "order_subtotal", nullable = false, precision = 14, scale = 2)
  private BigDecimal orderSubtotal = BigDecimal.ZERO;

  @Column(name = "order_date", nullable = false)
  private LocalDate orderDate;

  @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
  private BigDecimal shippingFee = BigDecimal.ZERO;

  @Column(name = "operator", nullable = false, length = 64)
  private String operator;

  @Column(name = "status", nullable = false)
  private String status = "未完納";

  @Column(name = "remarks", length = 255)
  private String remarks;

  @Column(name = "created_at", columnDefinition = "DATE DEFAULT CURRENT_DATE", updatable = false)
  @CreationTimestamp
  private LocalDate createdAt = LocalDate.now();

  @OneToMany
  @JoinColumn(name = "order_no", referencedColumnName = "order_no", insertable = false, updatable = false)
  private List<PurchaseOrderDetail> details;
}
