package com.example.backend.csv.repository;

import com.example.backend.entity.StockMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CsvUploadRepository extends JpaRepository<StockMaster, Long> {
    // 必要に応じて追加のクエリメソッドを定義
    boolean existsByItemName(String itemName);
    boolean existsByModelNumber(String modelNumber);
}