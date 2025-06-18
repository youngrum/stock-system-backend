package com.example.backend.csv.controller;

import com.example.backend.csv.service.CsvUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "CSVファイル処理API", description = "在庫情報のCSVファイルについてのアップロードとテンプレート情報の取得を提供します")
public class CsvUploadController {

    @Autowired
    private CsvUploadService csvUploadService;
    /**
     * 在庫情報CSVファイルのアップロードと処理
     * 
     * @param file アップロードされたCSVファイル
     * @return 処理結果のレスポンス
     */
    @Operation(summary = "在庫CSVファイルアップロード", 
               description = "在庫情報が記載されたCSVファイルをアップロードし、stock_masterテーブルに登録します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "アップロード成功"),
            @ApiResponse(responseCode = "400", description = "バリデーションエラー"),
            @ApiResponse(responseCode = "500", description = "サーバーエラー")
    })

    @PostMapping("/upload-csv")
    public ResponseEntity<Map<String, Object>> uploadCsv(@RequestParam("file") MultipartFile file) {

          Map<String, Object> response = new HashMap<>();
    
    // ファイルの基本チェック
    if (file.isEmpty()) {
        response.put("success", false);
        response.put("message", "ファイルが選択されていません。");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // ファイルタイプのチェック
    String contentType = file.getContentType();
    String filename = file.getOriginalFilename();
    
    // デバッグ用ログ出力（本番環境では削除）
    System.out.println("Content-Type: " + contentType);
    System.out.println("Filename: " + filename);
    
    // ファイル名での拡張子チェックを優先
    if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
        response.put("success", false);
        response.put("message", "CSVファイル（.csv拡張子）のみアップロード可能です。");
        response.put("uploadedContentType", contentType);
        response.put("uploadedFilename", filename);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    // Content-Typeのチェック
    if (contentType != null && !isAcceptableContentType(contentType)) {
        // Content-Typeが不正でも、拡張子が.csvなら警告のみ
        System.out.println("Warning: Unexpected content-type for CSV file: " + contentType);
    }

    try {
        // ファイル形式の実際の検証
        if (!isValidCsvFile(file)) {
            response.put("success", false);
            response.put("message", "アップロードされたファイルは有効なCSVファイルではありません。Excelで保存する際は「CSV（カンマ区切り）形式」を選択してください。");
            response.put("detectedFormat", "Excel形式またはバイナリファイル");
            response.put("expectedFormat", "テキスト形式のCSVファイル");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        // ヘッダー行の事前チェック
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            
            // BOM除去
            if (headerLine != null && headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }
            System.out.println("=== ヘッダー行詳細デバッグ ===");
            System.out.println("ヘッダー行: [" + headerLine + "]");
            System.out.println("ヘッダー長: " + (headerLine != null ? headerLine.length() : "null"));
            
            // 空白除去
            headerLine = headerLine != null ? headerLine.trim() : null;
    
            if (!csvUploadService.validateCsvFormat(headerLine)) {
                response.put("success", false);
                response.put("message", "CSVファイルのフォーマットが正しくありません。");
                response.put("expectedFormat", "item_name,model_number,category,manufacturer,suplier,current_stock,location,remarks");
                response.put("actualHeader", headerLine);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

        // CSV処理サービスを呼び出す
        List<String> errors = csvUploadService.uploadCsv(file.getInputStream());

        if (errors.isEmpty()) {
            response.put("success", true);
            response.put("message", "CSVファイルのアップロードと処理が完了しました。");
            response.put("filename", filename);
            response.put("fileSize", file.getSize());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            // 一部エラーがある場合
            response.put("success", false);
            response.put("message", "一部のデータでエラーが発生しました。");
            response.put("errors", errors);
            response.put("errorCount", errors.size());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

    } catch (Exception e) {
        e.printStackTrace();
        response.put("success", false);
        response.put("message", "CSVファイルの処理中にエラーが発生しました: " + e.getMessage());
        response.put("errorType", e.getClass().getSimpleName());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
 }
    /**
     * 在庫登録用CSVテンプレートファイルの情報を取得
     */
    @Operation(summary = "CSVテンプレートダウンロード", 
               description = "在庫登録用CSVテンプレートファイルの情報を返します")
    @GetMapping("/csv-template-info")
    public ResponseEntity<Map<String, Object>> getCsvTemplateInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("filename", "stock_master_template.csv");
        response.put("headers", new String[]{
            "item_name", "model_number", "category", "manufacturer", "suplier", "current_stock", "location", "remarks"
        });
        response.put("headerDescriptions", Map.of(
            "item_name", "商品名（必須）",
            "model_number", "型番（任意）",
            "category", "カテゴリ（必須）", 
            "manufacturer", "メーカー（任意、未設定の場合は'-'）",
            "suplier", "仕入れ先（任意、未設定の場合は'-'）",
            "current_stock", "現在庫数（任意、未設定の場合は0）",
            "location", "保管場所（任意、未設定の場合は'-'）",
            "remarks", "備考（任意、未設定の場合は'-'）"
        ));
        response.put("example", Map.of(
            "item_name", "テスト商品A",
            "model_number", "MOD-001", 
            "category", "電子機器",
            "manufacturer", "○○電機",
            "manufacturer", "○○商事",
            "current_stock", "100",
            "location", "倉庫A",
            "remarks", "特記事項なし"
        ));
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 受け入れ可能なContent-Typeかどうかを判定
     */
    private boolean isAcceptableContentType(String contentType) {
        return contentType.equals("text/csv") ||
              contentType.equals("application/csv") ||
              contentType.equals("text/plain") ||
              contentType.equals("application/vnd.ms-excel") ||  // Excel形式のCSV
              contentType.equals("text/comma-separated-values") ||
              contentType.startsWith("text/"); // text/*は許可
    }
    /**
 * ファイルが有効なCSV形式かどうかを検証
 */
private boolean isValidCsvFile(MultipartFile file) {
    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
        
        String firstLine = reader.readLine();
        if (firstLine == null) {
            return false; // 空ファイル
        }
        
        // バイナリデータの検出（Excel形式など）
        // XML宣言やExcel特有の文字列をチェック
        if (firstLine.contains("[Content_Types].xml") || 
            firstLine.contains("_rels/") ||
            firstLine.startsWith("PK") || // ZIP形式（xlsx）
            firstLine.contains("<?xml") ||
            firstLine.length() > 1000) { // 異常に長いヘッダー行
            return false;
        }
        
        // 制御文字の検出（printable文字以外）
        for (char c : firstLine.toCharArray()) {
            if (c < 32 && c != '\t' && c != '\r' && c != '\n') {
                return false; // バイナリデータの可能性
            }
        }
        
        return true;
        
    } catch (Exception e) {
        return false;
    }
}
}