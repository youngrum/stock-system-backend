package com.example.backend.repository;

import com.example.backend.entity.StockMaster;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockMasterRepository extends JpaRepository<StockMaster, String> {

    // Spring Data JPAの継承で対応するクエリが以下を生成・実装
    // SELECT * FROM stock_master
    // WHERE LOWER(itemName) LIKE LOWER('%itemName%')
    // AND LOWER(modelNumber) LIKE LOWER('%modelNumber%')
    // AND LOWER(category) LIKE LOWER('%category%')

    Page<StockMaster> findByItemNameContainingIgnoreCaseAndModelNumberContainingIgnoreCaseAndCategoryContainingIgnoreCase(
            String itemName, String modelNumber, String category, Pageable pageable);

    // 型番と品名の完全一致で検索（発注登録用）
    Optional<StockMaster> findByModelNumberAndItemName(String modelNumber, String itemName);
}
