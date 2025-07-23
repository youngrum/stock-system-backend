package com.example.backend.asset.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.asset.dto.AssetMasterRequest;
import com.example.backend.asset.repository.AssetMasterRepository;
import com.example.backend.entity.AssetMaster;
import com.example.backend.entity.InventoryTransaction;

import jakarta.transaction.Transactional;

@Service
public class AssetService {
    private final AssetMasterRepository assetMasterRepository;

    @Autowired
    public AssetService(AssetMasterRepository assetMasterRepository) {
        this.assetMasterRepository = assetMasterRepository;
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
    public Page<AssetMaster> searchAsset(String assetCode, String assetName, String category, String modelNumber,
            Pageable pageable) {

        // 空の場合は空文字に変換（部分一致検索に対応）
        String assetCodeKeyword = (assetCode != null) ? assetCode : "";
        String assetNameKeyword = (assetName != null) ? assetName : "";
        String categoryKeyword = (category != null) ? category : "";
        String modelNumberKeyword = (modelNumber != null) ? modelNumber : "";

        System.out.printf(
                "🔍 検索条件: assetCodeKeyword='%s', assetNameKeyword='%s', categoryKeyword='%s', modelNumberKeyword='%s'%n",
                assetCodeKeyword, assetNameKeyword, categoryKeyword, modelNumberKeyword);

        if(isBlank(assetCode) && isBlank(assetName) && isBlank(modelNumber) && isBlank(category)){
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
    public AssetMaster createAsset(AssetMasterRequest req) {
        // デバッグログは本番コードでは通常System.out.printlnではなくロガーを使用します
        // System.out.println("Creating Asset with request: " + req);

        // 1. DTOからエンティティへの変換と初期値の設定
        AssetMaster asset = new AssetMaster();

        // 必須項目（リクエストDTOのバリデーションが通っていることを前提）
        asset.setAssetCode(req.getAssetCode());
        asset.setAssetName(req.getAssetName());
        asset.setCategory(req.getCategory()); // 追加

        // 任意項目
        asset.setSerialNumber(req.getSerialNumber());
        asset.setManufacturer(req.getManufacturer());
        asset.setModelNumber(req.getModelNumber());
        asset.setSupplier(req.getSupplier());
        asset.setPurchasePrice(req.getPurchasePrice());
        asset.setLocation(req.getLocation());
        asset.setFixedAssetManageNo(req.getFixedAssetManageNo());
        asset.setRemarks(req.getRemarks());

        // boolean型フラグのデフォルト値設定 (リクエストでnullの場合にfalseを設定)
        // Booleanオブジェクトのequalsメソッドを使ってnullチェックを安全に行う
        asset.setMonitored(Boolean.TRUE.equals(req.getMonitored())); // req.getMonitored()がnullならfalse
        asset.setCalibrationRequired(Boolean.TRUE.equals(req.getCalibrationRequired())); // req.getCalibrationRequired()がnullならfalse

        // 校正関連日付
        asset.setLastCalibrationDate(req.getLastCalibrationDate());
        asset.setNextCalibrationDueDate(req.getNextCalibrationDate());

        // システムが自動設定する項目
        asset.setRegistDate(LocalDate.now()); // 登録日を現在日付で自動設定
        asset.setStatus("登録済"); // 運用ステータスを「登録済」に設定 (定数クラスを使用することを強く推奨します: 例 "AssetStatus.REGISTERED.getValue()")

        // createdAt, updatedAt は @PrePersist/@PreUpdate アノテーションにより自動設定されます。
        // id は @GeneratedValue アノテーションにより自動生成されます。

        // 2. データベースに保存
        return assetMasterRepository.save(asset);
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
