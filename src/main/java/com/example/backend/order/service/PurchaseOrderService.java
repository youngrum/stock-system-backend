package com.example.backend.order.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.entity.PurchaseOrderDetail;
import com.example.backend.entity.StockMaster;
import com.example.backend.entity.InventoryTransaction;
import com.example.backend.entity.AssetMaster;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.inventory.repository.StockMasterRepository;
import com.example.backend.inventory.repository.InventoryTransactionRepository;
import com.example.backend.order.dto.PurchaseOrderRequest;
import com.example.backend.order.repository.PurchaseOrderDetailRepository;
import com.example.backend.order.repository.PurchaseOrderRepository;
import com.example.backend.asset.repository.AssetMasterRepository;
import com.example.backend.common.service.ItemCodeGenerator;
import com.example.backend.common.service.OrderNumberGenerator;
import com.example.backend.common.service.TransactionIdGenerator;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // // 手動コンストラクタは不要
public class PurchaseOrderService {

  private final PurchaseOrderRepository purchaseOrderRepository;
  private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;
  private final StockMasterRepository stockMasterRepository;
  private final AssetMasterRepository assetMasterRepository;
  private final InventoryTransactionRepository inventoryTransactionRepository;
  private final ItemCodeGenerator itemCodeGenerator;
  private final TransactionIdGenerator transactionIdGenerator;
  private final OrderNumberGenerator orderNumberGenerator;

  /**
   * 発注登録を行う
   *
   * @param req 発注リクエスト
   * @return 発注番号
   * @throws IllegalArgumentException 不適切なorderTypeまたは明細タイプが含まれていた場合
   */
  @Transactional
  public String registerOrder(PurchaseOrderRequest req) {

    if (req.getOrderType() == null || req.getOrderType().isBlank()) {
      throw new IllegalArgumentException("orderTypeは必須です。");
    }

    // 1. 実行者をセット
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    req.setOperator(username);
    System.out.println("実行者: " + req.getOperator());

    // 2. 発注ヘッダー作成
    PurchaseOrder header = createOrderHeader(req, username);

    // 3. 明細登録
    BigDecimal totalAmountFromDetails = BigDecimal.ZERO;

    if ("INVENTORY".equalsIgnoreCase(req.getOrderType())) {
      System.out.println("在庫発注として処理を開始");
      totalAmountFromDetails = processInventoryOrderDetails(header, req.getDetails(), username);
    } else if ("ASSET".equalsIgnoreCase(req.getOrderType())) {
      System.out.println("設備/校正発注として処理を開始");
      totalAmountFromDetails = processAssetOrderDetails(header, req.getDetails(), username);
    } else {
      throw new IllegalArgumentException("無効な発注タイプ: " + req.getOrderType());
    }

    // 4. 発注ヘッダーの小計を更新
    header.setOrderSubtotal(totalAmountFromDetails);
    purchaseOrderRepository.save(header);
    System.out.println("発注登録が正常に完了しました。発注No: " + header.getOrderNo());
    return header.getOrderNo();
  }
  
    private PurchaseOrder createOrderHeader(PurchaseOrderRequest req, String username) {
    PurchaseOrder header = new PurchaseOrder();

    // NUMBERING_MASTER テーブルベースで orderNo を直接採番
    String orderNo = orderNumberGenerator.generateOrderNo();
    header.setOrderNo(orderNo);

    header.setSupplier(req.getSupplier());
    header.setShippingFee(req.getShippingFee());
    header.setOperator(username);
    header.setRemarks(req.getRemarks());
    header.setOrderDate(LocalDate.now());
    header.setOrderSubtotal(BigDecimal.ZERO);
    header.setCalibrationCert(req.getCalibrationCert());
    header.setTraceabilityCert(req.getTraceabilityCert());
    header.setOrderType(req.getOrderType());

    purchaseOrderRepository.save(header);
    purchaseOrderRepository.flush();
    System.out.println("発注ヘッダー登録完了: OrderNo=" + header.getOrderNo());
    return header;
  }

  /**
   * 在庫発注明細の具体的な処理。すべての明細がITEMタイプであることを前提。
   */
  private BigDecimal processInventoryOrderDetails(
      PurchaseOrder header, List<PurchaseOrderRequest.Detail> details, String username) {
    BigDecimal currentTotal = BigDecimal.ZERO;
    for (PurchaseOrderRequest.Detail d : details) {
      // 在庫発注なので、ITEMタイプ以外の明細やネストされたサービスは許可しない
      if (!"ITEM".equalsIgnoreCase(d.getItemType())) {
        throw new IllegalArgumentException("在庫発注にはITEMタイプの明細のみが許可されます: " + d.getItemType());
      }
      if (d.getServices() != null && !d.getServices().isEmpty()) {
        throw new IllegalArgumentException("在庫発注のITEM明細にはネストされたサービスを含めることはできません。");
      }

      System.out.println("在庫ITEM明細処理中: " + d.getItemName());

      StockMaster stock;
       // itemCodeから在庫の存在チェック なければ型番＋品名で検索 在庫なければ新規登録
      if (d.getItemCode() != null && !d.getItemCode().isBlank()) {
        System.out.println("▶ itemCode指定あり: " + d.getItemCode());
        stock = stockMasterRepository.findByItemCode(d.getItemCode())
            .orElseThrow(() -> new ResourceNotFoundException("itemCodeが存在しません: " + d.getItemCode()));
      } else {
        System.out.println("▶ itemCodeなし → 型番＋品名で検索: " + d.getModelNumber() + " / " + d.getItemName());
        stock = stockMasterRepository
            .findByModelNumberAndItemName(d.getModelNumber(), d.getItemName())
            .orElseGet(() -> {
              StockMaster s = new StockMaster();
              System.out.println("▶ 新規登録 : " + d);
              s.setItemName(d.getItemName());
              s.setModelNumber(d.getModelNumber());
              s.setCategory(d.getCategory());
              s.setLocation(d.getLocation());
              System.out.println("保管先："+d.getLocation());
              s.setCurrentStock(BigDecimal.ZERO);
              StockMaster saved = stockMasterRepository.save(s);
              // itemCodeを生成
              String code = itemCodeGenerator.generateItemCode(saved.getId());
              saved.setItemCode(code);

              stockMasterRepository.flush(); // itemCodeを更新して保存
              System.out.println("▶ 新規登録 itemCode: " + saved.getItemCode());
              return saved;
            });
      }

      System.out.println("▶ 明細登録準備完了 → " + stock.getItemCode());

      // 発注明細の登録
      PurchaseOrderDetail detail = new PurchaseOrderDetail();
      detail.setItemCode(stock.getItemCode());
      detail.setItemType(d.getItemType());
      detail.setItemName(stock.getItemName());
      detail.setModelNumber(stock.getModelNumber());
      detail.setCategory(stock.getCategory());
      detail.setQuantity(d.getQuantity());
      detail.setPurchasePrice(d.getPurchasePrice());
      detail.setReceivedQuantity(BigDecimal.ZERO);
      detail.setStatus("未入庫");
      detail.setRemarks(d.getRemarks());
      detail.setPurchaseOrder(header);

      // サービス関連カラムはNULL 在庫発注に不要
      detail.setServiceType(null);
      detail.setRelatedAsset(null);
      detail.setLinkedId(null);

      purchaseOrderDetailRepository.save(detail);

      currentTotal = currentTotal.add(d.getQuantity().multiply(d.getPurchasePrice()));
      System.out.println("現在の小計:" + currentTotal);

      // 4. 入庫トランザクション登録
      // 在庫品の場合のみ、InventoryTransactionを作成
      InventoryTransaction tx = InventoryTransaction.createTransactionForPurchaseOrder(
          username, stock, header, d, transactionIdGenerator, inventoryTransactionRepository);
      System.out.println(tx);
      inventoryTransactionRepository.save(tx);
    }
    return currentTotal;
  }

  /**
   * ITEM（設備）とSERVICE（校正・修理）発注明細の処理
   */
private BigDecimal processAssetOrderDetails(
    PurchaseOrder header, List<PurchaseOrderRequest.Detail> details, String username) {
    BigDecimal currentTotal = BigDecimal.ZERO;
    for (PurchaseOrderRequest.Detail d : details) {
      System.out.println("▶ 設備/校正明細処理中: " + d.getItemName() + " (itemType: " + d.getItemType() + ")");

      PurchaseOrderDetail detail = new PurchaseOrderDetail();
      detail.setItemName(d.getItemName());
      detail.setQuantity(d.getQuantity());
      detail.setPurchasePrice(d.getPurchasePrice());
      detail.setRemarks(d.getRemarks());
      detail.setStatus("未入庫");
      detail.setItemType(d.getItemType());

      if ("ITEM".equalsIgnoreCase(d.getItemType())) {
        // ITEMタイプの明細処理 (設備品 - 発注情報として記録)
        // ここではAssetMasterは作成しない。
        detail.setItemCode(d.getItemCode());
        detail.setModelNumber(d.getModelNumber());
        detail.setCategory(d.getCategory());
        detail.setReceivedQuantity(BigDecimal.ZERO); // 設備も受け取り数量を追跡

        // AssetMaster.idは、納品後に別途登録される際に紐付けられるため、ここではnull
        detail.setAssetId(null);

        // サービス関連カラムはNULL
        detail.setServiceType(null);
        detail.setRelatedAsset(null);
        detail.setLinkedId(null);

        // AssetTransactionでトランザクションレコード発行

      } else if ("SERVICE".equalsIgnoreCase(d.getItemType())) {
        // ---------- SERVICEタイプの明細処理 (独立したサービス または 既存資産へのサービス) ----------
        detail.setServiceType(d.getServiceType());

        // 既存資産へのサービスの場合、relatedAssetId (Long) を使ってAssetMasterオブジェクトを取得しセット
        if (d.getRelatedAssetId() != null) { // DTOにrelatedAssetIdが提供されている場合
            AssetMaster existingAsset = assetMasterRepository.findById(d.getRelatedAssetId())
                                        .orElseThrow(() -> new ResourceNotFoundException("対象の既存設備が見つかりません。ID: " + d.getRelatedAssetId()));
            detail.setRelatedAsset(existingAsset); // AssetMasterオブジェクトをセット
        } else {
            detail.setRelatedAsset(null);
        }

        detail.setLinkedId(null); // 独立したサービスなのでlinkedIdはなし

        // 物品関連カラムはNULL
        detail.setItemCode(null);
        detail.setModelNumber(null);
        detail.setCategory(null);
        detail.setReceivedQuantity(null);
        detail.setAssetId(null); // サービスにはassetIdは不要

        // サービス明細の場合、InventoryTransactionは作成しない（在庫変動がないため）
      } else {
        throw new IllegalArgumentException("無効なitemTypeが指定されました: " + d.getItemType());
      }

      // トップレベルのPurchaseOrderDetailを保存
      PurchaseOrderDetail savedTopLevelDetail = purchaseOrderDetailRepository.save(detail);
      System.out.println("トップレベル明細登録完了: ID=" + savedTopLevelDetail.getId() + ", itemType=" + savedTopLevelDetail.getItemType());

      currentTotal = currentTotal.add(d.getQuantity().multiply(d.getPurchasePrice()));

      // ITEMタイプで、かつネストされたサービスがある場合、ここで処理
      if ("ITEM".equals(savedTopLevelDetail.getItemType()) && d.getServices() != null && !d.getServices().isEmpty()) {
        System.out.println("ネストされたサービス明細を処理中...");
        for (PurchaseOrderRequest.ServiceRequest serviceReq : d.getServices()) {
          PurchaseOrderDetail serviceDetail = new PurchaseOrderDetail();
          serviceDetail.setItemType("SERVICE");
          serviceDetail.setServiceType(serviceReq.getServiceType());
          serviceDetail.setItemName(serviceReq.getItemName());
          serviceDetail.setQuantity(serviceReq.getQuantity());
          serviceDetail.setPurchasePrice(serviceReq.getPurchasePrice() != null ? serviceReq.getPurchasePrice() : BigDecimal.ZERO);

          serviceDetail.setStatus("未入庫");

          serviceDetail.setLinkedId(savedTopLevelDetail.getId());
          // ネストされたサービスの場合、親のITEM (設備) のAssetIdはまだ確定していないため、関連付けは行わない
          serviceDetail.setRelatedAsset(null); 

          // 物品関連カラムはNULL
          serviceDetail.setItemCode(null);
          serviceDetail.setModelNumber(null);
          serviceDetail.setCategory(null);
          serviceDetail.setReceivedQuantity(null);
          serviceDetail.setAssetId(null);

          purchaseOrderDetailRepository.save(serviceDetail);
          System.out.println("  ▶ ネストされたサービス明細登録完了: ID=" + serviceDetail.getId());

          currentTotal = currentTotal.add(serviceReq.getQuantity().multiply(serviceReq.getPurchasePrice()));
        }
      }
    }
    return currentTotal;
  }
}