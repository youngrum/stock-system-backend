package com.example.backend.entity;

import java.io.Serializable;
import lombok.Data;

@Data
public class PurchaseOrderDetailId implements Serializable {
  private String orderNo;
  private String itemCode;
}
