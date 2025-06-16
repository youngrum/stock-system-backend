INSERT INTO users
  (username, password, enabled)
VALUES
  ('tanaka taro', 'a111', TRUE);

INSERT INTO numbering_master
  (
  department_code,
  numbering_type,
  fiscal_year,
  current_decimal_number
  -- id, created_at, updated_at, version は自動設定またはデフォルト値が使われるため、通常は指定しない
  )
VALUES
  (
    'GENERAL', -- 部署コード (例: 汎用的な採番のため 'GENERAL' など)
    'ORDER', -- 番号の種別 (例: 注文番号のため 'ORDER')
    56, -- 新しい期 (例: 56期)
    1                  -- 採番を1から開始
);