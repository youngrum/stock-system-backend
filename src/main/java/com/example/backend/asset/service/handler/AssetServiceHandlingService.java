// src/main/java/com/example/backend/asset/service/AssetServiceHandlingService.java
package com.example.backend.asset.service.handler;

import com.example.backend.order.dto.AssetReceiveFromOrderRequest;
import com.example.backend.entity.PurchaseOrderDetail;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetServiceHandlingService {

    // ... 必要に応じてRepository (例: サービス履歴を管理するRepositoryなど)

    @Transactional
    public void handleServiceReceipt(AssetReceiveFromOrderRequest.Item deliveredAsset,
                                   PurchaseOrderDetail detail, BigDecimal totalReceived) {
        // ここで、サービス受領に関するバリデーションとロジックをすべて行う
        // 資産台帳には登録せず、発注明細のステータス更新、サービス履歴レコードの作成、
        System.out.println("サービス品目を受領: " + detail.getItemName());

        detail.setReceivedQuantity(totalReceived);  // 数量を演算結果から取得 基本 0 or 1
        if (totalReceived.compareTo(detail.getQuantity()) >= 0) {
            detail.setStatus("完了");
        } else if (totalReceived.compareTo(BigDecimal.ZERO) > 0) {
            detail.setStatus("一部入庫");
        } else {
            detail.setStatus("未入庫");
        }
        
    }
}