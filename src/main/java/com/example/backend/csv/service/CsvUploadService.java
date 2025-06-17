package com.example.backend.csv.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvUploadService {

  public List<String> importCsv(InputStream inputStream) throws Exception {
    List<String> errors = new ArrayList<>();
    int lineNumber = 0;

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      // ヘッダー行をスキップする場合
      reader.readLine(); // ヘッダーを読み飛ばす

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        // ここでCSVの各行を解析し、データベースに保存するロジックを実装
        // 例: line.split(",") でカンマ区切りに分割
        String[] data = line.split(",");

        // 例: 簡易的なバリデーション
        if (data.length < 3) { // 少なくとも3列あると仮定
          errors.add("行 " + lineNumber + ": 列数が足りません。");
          continue;
        }

        try {
          String productCode = data[0].trim();
          String productName = data[1].trim();
          int stockQuantity = Integer.parseInt(data[2].trim());

          // TODO: ここでデータベースへの保存ロジックを実装
          // 例: yourProductRepository.save(new Product(productCode, productName,
          // stockQuantity));

          System.out.println("Processing: " + productCode + ", " + productName + ", " + stockQuantity);

        } catch (NumberFormatException e) {
          errors.add("行 " + lineNumber + ": 在庫数が数値ではありません。");
        } catch (Exception e) {
          errors.add("行 " + lineNumber + ": 処理中に予期せぬエラーが発生しました: " + e.getMessage());
        }
      }
    }
    return errors;
  }
}
