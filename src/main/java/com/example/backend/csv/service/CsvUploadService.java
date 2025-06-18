package com.example.backend.csv.service;

import com.example.backend.entity.InventoryTransaction;
import com.example.backend.entity.StockMaster;
import com.example.backend.inventory.repository.StockMasterRepository;
import com.example.backend.inventory.repository.InventoryTransactionRepository;
import com.example.backend.common.service.ItemCodeGenerator;
import com.example.backend.common.service.TransactionIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays; 

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvUploadService {

    @Autowired
    private StockMasterRepository stockMasterRepository;
    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;
    @Autowired
    private ItemCodeGenerator itemCodeGenerator;
    @Autowired
    private TransactionIdGenerator transactionIdGenerator;

    /**
     * CSVアップロード処理 - InputStreamを一度だけ読む版
     */
    public List<String> uploadCsv(InputStream inputStream) throws Exception {
        List<String> errors = new ArrayList<>();
        List<CsvRowData> validatedRows = new ArrayList<>();
        int lineNumber = 0;

        // 第1段階：全データを読み込み・検証
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            
            String headerLine = reader.readLine();
            if (headerLine == null) {
                errors.add("CSVファイルが空です。");
                return errors;
            }
            
            System.out.println("ヘッダー行: " + headerLine);

            // 全行を読み込み、検証とデータ保持
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] data = parseCsvLine(line);
                    System.out.println("検証中 - data: " + Arrays.toString(data));

                    // データのバリデーション
                    StockMaster.validateCsvData(data);
                    
                    // 検証済みデータを保存
                    validatedRows.add(new CsvRowData(lineNumber, data));
                    System.out.println("検証OK - 行 " + lineNumber);
                    
                } catch (IllegalArgumentException e) {
                    errors.add(lineNumber + "行目: " + e.getMessage());
                    System.out.println("検証エラー - 行 " + lineNumber + ": " + e.getMessage());
                } catch (Exception e) {
                    errors.add(lineNumber + "行目: 処理中に予期せぬエラーが発生しました: " + e.getMessage());
                    System.out.println("検証例外 - 行 " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        System.out.println("事前検証完了: 総行数=" + lineNumber + ", エラー数=" + errors.size() + ", 有効行数=" + validatedRows.size());

        // エラーがある場合は検証結果のみ返す
        if (!errors.isEmpty()) {
            return errors;
        }

        // 第2段階：エラーがない場合のみDB処理
        return processValidatedData(validatedRows);
    }

    /**
     * 検証済みデータのDB処理（トランザクション内）
     */
    @Transactional
    private List<String> processValidatedData(List<CsvRowData> validatedRows) throws Exception {
        List<String> errors = new ArrayList<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            for (CsvRowData rowData : validatedRows) {
                System.out.println("DB処理中 - 行 " + rowData.lineNumber + ": " + Arrays.toString(rowData.data));

                // StockMasterエンティティを作成・保存
                StockMaster stockMaster = StockMaster.createStockFromCsv(
                    rowData.data, 
                    stockMasterRepository, 
                    itemCodeGenerator
                );
                
                System.out.println("DB登録完了 - 行 " + rowData.lineNumber + ": " + 
                    stockMaster.getItemName() + " (商品コード: " + stockMaster.getItemCode() + ")");

                // トランザクション履歴登録
                InventoryTransaction tx = InventoryTransaction.createTransactionForCsv(
                    rowData.data, stockMaster, username, transactionIdGenerator, inventoryTransactionRepository
                );
                System.out.println(" - トランザクション登録完了: " + tx.getTransactionId());
            }

            System.out.println("CSV処理完了: " + validatedRows.size() + "件の商品を登録しました。");
            
        } catch (Exception e) {
            System.out.println("DB処理中にエラーが発生、トランザクションロールバック: " + e.getMessage());
            throw e; // トランザクションロールバック
        }

        return errors; // 正常時は空のリスト
    }

    /**
     * CSVの行データを保持するクラス
     */
    private static class CsvRowData {
        public final int lineNumber;
        public final String[] data;
        
        public CsvRowData(int lineNumber, String[] data) {
            this.lineNumber = lineNumber;
            this.data = data;
        }
    }

    /**
     * CSV行をパースする（カンマ区切り、ダブルクォート対応）
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                // ダブルクォートの処理
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // エスケープされたダブルクォート
                    field.append('"');
                    i++; // 次の文字をスキップ
                } else {
                    // クォートの開始/終了
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // フィールドの区切り
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        
        // 最後のフィールドを追加
        fields.add(field.toString());
        
        return fields.toArray(new String[0]);
    }

    /**
     * CSVフォーマットのバリデーション（ヘッダー行チェック）
     */
    public boolean validateCsvFormat(String headerLine) {
        if (headerLine == null || headerLine.trim().isEmpty()) {
            return false;
        }
        
        String[] headers = parseCsvLine(headerLine);
        
        // 期待されるヘッダー（順序は重要）
        String[] expectedHeaders = {
            "item_name", "model_number", "category", "manufacturer", "suplier", "current_stock", "location", "remarks"
        };
        
        if (headers.length < expectedHeaders.length) {
            return false;
        }
        
        // ヘッダーの内容をチェック（大文字小文字を無視、空白を除去）
        for (int i = 0; i < expectedHeaders.length; i++) {
            if (!headers[i].trim().toLowerCase().equals(expectedHeaders[i].toLowerCase())) {
                System.out.println("ヘッダー不一致: 期待値=" + expectedHeaders[i] + ", 実際=" + headers[i]);
                return false;
            }
        }
        
        return true;
    }
}