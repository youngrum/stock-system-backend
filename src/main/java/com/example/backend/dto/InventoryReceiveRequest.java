// 入庫登録リクエスト
package com.example.backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InventoryReceiveRequest {

  private String orderNo; // 発注番号（必須）
  private String itemCode; // 商品コード（必須）
  private Integer quantity; // 入庫数（必須）
  private String operator; // 担当者（必須）

  private String manufacturer; // メーカー（任意）
  private String supplier; // 仕入先（任意）
  private BigDecimal purchasePrice; // 仕入れ単価（任意）
  private String remarks; // 備考（任意）
  // private boolean confirm;// 類似品確認後の本登録フラグ
}
