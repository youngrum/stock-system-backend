// InventoryTransaction.java
package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.backend.common.service.TransactionIdGenerator;
import com.example.backend.inventory.repository.InventoryTransactionRepository;
import com.example.backend.inventory.dto.InventoryDispatchRequest;
import com.example.backend.inventory.dto.InventoryReceiveRequest;
import com.example.backend.inventory.dto.StockMasterRequest;
import com.example.backend.order.dto.InventoryReceiveFromOrderRequest;
import com.example.backend.order.dto.PurchaseOrderRequest;

@Entity
@Table(name = "inventory_transaction")
@Data
public class InventoryTransaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // サロゲートキー

  @Column(name = "transaction_id")
  private String transactionId;

  // NULL許容: 出庫にはorderNoがない可能性あり
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_no", referencedColumnName = "order_no")
  private PurchaseOrder purchaseOrder;

  @ManyToOne(fetch = FetchType.LAZY) // 他のエンティティ(StockMaster)の参照を宣言
  // InventoryTransactionテーブルのitem_codeを使ってSTOCK_MASTERのitem_codeを外部参照
  @JoinColumn(name = "item_code", referencedColumnName = "item_code", nullable = false)
  private StockMaster stockItem;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 32)
  private TransactionType transactionType;

  @Column(name = "quantity", nullable = false)
  private BigDecimal quantity;

  @Column(name = "operator", nullable = false, length = 64)
  private String operator;

  @Column(name = "transaction_time", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  private LocalDateTime transactionTime;

  @Column(name = "manufacturer", length = 64)
  private String manufacturer;

  @Column(name = "supplier", length = 64)
  private String supplier;

  @Column(name = "purchase_price", precision = 10, scale = 2)
  private BigDecimal purchasePrice;

  @Column(name = "remarks", length = 255)
  private String remarks;

  // ===== ファクトリメソッド =====

  /**
   * 新規在庫登録
   */
  public static InventoryTransaction createReceiveTransaction(StockMaster stockItem, StockMasterRequest req,
      String username) {
    
    // トランザクションIDを生成
    TransactionIdGenerator generator = new TransactionIdGenerator();
    String txNo = generator.generateTxNo();

    // 登録した数量によってトランザクションタイプを分岐
    InventoryTransaction tx = new InventoryTransaction();
    tx.setStockItem(stockItem);
    tx.setTransactionId(txNo);
    // 登録した数量によってトランザクションタイプを分岐
    if (req.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
      tx.setTransactionType(InventoryTransaction.TransactionType.ITEM_REGIST);
      tx.setQuantity(BigDecimal.ZERO); // 明示的に0を設定 (一応)
    } else {
      tx.setTransactionType(InventoryTransaction.TransactionType.MANUAL_RECEIVE);
      tx.setQuantity(req.getCurrentStock());
    }
    tx.setOperator(username);
    tx.setRemarks(req.getRemarks());
    tx.setPurchaseOrder(null);
    tx.setTransactionTime(LocalDateTime.now());
    return tx;
  }

  /**
   * @param stock    在庫マスタ
   * @param order    発注情報
   * @param req      入庫リクエスト情報
   * @param operator オペレーター名
   * @return 入庫処理 (モーダルからの手動入庫)
   */
  public static InventoryTransaction createTransactionForManualReceive(
      StockMaster stock, PurchaseOrder order, InventoryReceiveRequest req, String operator) {

    // トランザクションIDを生成
    TransactionIdGenerator generator = new TransactionIdGenerator();
    String txNo = generator.generateTxNo();

    InventoryTransaction tx = new InventoryTransaction();
    tx.setStockItem(stock);
    tx.setPurchaseOrder(order);
    tx.setTransactionId(txNo);
    tx.setTransactionType(TransactionType.MANUAL_RECEIVE);
    tx.setQuantity(req.getQuantity());
    tx.setOperator(operator);
    tx.setTransactionTime(LocalDateTime.now());
    tx.setManufacturer(req.getManufacturer());
    tx.setSupplier(req.getSupplier());
    tx.setPurchasePrice(req.getPurchasePrice());
    tx.setRemarks(req.getRemarks());
    return tx;
  }

  /**
   * 出庫トランザクションを生成
   * 
   * @param stockItem 在庫マスタ
   * @param req       出庫リクエスト情報
   * @param username  オペレーター名
   * @return 入庫処理 (モーダルからの手動入庫)
   */
  public static InventoryTransaction createTransactionforDispatch(StockMaster stockItem,
      InventoryDispatchRequest req, String username) {

    // トランザクションIDを生成
    TransactionIdGenerator generator = new TransactionIdGenerator();
    String txNo = generator.generateTxNo();
        
    // 出庫トランザクション登録
    InventoryTransaction tx = new InventoryTransaction();
    tx.setTransactionId(txNo);
    tx.setStockItem(stockItem);
    tx.setTransactionType(InventoryTransaction.TransactionType.MANUAL_DISPATCH);
    tx.setQuantity(req.getQuantity());
    tx.setRemarks(req.getRemarks());
    tx.setOperator(username);
    tx.setTransactionTime(LocalDateTime.now());
    return tx;
  }

  /**
   * 発注登録済み商品の納品＝入庫トランザクションを生成
   * 
   * @param stock         在庫マスタ
   * @param item          個々の発注商品
   * @param order         発注情報
   * @param req           リクエスト情報
   * @param purchasePrice 購入価格 サービス層でDB から単価を取得
   * @param operator      オペレーター名
   * @return 入庫トランザクション
   */
  public static InventoryTransaction createTransactionForPurchaseReceive(
      StockMaster stock,
      InventoryReceiveFromOrderRequest.Item item,
      PurchaseOrder order,
      InventoryReceiveFromOrderRequest req,
      BigDecimal purchasePrice,
      String operator) {

    // トランザクションIDを生成
    TransactionIdGenerator generator = new TransactionIdGenerator();
    String txNo = generator.generateTxNo();

    InventoryTransaction tx = new InventoryTransaction();
    tx.setTransactionId(txNo);
    tx.setPurchaseOrder(order);
    tx.setStockItem(stock);
    tx.setQuantity(item.getReceivedQuantity());
    tx.setPurchasePrice(purchasePrice);
    tx.setTransactionType(TransactionType.PURCHASE_RECEIVE);
    tx.setOperator(operator);
    tx.setTransactionTime(LocalDateTime.now());
    tx.setRemarks(item.getRemarks());
    return tx;
  }

  /**
   * 発注登録 (納品前処理)
   * 
   * @param operator      オペレーター名
   * @param stock         在庫マスタ
   * @param order         発注情報
   * @param req           リクエスト情報
   * @param purchasePrice 購入価格 サービス層でDB から単価を取得
   * @param detail        発注明細
   * @return 入庫トランザクション
   */
  public static InventoryTransaction createTransactionForPurchaseOrder(
      String operator,
      StockMaster stock,
      PurchaseOrder order,
      PurchaseOrderRequest req,
      BigDecimal purchasePrice,
      PurchaseOrderRequest.Detail detail) {

    // トランザクションIDを生成
    TransactionIdGenerator generator = new TransactionIdGenerator();
    String txNo = generator.generateTxNo();

    InventoryTransaction tx = new InventoryTransaction();
    tx.setTransactionId(txNo);
    tx.setPurchaseOrder(order);
    tx.setStockItem(stock);
    tx.setQuantity(detail.getQuantity());
    tx.setPurchasePrice(purchasePrice);
    tx.setTransactionType(TransactionType.ORDER_REGIST);
    tx.setOperator(operator);
    tx.setTransactionTime(LocalDateTime.now());
    tx.setRemarks(detail.getRemarks());
    return tx;
  }

  public static InventoryTransaction createTransactionForCsv(
      String[] data, StockMaster stock, String username, TransactionIdGenerator transactionIdGenerator,InventoryTransactionRepository inventoryTransactionRepository) {

    // トランザクションIDを生成
    String txNo = transactionIdGenerator.generateTxNo();
    InventoryTransaction tx = new InventoryTransaction();
    
    tx.setTransactionId(txNo);
    tx.setStockItem(stock);
    tx.setTransactionType(TransactionType.ITEM_REGIST);
    tx.setManufacturer(data[3]);  // CSVのメーカー列manufacturerを使用
    tx.setSupplier(data[4]);  // CSVの仕入れ先列suplierを使用
    tx.setQuantity(new BigDecimal(data[5])); // CSVの数量列current_stockを使用
    tx.setRemarks(data[7]); // CSVの備考列remarksを使用
    tx.setOperator(username);
    tx.setTransactionTime(LocalDateTime.now());
    inventoryTransactionRepository.save(tx);
    return tx;
  }

  public enum TransactionType {
    MANUAL_RECEIVE, // 手動入庫(inventory/newで新規登録の数量1以上を登録・invenventory/で入庫登録)
    PURCHASE_RECEIVE, // 発注物の納品による入庫
    ITEM_REGIST, // inventory/newで新規在庫登録フォームからの登録
    ORDER_REGIST, // 発注ヘッダーの登録 ※在庫変動なし
    MANUAL_DISPATCH, // 通常出庫（出荷・廃棄含む）
    RETURN_DISPATCH // 返品・キャンセルによる出庫 ※未実装
  }
}