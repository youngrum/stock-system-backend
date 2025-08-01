// PurchaseOrderDetail.java
package com.example.backend.entity;

import java.math.BigDecimal;

import com.example.backend.order.dto.PurchaseOrderRequest;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "purchase_order_detail")
@Data
public class PurchaseOrderDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_no", referencedColumnName = "order_no", nullable = false)
  @JsonBackReference
  private PurchaseOrder purchaseOrder;

  @Column(name = "item_type", length = 50, nullable = false) // 物品かサービスか分岐（ITEM, SERVICE）
  private String itemType = "ITEM";

  @Column(name = "service_type", nullable = true) // 校正or修理か分岐(CALIBRATION, REPAIR)
  private String serviceType;

  @Column(name = "manufacturer", length = 64, nullable = true) // メーカー
  private String manufacturer = "-";

  @Column(name = "item_code", length = 64, nullable = true)
  private String itemCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "related_asset_id", referencedColumnName = "id", nullable = true)
  private AssetMaster relatedAsset; // 既存購入設備のID (AssetMaster.id)を参照

  @Column(name = "linked_id", nullable = true)
  private Long linkedId; // 新規購入設備の発注明細ID (PurchaseOrderDetail.id)を参照

  @Column(name = "item_name", length = 128, nullable = false)
  private String itemName;

  @Column(name = "model_number", nullable = true)
  private String modelNumber = "-";

  @Column(name = "category", length = 64, nullable = false)
  private String category;

  @Column(name = "quantity", precision = 10, scale = 2, nullable = true) // 発注数
  private java.math.BigDecimal quantity = java.math.BigDecimal.ZERO;;

  @Column(name = "purchase_price", precision = 14, scale = 2, nullable = false)
  private java.math.BigDecimal purchasePrice = java.math.BigDecimal.ZERO;;

  @Column(name = "received_quantity", length = 20, nullable = true) // 受領数
  private java.math.BigDecimal receivedQuantity = java.math.BigDecimal.ZERO;

  @Column(name = "status", length = 20, nullable = true)
  private String status = "未入庫";

  @Column(name = "remarks", length = 20, nullable = true)
  private String remarks;

  /**
   * 発注明細の基本情報を登録
   * @param header
   * @param detail
   * @return
   */
  public static PurchaseOrderDetail createBaseOrderDetail(PurchaseOrder header, PurchaseOrderRequest.Detail detail) {
      PurchaseOrderDetail orderDetail = new PurchaseOrderDetail();
      orderDetail.setPurchaseOrder(header);
      orderDetail.setItemName(detail.getItemName());
      orderDetail.setQuantity(detail.getQuantity());
      orderDetail.setPurchasePrice(detail.getPurchasePrice());
      orderDetail.setRemarks(detail.getRemarks());
      orderDetail.setStatus("未入庫");
      orderDetail.setItemType(detail.getItemType());
      return orderDetail;
  }

  /**
   * ネストされたservicesオブジェクトを発注明細オブジェクト用に加工して登録
   * @param header
   * @param serviceReq
   * @param parentDetail
   * @return
   */
  public static PurchaseOrderDetail createNestedServiceDetail(PurchaseOrder header, PurchaseOrderRequest.ServiceRequest serviceReq, PurchaseOrderDetail parentDetail) {
      PurchaseOrderDetail serviceDetail = new PurchaseOrderDetail();
      serviceDetail.setPurchaseOrder(header);
      serviceDetail.setItemType("SERVICE");
      serviceDetail.setServiceType(serviceReq.getServiceType());
      serviceDetail.setItemName(serviceReq.getItemName());
      serviceDetail.setQuantity(serviceReq.getQuantity());
      serviceDetail.setPurchasePrice(serviceReq.getPurchasePrice() != null ? serviceReq.getPurchasePrice() : BigDecimal.ZERO);
      serviceDetail.setStatus("未入庫");
      serviceDetail.setReceivedQuantity(BigDecimal.ZERO);
      serviceDetail.setLinkedId(parentDetail.getId());
      serviceDetail.setRelatedAsset(null);
      
      // 物品関連カラムはNULL
      serviceDetail.setItemCode(null);
      serviceDetail.setModelNumber(null);
      
      return serviceDetail;
  }

}