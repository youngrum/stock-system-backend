package com.example.backend.order.service;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.order.dto.OrderHistoryResponse;
import com.example.backend.order.repository.OrderHistoryRepository;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;
import com.example.backend.order.repository.PurchaseOrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public Page<OrderHistoryResponse> getOrderHistory(String orderNo, int page, int size, LocalDate fromDate,
            LocalDate toDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 条件: orderNo指定あり
        if (orderNo != null && !orderNo.isBlank()) {
            PurchaseOrder order = purchaseOrderRepository.findByOrderNo(orderNo)
                    .orElseThrow(() -> new ResourceNotFoundException("発注が見つかりません（orderNo: " + orderNo + "）"));
            List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByOrderNo(orderNo);
            OrderHistoryResponse response = OrderHistoryResponse.from(order, details);
            return new PageImpl<>(List.of(response), pageable, 1);
        }

        // 条件: 日付フィルターあり
        Page<PurchaseOrder> ordersPage;
        // if (fromDate != null && toDate != null) {
        //     LocalDate from = fromDate;
        //     LocalDate to = toDate;
        //     ordersPage = purchaseOrderRepository.findByCreatedAtBetween(from, to, pageable);
        // } else {
        //     ordersPage = purchaseOrderRepository.findAll(pageable);
        // }
        if (fromDate != null && toDate != null) {
            ordersPage = purchaseOrderRepository.findByCreatedAtBetween(fromDate, toDate, pageable);
        } else if (fromDate != null) {
            ordersPage = purchaseOrderRepository.findByCreatedAtAfter(fromDate.minusDays(1), pageable);
        } else if (toDate != null) {
            ordersPage = purchaseOrderRepository.findByCreatedAtBefore(toDate.plusDays(1), pageable);
        } else {
            ordersPage = purchaseOrderRepository.findAll(pageable);
        }
        

        List<OrderHistoryResponse> responseList = ordersPage.stream()
                .map(order -> {
                    List<PurchaseOrderDetail> details = purchaseOrderDetailRepository.findByOrderNo(order.getOrderNo());
                    return OrderHistoryResponse.from(order, details);
                }).toList();

        return new PageImpl<>(responseList, pageable, ordersPage.getTotalElements());

    }
}