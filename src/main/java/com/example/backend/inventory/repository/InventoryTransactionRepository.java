package com.example.backend.inventory.repository;

import com.example.backend.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

  // ある商品に対する履歴（入庫・出庫問わず）
  List<InventoryTransaction> findByStockItem_ItemCodeOrderByTransactionTimeDesc(String itemCode);

  // 特定の発注番号に紐づく明細（入庫履歴）
  List<InventoryTransaction> findByPurchaseOrder_OrderNo(String orderNo);

  // 入庫のみ抽出
  List<InventoryTransaction> findByTransactionType(InventoryTransaction.TransactionType type);

  // ページング機能
  Page<InventoryTransaction> findByStockItem_ItemCodeOrderByTransactionTimeDesc(String itemCode, Pageable pageable);

  // 全取引履歴閲覧
  Page<InventoryTransaction> findAllByOrderByTransactionTimeDesc(Pageable pageable);

  // アイテムコード部分一致検索（ページング対応）
  Page<InventoryTransaction> findByStockItem_ItemCodeContainingOrderByTransactionTimeDesc(String itemCode, Pageable pageable);

  // 操作者部分一致検索（ページング対応）
  Page<InventoryTransaction> findByOperatorContainingOrderByTransactionTimeDesc(String operator, Pageable pageable);

  // 期間指定検索（ページング対応）
  Page<InventoryTransaction> findByTransactionTimeBetweenOrderByTransactionTimeDesc(LocalDateTime fromDateTime, LocalDateTime toDateTime, Pageable pageable);

  // アイテムコード + 操作者の複合検索（ページング対応）
  Page<InventoryTransaction> findByStockItem_ItemCodeContainingAndOperatorContainingOrderByTransactionTimeDesc(String itemCode, String operator, Pageable pageable);

  // アイテムコード + 期間の複合検索（ページング対応）
  Page<InventoryTransaction> findByStockItem_ItemCodeContainingAndTransactionTimeBetweenOrderByTransactionTimeDesc(String itemCode, LocalDateTime fromDateTime, LocalDateTime toDateTime, Pageable pageable);

  // 操作者 + 期間の複合検索（ページング対応）
  Page<InventoryTransaction> findByOperatorContainingAndTransactionTimeBetweenOrderByTransactionTimeDesc(String operator, LocalDateTime fromDateTime, LocalDateTime toDateTime, Pageable pageable);

  // アイテムコード + 操作者 + 期間の複合検索（ページング対応）
  Page<InventoryTransaction> findByStockItem_ItemCodeContainingAndOperatorContainingAndTransactionTimeBetweenOrderByTransactionTimeDesc(String itemCode, String operator, LocalDateTime fromDateTime, LocalDateTime toDateTime, Pageable pageable);

}
