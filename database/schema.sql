-- 保险系统数据库初始化脚本
-- 创建数据库和表结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS insurance_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE insurance_system;

-- 1. 顾客信息表 (customers)
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(20) UNIQUE NOT NULL COMMENT '顾客编号',
    first_name VARCHAR(50) NOT NULL COMMENT '名',
    last_name VARCHAR(50) NOT NULL COMMENT '姓',
    first_name_kana VARCHAR(50) COMMENT '名(カナ)',
    last_name_kana VARCHAR(50) COMMENT '姓(カナ)',
    gender ENUM('M', 'F') NOT NULL COMMENT '性别: M=男, F=女',
    birth_date DATE NOT NULL COMMENT '出生日期',
    age INT NOT NULL COMMENT '年龄',
    postal_code VARCHAR(7) COMMENT '邮政编码',
    prefecture VARCHAR(20) COMMENT '都道府県',
    city VARCHAR(50) COMMENT '市区町村',
    address_line1 VARCHAR(100) COMMENT '住所1',
    address_line2 VARCHAR(100) COMMENT '住所2',
    phone_number VARCHAR(20) COMMENT '电话号码',
    email VARCHAR(100) COMMENT '邮箱',
    occupation VARCHAR(50) COMMENT '职业',
    annual_income DECIMAL(12,2) COMMENT '年收入',
    family_composition VARCHAR(50) COMMENT '家庭构成',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT(1) DEFAULT 0 COMMENT '削除フラグ: 0=有効, 1=削除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='顾客信息表';

-- 2. 保险商品表 (insurance_products)
CREATE TABLE IF NOT EXISTS insurance_products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(20) UNIQUE NOT NULL COMMENT '商品コード',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    product_category VARCHAR(50) NOT NULL COMMENT '商品カテゴリ: 学資保険, 医療保険, 生命保険',
    product_type VARCHAR(50) NOT NULL COMMENT '商品タイプ: 子ども保険, 学資保険, 医療保険, 生命保険',
    description TEXT COMMENT '商品説明',
    min_insured_amount DECIMAL(12,2) NOT NULL COMMENT '最低保険金額',
    max_insured_amount DECIMAL(12,2) NOT NULL COMMENT '最高保険金額',
    min_insurance_period INT NOT NULL COMMENT '最低保険期間(年)',
    max_insurance_period INT NOT NULL COMMENT '最高保険期間(年)',
    min_entry_age INT NOT NULL COMMENT '最低加入年齢',
    max_entry_age INT NOT NULL COMMENT '最高加入年齢',
    payment_methods JSON COMMENT '支払い方法のJSON配列',
    features JSON COMMENT '特徴のJSON配列',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保险商品表';

-- 3. 保险料率表 (premium_rates)
CREATE TABLE IF NOT EXISTS premium_rates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL COMMENT '商品ID',
    gender ENUM('M', 'F') NOT NULL COMMENT '性别',
    entry_age INT NOT NULL COMMENT '加入年齢',
    insurance_period INT NOT NULL COMMENT '保険期間',
    base_rate DECIMAL(10,6) NOT NULL COMMENT '基本料率',
    loading_rate DECIMAL(10,6) NOT NULL COMMENT '付加料率',
    valid_from DATE NOT NULL COMMENT '適用開始日',
    valid_to DATE COMMENT '適用終了日',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES insurance_products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='保险料率表';

-- 4. 契约表 (contracts)
CREATE TABLE IF NOT EXISTS contracts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contract_number VARCHAR(20) UNIQUE NOT NULL COMMENT '契約番号',
    customer_id INT NOT NULL COMMENT '顧客ID',
    product_id INT NOT NULL COMMENT '商品ID',
    contract_status ENUM('見積', '仮申込', '本申込', '審査中', '承認', '却下', '解約', '失効', '満期') NOT NULL DEFAULT '見積',
    insured_amount DECIMAL(12,2) NOT NULL COMMENT '保険金額',
    insurance_period INT NOT NULL COMMENT '保険期間',
    monthly_premium DECIMAL(10,2) NOT NULL COMMENT '月々保険料',
    annual_premium DECIMAL(10,2) NOT NULL COMMENT '年間保険料',
    contract_start_date DATE COMMENT '契約開始日',
    contract_end_date DATE COMMENT '契約終了日',
    payment_method ENUM('口座引落', 'クレジットカード', '銀行振込', '窓口') NOT NULL DEFAULT '口座引落',
    payment_frequency ENUM('月払', '年払', '一時払') NOT NULL DEFAULT '月払',
    sales_person_id INT COMMENT '営業担当者ID',
    branch_office_id INT COMMENT '営業所ID',
    application_date DATE COMMENT '申込日',
    approval_date DATE COMMENT '承認日',
    rejection_reason TEXT COMMENT '却下理由',
    special_conditions TEXT COMMENT '特別条件',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES insurance_products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='契约表';

-- 5. 被保险人表 (insured_persons)
CREATE TABLE IF NOT EXISTS insured_persons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT NOT NULL COMMENT '契約ID',
    relationship VARCHAR(20) NOT NULL COMMENT '本人との関係: 本人, 配偶者, 子ども, その他',
    first_name VARCHAR(50) NOT NULL COMMENT '名',
    last_name VARCHAR(50) NOT NULL COMMENT '姓',
    first_name_kana VARCHAR(50) COMMENT '名(カナ)',
    last_name_kana VARCHAR(50) COMMENT '姓(カナ)',
    gender ENUM('M', 'F') NOT NULL COMMENT '性别',
    birth_date DATE NOT NULL COMMENT '出生日期',
    age INT NOT NULL COMMENT '年龄',
    occupation VARCHAR(50) COMMENT '职业',
    health_status TEXT COMMENT '健康状態',
    medical_history TEXT COMMENT '既往歴',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='被保险人表';

-- 6. 支付记录表 (payment_records)
CREATE TABLE IF NOT EXISTS payment_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT NOT NULL COMMENT '契約ID',
    payment_date DATE NOT NULL COMMENT '支払日',
    payment_amount DECIMAL(10,2) NOT NULL COMMENT '支払金額',
    payment_method ENUM('口座引落', 'クレジットカード', '銀行振込', '窓口') NOT NULL COMMENT '支払方法',
    payment_status ENUM('成功', '失敗', '未処理', '遅延') NOT NULL DEFAULT '未処理',
    reference_number VARCHAR(50) COMMENT '参照番号',
    failure_reason TEXT COMMENT '失敗理由',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- 7. 给付金请求表 (claims)
CREATE TABLE IF NOT EXISTS claims (
    id INT AUTO_INCREMENT PRIMARY KEY,
    claim_number VARCHAR(20) UNIQUE NOT NULL COMMENT '請求番号',
    contract_id INT NOT NULL COMMENT '契約ID',
    claim_type ENUM('入院給付金', '手術給付金', '死亡保険金', '障害給付金', 'その他') NOT NULL COMMENT '請求タイプ',
    claim_date DATE NOT NULL COMMENT '請求日',
    incident_date DATE NOT NULL COMMENT '事故日',
    claim_amount DECIMAL(12,2) NOT NULL COMMENT '請求金額',
    claim_status ENUM('受付', '審査中', '承認', '却下', '支払済', '取消') NOT NULL DEFAULT '受付',
    claim_reason TEXT NOT NULL COMMENT '請求理由',
    medical_institution_name VARCHAR(100) COMMENT '医療機関名',
    medical_institution_address TEXT COMMENT '医療機関住所',
    doctor_name VARCHAR(50) COMMENT '医師名',
    diagnosis TEXT COMMENT '診断名',
    supporting_documents JSON COMMENT '添付書類のJSON配列',
    approval_date DATE COMMENT '承認日',
    payment_date DATE COMMENT '支払日',
    approval_amount DECIMAL(12,2) COMMENT '承認金額',
    rejection_reason TEXT COMMENT '却下理由',
    reviewer_id INT COMMENT '審査員ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='给付金请求表';

-- 8. 资料请求表 (document_requests)
CREATE TABLE IF NOT EXISTS document_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(20) UNIQUE NOT NULL COMMENT '請求番号',
    customer_id INT NOT NULL COMMENT '顧客ID',
    product_id INT COMMENT '商品ID',
    request_type ENUM('資料請求', '仮申込', '本申込', '変更申請') NOT NULL COMMENT '請求タイプ',
    request_status ENUM('受付', '処理中', '完了', '取消') NOT NULL DEFAULT '受付',
    requested_documents JSON COMMENT '請求書類のJSON配列',
    shipping_address TEXT COMMENT '送付先住所',
    shipping_method ENUM('郵便', '宅配便', 'メール', 'ダウンロード') NOT NULL DEFAULT '郵便',
    contact_preference ENUM('電話', 'メール', '郵便', 'なし') NOT NULL DEFAULT '電話',
    notes TEXT COMMENT '備考',
    follow_up_date DATE COMMENT 'フォローアップ日',
    sales_person_id INT COMMENT '営業担当者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES insurance_products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资料请求表';

-- 9. 用户表 (系统用户) (users)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT 'ユーザー名',
    password VARCHAR(255) NOT NULL COMMENT 'パスワード',
    full_name VARCHAR(100) NOT NULL COMMENT '氏名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT 'メールアドレス',
    phone_number VARCHAR(20) COMMENT '電話番号',
    user_role ENUM('admin', 'sales', 'underwriter', 'claims', 'customer_service') NOT NULL COMMENT 'ユーザーロール',
    department VARCHAR(50) COMMENT '部署',
    position VARCHAR(50) COMMENT '役職',
    branch_office_id INT COMMENT '営業所ID',
    last_login_at TIMESTAMP NULL COMMENT '最終ログイン日時',
    account_status ENUM('有効', '無効', '停止') NOT NULL DEFAULT '有効',
    password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 10. 系统日志表 (system_logs)
CREATE TABLE IF NOT EXISTS system_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    log_type ENUM('INFO', 'WARNING', 'ERROR', 'DEBUG') NOT NULL COMMENT 'ログタイプ',
    user_id INT COMMENT 'ユーザーID',
    action VARCHAR(100) NOT NULL COMMENT '実行アクション',
    target_type VARCHAR(50) COMMENT '対象タイプ',
    target_id INT COMMENT '対象ID',
    description TEXT COMMENT '詳細説明',
    ip_address VARCHAR(45) COMMENT 'IPアドレス',
    user_agent VARCHAR(255) COMMENT 'ユーザーエージェント',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- 11. 营业所表 (branch_offices)
CREATE TABLE IF NOT EXISTS branch_offices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(10) UNIQUE NOT NULL COMMENT '営業所コード',
    branch_name VARCHAR(100) NOT NULL COMMENT '営業所名',
    branch_type ENUM('本社', '支社', '営業所', '代理店') NOT NULL COMMENT '営業所タイプ',
    postal_code VARCHAR(7) COMMENT '郵便番号',
    prefecture VARCHAR(20) COMMENT '都道府県',
    city VARCHAR(50) COMMENT '市区町村',
    address_line1 VARCHAR(100) COMMENT '住所1',
    address_line2 VARCHAR(100) COMMENT '住所2',
    phone_number VARCHAR(20) NOT NULL COMMENT '電話番号',
    fax_number VARCHAR(20) COMMENT 'FAX番号',
    email VARCHAR(100) COMMENT 'メールアドレス',
    business_hours VARCHAR(100) COMMENT '営業時間',
    established_date DATE COMMENT '開設日',
    manager_name VARCHAR(50) COMMENT '支店長名',
    employee_count INT DEFAULT 0 COMMENT '従業員数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营业所表';

-- 12. FAQ表 (faqs)
CREATE TABLE IF NOT EXISTS faqs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL COMMENT 'カテゴリ',
    question TEXT NOT NULL COMMENT '質問',
    answer TEXT NOT NULL COMMENT '回答',
    display_order INT DEFAULT 0 COMMENT '表示順序',
    is_published TINYINT(1) DEFAULT 1 COMMENT '公開フラグ: 1=公開, 0=非公開',
    view_count INT DEFAULT 0 COMMENT '閲覧数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_flag TINYINT(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='FAQ表';

-- 创建索引以提高查询性能
CREATE INDEX idx_customers_customer_code ON customers(customer_code);
CREATE INDEX idx_customers_email ON customers(email);
CREATE INDEX idx_customers_name ON customers(last_name, first_name);
CREATE INDEX idx_contracts_contract_number ON contracts(contract_number);
CREATE INDEX idx_contracts_customer_id ON contracts(customer_id);
CREATE INDEX idx_contracts_product_id ON contracts(product_id);
CREATE INDEX idx_contracts_status ON contracts(contract_status);
CREATE INDEX idx_premium_rates_product_id ON premium_rates(product_id);
CREATE INDEX idx_premium_rates_age_period ON premium_rates(gender, entry_age, insurance_period);
CREATE INDEX idx_premium_rates_validity ON premium_rates(valid_from, valid_to);
CREATE INDEX idx_products_product_code ON insurance_products(product_code);
CREATE INDEX idx_products_category ON insurance_products(product_category);
CREATE INDEX idx_claims_contract_id ON claims(contract_id);
CREATE INDEX idx_claims_status ON claims(claim_status);
CREATE INDEX idx_claims_claim_number ON claims(claim_number);
CREATE INDEX idx_document_requests_customer_id ON document_requests(customer_id);
CREATE INDEX idx_document_requests_status ON document_requests(request_status);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(user_role);
CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX idx_system_logs_created_at ON system_logs(created_at);
CREATE INDEX idx_system_logs_action ON system_logs(action);
CREATE INDEX idx_branch_offices_code ON branch_offices(branch_code);
CREATE INDEX idx_faq_category ON faqs(category);

-- 创建视图
CREATE VIEW vw_contract_details AS
SELECT 
    c.id,
    c.contract_number,
    c.customer_id,
    c.product_id,
    c.contract_status,
    c.insured_amount,
    c.insurance_period,
    c.monthly_premium,
    c.annual_premium,
    c.contract_start_date,
    c.contract_end_date,
    c.payment_method,
    c.payment_frequency,
    c.application_date,
    c.approval_date,
    c.created_at,
    c.updated_at,
    CONCAT(cu.last_name, ' ', cu.first_name) AS customer_name,
    cu.email AS customer_email,
    cu.phone_number AS customer_phone,
    p.product_name,
    p.product_category,
    p.product_type,
    CONCAT(u.full_name, ' (', u.user_role, ')') AS sales_person_name
FROM contracts c
JOIN customers cu ON c.customer_id = cu.id
JOIN insurance_products p ON c.product_id = p.id
LEFT JOIN users u ON c.sales_person_id = u.id;

-- 创建存储过程：计算保险料率
DELIMITER //
CREATE PROCEDURE calculate_premium(
    IN p_product_id INT,
    IN p_gender ENUM('M', 'F'),
    IN p_entry_age INT,
    IN p_insurance_period INT,
    IN p_insured_amount DECIMAL(12,2),
    OUT p_monthly_premium DECIMAL(10,2),
    OUT p_annual_premium DECIMAL(10,2)
)
BEGIN
    DECLARE v_base_rate DECIMAL(10,6);
    DECLARE v_loading_rate DECIMAL(10,6);
    DECLARE v_rate_factor DECIMAL(10,6);
    DECLARE v_base_premium DECIMAL(12,2);
    
    -- 获取料率
    SELECT base_rate, loading_rate 
    INTO v_base_rate, v_loading_rate
    FROM premium_rates 
    WHERE product_id = p_product_id 
      AND gender = p_gender 
      AND entry_age = p_entry_age 
      AND insurance_period = p_insurance_period 
      AND valid_from <= CURDATE() 
      AND (valid_to IS NULL OR valid_to >= CURDATE())
    LIMIT 1;
    
    -- 计算料率系数
    SET v_rate_factor = v_base_rate + v_loading_rate;
    
    -- 计算基础保费
    SET v_base_premium = p_insured_amount * v_rate_factor;
    
    -- 计算月保费和年保费
    SET p_monthly_premium = v_base_premium / 12;
    SET p_annual_premium = v_base_premium;
    
END //
DELIMITER ;

-- 创建触发器：自动更新年龄
DELIMITER //
CREATE TRIGGER update_customer_age 
BEFORE INSERT ON customers 
FOR EACH ROW
BEGIN
    IF NEW.birth_date IS NOT NULL THEN
        SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birth_date, CURDATE());
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER update_customer_age_update 
BEFORE UPDATE ON customers 
FOR EACH ROW
BEGIN
    IF NEW.birth_date IS NOT NULL THEN
        SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birth_date, CURDATE());
    END IF;
END //
DELIMITER ;

-- 创建触发器：自动更新被保险人年龄
DELIMITER //
CREATE TRIGGER update_insured_person_age 
BEFORE INSERT ON insured_persons 
FOR EACH ROW
BEGIN
    IF NEW.birth_date IS NOT NULL THEN
        SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birth_date, CURDATE());
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER update_insured_person_age_update 
BEFORE UPDATE ON insured_persons 
FOR EACH ROW
BEGIN
    IF NEW.birth_date IS NOT NULL THEN
        SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birth_date, CURDATE());
    END IF;
END //
DELIMITER ;