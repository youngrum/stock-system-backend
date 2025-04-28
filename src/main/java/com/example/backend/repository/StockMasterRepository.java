package com.example.backend.repository;

import com.example.backend.entity.StockMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMasterRepository extends JpaRepository<StockMaster, String> {

    Page<StockMaster> findByItemNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(
            String itemName, String category, Pageable pageable
    );
}
