package com.example.backend.order.repository;

import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.entity.PurchaseOrderDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderDetailRepository
    extends JpaRepository<PurchaseOrderDetail, PurchaseOrderDetailId> {

  // 特定の発注に属する明細一覧を取得
  List<PurchaseOrderDetail> findByOrderNo(String orderNo);

  // 発注番号 + 商品コードで1明細を取得（省略可、JpaRepositoryが自動生成）
  Optional<PurchaseOrderDetail> findByOrderNoAndItemCode(String orderNo, String itemCode);
}
