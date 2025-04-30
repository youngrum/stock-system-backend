package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
public class StockMaster {
    @Id
    private String itemCode; // 商品コード
    private String itemName; // 商品名
    private String modelNumber; // 型番
    private String manufacturer; // メーカー
    private String supplier; // 仕入先
    private String category; // カテゴリ
    private Integer currentStock; // 現在庫数
    private String remarks; // 備考
    @Column(updatable = false)
    private Instant createdAt; // 登録日時（作成時のみ）
    private Instant updatedAt; // テーブル更新日時（作成・更新時）
    private Instant stockUpdatedAt; // 在庫更新日時
}
