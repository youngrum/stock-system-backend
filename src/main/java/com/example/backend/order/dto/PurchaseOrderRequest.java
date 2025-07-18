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
    private String itemCode;
    private String itemName;
    private String itemType;  // 物品orサービスの分岐（ITEM, SERVICE）
    private String serviceType; // 校正or修理の分岐 (CALIBRATION, REPAIR)
    private String modelNumber;
    private Long relatedAssetId;
    private String category;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private String location; // 保管場所（任意）
    private String remarks;
    // ITEMタイプの場合にネストされるサービス(=校正)明細
    private List<ServiceRequest> services;
  }

  @Data
  public static class ServiceRequest {
    private String serviceType; // "SERVICE"固定
    private BigDecimal servicePrice; // 校正料金
    private String itemName;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
  }
}
