package com.example.backend.csv.controller;

import com.example.backend.csv.service.CsvUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/api")
@Tag(name = "CSVファイルアップロードAPI", description = "在庫情報のCSVファイルアップロードと処理")
public class CsvUploadController {

  @Autowired
  private CsvUploadService csvUploadService;

  @SuppressWarnings("null")
  @PostMapping("/upload-csv")
  public ResponseEntity<Map<String, String>> uploadCsv(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return new ResponseEntity<>(Map.of("message", "ファイルが選択されていません。"), HttpStatus.BAD_REQUEST);
    }
    if (!file.getContentType().equals("text/csv")) {
      return new ResponseEntity<>(Map.of("message", "CSVファイルのみアップロード可能です。"), HttpStatus.BAD_REQUEST);
    }

    try {
      // CSV処理サービスを呼び出す (別途実装)
      List<String> errors = csvUploadService.importCsv(file.getInputStream());

      if (errors.isEmpty()) {
        return new ResponseEntity<>(Map.of("message", "CSVファイルのアップロードと処理が完了しました。"), HttpStatus.OK);
      } else {
        // エラーがある場合
        return new ResponseEntity<>(Map.of("message", "一部のデータでエラーが発生しました。", "details", String.join("\n", errors)),
            HttpStatus.BAD_REQUEST);
      }

    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(Map.of("message", "CSVファイルの処理中にエラーが発生しました: " + e.getMessage()),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}