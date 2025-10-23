package com.example.backend.order.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.entity.PurchaseOrder;
import com.example.backend.order.dto.PurchaseOrderRequest;
import com.example.backend.order.repository.PurchaseOrderRepository;
import com.example.backend.common.service.OrderNumberGenerator;
import com.example.backend.order.service.handler.InventoryOrderHandler;
import com.example.backend.order.service.handler.AssetOrderHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final InventoryOrderHandler inventoryOrderHandler;
    private final AssetOrderHandler assetOrderHandler;

    /**
     * 発注登録を行う
     *
     * @param req 発注リクエスト
     * @return 発注番号
     * @throws IllegalArgumentException 不適切なorderTypeまたは明細タイプが含まれていた場合
     */
    @Transactional
    public String registerOrder(PurchaseOrderRequest req) {
        // orderTypeを含まないリクエストははじく
        validateOrderRequest(req);

        // 実行者をセット
        String username = getCurrentUsername();
        req.setOperator(username);
        System.out.println("実行者: " + req.getOperator());

        // 発注ヘッダー作成
        PurchaseOrder header = createOrderHeader(req, username);

        // 発注タイプに応じた処理を委譲
        BigDecimal totalAmount = processOrderDetails(header, req, username);

        // 発注ヘッダーの小計を更新
        header.setOrderSubtotal(totalAmount);
        purchaseOrderRepository.save(header);

        System.out.println("発注登録が正常に完了しました。発注No: " + header.getOrderNo());
        return header.getOrderNo();
    }

    private void validateOrderRequest(PurchaseOrderRequest req) {
        if (req.getOrderType() == null || req.getOrderType().isBlank()) {
            throw new IllegalArgumentException("orderTypeは必須です。");
        }
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private PurchaseOrder createOrderHeader(PurchaseOrderRequest req, String username) {
        PurchaseOrder header = new PurchaseOrder();

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
        header.setOrderType(PurchaseOrder.OrderType.valueOf(req.getOrderType()));

        purchaseOrderRepository.save(header);
        // トランザクション中にDB更新
        purchaseOrderRepository.flush();

        System.out.println("発注ヘッダー登録完了: OrderNo=" + header.getOrderNo());
        return header;
    }

    private BigDecimal processOrderDetails(PurchaseOrder header, PurchaseOrderRequest req, String username) {
        return switch (req.getOrderType().toUpperCase()) {
            case "INVENTORY" -> {
                System.out.println("在庫発注として処理を開始");
                yield inventoryOrderHandler.processOrderDetails(header, req.getDetails(), username);
            }
            case "ASSET" -> {
                System.out.println("設備/校正発注として処理を開始");
                yield assetOrderHandler.processOrderDetails(header, req.getDetails(), username);
            }
            default -> throw new IllegalArgumentException("無効な発注タイプ: " + req.getOrderType());
        };
    }
}