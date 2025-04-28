package com.example.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
public class StockMaster {
    @Id
    private String itemCode;     // 部材コード
    private String itemName;     // 部材名
    private String category;     // カテゴリ
    private Integer currentStock; // 現在庫
    private Instant lastUpdated; // 最終更新日時
}
