// AssetMaster.java
package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_master") 
@Data
@NoArgsConstructor
@AllArgsConstructor 
public class AssetMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id")
    private Long id;

    @Column(name = "asset_code", length = 255, unique = true, nullable = true) //設備ID 仮登録後手動入力
    private String assetCode;

    @Column(name = "asset_name", length = 255, nullable = false) // 設備名
    private String assetName;

    @Column(name = "manufacturer", length = 64) // メーカー
    private String manufacturer = "-";

    @Column(name = "model_number", length = 255, nullable = true) // 型番
    private String modelNumber = "-"; // 初期値を設定;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "supplier", length = 64, nullable = false)
    private String supplier = "-";

    @Column(name = "serial_number", length = 100, nullable = true) // 製造番号 仮登録後手動入力
    private String serialNumber;

    @Column(name = "regist_date", nullable = true) //設備登録日 仮登録後手動入力
    private LocalDate registDate;

    @Column(name = "purchase_price", precision = 14, scale = 2, nullable = false) // 購入金額
    private BigDecimal purchasePraie = BigDecimal.ZERO;
    
    @Column(name = "status", length = 50, nullable = false) // ステータス 自動入力
    private String status = "発注済"; // 初期値を設定

    @Column(name = "location", length = 100, nullable = true) // 使用/保管場所 仮登録後手動入力
    private String location = "-"; // 初期値を設定

    @Column(name = "last_calibration_date", nullable = true) // 前回校正日 仮登録後手動入力
    private LocalDate lastCalibrationDate;

    @Column(name = "next_calibration_due_date", nullable = true) // 次回校正期限 仮登録後手動入力
    private LocalDate nextCalibrationDueDate;

    @Column(name = "fixed_asset_manage_no", length = 255, nullable = true) // 固定資産管理番号
    private String fixedAssetManageNo = "-"; // 初期値を設定;

    @Column(name = "monitored", nullable = false) // 監視対象フラグ 定期的な校正や点検通知のためのフラグ
    private Boolean monitored;

    @Column(name = "calibration_required", nullable = false)
    private Boolean calibrationRequired; // 校正の要否判定フラグ trueかつmonitored=trueで通知が走る 校正いらないならfalse

    @Column(name = "remarks", length = 255, nullable = true) // 備考 購入目的・所有者などを記載
    private String remarks; 

    @Column(name = "created_at", nullable = false, updatable = false) // updatable = false で更新時に値を上書きしない
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ORDERED";
        }
        if (this.monitored == null) {
            this.monitored = true;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}