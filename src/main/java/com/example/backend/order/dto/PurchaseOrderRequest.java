package com.example.backend.order.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 発注登録のためのリクエストDTO 
 */
@Data
public class PurchaseOrderRequest {
  private String supplier;
  private BigDecimal shippingFee;
  private BigDecimal discount; // 割引
  private String orderType; // 発注区分 "INVENTORY" or "ASSET"
  private String operator;
  private BigDecimal calibrationCert; // 校正証明書データ料
  private BigDecimal traceabilityCert; // トレーサビリティ証明書データ料
  private String remarks;
  private List<Detail> details;

  @Data
  public static class Detail {
    private String itemCode; // 在庫系
    private String itemName; // 共通
    private String itemType;  // 設備系 物品orサービスの分岐（ITEM, SERVICE）
    private String serviceType; // 設備系 校正or修理の分岐 (CALIBRATION, REPAIR)
    private String modelNumber; // 共通
    private Long relatedAssetId; // 設備系
    private String category; // 共通
    private BigDecimal quantity; // 共通
    private BigDecimal purchasePrice; // 共通
    private String location; // 在庫系 保管場所（任意）
    private String remarks; // 共通
    // 設備系 ITEMタイプの場合にネストされるサービス(=校正)明細
    private List<ServiceRequest> services;
  }

  @Data
  public static class ServiceRequest {
    private String serviceType; // 設備系 校正or修理の分岐 (CALIBRATION, REPAIR)
    private BigDecimal purchasePrice;
    private String itemName;
    private BigDecimal quantity;
  }
}
