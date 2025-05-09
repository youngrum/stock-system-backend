package com.example.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class InventoryReceiveFromOrderRequest {
  private String orderNo;
  private String operator;
  private List<Item> items;

  @Data
  public static class Item {
    private String itemCode;
    private Integer receivedQuantity;
    private Integer purchasePrice;
    private String remarks;
  }
}
