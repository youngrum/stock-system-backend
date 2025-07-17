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

    @Column(name = "asset_code", length = 255, unique = true, nullable = true)
    private String assetCode;

    @Column(name = "serial_number", length = 100, nullable = true)
    private String serialNumber;

    @Column(name = "item_name", length = 255, nullable = false)
    private String itemName;

    @Column(name = "model_number", length = 255, nullable = true)
    private String modelNumber = "-"; // 初期値を設定;

    @Column(name = "category", length = 100, nullable = false)
    private String category;

    @Column(name = "regist_date", nullable = true)
    private LocalDate registDate;

    @Column(name = "location", length = 100, nullable = true)
    private String location = "-"; // 初期値を設定

    @Column(name = "status", length = 50, nullable = false)
    private String status = "発注済み"; // 初期値を設定

    @Column(name = "last_calibration_date", nullable = true)
    private LocalDate lastCalibrationDate;

    @Column(name = "next_calibration_due_date", nullable = true)
    private LocalDate nextCalibrationDueDate;

    @Column(name = "monitored", nullable = false) // 監視対象フラグ 定期的な校正や点検通知のためのフラグ
    private Boolean monitored;

    @Column(name = "remarks", length = 255, nullable = true)
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