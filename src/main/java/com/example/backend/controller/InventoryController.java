package com.example.backend.controller;

import com.example.backend.dto.InventoryDispatchRequest;
import com.example.backend.dto.InventoryReceiveRequest;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.Map;

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
            Pageable pageable) {
        return inventoryService.searchStock(keyword, category, pageable);
    }

    @Operation(summary = "単一在庫詳細取得")
    @GetMapping("/inventory/{itemCode}")
    public StockMaster getInventoryDetail(@PathVariable String itemCode) {
        return inventoryService.getStockByItemCode(itemCode);
    }

    @Operation(summary = "入庫登録")
    @PostMapping("/inventory/receive")
    public ResponseEntity<?> receiveInventory(@RequestBody InventoryReceiveRequest request) {
        long transactionId = inventoryService.receiveInventory(request);
        return ResponseEntity.ok(
            Map.of(
                "status", 200,
                "message", "Stock received successfully.",
                "data", Map.of(
                    "transactionId", transactionId
                )
            )
        );
    }

    @Operation(summary = "出庫登録")
    @PostMapping("/inventory/dispatch")
    public ResponseEntity<?> dispatchInventory(@RequestBody InventoryDispatchRequest request) {
        long transactionId = inventoryService.dispatchInventory(request);
        return ResponseEntity.ok(
            Map.of(
                "status", 200,
                "message", "Stock dispatched successfully.",
                "data", Map.of(
                    "transactionId", transactionId
                )
            )
        );
    }
    

    @Operation(summary = "入出庫履歴の取得（ページング対応）")
    @GetMapping("/inventory/{itemCode}/history")
    public Page<InventoryTransaction> getInventoryHistory(
            @PathVariable String itemCode,
            @PageableDefault(sort = "transactionTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return inventoryService.getTransactionHistory(itemCode, pageable);
    }

    @Operation(summary = "全トランザクション履歴の取得（ページング対応）")
    @GetMapping("/transactions")
    public Page<InventoryTransaction> getAllTransactions(
            @PageableDefault(sort = "transactionTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return inventoryService.getAllTransactionHistory(pageable);
    }
}
