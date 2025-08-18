package com.example.backend.order.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
/**
 * * 発注登録に行う入庫登録リクエスト
 */
public class InventoryReceiveFromOrderRequest {
  private String orderNo;
  private String orderType; // 発注区分（INVENT / ASSET）
  private String operator;
  private List<Item> items;

  @Data
  public static class Item {
    private String itemCode;
    private BigDecimal receivedQuantity;
    // ↓ リクエスト側で明示的に価格を渡さず、サーバー側で purchase_order_detail から取得する方針に変更
    // private Integer purchasePrice;
    private String remarks;
  }
}
