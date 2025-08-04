package com.example.backend.order.repository;

import com.example.backend.entity.PurchaseOrder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

  Optional<PurchaseOrder> findByOrderNoAndOrderType(String oderNo, String oderType);

  // 発注日付順で一覧取得なども可
  Page<PurchaseOrder> findByOrderTypeByOrderDateDesc(String oderType, Pageable pageable);

  // 過去の発注履歴一覧取得を発注タイプごとに表示
  Optional<PurchaseOrder> findByOrderTypeOrderOrderNoAndOrderType(String orderNo, String oderType);

  // 特定期間内全件取得
  Page<PurchaseOrder> findByOrderTypeOrderByCreatedAtBetween(LocalDate fromDate, LocalDate toDate, String oderType, Pageable pageable);

  // 特定期日以降全件取得
  Page<PurchaseOrder> findByOrderTypeOrderByCreatedAtAfter(LocalDate fromDate, String oderType, Pageable pageable);

  // 特定期日以前全件取得
  Page<PurchaseOrder> findByOrderTypeOrderByCreatedAtBefore(LocalDate toDate, String oderType, Pageable pageable);
  

}
