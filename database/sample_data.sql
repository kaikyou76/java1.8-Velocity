-- 保险系统样本数据初始化脚本
-- 插入测试数据

USE insurance_system;

-- 1. 插入保险商品数据
INSERT INTO insurance_products (product_code, product_name, product_category, product_type, description, min_insured_amount, max_insured_amount, min_insurance_period, max_insurance_period, min_entry_age, max_entry_age, payment_methods, features) VALUES
('EDU001', '学資保険プランA', '学資保険', '子ども保険', '子どもの教育資金を確実に準備するための学資保険です。大学進学時にお祝い金が支給されます。', 1000000.00, 5000000.00, 5, 18, 0, 12, '["口座引落", "クレジットカード", "銀行振込"]', '["大学進学お祝い金", "契約者保障特約", "医療特約付帯可能"]'),
('EDU002', '学資保険プランB', '学資保険', '子ども保険', '返戻率の高い学資保険プランです。満期時には払込保険料の110%以上が戻ります。', 2000000.00, 10000000.00, 10, 22, 0, 10, '["口座引落", "クレジットカード"]', '["高返戻率", "契約者死亡時保険料払込免除", "進学時一時金"]'),
('MED001', '医療保険プランA', '医療保険', '医療保険', '入院・手術を幅広く保障する医療保険です。先進医療にも対応しています。', 3000000.00, 10000000.00, 1, 10, 0, 65, '["口座引落", "クレジットカード", "銀行振込", "窓口"]', '["入院保障", "手術保障", "先進医療対応", "通院保障"]'),
('LIFE001', '終身保険プランA', '生命保険', '生命保険', '一生涯の保障を提供する終身保険です。解約返戻金も充実しています。', 5000000.00, 50000000.00, 10, 30, 20, 60, '["口座引落", "銀行振込"]', '["終身保障", "解約返戻金", "三大疾病保障", "介護保障"]');

-- 2. 插入保险料率数据 (学資保険プランA)
INSERT INTO premium_rates (product_id, gender, entry_age, insurance_period, base_rate, loading_rate, valid_from, valid_to) VALUES
(1, 'M', 0, 18, 0.001200, 0.000100, '2024-01-01', NULL),
(1, 'M', 1, 18, 0.001150, 0.000100, '2024-01-01', NULL),
(1, 'M', 2, 18, 0.001100, 0.000100, '2024-01-01', NULL),
(1, 'M', 3, 18, 0.001050, 0.000100, '2024-01-01', NULL),
(1, 'M', 4, 18, 0.001000, 0.000100, '2024-01-01', NULL),
(1, 'M', 5, 18, 0.000950, 0.000100, '2024-01-01', NULL),
(1, 'F', 0, 18, 0.001100, 0.000080, '2024-01-01', NULL),
(1, 'F', 1, 18, 0.001050, 0.000080, '2024-01-01', NULL),
(1, 'F', 2, 18, 0.001000, 0.000080, '2024-01-01', NULL),
(1, 'F', 3, 18, 0.000950, 0.000080, '2024-01-01', NULL),
(1, 'F', 4, 18, 0.000900, 0.000080, '2024-01-01', NULL),
(1, 'F', 5, 18, 0.000850, 0.000080, '2024-01-01', NULL);

-- 3. 插入顾客数据
INSERT INTO customers (customer_code, first_name, last_name, first_name_kana, last_name_kana, gender, birth_date, age, postal_code, prefecture, city, address_line1, address_line2, phone_number, email, occupation, annual_income, family_composition) VALUES
('CUST0001', '太郎', '山田', 'タロウ', 'ヤマダ', 'M', '1985-03-15', 39, '1000001', '東京都', '千代田区', '千代田1-1-1', 'マンション101', '03-1234-5678', 'taro.yamada@example.com', '会社員', 8000000.00, '妻、子ども2人'),
('CUST0002', '花子', '鈴木', 'ハナコ', 'スズキ', 'F', '1988-07-22', 36, '1500043', '東京都', '渋谷区', '道玄坂2-3-4', 'アパート201', '03-9876-5432', 'hanako.suzuki@example.com', '看護師', 6000000.00, '夫、子ども1人'),
('CUST0003', '健太', '佐藤', 'ケンタ', 'サトウ', 'M', '1990-12-10', 33, '2200004', '神奈川県', '横浜市', '西区みなとみらい5-6-7', 'タワーマンション3001', '045-111-2222', 'kenta.sato@example.com', 'エンジニア', 10000000.00, '独身'),
('CUST0004', '美咲', '田中', 'ミサキ', 'タナカ', 'F', '1982-05-30', 42, '5300001', '大阪府', '大阪市', '北区梅田1-2-3', 'オフィスビル808', '06-3333-4444', 'misaki.tanaka@example.com', '経理', 7000000.00, '夫、子ども2人');

-- 4. 插入营业所数据
INSERT INTO branch_offices (branch_code, branch_name, branch_type, postal_code, prefecture, city, address_line1, address_line2, phone_number, fax_number, email, business_hours, established_date, manager_name, employee_count) VALUES
('HO001', '本社', '本社', '1000001', '東京都', '千代田区', '丸の内1-1-1', '保険ビル10F', '03-1111-2222', '03-1111-2223', 'headoffice@insurance.co.jp', '月〜金 9:00-17:00', '2000-04-01', '山本一郎', 200),
('BR001', '東京支店', '支社', '1500043', '東京都', '渋谷区', '渋谷2-3-4', '渋谷ビル5F', '03-3333-4444', '03-3333-4445', 'tokyo@insurance.co.jp', '月〜金 9:00-17:00', '2005-06-15', '佐藤健二', 50),
('BR002', '大阪支店', '支社', '5300001', '大阪府', '大阪市', '北区梅田1-2-3', '梅田ビル8F', '06-5555-6666', '06-5555-6667', 'osaka@insurance.co.jp', '月〜金 9:00-17:00', '2008-09-20', '田中三郎', 40);

-- 5. 插入用户数据
INSERT INTO users (username, password, full_name, email, phone_number, user_role, department, position, branch_office_id, account_status) VALUES
('admin', '$2a$10$rOzJqK1YQ1NQ1NQ1NQ1NQe', 'システム管理者', 'admin@insurance.co.jp', '03-1111-9999', 'admin', '情報システム部', '部長', 1, '有効'),
('yamada_sales', '$2a$10$rOzJqK1YQ1NQ1NQ1NQ1NQe', '山田営業', 'yamada.sales@insurance.co.jp', '03-2222-8888', 'sales', '営業部', '営業担当', 2, '有効'),
('sato_under', '$2a$10$rOzJqK1YQ1NQ1NQ1NQ1NQe', '佐藤審査', 'sato.under@insurance.co.jp', '03-3333-7777', 'underwriter', '審査部', '審査員', 1, '有効'),
('tanaka_claims', '$2a$10$rOzJqK1YQ1NQ1NQ1NQ1NQe', '田中給付', 'tanaka.claims@insurance.co.jp', '03-4444-6666', 'claims', '給付金部', '給付担当', 1, '有効');

-- 6. 插入契约数据
INSERT INTO contracts (contract_number, customer_id, product_id, contract_status, insured_amount, insurance_period, monthly_premium, annual_premium, contract_start_date, contract_end_date, payment_method, payment_frequency, sales_person_id, branch_office_id, application_date, approval_date) VALUES
('CONTRACT0001', 1, 1, '承認', 3000000.00, 18, 2500.00, 30000.00, '2024-01-15', '2042-01-15', '口座引落', '月払', 2, 2, '2024-01-10', '2024-01-12'),
('CONTRACT0002', 2, 3, '承認', 5000000.00, 10, 4166.67, 50000.00, '2024-02-01', '2034-02-01', 'クレジットカード', '月払', 2, 2, '2024-01-25', '2024-01-28'),
('CONTRACT0003', 3, 4, '審査中', 10000000.00, 20, 8333.33, 100000.00, NULL, NULL, '口座引落', '月払', 2, 2, '2024-02-15', NULL);

-- 7. 插入被保险人数据
INSERT INTO insured_persons (contract_id, relationship, first_name, last_name, first_name_kana, last_name_kana, gender, birth_date, age, occupation, health_status, medical_history) VALUES
(1, '子ども', '一郎', '山田', 'イチロウ', 'ヤマダ', 'M', '2020-05-20', 3, '幼稚園児', '良好', 'なし'),
(2, '本人', '花子', '鈴木', 'ハナコ', 'スズキ', 'F', '1988-07-22', 36, '看護師', '良好', '花粉症'),
(3, '本人', '健太', '佐藤', 'ケンタ', 'サトウ', 'M', '1990-12-10', 33, 'エンジニア', '良好', 'なし');

-- 8. 插入FAQ数据
INSERT INTO faqs (category, question, answer, display_order, is_published, view_count) VALUES
('学資保険', '学資保険とは何ですか？', '学資保険は、お子様の教育資金を計画的に準備するための保険です。満期時には学資金が支払われ、契約者に万一のことがあった場合には保険料の払込が免除されます。', 1, 1, 150),
('学資保険', '加入できる年齢は何歳までですか？', '学資保険はお子様が0歳から12歳まで加入可能です。年齢によって保険料や保障内容が異なりますので、詳しくはお問い合わせください。', 2, 1, 120),
('医療保険', '医療保険でカバーされる治療は？', '医療保険では、入院・手術・通院治療などを保障します。先進医療や特定疾病にも対応しているプランもあります。', 3, 1, 200),
('申込', '申込から契約までどのくらいかかりますか？', '通常、申込から審査・契約成立まで1〜2週間程度かかります。審査内容によってはもう少々お時間をいただく場合があります。', 4, 1, 80);

-- 9. 插入支付记录数据
INSERT INTO payment_records (contract_id, payment_date, payment_amount, payment_method, payment_status, reference_number) VALUES
(1, '2024-02-15', 2500.00, '口座引落', '成功', 'PAY202402150001'),
(1, '2024-03-15', 2500.00, '口座引落', '成功', 'PAY202403150001'),
(2, '2024-02-01', 4166.67, 'クレジットカード', '成功', 'PAY202402010001');

-- 10. 插入资料请求数据
INSERT INTO document_requests (request_number, customer_id, product_id, request_type, request_status, requested_documents, shipping_address, shipping_method, contact_preference, notes) VALUES
('REQ0001', 1, 1, '資料請求', '完了', '["パンフレット", "約款", "申込書"]', '東京都千代田区千代田1-1-1 マンション101', '郵便', 'メール', '学資保険の資料希望'),
('REQ0002', 2, 3, '仮申込', '処理中', '["仮申込書", "健康告知書"]', '東京都渋谷区道玄坂2-3-4 アパート201', 'メール', '電話', '医療保険の仮申込');

-- 验证数据插入
SELECT 'データ挿入完了' AS status;
SELECT COUNT(*) AS customer_count FROM customers;
SELECT COUNT(*) AS product_count FROM insurance_products;
SELECT COUNT(*) AS contract_count FROM contracts;