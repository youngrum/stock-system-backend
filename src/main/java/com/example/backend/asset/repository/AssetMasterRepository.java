package com.example.backend.asset.repository;

import com.example.backend.entity.AssetMaster; // AssetMasterエンティティのパスを適切にインポート

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetMasterRepository extends JpaRepository<AssetMaster, Long> {


    /**
     * 
     * @return assetCode=nullの値を最初に表示する
     */
    @Query("SELECT am FROM AssetMaster am ORDER BY CASE WHEN am.assetCode IS NULL THEN 0 ELSE 1 END, am.assetCode ASC")
    Page<AssetMaster> findAllOrderByAssetCodeNullFirst(Pageable pageable);
    
    /**
     * 指定された設備管理番号（AssetCode）で設備を検索
     * @param assetCode 検索する設備管理番号
     * @return 該当するAssetMaster（存在しない場合はOptional.empty()）
     */
    Page<AssetMaster> findByAssetCodeContaining(String assetCode, Pageable pageable);

    /**
     * 指定された型番・設備名・カテゴリーで検索
     * @param modelNumber
     * @param assetName
     * @param category 
     * @return 該当するAssetMaster（存在しない場合はOptional.empty()）
     */
    Page<AssetMaster> findByAssetNameContainingAndCategoryContainingAndModelNumberContaining(String assetName, String category, String modelNumber, Pageable pageable);

    Optional<AssetMaster> findByAssetCode(String newAssetCode);
}