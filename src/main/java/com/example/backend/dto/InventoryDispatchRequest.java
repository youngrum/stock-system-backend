
// 出庫登録リクエスト
package com.example.backend.dto;

import lombok.Data;

@Data
public class InventoryDispatchRequest {

  private String itemCode; // 商品コード（必須）
  private Integer quantity; // 出庫数（必須）
  private String operator; // 担当者（必須）
  private String remarks; // 備考（任意）
}
