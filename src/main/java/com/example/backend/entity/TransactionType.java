package com.example.backend.entity;

public enum TransactionType {
  MANUAL_RECEIVE,     // 手動入庫
  PURCHASE_RECEIVE,   // 発注品の納品による入庫
  MANUAL_DISPATCH,    // 通常出庫（出荷・廃棄含む）
  RETURN_DISPATCH     // 返品・キャンセルによる出庫
}
