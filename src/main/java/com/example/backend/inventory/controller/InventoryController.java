package com.example.backend.inventory.controller;

import com.example.backend.entity.StockMaster;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.inventory.dto.InventoryDispatchRequest;
import com.example.backend.inventory.dto.InventoryReceiveRequest;
import com.example.backend.inventory.dto.StockMasterRequest;
import com.example.backend.inventory.service.InventoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import java.time.LocalDate;

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

  @Operation(summary = "在庫検索 全件取得時は ID, 品名, カテゴリー, 型番を空にする")
  @GetMapping("/inventory/search")
  public ResponseEntity<?> searchInventory(
      @Parameter(description = "ID") @RequestParam(required = false) String itemCode,
      @Parameter(description = "品名") @RequestParam(required = false) String itemName,
      @Parameter(description = "カテゴリー") @RequestParam(required = false) String category,
      @Parameter(description = "型番") @RequestParam(required = false) String modelNumber,
      @PageableDefault(sort = "itemCode", direction = Sort.Direction.ASC) Pageable pageable) {

    System.out.println(itemCode);
    System.out.println(itemName);
    System.out.println(category);
    System.out.println(modelNumber);

    Page<StockMaster> results = inventoryService.searchStock(itemCode, itemName, category, modelNumber, pageable);

    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "Inventory search successful.",
            "data", results));
  }

  @Operation(summary = "単一既存在庫情報取得")
  @GetMapping("/inventory/{itemCode}")
  public ResponseEntity<?> getStockByItemCode(@PathVariable String itemCode) {
    StockMaster inventory = inventoryService.getStockByItemCode(itemCode);
    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "Inventory fetched successfully.",
            "data", inventory));
  }

  @Operation(summary = "入庫登録")
  @PostMapping("/inventory/receive/{itemCode}")
  public ResponseEntity<?> receiveInventory(@RequestBody InventoryReceiveRequest request) {
    long transactionId = inventoryService.receiveInventory(request);
    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "Stock received successfully.",
            "transactionId", transactionId
              ));
  }

  @Operation(summary = "出庫登録")
  @PostMapping("/inventory/dispatch/{itemCode}")
  public ResponseEntity<?> dispatchInventory(@RequestBody InventoryDispatchRequest request) {
    long transactionId = inventoryService.dispatchInventory(request);
    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "Stock dispatched successfully.",
            "data", Map.of(
                "transactionId", transactionId)));
  }

  @Operation(summary = "いち在庫の入出庫履歴の取得（ページング対応）")
  @GetMapping("/inventory/{itemCode}/history")
  public ResponseEntity<?> getInventoryHistory(
      @PathVariable String itemCode,
      @PageableDefault(sort = "transactionTime", direction = Sort.Direction.DESC) Pageable pageable) {

    Page<InventoryTransaction> historyPage = inventoryService.getTransactionHistory(itemCode, pageable);

    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "Inventory history fetched successfully.",
            "data", historyPage));
  }

  @Operation(summary = "新規在庫ID発行・登録")
  @PostMapping("/inventory/new")
  public ResponseEntity<?> createStock(@RequestBody StockMasterRequest req) {
      StockMaster created = inventoryService.createStock(req);
      System.out.println(created);
      System.out.println("Created: " + created);
      
      return ResponseEntity.ok(
          Map.of(
            "status",200, 
            "message", "在庫を新規登録しました", 
            "data", created));
  }

  @Operation(summary = "全トランザクション履歴の取得（ページング対応・検索機能付き）")
  @GetMapping("/transactions")
  public ResponseEntity<?> getAllTransactions(
      @Parameter(description = "在庫ID") @RequestParam(required = false) String itemCode,
      @Parameter(description = "操作者") @RequestParam(required = false) String operator,
      @Parameter(description = "開始日") @RequestParam(required = false) 
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @Parameter(description = "終了日") @RequestParam(required = false) 
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @PageableDefault(sort = "transactionTime", direction = Sort.Direction.DESC) Pageable pageable) {

      Page<InventoryTransaction> allTransactions = inventoryService.getAllTransactionHistory(
          itemCode, operator, fromDate, toDate, pageable);

      return ResponseEntity.ok(
          Map.of(
              "status", 200,
              "message", "All inventory transactions fetched successfully.",
              "data", allTransactions));
  }
}