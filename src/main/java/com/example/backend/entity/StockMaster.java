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
    private String manufacturer;

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
}
