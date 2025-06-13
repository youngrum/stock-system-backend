package com.example.backend.order.dto;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;

import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Data
@Builder
/**
 *  発注履歴のレスポンスDTO
 */
public class OrderHistoryResponse {
    private String orderNo;
    private String supplier;
    private LocalDate orderDate;
    private BigDecimal shippingFee;
    private BigDecimal orderSubtotal;
    private String operator;
    private String status;
    private String remarks;
    private LocalDate createdAt;
    private List<OrderDetailResponse> details;

    @Builder
    @Data
    public static class OrderDetailResponse {
        private String orderNo;
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
            .orderDate(order.getOrderDate())
            .orderSubtotal(order.getOrderSubtotal())
            .operator(order.getOperator())
            .status(order.getStatus())
            .remarks(order.getRemarks())
            .createdAt(order.getCreatedAt())
            .details(
                details.stream()
                    .map(d -> OrderDetailResponse.builder()
                        .orderNo(order.getOrderNo())
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
