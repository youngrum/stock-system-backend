package com.example.backend.dto;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Data
@Builder
public class OrderHistoryResponse {
    private String orderNo;
    private String supplier;
    private BigDecimal shippingFee;
    private String operator;
    private String remarks;
    private LocalDateTime createdAt;
    private List<OrderDetailResponse> details;

    @Builder
    @Data
    public static class OrderDetailResponse {
        private String itemCode;
        private String itemName;
        private String modelNumber;
        private String category;
        private BigDecimal quantity;
        private BigDecimal purchasePrice;
        private BigDecimal receivedQuantity;
        private String status;
        private String remarks;
    }

    // PurchaseOrder エンティティから 当レスポンスを返すためのDTOを生成
    public static OrderHistoryResponse from(PurchaseOrder order, List<PurchaseOrderDetail> details) {
        return OrderHistoryResponse.builder()
            .orderNo(order.getOrderNo())
            .supplier(order.getSupplier())
            .shippingFee(order.getShippingFee())
            .operator(order.getOperator())
            .remarks(order.getRemarks())
            .details(
                details.stream()
                    .map(d -> OrderDetailResponse.builder()
                        .itemCode(d.getItemCode())
                        .itemName(d.getItemName())
                        .modelNumber(d.getModelNumber())
                        .category(d.getCategory())
                        .quantity(d.getQuantity())
                        .purchasePrice(d.getPurchasePrice())
                        .receivedQuantity(d.getReceivedQuantity())
                        .status(d.getStatus())
                        .remarks(d.getRemarks())
                        .build()
                    )
                    .collect(Collectors.toList())
            ).build();
        }
}    
