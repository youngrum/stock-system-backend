// src/main/java/com/example/backend/asset/service/AssetMasterRegistrationService.java
package com.example.backend.asset.service.handler;

import com.example.backend.order.dto.AssetReceiveFromOrderRequest;

import com.example.backend.asset.repository.AssetMasterRepository;
import com.example.backend.entity.AssetMaster;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.exception.ResourceNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AssetMasterRegistrationService {

    private final AssetMasterRepository assetMasterRepository;

    public AssetMasterRegistrationService(AssetMasterRepository assetMasterRepository) {
        this.assetMasterRepository = assetMasterRepository;
    }

    @Transactional
    public AssetMaster registerAsset(AssetReceiveFromOrderRequest.Item deliveredAsset,
                                   PurchaseOrderDetail detail, String supplier, BigDecimal totalReceived) {
                 
        // AssetMaster オブジェクトの生成とプロパティ設定
        AssetMaster newAsset = new AssetMaster();

        newAsset.setSupplier(supplier);    // 発注ヘッダーから仕入れ先を取得

        newAsset.setAssetName(detail.getItemName());          // 発注明細から名称を取得
        newAsset.setCategory(detail.getCategory());           // 発注明細からカテゴリーを取得
        newAsset.setManufacturer(detail.getManufacturer());   // 発注明細からメーカを取得
        newAsset.setModelNumber(detail.getModelNumber());     // 発注明細から型番を取得
        newAsset.setPurchasePrice(detail.getPurchasePrice()); // 発注明細の単価を取得

        newAsset.setCalibrationRequired(deliveredAsset.getCalibrationRequired()); // 校正要否をリクエストから取得
        newAsset.setQuantity(totalReceived);    // 数量を演算結果から取得
        newAsset.setStatus("納品済"); // ステータスを「納品済」に更新
        assetMasterRepository.save(newAsset);

        // 6. 発注明細テーブルの納品済み数量を更新
        detail.setReceivedQuantity(totalReceived);  // 数量を演算結果から取得

        // .compareTo() は 0 を返すと「等しい」、正なら「大きい」、負なら「小さい」。
        if (totalReceived.compareTo(detail.getQuantity()) >= 0) {
            detail.setStatus("完了");
        } else if (totalReceived.compareTo(BigDecimal.ZERO) > 0) {
            detail.setStatus("一部入庫");
        } else {
            detail.setStatus("未入庫");
        }

        return assetMasterRepository.save(newAsset);
    }
}