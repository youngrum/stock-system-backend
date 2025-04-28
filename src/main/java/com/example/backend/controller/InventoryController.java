package com.example.backend.controller;

import com.example.backend.entity.StockMaster;
import com.example.backend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "在庫管理API", description = "在庫情報取得・管理")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "在庫一覧取得")
    @GetMapping("/inventory")
    public Page<StockMaster> getInventory(Pageable pageable) {
        return inventoryService.getAllStock(pageable);
    }

    @Operation(summary = "在庫検索")
    @GetMapping("/inventory/search")
    public Page<StockMaster> searchInventory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Pageable pageable
    ) {
        return inventoryService.searchStock(keyword, category, pageable);
    }

    @Operation(summary = "単一在庫詳細取得")
    @GetMapping("/inventory/{itemCode}")
    public StockMaster getInventoryDetail(@PathVariable String itemCode) {
        return inventoryService.getStockByItemCode(itemCode);
    }
}
