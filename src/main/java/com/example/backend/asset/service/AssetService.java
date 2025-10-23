package com.example.backend.asset.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.asset.dto.AssetMasterRequest;
import com.example.backend.asset.dto.AssetUpdateRequest;
import com.example.backend.asset.repository.AssetMasterRepository;
import com.example.backend.entity.AssetMaster;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.order.dto.AssetReceiveFromOrderRequest;
import com.example.backend.order.repository.PurchaseOrderRepository;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;
import com.example.backend.exception.DuplicateAssetCodeException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

@Service
public class AssetService {
    private final AssetMasterRepository assetMasterRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;

    @Autowired
    public AssetService(AssetMasterRepository assetMasterRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderDetailRepository purchaseOrderDetailRepository) {
        this.assetMasterRepository = assetMasterRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderDetailRepository = purchaseOrderDetailRepository;
    }

    /**
     * パラメーター付在庫検索
     * 
     * @param itemCode
     * @param itemName
     * @param category
     * @param modelNumber
     * @param pageable
     * @return
     */
    public Page<AssetMaster> searchAssetTeble(String assetCode, String assetName, String category, String modelNumber,
            Pageable pageable) {

        // 空の場合は空文字に変換（部分一致検索に対応）
        String assetCodeKeyword = (assetCode != null) ? assetCode : "";
        String assetNameKeyword = (assetName != null) ? assetName : "";
        String categoryKeyword = (category != null) ? category : "";
        String modelNumberKeyword = (modelNumber != null) ? modelNumber : "";

        System.out.printf(
                "🔍 検索条件: assetCodeKeyword='%s', assetNameKeyword='%s', categoryKeyword='%s', modelNumberKeyword='%s'%n",
                assetCodeKeyword, assetNameKeyword, categoryKeyword, modelNumberKeyword);

        if (isBlank(assetCode) && isBlank(assetName) && isBlank(modelNumber) && isBlank(category)) {
            return assetMasterRepository.findAllOrderByAssetCodeNullFirst(pageable);
        }

        if (!isBlank(assetCodeKeyword)) {
            System.out.printf("!isBlank(assetCode)");
            // assetCode は一意なので他の条件を無視してよい
            return assetMasterRepository.findByAssetCodeContaining(assetCode, pageable);
        }
        // assetCode が空の場合、他の条件で検索
        return assetMasterRepository
                .findByAssetNameContainingAndCategoryContainingAndModelNumberContaining(
                        assetNameKeyword, categoryKeyword, modelNumberKeyword, pageable);
    }

    @Transactional
    public AssetMaster createAssetTable(AssetMasterRequest req) {
        System.out.println("Creating Asset with request: " + req);
        // エンティティにインスタンス生成とカラム情報追加を指示
        AssetMaster asset = AssetMaster.createFromManualForm(req);
        // データベースに保存
        return assetMasterRepository.save(asset);
    }

    @Transactional
    public AssetMaster updateAssetTable(Long id, AssetUpdateRequest updateRequest) {
        // 既存の設備品をIDに基づいて検索
        AssetMaster existingAsset = assetMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("不正なidへのリクエストです")); // 例外は適宜定義

        // DTOのassetCodeがnullでなく、かつ既存のassetCodeと異なる場合にチェック
        if (updateRequest.getAssetCode() != null
                && !existingAsset.getAssetCode().equals(updateRequest.getAssetCode())) {
            if (assetMasterRepository.findByAssetCode(updateRequest.getAssetCode()).isPresent()) {
                throw new DuplicateAssetCodeException("指定された設備コードは既に存在します: " + updateRequest.getAssetCode());
            }
        }

        // エンティティの更新メソッドを呼び出す
        existingAsset.updateFromManualForm(updateRequest);

        // 更新を保存
        return assetMasterRepository.save(existingAsset);
    }

    /**
     * 発注登録から設備品の受領を処理する
     * 
     * @param req 発注からの設備品受領リクエスト
     * @return 受領した設備品のリスト
     */
    @Transactional
    // 引数をAssetReceiveFromOrderRequestに変更
    public List<AssetMaster> receiveFromOrder(AssetReceiveFromOrderRequest req) {
        // 1. 発注書をorderNoで検索 (purchaseOrderIdではなくorderNoを使用)
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByOrderNo(req.getOrderNo())
                .orElseThrow(() -> new ResourceNotFoundException("発注書が見つかりません: No. " + req.getOrderNo()));

        // 発注区分が設備品向けであることを確認（ケースAの前提に基づくバリデーション）
        if (!"ASSET".equals(purchaseOrder.getOrderType())) {
            throw new IllegalArgumentException("この発注書は設備品向けではありません。");
        }

        List<AssetMaster> createdAssets = new ArrayList<>();

        // 納品される各品目（AssetItem）を処理
        for (AssetReceiveFromOrderRequest.Item deliveredAsset : req.getItems()) {
            // 2. 発注明細をitemNameとpurchaseOrderで検索
            PurchaseOrderDetail itemToDeliver = purchaseOrderDetailRepository
                    .findByPurchaseOrderAndItemName(purchaseOrder, deliveredAsset.getItemName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            String.format("発注番号 '%s' の品目コード '%s' に対応する明細が見つかりません。",
                                    req.getOrderNo(), deliveredAsset.getItemName())));

            // 3. 納品数量の検証 (Assetの場合、通常は1)
            BigDecimal currentReceivedQuantity = itemToDeliver.getReceivedQuantity();
            BigDecimal orderedQuantity = itemToDeliver.getQuantity();
            // AssetItemのreceivedQuantityはBigDecimalなのでintに変換または比較方法を調整

            BigDecimal quantityInThisDelivery = deliveredAsset.getReceivedQuantity();
            if (currentReceivedQuantity.add(quantityInThisDelivery).compareTo(orderedQuantity) > 0) {
                throw new IllegalArgumentException(
                        String.format("納品数量が発注数量を超過しています。品名: %s, 発注数: %d, 既に納品済み: %d, 今回納品: %d",
                                deliveredAsset.getItemName(), orderedQuantity, currentReceivedQuantity,
                                quantityInThisDelivery));
            }

            // 4. AssetMaster オブジェクトの生成とプロパティ設定
            AssetMaster newAsset = new AssetMaster();
            newAsset.setSerialNumber(deliveredAsset.getSerialNumber());
            newAsset.setAssetName(itemToDeliver.getItemName()); // 発注明細から名称をコピー
            newAsset.setCategory(itemToDeliver.getCategory());
            newAsset.setManufacturer(itemToDeliver.getManufacturer());
            newAsset.setModelNumber(itemToDeliver.getModelNumber());
            newAsset.setSupplier(purchaseOrder.getSupplier());
            newAsset.setPurchasePrice(itemToDeliver.getPurchasePrice()); // 発注明細の単価を使用
            newAsset.setRegistDate(itemToDeliver.getPurchaseOrder().getOrderDate()); // 発注日を登録日とするか、reqから納品日を取得
            newAsset.setStatus("納品済");
            newAsset.setCreatedAt(LocalDateTime.now());
            newAsset.setLastUpdated(LocalDateTime.now());
            assetMasterRepository.save(newAsset);

            // 6. 発注明細の納品済み数量を更新
            itemToDeliver.setQuantity(currentReceivedQuantity.add(quantityInThisDelivery));
            purchaseOrderDetailRepository.save(itemToDeliver);
        }

        // 7. 発注書全体のステータスを更新 (全ての明細が納品完了したかチェック)
        purchaseOrder.getDetails().forEach(item -> purchaseOrderDetailRepository.save(item)); // 念のため、関連する全てのアイテムを保存

        boolean allItemsDelivered = purchaseOrder.getDetails().stream()
                .allMatch(item -> item.getQuantity().compareTo(item.getReceivedQuantity()) <= 0);

        if (allItemsDelivered) {
            purchaseOrder.setStatus("完了");
        } else {
            purchaseOrder.setStatus("一部入庫");
        }
        purchaseOrderRepository.save(purchaseOrder);

        return createdAssets;
    }

    /**
     * 文字列が空またはnullかどうかを判定
     * 
     * @param value
     * @return
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
