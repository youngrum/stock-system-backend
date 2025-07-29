package com.example.backend.asset.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.asset.dto.AssetMasterRequest;
import com.example.backend.asset.dto.AssetUpdateRequest;
import com.example.backend.asset.repository.AssetMasterRepository;
import com.example.backend.asset.service.handler.AssetMasterRegistrationService;
import com.example.backend.asset.service.handler.AssetServiceHandlingService;
import com.example.backend.entity.AssetMaster;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrder.OrderType;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.order.dto.AssetReceiveFromOrderRequest;
import com.example.backend.order.repository.PurchaseOrderRepository;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;
import com.example.backend.exception.DuplicateAssetCodeException;
import com.example.backend.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;

@Service
public class AssetService {

    private final AssetMasterRegistrationService assetMasterRegistrationService;
    private final AssetMasterRepository assetMasterRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final AssetServiceHandlingService assetServiceHandlingService;

    @Autowired
    public AssetService(AssetMasterRepository assetMasterRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderDetailRepository purchaseOrderDetailRepository,
            AssetMasterRegistrationService assetMasterRegistrationService,
            AssetServiceHandlingService assetServiceHandlingService) {
        this.assetMasterRepository = assetMasterRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderDetailRepository = purchaseOrderDetailRepository;
        this.assetMasterRegistrationService = assetMasterRegistrationService;
        this.assetServiceHandlingService = assetServiceHandlingService;
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
        if (StringUtils.hasText(updateRequest.getAssetCode())
                && !Objects.equals(existingAsset.getAssetCode(), updateRequest.getAssetCode())) {
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

        // 発注区分が設備品向け(order_type="ASSET")であることを確認（ケースAの前提に基づくバリデーション）
         if (purchaseOrder.getOrderType() != OrderType.ASSET) {
            throw new IllegalArgumentException("この発注書は設備品向けではありません。");
        }

        // 複数明細同時処理用 ※未実装
        List<AssetMaster> createdAssets = new ArrayList<>();
        
        String supplier  = purchaseOrder.getSupplier();

        // 納品される各品目（AssetItem）を処理
        for (AssetReceiveFromOrderRequest.Item deliveredAsset : req.getItems()) {

            // 発注明細をitemNameとpurchaseOrderで検索
            // detail = 発注明細レコード
            PurchaseOrderDetail detail = purchaseOrderDetailRepository
                    .findById(deliveredAsset.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                           String.format("指定された発注明細 (ID: %d) が見つかりません。", deliveredAsset.getId())));
            String itemType = detail.getItemType();
            
            // 納品数量の検証 (Assetの場合、通常は1)
            // currentTotalReceived = データベースに登録されている明細の累計受領数量
            BigDecimal currentTotalReceived = detail.getReceivedQuantity();
            // orderQuantity = 発注登録された購入数
            BigDecimal orderQuantity = detail.getQuantity();
            // receivingNow = 本リクエストで入力された数量 (deliveredAsset から取得)
            BigDecimal receivingNow = deliveredAsset.getQuantity();

            // すでに全数が完了しているかのチェック
            // (累計受領数量 が 発注数量以上の場合)
            if (currentTotalReceived.compareTo(orderQuantity) >= 0) {
                throw new ValidationException("すでに全数が入庫済みのため、これ以上受け入れできません(品名: " + detail.getItemName() + "）");
            }

            // 今回の受領で発注数を超過しないかのチェック
            // (累計受領数量 + 今回の受領数量 が 発注数量を超過する場合)
            if (currentTotalReceived.add(receivingNow).compareTo(orderQuantity) > 0) {
                throw new ValidationException("受け入れ数が発注数を超えています（itemCode: " + detail.getItemName() + "）");
            }

            // 入庫数チェック
            BigDecimal totalReceived = detail.getReceivedQuantity().add(deliveredAsset.getQuantity());

            if (totalReceived.compareTo(detail.getQuantity()) > 0) {
                throw new IllegalArgumentException("受領数が発注数を超えています: " + detail.getItemName());
            }

            // 物品とサービス(修理・校正)で処理を分岐
            if ("ITEM".equals(itemType)) {
                // 物理的な設備品の登録処理
                // バリデーションもassetMasterRegistrationService内で行うか、ここで一部行う
                AssetMaster newAsset = assetMasterRegistrationService.registerAsset(deliveredAsset, detail, supplier, totalReceived);
                createdAssets.add(newAsset);

            } else if ("SERVICE".equals(itemType)) {
                // サービス（修理・校正など）の受領処理
                assetServiceHandlingService.handleServiceReceipt(deliveredAsset, detail, totalReceived);
            } else {
                throw new IllegalArgumentException("不明な品目タイプです: " + itemType);
            }

            purchaseOrderDetailRepository.save(detail);
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
