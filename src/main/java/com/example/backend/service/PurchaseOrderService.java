package com.example.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.backend.dto.PurchaseOrderRequest;
import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.entity.StockMaster;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.PurchaseOrderRepository;
import com.example.backend.repository.PurchaseOrderDetailRepository;
import com.example.backend.repository.StockMasterRepository;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

  private final PurchaseOrderRepository purchaseOrderRepository;
  private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
  private final StockMasterRepository stockMasterRepository;

  @Transactional
  public String registerOrder(PurchaseOrderRequest req) {
    // 1. 発注番号を生成
    String orderNo = generateNewOrderNo();

    // 2. 発注ヘッダー作成
    PurchaseOrder header = new PurchaseOrder();
    header.setOrderNo(orderNo);
    header.setSupplier(req.getSupplier());
    header.setShippingFee(req.getShippingFee());
    header.setOperator(req.getOperator());
    header.setRemarks(req.getRemarks());
    header.setOrderDate(LocalDate.now());
    header.setOrderSubtotal(BigDecimal.ZERO);
    purchaseOrderRepository.save(header);

    // 3. 明細登録
    BigDecimal total = BigDecimal.ZERO;

    for (PurchaseOrderRequest.Detail d : req.getDetails()) {
      System.out.println("▶ 処理中 item: " + d.getItemName());
      StockMaster stock;

      // ---------- 柔軟対応の在庫判定 ----------
      if (d.getItemCode() != null && !d.getItemCode().isBlank()) {
        System.out.println("▶ itemCode指定あり → 在庫確認中: " + d.getItemCode());
        // 既存の itemCode を直接指定された場合（既存在庫）
        stock = stockMasterRepository.findById(d.getItemCode())
            .orElseThrow(() -> new ResourceNotFoundException("itemCodeが存在しません: " + d.getItemCode()));
      } else {
        System.out.println("▶ itemCodeなし → 型番＋品名で検索: " + d.getModelNumber() + " / " + d.getItemName());
        // 型番 + 品名で既存在庫を検索（完全一致）
        stock = stockMasterRepository
            .findByModelNumberAndItemName(d.getModelNumber(), d.getItemName())
            .orElseGet(() -> {
              // 該当がなければ新規登録
              StockMaster s = new StockMaster();
              s.setItemCode(generateItemCode());
              s.setItemName(d.getItemName());
              s.setModelNumber(d.getModelNumber());
              s.setCategory(d.getCategory());
              s.setCurrentStock(0);
              StockMaster saved = stockMasterRepository.save(s);
              System.out.println("▶ 新規登録 itemCode: " + saved.getItemCode());
              return saved;
            });
      }

      System.out.println("▶ 明細登録準備完了 → " + stock.getItemCode());

      // ---------- 発注明細の登録 ----------
      PurchaseOrderDetail detail = new PurchaseOrderDetail();
      detail.setOrderNo(orderNo);
      detail.setItemCode(stock.getItemCode());
      detail.setItemName(stock.getItemName());
      detail.setModelNumber(stock.getModelNumber());
      detail.setCategory(stock.getCategory());
      detail.setQuantity(d.getQuantity());
      detail.setPurchasePrice(d.getPurchasePrice());
      detail.setReceivedQuantity(BigDecimal.ZERO);
      detail.setStatus("未入庫");
      detail.setRemarks(d.getRemarks());
      purchaseOrderDetailRepository.save(detail);

      total = total.add(d.getQuantity().multiply(d.getPurchasePrice()));
    }

    header.setOrderSubtotal(total);
    purchaseOrderRepository.save(header);
    return orderNo;
  }

  private String generateNewOrderNo() {
    return "PO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
        + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private String generateItemCode() {
    return "I" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }
}
