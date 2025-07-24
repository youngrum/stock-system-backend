// src/main/java/com/example/yourapp/dto/AssetUpdateRequest.java
package com.example.backend.asset.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.AssertTrue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetUpdateRequest {

    // assetCodeも任意項目とするため、@NotBlankを削除
    private String assetCode;

    private String assetName;
    private String category;
    private String serialNumber;
    private String manufacturer;
    private String modelNumber;
    private String supplier;

    // 購入価格は0以上、桁数制限のバリデーションは維持
    @DecimalMin(value = "0.00", inclusive = true, message = "購入価格は0以上の値を入力してください。")
    @Digits(integer = 10, fraction = 2, message = "購入価格は整数部10桁、小数部2桁以内で入力してください。")
    private BigDecimal purchasePrice;

    private String location;
    private String fixedAssetManageNo;
    private String remarks;

    private Boolean monitored;
    private Boolean calibrationRequired;

    private LocalDate lastCalibrationDate;
    private LocalDate nextCalibrationDueDate;

    // ----- バリデーション用メソッド -----
    // 校正が必要な場合の日付の整合性チェックは維持
    @AssertTrue(message = "校正が必要な場合、最終校正日と次回校正予定日は必須であり、次回校正予定日は最終校正日より後の日付である必要があります。")
    public boolean isCalibrationDatesValid() {
        boolean isRequired = Boolean.TRUE.equals(this.calibrationRequired);

        if (isRequired) {
            // 校正が必要な場合、両方のDateFieldがnullでないことを確認
            if (this.lastCalibrationDate == null || this.nextCalibrationDueDate == null) {
                return false; // どちらかの日付がnullなら無効
            }
            // 次回校正予定日が最終校正日より後の日付であることを確認
            return this.nextCalibrationDueDate.isAfter(this.lastCalibrationDate);
        }
        // calibrationRequired が false または null の場合、日付は任意なので常に有効
        return true;
    }
}