package com.insurance.batch;

import com.insurance.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * レポート生成バッチ処理クラス
 * 定期的に各種レポートを生成するバッチジョブ
 */
public class ReportGenerationBatch {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * バッチ処理を開始
     */
    public static void startBatchProcessing() {
        System.out.println("レポート生成バッチ処理を開始します...");
        
        // 毎週月曜日深夜4時に実行
        scheduler.scheduleAtFixedRate(
            ReportGenerationBatch::executeReportGeneration,
            getWeeklyInitialDelay(),
            7 * 24 * 60 * 60 * 1000, // 1週間
            TimeUnit.MILLISECONDS
        );
        
        // 毎月1日深夜5時に月次レポート
        scheduler.scheduleAtFixedRate(
            ReportGenerationBatch::executeMonthlyReports,
            getMonthlyInitialDelay(),
            30 * 24 * 60 * 60 * 1000L, // 約1ヶ月
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * バッチ処理を停止
     */
    public static void stopBatchProcessing() {
        System.out.println("レポート生成バッチ処理を停止します...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * レポート生成バッチ実行
     */
    private static void executeReportGeneration() {
        System.out.println("[" + new Date() + "] 週次レポート生成バッチを実行します");
        
        try {
            // 契約統計レポート
            generateContractStatsReport();
            System.out.println("契約統計レポートを生成しました");
            
            // 資料請求統計レポート
            generateRequestStatsReport();
            System.out.println("資料請求統計レポートを生成しました");
            
            // 売上統計レポート
            generateSalesReport();
            System.out.println("売上統計レポートを生成しました");
            
            System.out.println("[" + new Date() + "] 週次レポート生成バッチが正常に完了しました");
            
        } catch (Exception e) {
            System.err.println("[" + new Date() + "] レポート生成中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 月次レポート生成
     */
    private static void executeMonthlyReports() {
        System.out.println("[" + new Date() + "] 月次レポート生成バッチを実行します");
        
        try {
            // 月次契約レポート
            generateMonthlyContractReport();
            System.out.println("月次契約レポートを生成しました");
            
            // 月次売上レポート
            generateMonthlySalesReport();
            System.out.println("月次売上レポートを生成しました");
            
            // 月次顧客分析レポート
            generateMonthlyCustomerAnalysis();
            System.out.println("月次顧客分析レポートを生成しました");
            
            System.out.println("[" + new Date() + "] 月次レポート生成バッチが正常に完了しました");
            
        } catch (Exception e) {
            System.err.println("[" + new Date() + "] 月次レポート生成中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 契約統計レポート生成
     */
    private static void generateContractStatsReport() throws SQLException {
        String reportDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = "contract_stats_" + reportDate + ".csv";
        
        String sql = "SELECT " +
                   "COUNT(*) as total_contracts, " +
                   "SUM(CASE WHEN contract_status = '承認' THEN 1 ELSE 0 END) as approved_count, " +
                   "SUM(CASE WHEN contract_status = '審査中' THEN 1 ELSE 0 END) as pending_count, " +
                   "SUM(CASE WHEN contract_status = '取消' THEN 1 ELSE 0 END) as cancelled_count, " +
                   "SUM(CASE WHEN contract_status = '失効' THEN 1 ELSE 0 END) as lapsed_count, " +
                   "AVG(monthly_premium) as avg_monthly_premium, " +
                   "SUM(insured_amount) as total_insured_amount " +
                   "FROM contracts";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                int totalContracts = rs.getInt("total_contracts");
                int approvedCount = rs.getInt("approved_count");
                int pendingCount = rs.getInt("pending_count");
                int cancelledCount = rs.getInt("cancelled_count");
                int lapsedCount = rs.getInt("lapsed_count");
                double avgPremium = rs.getDouble("avg_monthly_premium");
                double totalInsured = rs.getDouble("total_insured_amount");
                
                // ここでCSVファイルやデータベースに保存
                System.out.println("契約統計レポート: " + fileName);
                System.out.println("総契約数: " + totalContracts);
                System.out.println("承認契約: " + approvedCount);
                System.out.println("審査中: " + pendingCount);
                System.out.println("取消: " + cancelledCount);
                System.out.println("失効: " + lapsedCount);
                System.out.println("平均月額保険料: " + avgPremium);
                System.out.println("総保険金額: " + totalInsured);
            }
        }
    }
    
    /**
     * 資料請求統計レポート生成
     */
    private static void generateRequestStatsReport() throws SQLException {
        String reportDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = "request_stats_" + reportDate + ".csv";
        
        String sql = "SELECT " +
                   "COUNT(*) as total_requests, " +
                   "SUM(CASE WHEN request_status = '新規' THEN 1 ELSE 0 END) as new_count, " +
                   "SUM(CASE WHEN request_status = '処理中' THEN 1 ELSE 0 END) as processing_count, " +
                   "SUM(CASE WHEN request_status = '完了' THEN 1 ELSE 0 END) as completed_count, " +
                   "SUM(CASE WHEN request_status = '取消' THEN 1 ELSE 0 END) as cancelled_count, " +
                   "AVG(TIMESTAMPDIFF(DAY, created_at, COALESCE(completed_date, NOW()))) as avg_processing_days " +
                   "FROM document_requests";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                int totalRequests = rs.getInt("total_requests");
                int newCount = rs.getInt("new_count");
                int processingCount = rs.getInt("processing_count");
                int completedCount = rs.getInt("completed_count");
                int cancelledCount = rs.getInt("cancelled_count");
                double avgProcessingDays = rs.getDouble("avg_processing_days");
                
                System.out.println("資料請求統計レポート: " + fileName);
                System.out.println("総リクエスト数: " + totalRequests);
                System.out.println("新規: " + newCount);
                System.out.println("処理中: " + processingCount);
                System.out.println("完了: " + completedCount);
                System.out.println("取消: " + cancelledCount);
                System.out.println("平均処理日数: " + avgProcessingDays + "日");
            }
        }
    }
    
    /**
     * 売上統計レポート生成
     */
    private static void generateSalesReport() throws SQLException {
        String reportDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String fileName = "sales_report_" + reportDate + ".csv";
        
        String sql = "SELECT " +
                   "SUM(pr.amount) as total_sales, " +
                   "COUNT(pr.id) as total_payments, " +
                   "AVG(pr.amount) as avg_payment_amount, " +
                   "MIN(pr.payment_date) as first_payment_date, " +
                   "MAX(pr.payment_date) as last_payment_date " +
                   "FROM payment_records pr " +
                   "WHERE pr.payment_status = '完了' " +
                   "AND pr.payment_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                double totalSales = rs.getDouble("total_sales");
                int totalPayments = rs.getInt("total_payments");
                double avgPayment = rs.getDouble("avg_payment_amount");
                Date firstPayment = rs.getDate("first_payment_date");
                Date lastPayment = rs.getDate("last_payment_date");
                
                System.out.println("売上統計レポート: " + fileName);
                System.out.println("総売上: " + totalSales);
                System.out.println("総支払い件数: " + totalPayments);
                System.out.println("平均支払い金額: " + avgPayment);
                System.out.println("最初の支払い日: " + firstPayment);
                System.out.println("最後の支払い日: " + lastPayment);
            }
        }
    }
    
    /**
     * 月次契約レポート生成
     */
    private static void generateMonthlyContractReport() throws SQLException {
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        String fileName = "monthly_contracts_" + month + ".csv";
        
        String sql = "SELECT " +
                   "ip.product_name, " +
                   "COUNT(c.id) as contract_count, " +
                   "SUM(c.insured_amount) as total_insured_amount, " +
                   "SUM(c.monthly_premium) as total_monthly_premium, " +
                   "AVG(c.monthly_premium) as avg_monthly_premium " +
                   "FROM contracts c " +
                   "JOIN insurance_products ip ON c.product_id = ip.id " +
                   "WHERE YEAR(c.created_at) = YEAR(CURDATE()) " +
                   "AND MONTH(c.created_at) = MONTH(CURDATE()) " +
                   "GROUP BY ip.product_name " +
                   "ORDER BY contract_count DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("月次契約レポート: " + fileName);
            while (rs.next()) {
                String productName = rs.getString("product_name");
                int contractCount = rs.getInt("contract_count");
                double totalInsured = rs.getDouble("total_insured_amount");
                double totalPremium = rs.getDouble("total_monthly_premium");
                double avgPremium = rs.getDouble("avg_monthly_premium");
                
                System.out.println(productName + ": " + contractCount + "件, " + 
                                 totalInsured + "円, " + totalPremium + "円/月");
            }
        }
    }
    
    /**
     * 月次売上レポート生成
     */
    private static void generateMonthlySalesReport() throws SQLException {
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        String fileName = "monthly_sales_" + month + ".csv";
        
        String sql = "SELECT " +
                   "DATE_FORMAT(pr.payment_date, '%Y-%m-%d') as payment_day, " +
                   "COUNT(pr.id) as daily_payments, " +
                   "SUM(pr.amount) as daily_sales, " +
                   "AVG(pr.amount) as avg_daily_payment " +
                   "FROM payment_records pr " +
                   "WHERE pr.payment_status = '完了' " +
                   "AND YEAR(pr.payment_date) = YEAR(CURDATE()) " +
                   "AND MONTH(pr.payment_date) = MONTH(CURDATE()) " +
                   "GROUP BY payment_day " +
                   "ORDER BY payment_day";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("月次売上レポート: " + fileName);
            while (rs.next()) {
                String paymentDay = rs.getString("payment_day");
                int dailyPayments = rs.getInt("daily_payments");
                double dailySales = rs.getDouble("daily_sales");
                double avgPayment = rs.getDouble("avg_daily_payment");
                
                System.out.println(paymentDay + ": " + dailyPayments + "件, " + 
                                 dailySales + "円, 平均: " + avgPayment + "円");
            }
        }
    }
    
    /**
     * 月次顧客分析レポート生成
     */
    private static void generateMonthlyCustomerAnalysis() throws SQLException {
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        String fileName = "customer_analysis_" + month + ".csv";
        
        String sql = "SELECT " +
                   "c.gender, " +
                   "FLOOR((YEAR(CURDATE()) - YEAR(c.birth_date)) / 10) * 10 as age_group, " +
                   "COUNT(DISTINCT cu.id) as customer_count, " +
                   "AVG(co.monthly_premium) as avg_premium, " +
                   "AVG(co.insured_amount) as avg_insured_amount " +
                   "FROM contracts co " +
                   "JOIN customers cu ON co.customer_id = cu.id " +
                   "JOIN insured_persons c ON co.id = c.contract_id AND c.relationship = '本人' " +
                   "WHERE YEAR(co.created_at) = YEAR(CURDATE()) " +
                   "AND MONTH(co.created_at) = MONTH(CURDATE()) " +
                   "GROUP BY c.gender, age_group " +
                   "ORDER BY c.gender, age_group";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("月次顧客分析レポート: " + fileName);
            while (rs.next()) {
                String gender = rs.getString("gender");
                int ageGroup = rs.getInt("age_group");
                int customerCount = rs.getInt("customer_count");
                double avgPremium = rs.getDouble("avg_premium");
                double avgInsured = rs.getDouble("avg_insured_amount");
                
                System.out.println(gender + " " + ageGroup + "代: " + customerCount + "人, " +
                                 "平均保険料: " + avgPremium + "円, " +
                                 "平均保険金額: " + avgInsured + "円");
            }
        }
    }
    
    /**
     * 週次初期遅延時間を計算（次の月曜日4時まで）
     */
    private static long getWeeklyInitialDelay() {
        long now = System.currentTimeMillis();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 4);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        }
        
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 月次初期遅延時間を計算（次の1日5時まで）
     */
    private static long getMonthlyInitialDelay() {
        long now = System.currentTimeMillis();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 5);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.MONTH, 1);
        }
        
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 手動でバッチ実行
     */
    public static void manualExecute() {
        System.out.println("手動でレポート生成バッチを実行します...");
        executeReportGeneration();
        executeMonthlyReports();
    }
}