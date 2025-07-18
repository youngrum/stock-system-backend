package com.example.backend.asset.repository;

import com.example.backend.entity.AssetMaster; // AssetMasterエンティティのパスを適切にインポート
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetMasterRepository extends JpaRepository<AssetMaster, Long> {

    /**
     * 指定された資産コード（AssetCode）で資産を検索
     * @param assetCode 検索する資産コード
     * @return 該当するAssetMaster（存在しない場合はOptional.empty()）
     */
    Optional<AssetMaster> findByAssetCode(String assetCode);

    /**
     * 指定された型番と資産名称で資産を検索します。
     * @param modelNumber 検索する型番
     * @param assetName 検索する資産名称
     * @return 該当するAssetMaster（存在しない場合はOptional.empty()）
     */
    Optional<AssetMaster> findByModelNumberAndAssetName(String modelNumber, String assetName);
}