package com.example.backend.order.repository;

import com.example.backend.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderHistoryRepository extends JpaRepository<PurchaseOrder, Long> {

    // 特定のorderNoで検索（存在しない場合にOptional.emptyを返す）
    Optional<PurchaseOrder> findByOrderNo(String orderNo);

}
