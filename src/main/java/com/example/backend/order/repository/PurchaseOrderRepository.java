package com.example.backend.order.repository;

import com.example.backend.entity.PurchaseOrder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
  // 発注日付順で一覧取得なども可
  List<PurchaseOrder> findAllByOrderByOrderDateDesc();

  // 過去の発注履歴一覧取得
  Optional<PurchaseOrder> findByOrderNo(String orderNo);

  Page<PurchaseOrder> findAll(Pageable pageable);

    // 特定期間内全件取得
    Page<PurchaseOrder> findByCreatedAtBetween(LocalDate fromDate, LocalDate toDate, Pageable pageable);
  
    // 特定期日以降全件取得
    Page<PurchaseOrder> findByCreatedAtAfter(LocalDate fromDate, Pageable pageable);

    // 特定期日以前全件取得
    Page<PurchaseOrder> findByCreatedAtBefore(LocalDate toDate, Pageable pageable);
  

}
