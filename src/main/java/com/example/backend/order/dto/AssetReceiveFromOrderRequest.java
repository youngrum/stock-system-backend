package com.example.backend.order.dto;

import lombok.Data;
import java.util.List;
import java.math.BigDecimal;

@Data
public class AssetReceiveFromOrderRequest {
  private String orderNo;
  private String orderType; // 発注区分（INVENT / ASSET）
  private List<Item> items;

  @Data
  public static class Item {
    private Long id; // 発注明細テーブルid
    // private String itemName; // 設備名
    private String itemType; // 設備系 物品orサービスの分岐（ITEM, SERVICE）
    // private String manufacturer; // メーカー
    // private String modelNumber; // 型番
    // private String category; // カテゴリ
    // private String supplier; // 仕入先
    // private BigDecimal purchasePrice; // 購入価格
    // private String serialNumber; // 製造番号
    private BigDecimal quantity; // 本リクエストで納品される数量 運用上1のみを想定
    private Boolean calibrationRequired; // 校正要否
    private String remarks; // 備考
  }

}