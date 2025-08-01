// AssetOrderHandler.java - アセット発注処理のハンドラー
package com.example.backend.order.service.handler;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.entity.AssetMaster;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.asset.repository.AssetMasterRepository;
import com.example.backend.order.dto.PurchaseOrderRequest;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AssetOrderHandler {

    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final AssetMasterRepository assetMasterRepository;

    /**
     * 設備系統の発注明細処理
     * @param header
     * @param details
     * @param username
     * @return 小計
     */
    @Transactional
    public BigDecimal processOrderDetails(PurchaseOrder header, List<PurchaseOrderRequest.Detail> details, String username) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (PurchaseOrderRequest.Detail detail : details) {
            System.out.println("▶ 設備/校正明細処理中: " + detail.getItemName() + " (itemType: " + detail.getItemType() + ")");
            
            PurchaseOrderDetail orderDetail = PurchaseOrderDetail.createBaseOrderDetail(header, detail);
            
            if ("ITEM".equalsIgnoreCase(detail.getItemType())) {
                configureItemDetail(orderDetail, detail);
            } else if ("SERVICE".equalsIgnoreCase(detail.getItemType())) {
                configureServiceDetail(orderDetail, detail);
            } else {
                throw new IllegalArgumentException("無効なitemTypeが指定されました: " + detail.getItemType());
            }
            
            PurchaseOrderDetail savedDetail = purchaseOrderDetailRepository.save(orderDetail);
            System.out.println("トップレベル明細登録完了: ID=" + savedDetail.getId() + ", itemType=" + savedDetail.getItemType());
            
            totalAmount = totalAmount.add(detail.getQuantity().multiply(detail.getPurchasePrice()));
            
            // ネストされたサービスの処理
            if ("ITEM".equals(savedDetail.getItemType()) && hasNestedServices(detail)) {
                totalAmount = totalAmount.add(processNestedServices(header, detail, savedDetail));
            }
        }
        
        return totalAmount;
    }

    /**
     * 設備品の発注登録処理
     * @param orderDetail
     * @param detail
     */
    private void configureItemDetail(PurchaseOrderDetail orderDetail, PurchaseOrderRequest.Detail detail) {
        orderDetail.setItemCode(detail.getItemCode());
        orderDetail.setModelNumber(detail.getModelNumber());
        orderDetail.setManufacturer(detail.getManufacturer());
        orderDetail.setCategory(detail.getCategory());
        orderDetail.setReceivedQuantity(BigDecimal.ZERO);
        
        // サービス関連カラムはNULL
        orderDetail.setServiceType(null);
        orderDetail.setRelatedAsset(null);
        orderDetail.setLinkedId(null);
    }

    /**
     *  設備品に対するサービス(校正・修理)の発注登録処理
     * @param orderDetail
     * @param detail
     */
    private void configureServiceDetail(PurchaseOrderDetail orderDetail, PurchaseOrderRequest.Detail detail) {
        
        if (detail.getRelatedAssetId() != null) {
            AssetMaster existingAsset = assetMasterRepository.findById(detail.getRelatedAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("対象の既存設備が見つかりません ID: " + detail.getRelatedAssetId()));
            orderDetail.setRelatedAsset(existingAsset);
        } else {
            throw new ResourceNotFoundException("サービスは既存設備との連携が必須です");
        }
        orderDetail.setCategory(getServiceCategory(detail.getServiceType()));
        orderDetail.setServiceType(detail.getServiceType());
        orderDetail.setLinkedId(null);

        // 
        // existingAsset.setStatus("校正中"); // 削除
        // assetMasterRepository.save(existingAsset); // 削除
        
        // 物品関連カラムはNULL or 0
        orderDetail.setItemCode(null);
        orderDetail.setModelNumber(null);
        orderDetail.setReceivedQuantity(BigDecimal.ZERO);
    }

    /**
     * 発注サービスのカテゴリ名称をserviceTypeから取得
     * @param serviceType
     * @return カテゴリ名称
     */

    private String getServiceCategory(String serviceType) {
        return switch (serviceType.toUpperCase()) {
            case "CALIBRATION" -> "校正依頼";
            case "REPAIR" -> "修理依頼";
            default -> throw new ResourceNotFoundException("正常なサービス区分をリクエストしてください");
        };
    }

    /**
     *  発注明細オブジェクトがservicesオブジェクトを持っているか判定
     * @param detail
     * @return
     */
    private boolean hasNestedServices(PurchaseOrderRequest.Detail detail) {
        return detail.getServices() != null && !detail.getServices().isEmpty();
    }

    /**
     * ネストされたservicesオブジェクトを発注明細オブジェクト用に加工して登録
     * @param header
     * @param detail
     * @param parentDetail
     * @return
     */
    private BigDecimal processNestedServices(PurchaseOrder header, PurchaseOrderRequest.Detail detail, PurchaseOrderDetail parentDetail) {
        System.out.println("ネストされたサービス明細を処理中...");
        BigDecimal nestedServiceAmount = BigDecimal.ZERO;
        
        for (PurchaseOrderRequest.ServiceRequest serviceReq : detail.getServices()) {
            PurchaseOrderDetail serviceDetail = PurchaseOrderDetail.createNestedServiceDetail(header, serviceReq, parentDetail);
            
            serviceDetail.setCategory(getServiceCategory(serviceReq.getServiceType()));

            purchaseOrderDetailRepository.save(serviceDetail);
            System.out.println("  ▶ ネストされたサービス明細登録完了: ID=" + serviceDetail.getId());
            
            nestedServiceAmount = nestedServiceAmount.add(serviceReq.getQuantity().multiply(serviceReq.getPurchasePrice()));
        }
        
        return nestedServiceAmount;
    }

}