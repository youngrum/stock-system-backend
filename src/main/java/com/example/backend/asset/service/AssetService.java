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
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.exception.DuplicateAssetCodeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional; 

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
        if (updateRequest.getAssetCode() != null && !existingAsset.getAssetCode().equals(updateRequest.getAssetCode())) {
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
     * 文字列が空またはnullかどうかを判定
     * 
     * @param value
     * @return
     */
    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
