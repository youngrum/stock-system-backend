package com.example.backend.order.contoroller;

import com.example.backend.inventory.service.InventoryService;
import com.example.backend.asset.service.AssetService;
import com.example.backend.order.dto.AssetReceiveFromOrderRequest;
import com.example.backend.order.dto.InventoryReceiveFromOrderRequest;
import com.example.backend.order.dto.OrderHistoryResponse;
import com.example.backend.order.dto.PurchaseOrderRequest;
import com.example.backend.order.service.OrderHistoryService;
import com.example.backend.order.service.PurchaseOrderService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.web.PageableDefault;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "発注管理API", description = "発注登録・履歴・納品処理")
@RequiredArgsConstructor
public class OrderController {

  private final PurchaseOrderService purchaseOrderService;
  private final OrderHistoryService orderHistoryService;
  private final InventoryService inventoryService;
  private final AssetService assetService;

  @Operation(summary = "発注履歴閲覧")
  @GetMapping("/order-history")
  public ResponseEntity<Page<OrderHistoryResponse>> getOrderHistory(
      @RequestParam(required = false) String orderNo,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
      @PageableDefault(size = 100, direction = Sort.Direction.ASC) Pageable pageable) {
    Page<OrderHistoryResponse> result = orderHistoryService.getOrderHistory(orderNo, page, size, fromDate, toDate);
    return ResponseEntity.ok(result);
  }

  @Operation(summary = "発注登録（新商品含む）")
  @PostMapping("/orders")
  public ResponseEntity<Map<String, Object>> registerOrder(@RequestBody PurchaseOrderRequest request) {
    System.out.println("Received order request: " + request);
    String orderNo = purchaseOrderService.registerOrder(request);
    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "発注が完了しました",
            "data", Map.of("orderNo", orderNo)));
  }

  @Operation(summary = "在庫品の納品登録・進捗更新")
  @PostMapping("/receive-inventory-from-order")
  public ResponseEntity<Map<String, Object>> receiveFromOrder(@RequestBody InventoryReceiveFromOrderRequest req) {
    inventoryService.receiveFromOrder(req);

    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "発注登録商品の入庫処理が完了しました"));
  }

  @Operation(summary = "設備品 入庫登録・発注進捗更新")
  @PostMapping("/receive-asset-from-order")
  public ResponseEntity<Map<String, Object>> receiveAssetFromOrder(@RequestBody AssetReceiveFromOrderRequest req) {
    assetService.receiveFromOrder(req);

    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "設備関連(校正・修理含む)納品処理が完了しました"));
  }
}
