package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_order")
@Data
public class PurchaseOrder {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;
  
  @Column(name = "order_no", unique = true, nullable = false)
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
  private String status = "未完了";

  @Column(name = "remarks", length = 255)
  private String remarks;

  @Column(name = "created_at", columnDefinition = "DATE DEFAULT CURRENT_DATE", updatable = false)
  @CreationTimestamp
  private LocalDate createdAt = LocalDate.now();

  @OneToMany(mappedBy = "purchaseOrder")
  @JsonManagedReference
  private List<PurchaseOrderDetail> details;
}
