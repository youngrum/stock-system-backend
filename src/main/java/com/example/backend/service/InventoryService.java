package com.example.backend.service;

import com.example.backend.dto.InventoryDispatchRequest;
import com.example.backend.dto.InventoryReceiveRequest;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.TransactionType;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.InventoryTransactionRepository;
import com.example.backend.repository.StockMasterRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final StockMasterRepository stockMasterRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    public InventoryService(StockMasterRepository stockMasterRepository,
            InventoryTransactionRepository inventoryTransactionRepository) {
        this.stockMasterRepository = stockMasterRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    // 在庫一覧取得
    public Page<StockMaster> getAllStock(Pageable pageable) {
        return stockMasterRepository.findAll(pageable);
    }

    // 在庫検索
    public Page<StockMaster> searchStock(String keyword, String category, Pageable pageable) {
        String kw = (keyword != null) ? keyword : "";
        String cat = (category != null) ? category : "";
        return stockMasterRepository.findByItemNameContainingIgnoreCaseAndCategoryContainingIgnoreCase(kw, cat,
                pageable);
    }

    // 単一在庫取得
    public StockMaster getStockByItemCode(String itemCode) {
        return stockMasterRepository.findById(itemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemCode));
    }

    // 新規在庫登録
    public StockMaster createStock(StockMaster stockMaster) {
        return stockMasterRepository.save(stockMaster);
    }

    // 入庫処理
    public void receiveInventory(InventoryReceiveRequest req) {
        // ログインユーザー名を自動セット
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);

        StockMaster stock = stockMasterRepository.findById(req.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("在庫が見つかりません"));

        InventoryTransaction tx = new InventoryTransaction();
        tx.setStockItem(stock);
        tx.setTransactionType(TransactionType.RECEIVE);
        tx.setQuantity(req.getQuantity());
        tx.setOperator(req.getOperator());
        tx.setTransactionTime(LocalDateTime.now());
        inventoryTransactionRepository.save(tx);

        stock.setCurrentStock(stock.getCurrentStock() + req.getQuantity());
        stockMasterRepository.save(stock);
    }

    // シンプルな出庫処理
    public void dispatchInventory(InventoryDispatchRequest req) {
        // ログインユーザー名を自動セット
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);

        StockMaster stock = stockMasterRepository.findById(req.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("在庫が見つかりません"));

        if (stock.getCurrentStock() < req.getQuantity()) {
            throw new RuntimeException("在庫が不足しています");
        }

        InventoryTransaction tx = new InventoryTransaction();
        tx.setStockItem(stock);
        tx.setTransactionType(TransactionType.DISPATCH);
        tx.setQuantity(req.getQuantity());
        tx.setOperator(req.getOperator());
        tx.setTransactionTime(LocalDateTime.now());
        inventoryTransactionRepository.save(tx);

        stock.setCurrentStock(stock.getCurrentStock() - req.getQuantity());
        stockMasterRepository.save(stock);
    }
}
