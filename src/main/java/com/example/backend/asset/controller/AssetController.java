package com.example.backend.asset.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.backend.asset.dto.AssetMasterRequest;
import com.example.backend.asset.dto.AssetUpdateRequest;
import com.example.backend.asset.service.AssetService;
import com.example.backend.entity.AssetMaster;
import com.example.backend.entity.StockMaster;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "設備管理API", description = "設備情報取得・管理")
public class AssetController {
    private final AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @Operation(summary = "設備品検索 全件取得時は 管理番号, 設備名, カテゴリー, 型番を空にする")
    @GetMapping("/asset/search")
      public ResponseEntity<?> searchAsset(
      @Parameter(description = "管理番号") @RequestParam(required = false) String assetCode,
      @Parameter(description = "設備名") @RequestParam(required = false) String assetName,
      @Parameter(description = "カテゴリー") @RequestParam(required = false) String category,
      @Parameter(description = "型番") @RequestParam(required = false) String modelNumber,
      @PageableDefault(size = 100, sort = "assetCode", direction = Sort.Direction.ASC) Pageable pageable) {

    System.out.println(assetCode);
    System.out.println(assetName);
    System.out.println(category);
    System.out.println(modelNumber);

    Page<AssetMaster> results = assetService.searchAssetTeble(assetCode, assetName, category, modelNumber, pageable);

    return ResponseEntity.ok(
        Map.of(
            "status", 200,
            "message", "asset search successful.",
            "data", results));
  }

    @Operation(summary = "新規設備品登録")
    @PostMapping("/asset/new")
    public ResponseEntity<?> createAsset(@Valid @RequestBody AssetMasterRequest req) {
        System.out.println("constroller req: "+req);
        AssetMaster created = assetService.createAssetTable(req);
        System.out.println(created);
        System.out.println("Created: " + created);
        
        return ResponseEntity.ok(
            Map.of(
                "status",200, 
                "message", "設備品を登録しました。未記載の項目は確定次第更新してください", 
                "data", created));
    }

    @Operation(summary = "既存レコード更新")
    @PostMapping("/asset/update/{id}")
    public ResponseEntity<?> updateAsset(
        @PathVariable Long id,
        @Valid @RequestBody AssetUpdateRequest request) {
        AssetMaster updated = assetService.updateAssetTable(id, request);
        return ResponseEntity.ok(
            Map.of(
                "status", 200,
                "message", "asset updated successfully.",
                "transactionId", updated
            )
        );
    }

    @Operation(summary = "単一既存在庫情報取得")
    @GetMapping("/asset/{assetCode}")
    public ResponseEntity<?> getAssetByItemCode(@PathVariable String assetCode) {
        AssetMaster asset = assetService.getAssetByItemCode(assetCode);
        return ResponseEntity.ok(
            Map.of(
                "status", 200,
                "message", "asset fetched successfully.",
                "data", asset));
    }
}
