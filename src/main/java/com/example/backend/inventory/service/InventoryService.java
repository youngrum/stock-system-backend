package com.example.backend.inventory.service;

import com.example.backend.common.service.ItemCodeGenerator;
import com.example.backend.common.service.OrderNumberGenerator;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.TransactionType;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.inventory.dto.InventoryDispatchRequest;
import com.example.backend.inventory.dto.InventoryReceiveRequest;
import com.example.backend.inventory.dto.StockMasterRequest;
import com.example.backend.inventory.repository.InventoryTransactionRepository;
import com.example.backend.inventory.repository.StockMasterRepository;
import com.example.backend.order.dto.InventoryReceiveFromOrderRequest;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;
import com.example.backend.order.repository.PurchaseOrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final ItemCodeGenerator itemCodeGenerator;
    private final OrderNumberGenerator orderNumberGenerator;

    private static final int DEFAULT_DAYS_BACK = 30; // toDateのみ指定時のデフォルト期間
    private static final int MAX_SEARCH_DAYS = 365;   // 最大検索可能期間（パフォーマンス対策）


    @Autowired
    public InventoryService(StockMasterRepository stockMasterRepository,
            InventoryTransactionRepository inventoryTransactionRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderDetailRepository purchaseOrderDetailRepository,
            ItemCodeGenerator itemCodeGenerator,
            OrderNumberGenerator orderNumberGenerator) {
        this.stockMasterRepository = stockMasterRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderDetailRepository = purchaseOrderDetailRepository;
        this.itemCodeGenerator = itemCodeGenerator;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    // パラメーター付在庫検索
    public Page<StockMaster> searchStock(String itemCode, String itemName, String category, String modelNumber,
            Pageable pageable) {

        // 空の場合は空文字に変換（部分一致検索に対応）
        String itemCodeKeyword = (itemCode != null) ? itemCode : "";
        String itemNameKeyword = (itemName != null) ? itemName : "";
        String categoryKeyword = (category != null) ? category : "";
        String modelNumberKeyword = (modelNumber != null) ? modelNumber : "";

        System.out.printf(
                "🔍 検索条件: itemCodeKeyword='%s', itemNameKeyword='%s', categoryKeyword='%s', modelNumberKeyword='%s'%n",
                itemCodeKeyword, itemNameKeyword, categoryKeyword, modelNumberKeyword);

        if (!isBlank(itemCode)) {
            System.out.printf("!isBlank(itemCode)");
            // itemCode は一意なので他の条件を無視してよい
            return stockMasterRepository.findByItemCodeContaining(itemCode, pageable);
        }
        // itemCode が空の場合、他の条件で検索
        return stockMasterRepository
                .findByItemCodeContainingAndItemNameContainingAndCategoryContainingAndModelNumberContaining(
                        itemCodeKeyword, itemNameKeyword,
                        categoryKeyword, modelNumberKeyword, pageable);
    }

    // 単一在庫取得
    public StockMaster getStockByItemCode(String itemCode) {
        return stockMasterRepository.findByItemCode(itemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + itemCode));
    }

    // 新規在庫登録
    @Transactional
    public StockMaster createStock(StockMasterRequest req) {
        System.out.println("Creating stock with request: " + req);
        // 1. 在庫を登録
        // 1-1. 仮保存して id を取得
        StockMaster stock = new StockMaster();
        stock.setItemCode("itemCode-null"); // 仮の itemCode
        stock.setItemName(req.getItemName());
        stock.setCategory(req.getCategory());
        stock.setModelNumber(req.getModelNumber());
        stock.setManufacturer(req.getManufacturer());
        stock.setCurrentStock(req.getCurrentStock());

        stock = stockMasterRepository.save(stock);
        System.out.println(stock);

        // 1-2. id ベースで itemCode を採番
        String code = itemCodeGenerator.generateItemCode(stock.getId());
        stock.setItemCode(code);

        // 1-3. 再保存（itemCodeを含めて）
        stock = stockMasterRepository.save(stock);
        // 在庫マスタに保存した内容を DB に反映させる（FLUSH）
        // 後続のトランザクション履歴登録で正規のitemCodeを読み取らせるため
        stockMasterRepository.flush();

        // 2. ログインユーザー名を取得
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 3. トランザクション履歴登録
        InventoryTransaction tx = new InventoryTransaction();
        tx.setStockItem(stock);
        // 登録した数量によってトランザクションタイプを分岐
        if (req.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
            tx.setTransactionType(TransactionType.ITEM_REGIST);
            tx.setQuantity(BigDecimal.ZERO); // 明示的に0を設定 (一応)
        } else {
            tx.setTransactionType(TransactionType.MANUAL_RECEIVE);
            tx.setQuantity(req.getCurrentStock());
        }
        tx.setOperator(username);
        tx.setRemarks(req.getRemarks());
        tx.setPurchaseOrder(null);
        tx.setTransactionTime(LocalDateTime.now());
        inventoryTransactionRepository.save(tx);

        return stock;
    }

    // 入庫処理
    @Transactional
    public Long receiveInventory(InventoryReceiveRequest req) {
        System.out.println(req);
        // 1. ログインユーザー名を取得
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);

        // 2. 該当在庫データを取得（なければエラー）
        StockMaster stock = stockMasterRepository.findByItemCode(req.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("在庫が見つかりません"));

        // PurchaseOrder オブジェクトを初期化
        PurchaseOrder order = null; 

        // リクエスト数量が0ではない場合のみ発注ヘッダーを作成
        if (req.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
            // 3. 発注ヘッダーを新規作成
            PurchaseOrder newOrder = new PurchaseOrder();
            newOrder.setOrderNo("order-no");
            newOrder.setOrderDate(LocalDate.now());
            newOrder.setShippingFee(BigDecimal.ZERO);
            newOrder.setOperator(username);
            newOrder.setSupplier(req.getSupplier());
            newOrder.setRemarks(req.getRemarks());
            newOrder.setOrderSubtotal(BigDecimal.ZERO);
            purchaseOrderRepository.save(newOrder);

            // 3-2. id ベースで oderNo を採番
            String code = orderNumberGenerator.generateOrderNo(newOrder.getId());
            newOrder.setOrderNo(code);
            purchaseOrderRepository.save(newOrder); // 採番されたOrderNoをDBに保存
            purchaseOrderRepository.flush(); // DBに反映

            order = newOrder; // 作成したPurchaseOrderをセット
        }

        // ---------- 発注明細の登録 ----------
        // ご提示のコードブロックをここに移動し、数量が0ではない場合のみ実行
        if (req.getQuantity().compareTo(BigDecimal.ZERO) != 0) {
            PurchaseOrderDetail detail = new PurchaseOrderDetail();
            detail.setPurchaseOrder(order); // ここで上記で作成したorderを使用
            detail.setItemCode(stock.getItemCode());
            detail.setItemName(stock.getItemName());
            detail.setModelNumber(stock.getModelNumber());
            detail.setCategory(stock.getCategory());
            detail.setQuantity(req.getQuantity());
            detail.setPurchasePrice(req.getPurchasePrice());
            detail.setReceivedQuantity(req.getQuantity()); // ここは入庫処理なので ReceivedQuantity にするなら、この場で完全入庫扱い
            detail.setStatus("完了"); // 数量と同じだけ入庫されるのであれば完了
            detail.setRemarks(req.getRemarks()); // remarksもOrderDetailに設定するなら
            purchaseOrderDetailRepository.save(detail);

        }

        // 5. 入庫トランザクション登録
        InventoryTransaction tx = new InventoryTransaction();
        tx.setStockItem(stock);
        tx.setPurchaseOrder(order);
        tx.setTransactionType(TransactionType.MANUAL_RECEIVE);
        tx.setQuantity(req.getQuantity());
        tx.setOperator(username);
        tx.setTransactionTime(LocalDateTime.now());
        tx.setManufacturer(req.getManufacturer());
        tx.setSupplier(req.getSupplier());
        tx.setPurchasePrice(req.getPurchasePrice());
        tx.setRemarks(req.getRemarks());
        inventoryTransactionRepository.save(tx);

        // 6. 在庫数を更新
        stock.setCurrentStock(stock.getCurrentStock().add(req.getQuantity()));
        stockMasterRepository.save(stock);

        // 7. 発注小計を加算
        BigDecimal lineTotal = req.getPurchasePrice().multiply(req.getQuantity());
        order.setOrderSubtotal(order.getOrderSubtotal().add(lineTotal));
        purchaseOrderRepository.save(order);

        // 8. 全明細が完了か判定してヘッダーに反映
        boolean allDone = purchaseOrderDetailRepository.findByPurchaseOrder_OrderNo(req.getOrderNo())
        .stream()
        .allMatch(d -> "完了".equals(d.getStatus()));

        if (allDone) {
            order.setStatus("完了");
            purchaseOrderRepository.save(order);
            purchaseOrderRepository.flush();
        }

        // 発行されたトランザクションIDを返す
        System.out.println(tx.getTransactionId());
        return tx.getTransactionId();
    }

    // シンプルな出庫処理
    public Long dispatchInventory(InventoryDispatchRequest req) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);

        StockMaster stock = stockMasterRepository.findByItemCode(req.getItemCode())
                .orElseThrow(() -> new ResourceNotFoundException("在庫が見つかりません"));

        if (stock.getCurrentStock().compareTo(req.getQuantity()) < 0) {
            throw new RuntimeException("在庫が不足しています");
        }

        // 出庫トランザクション登録
        InventoryTransaction tx = new InventoryTransaction();
        tx.setStockItem(stock);
        tx.setTransactionType(TransactionType.MANUAL_DISPATCH);
        tx.setQuantity(req.getQuantity());
        tx.setRemarks(req.getRemarks());
        tx.setOperator(username);
        tx.setTransactionTime(LocalDateTime.now());
        inventoryTransactionRepository.save(tx);

        stock.setCurrentStock(stock.getCurrentStock().subtract(req.getQuantity()));
        stockMasterRepository.save(stock);

        return tx.getTransactionId();
    }

    // 在庫ID指定で在庫履歴取得
    public Page<InventoryTransaction> getTransactionHistory(String itemCode, Pageable pageable) {
        return inventoryTransactionRepository.findByStockItem_ItemCodeOrderByTransactionTimeDesc(itemCode, pageable);
    }

    // 全取引履歴閲覧（検索機能付き）
    public Page<InventoryTransaction> getAllTransactionHistory(
        String itemCode, String operator, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
    
    // 検索条件をログ出力（デバッグ用）
    System.out.printf(
        "🔍 取引履歴検索条件: itemCode='%s', operator='%s', fromDate='%s', toDate='%s'%n",
        itemCode, operator, fromDate, toDate);

    // パラメーターの正規化
    String itemCodeKeyword = (itemCode != null && !itemCode.isBlank()) ? itemCode : null;
    String operatorKeyword = (operator != null && !operator.isBlank()) ? operator : null;
    
    // 期間の自動補完処理
    LocalDateTime fromDateTime = null;
    LocalDateTime toDateTime = null;
    
    if (fromDate != null && toDate != null) {
        // 両方指定されている場合
        fromDateTime = fromDate.atStartOfDay();
        toDateTime = toDate.atTime(23, 59, 59);
        
        // 期間の妥当性チェック
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("開始日は終了日より前の日付を指定してください");
        }
        
        // 最大検索期間のチェック（パフォーマンス対策）
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate);
        if (daysBetween > MAX_SEARCH_DAYS) {
            throw new IllegalArgumentException("検索期間は" + MAX_SEARCH_DAYS + "日以内で指定してください");
        }
        
    } else if (fromDate != null && toDate == null) {
        // fromDateのみ指定 → toDateは今日まで
        fromDateTime = fromDate.atStartOfDay();
        toDateTime = LocalDate.now().atTime(23, 59, 59);
        System.out.println("📅 toDateが未指定のため、今日(" + LocalDate.now() + ")までの範囲で検索します");
        
    } else if (fromDate == null && toDate != null) {
        // toDateのみ指定 → fromDateはデフォルト日数前から
        fromDateTime = toDate.minusDays(DEFAULT_DAYS_BACK).atStartOfDay();
        toDateTime = toDate.atTime(23, 59, 59);
        System.out.println("📅 fromDateが未指定のため、" + toDate.minusDays(DEFAULT_DAYS_BACK) + "からの範囲で検索します");
    }

    // 条件に応じた検索処理
    return executeTransactionSearch(itemCodeKeyword, operatorKeyword, fromDateTime, toDateTime, pageable);
}

    // 検索実行メソッド（可読性向上のため分離）
    private Page<InventoryTransaction> executeTransactionSearch(
            String itemCode, String operator, LocalDateTime fromDateTime, LocalDateTime toDateTime, Pageable pageable) {
        
        // 期間検索の有無を判定
        boolean hasDateRange = (fromDateTime != null && toDateTime != null);
        
        if (itemCode != null && operator != null && hasDateRange) {
            // 全条件指定
            return inventoryTransactionRepository.findByStockItem_ItemCodeContainingAndOperatorContainingAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                itemCode, operator, fromDateTime, toDateTime, pageable);
        } else if (itemCode != null && hasDateRange) {
            // アイテムコード + 期間
            return inventoryTransactionRepository.findByStockItem_ItemCodeContainingAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                itemCode, fromDateTime, toDateTime, pageable);
        } else if (operator != null && hasDateRange) {
            // 操作者 + 期間
            return inventoryTransactionRepository.findByOperatorContainingAndTransactionTimeBetweenOrderByTransactionTimeDesc(
                operator, fromDateTime, toDateTime, pageable);
        } else if (itemCode != null && operator != null) {
            // アイテムコード + 操作者
            return inventoryTransactionRepository.findByStockItem_ItemCodeContainingAndOperatorContainingOrderByTransactionTimeDesc(
                itemCode, operator, pageable);
        } else if (itemCode != null) {
            // アイテムコードのみ
            return inventoryTransactionRepository.findByStockItem_ItemCodeContainingOrderByTransactionTimeDesc(
                itemCode, pageable);
        } else if (operator != null) {
            // 操作者のみ
            return inventoryTransactionRepository.findByOperatorContainingOrderByTransactionTimeDesc(
                operator, pageable);
        } else if (hasDateRange) {
            // 期間のみ
            return inventoryTransactionRepository.findByTransactionTimeBetweenOrderByTransactionTimeDesc(
                fromDateTime, toDateTime, pageable);
        } else {
            // 条件なし（全件） - パフォーマンス対策として最近30日に限定
            LocalDateTime defaultFrom = LocalDate.now().minusDays(DEFAULT_DAYS_BACK).atStartOfDay();
            LocalDateTime defaultTo = LocalDate.now().atTime(23, 59, 59);
            System.out.println("📅 検索条件未指定のため、最近" + DEFAULT_DAYS_BACK + "日間の履歴を表示します");
            return inventoryTransactionRepository.findByTransactionTimeBetweenOrderByTransactionTimeDesc(
                defaultFrom, defaultTo, pageable);
        }
    }

    // 発注商品の納品処理
    @Transactional
    public void receiveFromOrder(InventoryReceiveFromOrderRequest req) {
        String orderNo = req.getOrderNo();
        System.out.println(orderNo);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        req.setOperator(username);

        PurchaseOrder order = purchaseOrderRepository.findByOrderNo(req.getOrderNo())
                .orElseThrow(() -> new ResourceNotFoundException("対象の発注番号が見つかりません"));

        for (InventoryReceiveFromOrderRequest.Item item : req.getItems()) {
            String itemCode = item.getItemCode();

            // 🔽 ここで DB から単価を取得
            BigDecimal purchasePrice = purchaseOrderDetailRepository
                    .findByPurchaseOrder_OrderNoAndItemCode(orderNo, itemCode)
                    .map(PurchaseOrderDetail::getPurchasePrice)
                    .orElse(BigDecimal.ZERO); // fallback（または例外投げる）

            // 発注明細を取得
            PurchaseOrderDetail detail = purchaseOrderDetailRepository
                    .findByPurchaseOrder_OrderNoAndItemCode(req.getOrderNo(), itemCode)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "発注明細が見つかりません（orderNo: " + req.getOrderNo() + ", itemCode: " + itemCode + "）"));

            // ▼ ここで受け入れ済み数量チェックを追加
            BigDecimal receivedSoFar = detail.getReceivedQuantity() != null ? detail.getReceivedQuantity()
                    : BigDecimal.ZERO;
            BigDecimal orderQuantity = detail.getQuantity();
            BigDecimal receivingNow = item.getReceivedQuantity();

            if (receivedSoFar.compareTo(orderQuantity) >= 0) {
                throw new ValidationException("すでに全数が入庫済みのため、これ以上受け入れできません（itemCode: " + itemCode + "）");
            }

            if (receivedSoFar.add(receivingNow).compareTo(orderQuantity) > 0) {
                throw new ValidationException("受け入れ数が発注数を超えています（itemCode: " + itemCode + "）");
            }

            // 在庫マスタを取得
            StockMaster stock = stockMasterRepository.findByItemCode(itemCode)
                    .orElseThrow(() -> new ResourceNotFoundException("在庫が見つかりません"));

            // 入庫数チェック
            BigDecimal totalReceived = detail.getReceivedQuantity().add(item.getReceivedQuantity());

            if (totalReceived.compareTo(detail.getQuantity()) > 0) {
                throw new IllegalArgumentException("受領数が発注数を超えています: " + itemCode);
            }

            // 在庫数更新
            stock.setCurrentStock(stock.getCurrentStock().add(item.getReceivedQuantity()));
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
            tx.setTransactionType(TransactionType.PURCHASE_RECEIVE);
            tx.setOperator(req.getOperator());
            tx.setTransactionTime(LocalDateTime.now());
            tx.setRemarks(item.getRemarks());
            inventoryTransactionRepository.save(tx);
        }

        // 全明細が完了か判定してヘッダーに反映
        boolean allDone = purchaseOrderDetailRepository.findByPurchaseOrder_OrderNo(req.getOrderNo())
                .stream()
                .allMatch(d -> "完了".equals(d.getStatus()));

        if (allDone) {
            order.setStatus("完了");
            purchaseOrderRepository.save(order);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}
