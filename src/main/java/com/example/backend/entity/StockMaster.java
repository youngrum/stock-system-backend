package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_master")
@Data
public class StockMaster {

    @Id
    @Column(name = "item_code", length = 32)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 128)
    private String itemName;

    @Column(name = "model_number", length = 64)
    private String modelNumber;

    @Column(name = "category", nullable = false, length = 32)
    private String category;

    @Column(name = "current_stock", nullable = false)
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "last_updated", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;
}
