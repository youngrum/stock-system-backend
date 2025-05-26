package com.example.backend.common.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class OderNumberGenerator {
    
    private static final String PREFIX = "SG";      // 接頭辞（担当部署ID）
    private static final int BASE_TERM = 56;        // 期の起点（24/8/1～25/7/30 = 56期）
    private static final int START_YEAR = 2024;     // 56期の開始年
    private static final int ZERO_PADDING = 5;      // idをゼロ埋めする桁数（例：00001）

    /**
     * 登録済みIDから itemCode を発行する
     *
     * @param id サロゲートキー（stock_master.id）
     * @return itemCode（例: SG-56-00001）
     */
    public String generateItemCode(Long id) {
        int term = resolveCurrentTerm();
        String idFormatted = String.format("%0" + ZERO_PADDING + "d", id); // ゼロ埋め（5桁）
        return String.format("%s-%d-%s", PREFIX, term, idFormatted);
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
