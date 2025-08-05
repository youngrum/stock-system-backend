package com.example.backend.order.repository;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrder.OrderType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    // 発注番号と発注タイプで取得
    Optional<PurchaseOrder> findByOrderNoAndOrderType(String orderNo, OrderType orderType);

    // 発注タイプと発注日付の新しい順で一覧取得
    Page<PurchaseOrder> findByOrderTypeOrderByOrderDateDesc(OrderType orderType, Pageable pageable);

    // 発注タイプと特定期間内全件取得
    Page<PurchaseOrder> findByOrderTypeAndCreatedAtBetweenOrderByCreatedAtDesc(OrderType orderType, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    // 発注タイプと特定期日以降全件取得
    Page<PurchaseOrder> findByOrderTypeAndCreatedAtAfterOrderByCreatedAtDesc(OrderType orderType, LocalDate fromDate, Pageable pageable);

    // 発注タイプと特定期日以前全件取得
    Page<PurchaseOrder> findByOrderTypeAndCreatedAtBeforeOrderByCreatedAtDesc(OrderType orderType, LocalDate toDate, Pageable pageable);
}
