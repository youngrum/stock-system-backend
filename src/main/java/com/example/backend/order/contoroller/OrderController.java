package com.example.backend.order.contoroller;

import com.example.backend.inventory.service.InventoryService;
import com.example.backend.order.dto.InventoryReceiveFromOrderRequest;
import com.example.backend.order.dto.OrderHistoryResponse;
import com.example.backend.order.dto.PurchaseOrderRequest;
import com.example.backend.order.service.OrderHistoryService;
import com.example.backend.order.service.PurchaseOrderService;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

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


    @Operation(summary = "発注履歴閲覧")
    @GetMapping("/order-history")
    public ResponseEntity<Page<OrderHistoryResponse>> getOrderHistory(
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Page<OrderHistoryResponse> result = orderHistoryService.getOrderHistory(orderNo, page, size, fromDate, toDate);
        return ResponseEntity.ok(result);
    }

    
  @Operation(summary = "発注登録（新商品含む）")
  @PostMapping("/orders")
  public ResponseEntity<Map<String, Object>> registerOrder(@RequestBody PurchaseOrderRequest request) {
    String orderNo = purchaseOrderService.registerOrder(request);
    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "発注が完了しました",
            "data", Map.of("orderNo", orderNo)));
  }

  @Operation(summary = "納品登録・進捗更新")
  @PostMapping("/receive-from-order")
  public ResponseEntity<Map<String, Object>> receiveFromOrder(@RequestBody InventoryReceiveFromOrderRequest req) {
    inventoryService.receiveFromOrder(req);

    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "入庫処理が完了しました"));
  }
}
