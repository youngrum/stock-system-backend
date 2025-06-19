package com.example.backend.order.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.inventory.repository.StockMasterRepository;
import com.example.backend.inventory.repository.InventoryTransactionRepository;
import com.example.backend.order.dto.PurchaseOrderRequest;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;
import com.example.backend.order.repository.PurchaseOrderRepository;
import com.example.backend.common.service.ItemCodeGenerator;
import com.example.backend.common.service.OrderNumberGenerator;
import com.example.backend.common.service.TransactionIdGenerator;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // // 手動コンストラクタは不要
public class PurchaseOrderService {

  private final PurchaseOrderRepository purchaseOrderRepository;
  private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
  private final StockMasterRepository stockMasterRepository;
  private final InventoryTransactionRepository inventoryTransactionRepository;
  private final ItemCodeGenerator itemCodeGenerator;
  private final TransactionIdGenerator transactionIdGenerator;
  private final OrderNumberGenerator orderNumberGenerator;

  /**
   * 発注登録を行う
   *
   * @param req 発注リクエスト
   * @return 発注番号
   */
  @Transactional
  public String registerOrder(PurchaseOrderRequest req) {
    // 1. 実行者をセット
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    req.setOperator(username);
    System.out.println(req.getOperator());

    // 2-1. 発注ヘッダー作成 id取得のため
    PurchaseOrder header = new PurchaseOrder();
    
    // 2-2. NUMBERING_MASTER テーブルベースで orderNo を直接採番
    String orderNo = orderNumberGenerator.generateOrderNo();
    header.setOrderNo(orderNo);

    header.setSupplier(req.getSupplier());
    header.setShippingFee(req.getShippingFee());
    header.setOperator(username);
    header.setRemarks(req.getRemarks());
    header.setOrderDate(LocalDate.now());
    header.setOrderSubtotal(BigDecimal.ZERO);
    purchaseOrderRepository.save(header);

    purchaseOrderRepository.flush(); // DBに反映
    System.out.println("発注No登録完了: " + header.getOrderNo());

    // 3. 明細登録
    BigDecimal total = BigDecimal.ZERO;

    for (PurchaseOrderRequest.Detail d : req.getDetails()) {
      System.out.println("▶ 処理中 item: " + d.getItemName());
      StockMaster stock;

      // ---------- 柔軟対応の在庫判定 ----------
      if (d.getItemCode() != null && !d.getItemCode().isBlank()) {
        System.out.println("▶ itemCode指定あり → 在庫確認中: " + d.getItemCode());
        // 既存の itemCode を直接指定された場合（既存在庫）
        stock = stockMasterRepository.findByItemCode(d.getItemCode())
            .orElseThrow(() -> new ResourceNotFoundException("itemCodeが存在しません: " + d.getItemCode()));
      } else {
        System.out.println("▶ itemCodeなし → 型番＋品名で検索: " + d.getModelNumber() + " / " + d.getItemName());
        // 型番 + 品名で既存在庫を検索（完全一致）
        stock = stockMasterRepository
            .findByModelNumberAndItemName(d.getModelNumber(), d.getItemName())
            .orElseGet(() -> {
              // 該当がなければ新規登録
              StockMaster s = new StockMaster();
              System.out.println("▶ 新規登録 : " + d);
              s.setItemName(d.getItemName());
              s.setModelNumber(d.getModelNumber());
              s.setCategory(d.getCategory());
              s.setLocation(d.getLocation());
              System.out.println("保管先："+d.getLocation());
              s.setCurrentStock(BigDecimal.ZERO);
              StockMaster saved = stockMasterRepository.save(s);
              String code = itemCodeGenerator.generateItemCode(saved.getId());
              // itemCodeを生成して保存
              saved.setItemCode(code);
              stockMasterRepository.flush();
              System.out.println("▶ 新規登録 itemCode: " + saved.getItemCode());
              return saved;
            });
      }

      System.out.println("▶ 明細登録準備完了 → " + stock.getItemCode());

      // ---------- 発注明細の登録 ----------
      PurchaseOrderDetail detail = new PurchaseOrderDetail();
      detail.setItemCode(stock.getItemCode());
      detail.setItemName(stock.getItemName());
      detail.setModelNumber(stock.getModelNumber());
      detail.setCategory(stock.getCategory());
      detail.setQuantity(d.getQuantity());
      detail.setPurchasePrice(d.getPurchasePrice());
      detail.setReceivedQuantity(BigDecimal.ZERO);
      detail.setStatus("未入庫");
      detail.setRemarks(d.getRemarks());
      detail.setPurchaseOrder(header);
      purchaseOrderDetailRepository.save(detail);

      total = total.add(d.getQuantity().multiply(d.getPurchasePrice()));
      System.out.println("total:" + total);

      // 4. 入庫トランザクション登録
      InventoryTransaction tx = InventoryTransaction.createTransactionForPurchaseOrder(
          username, stock, header, req, d, transactionIdGenerator, inventoryTransactionRepository);
      System.out.println(tx);
      inventoryTransactionRepository.save(tx);
    }

    header.setOrderSubtotal(total);
    purchaseOrderRepository.save(header);
    return orderNo;
  }
}
