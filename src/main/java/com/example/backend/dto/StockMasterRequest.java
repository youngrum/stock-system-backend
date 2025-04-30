// 在庫マスタ登録用
package com.example.backend.dto;

import lombok.Data;

@Data
public class StockMasterRequest {

  private String itemCode; // 商品コード
  private String itemName; // 名称
  private String modelNumber; // 型番（任意）
  private String category; // カテゴリ
  private Integer currentStock; // 初期在庫（任意）
}
