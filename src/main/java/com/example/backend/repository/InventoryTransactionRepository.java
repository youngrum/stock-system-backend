package com.example.backend.repository;

import com.example.backend.entity.InventoryTransaction;
import com.example.backend.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

  // ある商品に対する履歴（入庫・出庫問わず）
  List<InventoryTransaction> findByStockItem_ItemCodeOrderByTransactionTimeDesc(String itemCode);

  // 特定の発注番号に紐づく明細（入庫履歴）
  List<InventoryTransaction> findByPurchaseOrder_OrderNo(String orderNo);

  // 入庫のみ抽出
  List<InventoryTransaction> findByTransactionType(TransactionType type);

  // ページング機能
  Page<InventoryTransaction> findByStockItemItemCodeOrderByTransactionTimeDesc(String itemCode, Pageable pageable);
}
