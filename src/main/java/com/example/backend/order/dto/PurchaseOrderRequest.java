// 発注ヘッダーの登録リクエスト
package com.example.backend.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderRequest {
  private String supplier;
  private BigDecimal shippingFee;
  private String operator;
  private String remarks;
  private List<Detail> details;

  @Data
  public static class Detail {
    private String itemCode;
    private String itemName;
    private String modelNumber;
    private String category;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private String remarks;
  }
}
