package com.example.backend.order.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class AssetReceiveFromOrderRequest {
  private String orderNo;
  private String orderType; // 発注区分（在庫 / 設備）
  private List<Item> items;

  @Data
  public static class Item {
    private String itemName; // 設備名
    private String itemType; // 設備系 物品orサービスの分岐（ITEM, SERVICE）
    private String manufacturer; // メーカー
    private String modelNumber; // 型番
    private String category; // カテゴリ
    private String supplier; // 仕入先
    private BigDecimal purchasePrice; // 購入価格
    private String serialNumber; // 製造番号
    private BigDecimal receivedQuantity; // 受領数量
    private String remarks; // 備考
  }

}