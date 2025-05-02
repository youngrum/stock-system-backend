package com.example.backend.service;

import com.example.backend.dto.InventoryDispatchRequest;
import com.example.backend.dto.InventoryReceiveRequest;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.TransactionType;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.repository.InventoryTransactionRepository;
import com.example.backend.repository.StockMasterRepository;
import com.example.backend.repository.PurchaseOrderRepository;
import com.example.backend.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class InventoryService {

    private final StockMasterRepository stockMasterRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    public InventoryService(StockMasterRepository stockMasterRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            PurchaseOrderRepository purchaseOrderRepository) {
        this.stockMasterRepository = stockMasterRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
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
        // 1. ログインユーザー名を取得
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);

        // 2. 該当在庫データを取得（なければエラー）
        StockMaster stock = stockMasterRepository.findById(req.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("在庫が見つかりません"));

        // 3. 自動で orderNo を発行
        String orderNo = generateNewOrderNo();

        // 4. purchase_order を新規作成
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(orderNo);
        order.setOrderDate(LocalDate.now());
        order.setShippingFee(BigDecimal.ZERO);
        order.setOperator(username);
        order.setSupplier(req.getSupplier());
        order.setRemarks("入庫時に自動生成");
        order.setOrderSubtotal(BigDecimal.ZERO);
        purchaseOrderRepository.save(order);

        // 5. 入庫トランザクション登録
        InventoryTransaction tx = new InventoryTransaction();
        tx.setStockItem(stock);
        tx.setPurchaseOrder(order);
        tx.setTransactionType(TransactionType.RECEIVE);
        tx.setQuantity(req.getQuantity());
        tx.setOperator(username);
        tx.setTransactionTime(LocalDateTime.now());
        tx.setManufacturer(req.getManufacturer());
        tx.setSupplier(req.getSupplier());
        tx.setPurchasePrice(req.getPurchasePrice());
        tx.setRemarks(req.getRemarks());
        inventoryTransactionRepository.save(tx);

        // 6. 在庫数を更新
        stock.setCurrentStock(stock.getCurrentStock() + req.getQuantity());
        stockMasterRepository.save(stock);

        // 7. 発注小計を加算
        BigDecimal lineTotal = req.getPurchasePrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        order.setOrderSubtotal(order.getOrderSubtotal().add(lineTotal));
        purchaseOrderRepository.save(order);
    }

    // 自動発番メソッド
    private String generateNewOrderNo() {
        return "PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // シンプルな出庫処理
    public void dispatchInventory(InventoryDispatchRequest req) {
        // ログインユーザー名を自動セット
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);
        System.out.println("ログイン中のユーザー名: " + username);

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

    // ページング機能
    public Page<InventoryTransaction> getTransactionHistory(String itemCode, Pageable pageable) {
        return inventoryTransactionRepository.findByStockItemItemCodeOrderByTransactionTimeDesc(itemCode, pageable);
    }

    // 全取引履歴閲覧
    public Page<InventoryTransaction> getAllTransactionHistory(Pageable pageable) {
        return inventoryTransactionRepository.findAllByOrderByTransactionTimeDesc(pageable);
    }
}
