package com.example.backend.asset.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetMasterRequest {
    @NotBlank(message = "管理番号は必須です")
    private String assetCode;              // 管理番号 ← 必須 確定したものとして登録する
    @NotBlank(message = "設備名は必須です")
    private String assetName;              // 設備名
    @NotBlank(message = "カテゴリーは必須です")
    private String category;               // カテゴリー
    @NotBlank(message = "仕入れ先は必須です 不明なら - として登録してください")
    private String supplier;               // 仕入先

    private String serialNumber;           // 製造番号
    private String manufacturer;           // メーカー
    private String modelNumber;            // 型番

    @NotNull(message = "購入価格は必須です 不明なら 0 として登録してください")
    @DecimalMin(value = "0.00", inclusive = true, message = "購入価格は0以上の値を入力してください。") // 0.00 以上であることをチェック (0 も含む)
    @Digits(integer = 10, fraction = 2, message = "購入価格は整数部10桁、小数部2桁以内で入力してください。")
    private BigDecimal purchasePrice;      // 購入金額

    private String location;               // 保管/設置場所
    private String fixedAssetManageNo;     // 固定資産管理番号
    private String remarks;                // 備考
    private Boolean monitored;             // 監視フラグ
    private Boolean calibrationRequired;   // 校正要否フラグ
    private LocalDate lastCalibrationDate; // 前回校正日 calibrationRequired=TUREで必須
    private LocalDate nextCalibrationDate; // 次回校正期限 calibrationRequired=TUREで必須

    // バリデーション用メソッド
    @AssertTrue(message = "校正が必要な場合、最終校正日と次回校正予定日は必須であり、次回校正予定日は最終校正日より後の日付である必要があります。")
    public boolean isCalibrationDatesValid() {
        // calibrationRequired が null の場合、false と同じ扱いとする
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
