package com.example.backend.csv.service;

import com.example.backend.entity.StockMaster;
import com.example.backend.inventory.repository.StockMasterRepository;
import com.example.backend.common.service.ItemCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private ItemCodeGenerator itemCodeGenerator;

    @Transactional
    public List<String> importCsv(InputStream inputStream) throws Exception {
        List<String> errors = new ArrayList<>();
        List<StockMaster> successfulInserts = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            
            // ヘッダー行をスキップ
            String headerLine = reader.readLine();
            if (headerLine == null) {
                errors.add("CSVファイルが空です。");
                return errors;
            }
            
            System.out.println("ヘッダー行: " + headerLine);

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 空行をスキップ
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    // CSVの各行を解析
                    String[] data = parseCsvLine(line);
                    
                    // データのバリデーション
                    StockMaster.validateCsvData(data);
                    
                    // StockMasterエンティティを作成・保存
                    StockMaster stockMaster = StockMaster.createStockFromCsv(
                        data, 
                        stockMasterRepository, 
                        itemCodeGenerator
                    );
                    
                    successfulInserts.add(stockMaster);
                    System.out.println("登録完了 - 行 " + lineNumber + ": " + 
                        stockMaster.getItemName() + " (商品コード: " + stockMaster.getItemCode() + ")");

                } catch (IllegalArgumentException e) {
                    errors.add("行 " + lineNumber + ": " + e.getMessage());
                } catch (Exception e) {
                    errors.add("行 " + lineNumber + ": 処理中に予期せぬエラーが発生しました: " + e.getMessage());
                    // トランザクションをロールバックするため、例外を再スロー
                    throw e;
                }
            }

            // 成功したレコード数をログ出力
            System.out.println("CSV処理完了: " + successfulInserts.size() + "件の商品を登録しました。");
            
        } catch (Exception e) {
            // トランザクション例外の場合は再スロー
            if (!(e instanceof IllegalArgumentException)) {
                errors.add("CSVファイルの読み取り中にエラーが発生しました: " + e.getMessage());
                throw e;
            }
        }

        return errors;
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
            "item_name", "model_number", "category", "manufacturer", "current_stock", "location"
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