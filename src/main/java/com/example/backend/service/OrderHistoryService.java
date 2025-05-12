package com.example.backend.service;

import com.example.backend.dto.OrderHistoryResponse;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.repository.OrderHistoryRepository;
import com.example.backend.repository.PurchaseOrderDetailRepository;
import com.example.backend.repository.PurchaseOrderRepository;
import com.example.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public List<OrderHistoryResponse> getOrderHistory(String orderNo) {
        List<PurchaseOrder> orders;

        if (orderNo != null && !orderNo.isEmpty()) {
            PurchaseOrder order = purchaseOrderRepository
                .findByOrderNo(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("該当の発注が見つかりません"));
            orders = List.of(order);
        } else {
            orders = purchaseOrderRepository.findAll();
        }

        return orders.stream().map(order -> {
            List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByOrderNo(order.getOrderNo());
            return OrderHistoryResponse.from(order, details);
        }).toList();
    }
}