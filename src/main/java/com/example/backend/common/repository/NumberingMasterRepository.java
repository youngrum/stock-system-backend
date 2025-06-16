package com.example.backend.common.repository;

import com.example.backend.entity.NumberingMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface NumberingMasterRepository extends JpaRepository<NumberingMaster, Long> {
    /**
     * 業務ロジックで使用するためのメソッド。
     * 指定された部署コード、番号種別、年度に基づいて、排他制御付きで番号マスタを取得します。
     *
     * @param departmentCode 部署コード
     * @param numberingType  番号種別
     * @param fiscalYear     年度
     * @return 該当する番号マスタのOptional
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM NumberingMaster n WHERE n.departmentCode = :deptCode AND n.numberingType = :type AND n.fiscalYear = :year")
    Optional<NumberingMaster> findByDepartmentCodeAndNumberingTypeAndFiscalYearForUpdate(
            @Param("deptCode") String departmentCode,
            @Param("type") String numberingType,
            @Param("year") Integer fiscalYear);

    Optional<NumberingMaster> findByDepartmentCodeAndNumberingTypeAndFiscalYear(
            String departmentCode, String numberingType, Integer fiscalYear);
}