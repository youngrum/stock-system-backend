package com.example.backend.repository;

import com.example.backend.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
  // 発注日付順で一覧取得なども可
  List<PurchaseOrder> findAllByOrderByOrderDateDesc();
}
