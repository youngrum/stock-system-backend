package com.example.backend.service;

import com.example.backend.dto.InventoryDispatchRequest;
import com.example.backend.dto.InventoryReceiveFromOrderRequest;
import com.example.backend.dto.InventoryReceiveRequest;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.TransactionType;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.repository.InventoryTransactionRepository;
import com.example.backend.repository.PurchaseOrderDetailRepository;
import com.example.backend.repository.StockMasterRepository;
import com.example.backend.repository.PurchaseOrderRepository;
import com.example.backend.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import jakarta.validation.ValidationException;

@Service
public class InventoryService {

    private final StockMasterRepository stockMasterRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;

    @Autowired
    public InventoryService(StockMasterRepository stockMasterRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderDetailRepository purchaseOrderDetailRepository) {
        this.stockMasterRepository = stockMasterRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderDetailRepository = purchaseOrderDetailRepository;
    }

    // パラメーター付在庫検索
    public Page<StockMaster> searchStock(String itemName, String modelNumber, String category, Pageable pageable) {
        // 空の場合は空文字に変換（部分一致検索に対応）
        String nameKeyword = (itemName != null) ? itemName : "";
        String modelKeyword = (modelNumber != null) ? modelNumber : "";
        String categoryKeyword = (category != null) ? category : "";
        return stockMasterRepository
                .findByItemNameContainingIgnoreCaseAndModelNumberContainingIgnoreCaseAndCategoryContainingIgnoreCase(
                        nameKeyword, modelKeyword, categoryKeyword, pageable);
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
    public Long receiveInventory(InventoryReceiveRequest req) {
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

        // 発行されたトランザクションIDを返す
        return tx.getTransactionId();
    }

    // 自動発番メソッド
    private String generateNewOrderNo() {
        return "PO-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    // シンプルな出庫処理
    public Long dispatchInventory(InventoryDispatchRequest req) {
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
        tx.setOperator(username);
        tx.setTransactionTime(LocalDateTime.now());
        inventoryTransactionRepository.save(tx);

        stock.setCurrentStock(stock.getCurrentStock() - req.getQuantity());
        stockMasterRepository.save(stock);

        return tx.getTransactionId();
    }

    // ページング機能
    public Page<InventoryTransaction> getTransactionHistory(String itemCode, Pageable pageable) {
        return inventoryTransactionRepository.findByStockItemItemCodeOrderByTransactionTimeDesc(itemCode, pageable);
    }

    // 全取引履歴閲覧
    public Page<InventoryTransaction> getAllTransactionHistory(Pageable pageable) {
        return inventoryTransactionRepository.findAllByOrderByTransactionTimeDesc(pageable);
    }

    @Transactional
    public void receiveFromOrder(InventoryReceiveFromOrderRequest req) {
        String orderNo = req.getOrderNo();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);

        PurchaseOrder order = purchaseOrderRepository.findById(req.getOrderNo())
                .orElseThrow(() -> new ResourceNotFoundException("発注が見つかりません"));

        for (InventoryReceiveFromOrderRequest.Item item : req.getItems()) {
            String itemCode = item.getItemCode();

            // 🔽 ここで DB から単価を取得
            BigDecimal purchasePrice = purchaseOrderDetailRepository
                    .findByOrderNoAndItemCode(orderNo, itemCode)
                    .map(PurchaseOrderDetail::getPurchasePrice)
                    .orElse(BigDecimal.ZERO); // fallback（または例外投げる）

            // 発注明細を取得
            PurchaseOrderDetail detail = purchaseOrderDetailRepository
                    .findByOrderNoAndItemCode(req.getOrderNo(), itemCode)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "発注明細が見つかりません（orderNo: " + req.getOrderNo() + ", itemCode: " + itemCode + "）"));

            // ▼ ここで受け入れ済み数量チェックを追加
            BigDecimal receivedSoFar = detail.getReceivedQuantity() != null ? detail.getReceivedQuantity()
                    : BigDecimal.ZERO;
            BigDecimal orderQuantity = detail.getQuantity();
            BigDecimal receivingNow = BigDecimal.valueOf(item.getReceivedQuantity());

            if (receivedSoFar.compareTo(orderQuantity) >= 0) {
                throw new ValidationException("すでに全数が入庫済みのため、これ以上受け入れできません（itemCode: " + itemCode + "）");
            }

            if (receivedSoFar.add(receivingNow).compareTo(orderQuantity) > 0) {
                throw new ValidationException("受け入れ数が発注数を超えています（itemCode: " + itemCode + "）");
            }

            // 在庫マスタを取得
            StockMaster stock = stockMasterRepository.findById(itemCode)
                    .orElseThrow(() -> new ResourceNotFoundException("在庫が見つかりません"));

            // 入庫数チェック
            BigDecimal totalReceived = detail.getReceivedQuantity()
                    .add(BigDecimal.valueOf(item.getReceivedQuantity()));

            if (totalReceived.compareTo(detail.getQuantity()) > 0) {
                throw new IllegalArgumentException("受領数が発注数を超えています: " + itemCode);
            }

            // 在庫数更新
            stock.setCurrentStock(stock.getCurrentStock() + item.getReceivedQuantity());
            stockMasterRepository.save(stock);

            // 明細更新
            detail.setReceivedQuantity(totalReceived);
            // .compareTo() は 0 を返すと「等しい」、正なら「大きい」、負なら「小さい」。
            if (totalReceived.compareTo(detail.getQuantity()) >= 0) {
                detail.setStatus("完了");
            } else if (totalReceived.compareTo(BigDecimal.ZERO) > 0) {
                detail.setStatus("一部入庫");
            } else {
                detail.setStatus("未入庫");
            }
            purchaseOrderDetailRepository.save(detail);

            // トランザクション登録
            InventoryTransaction tx = new InventoryTransaction();
            tx.setPurchaseOrder(order);
            tx.setOperator(req.getOperator());
            tx.setStockItem(stock);
            tx.setQuantity(item.getReceivedQuantity());
            tx.setPurchasePrice(purchasePrice);
            tx.setTransactionType(TransactionType.RECEIVE);
            tx.setOperator(req.getOperator());
            tx.setTransactionTime(LocalDateTime.now());
            tx.setRemarks(item.getRemarks());
            inventoryTransactionRepository.save(tx);
        }

        // 全明細が完了か判定してヘッダーに反映
        boolean allDone = purchaseOrderDetailRepository.findByOrderNo(req.getOrderNo())
                .stream()
                .allMatch(d -> "完了".equals(d.getStatus()));

        if (allDone) {
            order.setStatus("完了");
            purchaseOrderRepository.save(order);
        }
    }

}
