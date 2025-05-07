package com.example.backend.repository;

import com.example.backend.entity.StockMaster;

import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMasterRepository extends JpaRepository<StockMaster, String> {

    // Spring Data JPAの継承で対応するクエリが以下を生成・実装
    // SELECT * FROM stock_master
    // WHERE LOWER(item_name) LIKE LOWER('%keyword%')
    // AND LOWER(category) LIKE LOWER('%category%')

    Page<StockMaster> findByItemNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(
            String itemName, String category, Pageable pageable);
}
