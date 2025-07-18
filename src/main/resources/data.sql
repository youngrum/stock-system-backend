INSERT INTO users
  (username, password, enabled)
VALUES
  ('tanaka taro', 'a111', TRUE);

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

  INSERT INTO asset_master (
    asset_code,
    asset_name,
    model_number,
    category,
    manufacturer,
    regist_date,
    current_status,
    remarks
    -- その他の必須カラムがあれば追加
) VALUES (
    'AM-0001',             -- asset_code (ユニークな資産コード)
    'テスト設備A',           -- asset_name
    'Model-XYZ-123',       -- model_number
    '測定機器',              -- category
    'ABC社',                -- manufacturer
    '2024-01-15',          -- acquisition_date (取得日)
    '稼働中',                -- current_status (例: 稼働中, 保守中, 廃棄済み)
    'テストデータ用設備'     -- remarks
    -- その他のカラムの値があれば追加
);

-- 必要であれば、さらに別のレコードを追加
INSERT INTO asset_master (item_code, item_name, model_number, category, manufacturer, acquisition_date, current_status, remarks)
VALUES ('AM-0002', 'テスト設備B', 'Model-456-DEF', '校正器', 'DEF社', '2023-05-20', '稼働中', 'もう一つのテストデータ');