package com.example.backend.asset.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.asset.service.AssetService;
import com.example.backend.entity.AssetMaster;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "設備管理API", description = "設備情報取得・管理")
public class AssetController {
    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "在庫検索 全件取得時は ID, 品名, カテゴリー, 型番を空にする")
    @GetMapping("/asset/search")
      public ResponseEntity<?> searchInventory(
      @Parameter(description = "管理番号") @RequestParam(required = false) String assetCode,
      @Parameter(description = "設備名") @RequestParam(required = false) String assetName,
      @Parameter(description = "カテゴリー") @RequestParam(required = false) String category,
      @Parameter(description = "型番") @RequestParam(required = false) String modelNumber,
      @PageableDefault(size = 100, sort = "assetCode", direction = Sort.Direction.ASC) Pageable pageable) {

    System.out.println(assetCode);
    System.out.println(assetName);
    System.out.println(category);
    System.out.println(modelNumber);

    Page<AssetMaster> results = assetService.searchAsset(assetCode, assetName, category, modelNumber, pageable);

    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "Inventory search successful.",
            "data", results));
  }

}
