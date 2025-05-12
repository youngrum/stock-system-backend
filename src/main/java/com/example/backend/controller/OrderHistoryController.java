package com.example.backend.controller;

import com.example.backend.dto.OrderHistoryResponse;
import com.example.backend.service.OrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-history")
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderHistoryService orderHistoryService;

    @GetMapping
    public ResponseEntity<List<OrderHistoryResponse>> getOrderHistory(@RequestParam(required = false) String orderNo) {
        List<OrderHistoryResponse> result = orderHistoryService.getOrderHistory(orderNo);
        return ResponseEntity.ok(result);
    }
}
