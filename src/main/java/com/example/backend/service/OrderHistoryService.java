package com.example.backend.service;

import com.example.backend.dto.OrderHistoryResponse;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.repository.OrderHistoryRepository;
import com.example.backend.repository.PurchaseOrderDetailRepository;
import com.example.backend.repository.PurchaseOrderRepository;
import com.example.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public Page<OrderHistoryResponse> getOrderHistory(String orderNo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<OrderHistoryResponse> responseList;

        if (orderNo != null && !orderNo.isBlank()) {
            PurchaseOrder order = purchaseOrderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new ResourceNotFoundException("発注が見つかりません"));
            List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByOrderNo(orderNo);
            responseList = List.of(OrderHistoryResponse.from(order, details));
            return new PageImpl<>(responseList, pageable, 1);
        } else {
            Page<PurchaseOrder> ordersPage = purchaseOrderRepository.findAll(pageable);
            responseList = ordersPage.stream()
                    .map(order -> {
                        List<PurchaseOrderDetail> details = purchaseOrderDetailRepository
                                .findByOrderNo(order.getOrderNo());
                        return OrderHistoryResponse.from(order, details);
                    })
                    .toList();
            return new PageImpl<>(responseList, pageable, ordersPage.getTotalElements());

        }
    }
}