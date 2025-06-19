package com.example.backend.common.service;

import com.example.backend.entity.NumberingMaster;
import com.example.backend.common.repository.NumberingMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TransactionIdGenerator {

    private static final String DEPARTMENT_CODE = "S";
    private static final int BASE_TERM = 56; // 2024年8月1日からの期を56期とする
    private static final int START_YEAR = 2024;
    private static final int ZERO_PADDING = 6;

    @Autowired
    private NumberingMasterRepository numberingMasterRepository;

    /**
     * 新しい注文番号を生成する
     */
    @Transactional
    public String generateTxNo() {
        return generateNumber(DEPARTMENT_CODE, "TX");
    }

    /**
     * 指定された種別の番号を生成する
     */
    @Transactional
    public String generateNumber(String deptCode, String numberingType) {
        int currentTerm = resolveCurrentTerm();
        long nextNumber = getNextNumber(deptCode, numberingType, currentTerm);
        String numberFormatted = String.format("%0" + ZERO_PADDING + "X", nextNumber);
        return String.format("%s%d-%s", deptCode, currentTerm, numberFormatted);
    }

    /**
     * 採番マスタから次の番号を取得・更新
     */
    private long getNextNumber(String deptCode, String numberingType, int fiscalYear) {
        // 悲観的ロックで取得
        Optional<NumberingMaster> optionalMaster = numberingMasterRepository
                .findByDepartmentCodeAndNumberingTypeAndFiscalYearForUpdate(deptCode, numberingType, fiscalYear);

        NumberingMaster master;
        if (optionalMaster.isPresent()) {
            master = optionalMaster.get();
        } else {
            // 新規作成
            master = new NumberingMaster(deptCode, numberingType, fiscalYear);
            master = numberingMasterRepository.save(master);
        }

        // 次の番号を取得（内部でインクリメント）
        long nextNumber = master.getNextNumber();

        // 更新を保存
        numberingMasterRepository.save(master);

        return nextNumber;
    }

    /**
     * 現在の期を計算する
     */
    public int resolveCurrentTerm() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();

        LocalDate cutoff = LocalDate.of(year, 8, 1);

        if (now.isBefore(cutoff)) {
            year--;
        }

        return BASE_TERM + (year - START_YEAR);
    }    
}
