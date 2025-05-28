package com.example.backend.common.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ItemCodeGenerator {

    private static final String PREFIX = "SG"; // 接頭辞（担当部署ID）
    private static final int BASE_TERM = 56; // 期の起点（24/8/1～25/7/30 = 56期）
    private static final int START_YEAR = 2024; // 56期の開始年
    private static final int ZERO_PADDING = 6; // idをゼロ埋めする桁数（例：000001）

    /**
     * 登録済みIDから itemCode を発行する
     *
     * @param id サロゲートキー（stock_master.id）
     * @return itemCode（例: SG-56-）
     */
    public String generateItemCode(Long id) {
        int term = resolveCurrentTerm();
        String idFormatted = String.format("%0" + ZERO_PADDING + "X", id); // ゼロ埋め（6桁）
        return String.format("%s%d-%s", PREFIX, term, idFormatted); // SG56-4E7
    }

    /**
     * 現在の期を計算する（毎年8月1日切り替え）
     *
     * @return 現在の期（例: 56, 57, ...）
     */
    public int resolveCurrentTerm() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        // この年の8月1日が期の切り替え日
        LocalDate cutoff = LocalDate.of(year, 8, 1);

        // もしまだ8月1日を過ぎていないなら、前年の期とみなす
        if (now.isBefore(cutoff)) {
            year--; // ← 期は前年に属する
        }

        return BASE_TERM + (year - START_YEAR);
    }
}
