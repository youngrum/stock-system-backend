package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

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
}
