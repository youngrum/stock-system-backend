package com.example.backend.asset.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AssetMasterRequest {
    private String assetCode;              // 管理番号 ← 必須 確定したものとして登録する
    private String assetName;              // 設備名
    private String category;               // カテゴリー
    private String serialNumber;           // 製造番号
    private String manufacturer;           // メーカー
    private String modelNumber;            // 型番
    private String supplier;               // 仕入先
    private BigDecimal purchasePrice;      // 購入金額
    private String location;               // 保管/設置場所
    private String fixedAssetManageNo;     // 固定資産管理番号
    private String remarks;                // 備考
    private Boolean monitored;             // 監視フラグ
    private Boolean calibrationRequired;   // 校正要否フラグ
    private LocalDate lastCalibrationDate; // 前回校正日 calibrationRequired=TUREで必須
    private LocalDate nextCalibrationDate; // 次回校正期限 calibrationRequired=TUREで必須

}
