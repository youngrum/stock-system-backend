package com.example.backend.controller;

import com.example.backend.dto.OrderHistoryResponse;
import com.example.backend.service.OrderHistoryService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "発注履歴閲覧API", description = "発注履歴をリストとして取得")
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    @Operation(summary = "発注履歴閲覧")
    @GetMapping("/order-history")
    public ResponseEntity<Page<OrderHistoryResponse>> getOrderHistory(
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderHistoryResponse> result = orderHistoryService.getOrderHistory(orderNo, page, size);
        return ResponseEntity.ok(result);
    }
}
