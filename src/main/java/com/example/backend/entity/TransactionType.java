package com.example.backend.entity;

public enum TransactionType {
  MANUAL_RECEIVE,     // 手動入庫(inventory/newで新規登録の数量1以上を登録・invenventory/で入庫登録)
  PURCHASE_RECEIVE,   // 発注物の納品による入庫
  ITEM_REGIST,        // inventory/newで新規在庫登録フォームからの登録
  ORDER_REGIST,       // 発注ヘッダーの登録 ※在庫変動なし
  MANUAL_DISPATCH,    // 通常出庫（出荷・廃棄含む）
  RETURN_DISPATCH     // 返品・キャンセルによる出庫 ※未実装
}
