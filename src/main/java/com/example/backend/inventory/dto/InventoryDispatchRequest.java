
// 出庫登録リクエスト
package com.example.backend.inventory.dto;

import lombok.Data;
import java.math.BigDecimal;


@Data
public class InventoryDispatchRequest {

  private String itemCode; // 商品コード（必須）
  private BigDecimal quantity; // 出庫数（必須）
  private String operator; // 担当者（必須）
  private String remarks; // 備考（任意）
}
