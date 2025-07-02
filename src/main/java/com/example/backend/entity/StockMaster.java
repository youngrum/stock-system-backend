// StockMaster.java
package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend.common.service.ItemCodeGenerator;
import com.example.backend.inventory.dto.StockMasterRequest;
import com.example.backend.inventory.repository.StockMasterRepository;

@Entity
@Table(name = "stock_master")
@Data
public class StockMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // サロゲートキー


    @Column(name = "item_code", length = 32, unique = true, nullable = true)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(name = "model_number", length = 64)
    private String modelNumber;

    @Column(name = "category", nullable = false, length = 32)
    private String category;

    @Column(name = "manufacturer", length = 64)
    private String manufacturer = "-";

    @Column(name = "current_stock", nullable = false)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name ="location", length = 64)
    private String location = "-";

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.lastUpdated = LocalDateTime.now();
    }
    /**
     * 新規在庫ID発行・登録
     */
    public static StockMaster createStock(StockMasterRequest req,
                                    StockMasterRepository repository,
                                    ItemCodeGenerator itemCodeGenerator) {
        // 1. エンティティインスタンスを生成し、仮保存
        StockMaster stock = new StockMaster();
        stock.setItemCode("itemCode-null"); // 仮のitemCode
        stock.setItemName(req.getItemName());
        stock.setCategory(req.getCategory());
        stock.setManufacturer(req.getManufacturer());
        stock.setModelNumber(req.getModelNumber());
        stock.setCurrentStock(req.getCurrentStock());
        stock.setCurrentStock(req.getCurrentStock() != null ? req.getCurrentStock() : BigDecimal.ZERO);
        stock.setLocation(req.getLocation() != null ? req.getLocation() : "-");

        // 2. 仮保存してIDを取得
        stock = repository.save(stock);
        
        // 3. IDベースでitemCodeを採番
        String code = itemCodeGenerator.generateItemCode(stock.getId());
        System.out.println("Generated itemCode: " + code);                     
        stock.setItemCode(code);
        
        // 4. 正式なitemCodeで再保存
        stock = repository.save(stock);
        
        // 5. DBに反映（FLUSH）
        repository.flush();
        
        return stock;
    }

    public static StockMaster createStockFromCsv(String[] csvData,
                                           StockMasterRepository repository,
                                           ItemCodeGenerator itemCodeGenerator) throws Exception {
        // データの妥当性チェック
        if (csvData.length < 8) {
            throw new IllegalArgumentException("CSVデータが不完全です。8列必要です。");
        }

        // 1. エンティティインスタンスを生成
        StockMaster stock = new StockMaster();
        stock.setItemCode("itemCode-null"); // 仮のitemCode
        
        // 2. CSVデータを設定（トリムして設定）
        stock.setItemName(csvData[0].trim());
        stock.setModelNumber(csvData[1].trim().isEmpty() ? "-" : csvData[1].trim());
        stock.setCategory(csvData[2].trim());
        stock.setManufacturer(csvData[3].trim().isEmpty() ? "-" : csvData[3].trim());
        //csvData[4] = suplier はStockMasterには存在しないため、コメントアウト
        // stock.set(csvData[4].trim().isEmpty() ? "-" : csvData[4].trim());
        
        // 在庫数の変換
        try {
            String stockStr = csvData[5] != null ? csvData[5].trim() : "";
            if (stockStr.isEmpty()) {  // 空文字列チェックを先に
                stock.setCurrentStock(BigDecimal.ZERO);
            } else {
                stock.setCurrentStock(new BigDecimal(stockStr));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("在庫数が数値ではありません: " + csvData[5]);
        }
        
        stock.setLocation(csvData[6].trim().isEmpty() ? "-" : csvData[6].trim());

        // csvData[8]=remarksはStockMasterには存在しないため、コメントアウト
        // stock.setremarks(csvData[8].trim().isEmpty() ? "-" : csvData[8].trim());

        // 3. 仮保存してIDを取得
        stock = repository.save(stock);
                
        // 4. IDベースでitemCodeを採番
        String code = itemCodeGenerator.generateItemCode(stock.getId());
        stock.setItemCode(code);
        
        // 5. 正式なitemCodeで再保存
        stock = repository.save(stock);

        // 6. DBに反映（FLUSH）
        repository.flush();

        return stock;
    }

    /**
     * CSVアップロード用簡易バリデーション
     */
    public static void validateCsvData(String[] csvData) throws Exception {
        if (csvData.length < 8) {
            throw new IllegalArgumentException("CSVデータが不完全です。必要な列数: 8, 実際の列数: " + csvData.length);
        }

        // 必須フィールドのチェック
        if (csvData[0].trim().isEmpty()) {
            throw new IllegalArgumentException("商品名は必須です");
        }
        
        // 必須フィールドのチェック
        if (csvData[1].trim().isEmpty()) {
            throw new IllegalArgumentException("型番は必須です");
        }
        
        if (csvData[2].trim().isEmpty()) {
            throw new IllegalArgumentException("カテゴリは必須です");
        }

        System.out.println("csvData[5]："+csvData[5]);

        // 在庫数の数値チェック
        try {
                String stockStr = csvData[5] != null ? csvData[5].trim() : "";
                if (stockStr.isEmpty()) {  // 空文字列チェックを先に
                    stockStr = 0 + ""; // 空文字列の場合は0に変換
                }else {
                    new BigDecimal(stockStr); // 数値変換
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("在庫数が数値ではありません: " + csvData[5]);
            }
    }
}
