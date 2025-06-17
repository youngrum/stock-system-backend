INSERT INTO users
  (username, password, enabled)
VALUES
  ('tanaka taro', 'a111', TRUE),
  ('kobayashi megumi', 'hGf%4VbnM@1L', TRUE),
  ('yamamoto shota', 'zYx#67OpQrs3', TRUE);

INSERT INTO numbering_master
  (
    department_code,
    numbering_type,
    fiscal_year,
    current_decimal_number,
    created_at,
    updated_at,
    version
  )
VALUES
  (
    'S',
    'ORDER',
    56,
    0, -- @PrePersistの初期化と一致させる
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0 -- @Versionの初期値
  );