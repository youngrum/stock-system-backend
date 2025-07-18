package com.example.backend.order.service.handler;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.inventory.repository.StockMasterRepository;
import com.example.backend.inventory.repository.InventoryTransactionRepository;
import com.example.backend.order.dto.PurchaseOrderRequest;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;
import com.example.backend.common.service.ItemCodeGenerator;
import com.example.backend.common.service.TransactionIdGenerator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryOrderHandler {

    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
    private final StockMasterRepository stockMasterRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ItemCodeGenerator itemCodeGenerator;
    private final TransactionIdGenerator transactionIdGenerator;

    /**
     * 在庫発注明細の処理
     */
    @Transactional
    public BigDecimal processOrderDetails(PurchaseOrder header, List<PurchaseOrderRequest.Detail> details, String username) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (PurchaseOrderRequest.Detail detail : details) {
            validateInventoryOrderDetail(detail);
            
            StockMaster stock = findOrCreateStockMaster(detail);
            PurchaseOrderDetail orderDetail = createOrderDetail(header, detail, stock);
            
            purchaseOrderDetailRepository.save(orderDetail);
            
            totalAmount = totalAmount.add(detail.getQuantity().multiply(detail.getPurchasePrice()));
            System.out.println("現在の小計:" + totalAmount);
            
            createInventoryTransaction(stock, header, detail, username);
        }
        
        return totalAmount;
    }

    private void validateInventoryOrderDetail(PurchaseOrderRequest.Detail detail) {
        if (detail.getServices() != null && !detail.getServices().isEmpty()) {
            throw new IllegalArgumentException("在庫発注のITEM明細にはネストされたサービスを含めることはできません。");
        }
        System.out.println("在庫明細処理中: " + detail.getItemName());
    }

    private StockMaster findOrCreateStockMaster(PurchaseOrderRequest.Detail detail) {
        if (detail.getItemCode() != null && !detail.getItemCode().isBlank()) {
            System.out.println("▶ itemCode指定あり: " + detail.getItemCode());
            return stockMasterRepository.findByItemCode(detail.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("itemCodeが存在しません: " + detail.getItemCode()));
        } else {
            System.out.println("▶ itemCodeなし → 型番＋品名で検索: " + detail.getModelNumber() + " / " + detail.getItemName());
            return stockMasterRepository
                .findByModelNumberAndItemName(detail.getModelNumber(), detail.getItemName())
                .orElseGet(() -> createNewStockMaster(detail));
        }
    }

    private StockMaster createNewStockMaster(PurchaseOrderRequest.Detail detail) {
        StockMaster stockMaster = new StockMaster();
        System.out.println("▶ 新規登録 : " + detail);
        
        stockMaster.setItemName(detail.getItemName());
        stockMaster.setModelNumber(detail.getModelNumber());
        stockMaster.setCategory(detail.getCategory());
        stockMaster.setLocation(detail.getLocation());
        stockMaster.setCurrentStock(BigDecimal.ZERO);
        
        StockMaster saved = stockMasterRepository.save(stockMaster);
        
        String itemCode = itemCodeGenerator.generateItemCode(saved.getId());
        saved.setItemCode(itemCode);
        
        stockMasterRepository.flush();
        System.out.println("▶ 新規登録 itemCode: " + saved.getItemCode());
        
        return saved;
    }

    private PurchaseOrderDetail createOrderDetail(PurchaseOrder header, PurchaseOrderRequest.Detail detail, StockMaster stock) {
        System.out.println("▶ 明細登録準備完了 → " + stock.getItemCode());
        
        PurchaseOrderDetail orderDetail = new PurchaseOrderDetail();
        orderDetail.setItemCode(stock.getItemCode());
        orderDetail.setItemType("ITEM");
        orderDetail.setItemName(stock.getItemName());
        orderDetail.setModelNumber(stock.getModelNumber());
        orderDetail.setCategory(stock.getCategory());
        orderDetail.setQuantity(detail.getQuantity());
        orderDetail.setPurchasePrice(detail.getPurchasePrice());
        orderDetail.setReceivedQuantity(BigDecimal.ZERO);
        orderDetail.setStatus("未入庫");
        orderDetail.setRemarks(detail.getRemarks());
        orderDetail.setPurchaseOrder(header);
        
        // サービス関連カラムはNULL
        orderDetail.setServiceType(null);
        orderDetail.setRelatedAsset(null);
        orderDetail.setLinkedId(null);
        
        return orderDetail;
    }

    private void createInventoryTransaction(StockMaster stock, PurchaseOrder header, PurchaseOrderRequest.Detail detail, String username) {
        InventoryTransaction transaction = InventoryTransaction.createTransactionForPurchaseOrder(
            username, stock, header, detail, transactionIdGenerator, inventoryTransactionRepository);
        System.out.println(transaction);
        inventoryTransactionRepository.save(transaction);
    }
}