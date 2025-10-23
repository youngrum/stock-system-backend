// src/main/java/com/example/yourapp/dto/AssetUpdateRequest.java
package com.example.backend.asset.dto;

import jakarta.validation.constraints.AssertTrue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetUpdateRequest {
    private String assetCode;
    private String serialNumber;
    private String location;
    private String fixedAssetManageNo;
    private LocalDate registDate;
    private String remarks;
    private String status;
    private Boolean monitored;
    private Boolean calibrationRequired;
    private LocalDate lastCalibrationDate;
    private LocalDate nextCalibrationDate;

    // ----- バリデーション用メソッド -----
    // 校正が必要な場合の日付の整合性チェックは維持
    @AssertTrue(message = "校正が必要な場合、最終校正日と次回校正予定日は必須であり、次回校正予定日は最終校正日より後の日付である必要があります。")
    public boolean isCalibrationDatesValid() {
        boolean isRequired = Boolean.TRUE.equals(this.calibrationRequired);

        if (isRequired) {
            // 校正が必要な場合、両方のDateFieldがnullでないことを確認
            if (this.lastCalibrationDate == null || this.nextCalibrationDate == null) {
                return false; // どちらかの日付がnullなら無効
            }
            // 次回校正予定日が最終校正日より後の日付であることを確認
            return this.nextCalibrationDate.isAfter(this.lastCalibrationDate);
        }
        // calibrationRequired が false または null の場合、日付は任意なので常に有効
        return true;
    }
}