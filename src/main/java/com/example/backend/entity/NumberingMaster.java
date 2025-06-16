package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "numbering_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NumberingMaster {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "department_code", nullable = false, length = 10)
  private String departmentCode;

  /**
   * 番号の種別を示すフィールド。
   * 例: "ORDER", "INVOICE" など。現時点では注文番号("ORDER")のみを想定。
   * 番号の種別は、将来的に他の用途にも拡張可能。
   */
  @Column(name = "numbering_type", nullable = false, length = 20)
  private String numberingType;

  @Column(name = "fiscal_year", nullable = false)
  private Integer fiscalYear;

  @Column(name = "current_decimal_number", nullable = false)
  private Long currentDecimalNumber;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Version // 楽観的排他制御
  private Integer version;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    if (currentDecimalNumber == null) {
      currentDecimalNumber = 0L;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // ビジネスメソッド
  public Long getNextNumber() {
    return ++currentDecimalNumber;
  }

  // 初期化用コンストラクタ
  public NumberingMaster(String departmentCode, String numberingType, Integer fiscalYear) {
    this.departmentCode = departmentCode;
    this.numberingType = numberingType;
    this.fiscalYear = fiscalYear;
    this.currentDecimalNumber = 0L;
  }
}