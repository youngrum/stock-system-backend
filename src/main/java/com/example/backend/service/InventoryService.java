package com.example.backend.service;

import com.example.backend.entity.StockMaster;
import com.example.backend.repository.StockMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.example.backend.exception.ResourceNotFoundException;

@Service
public class InventoryService {

    private final StockMasterRepository stockMasterRepository;

    @Autowired
    public InventoryService(StockMasterRepository stockMasterRepository) {
        this.stockMasterRepository = stockMasterRepository;
    }

    public Page<StockMaster> getAllStock(Pageable pageable) {
        return stockMasterRepository.findAll(pageable);
    }

    public Page<StockMaster> searchStock(String keyword, String category, Pageable pageable) {
        String kw = (keyword != null) ? keyword : "";
        String cat = (category != null) ? category : "";
        return stockMasterRepository.findByItemNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(kw, cat, pageable);
    }

    public StockMaster getStockByItemCode(String itemCode) {
        return stockMasterRepository.findById(itemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemCode));
    }
}
