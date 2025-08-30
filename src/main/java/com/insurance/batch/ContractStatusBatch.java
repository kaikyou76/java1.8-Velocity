package com.insurance.batch;

import com.insurance.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 契約ステータス更新バッチ処理クラス
 * 定期的に契約ステータスを更新するバッチジョブ
 */
public class ContractStatusBatch {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * バッチ処理を開始
     */
    public static void startBatchProcessing() {
        System.out.println("契約ステータス更新バッチ処理を開始します...");
        
        // 毎日深夜3時に実行
        scheduler.scheduleAtFixedRate(
            ContractStatusBatch::executeStatusUpdate,
            getInitialDelay(3), // 3時
            24 * 60 * 60 * 1000, // 24時間
            TimeUnit.MILLISECONDS
        );
        
        // 毎時30分に支払いチェック
        scheduler.scheduleAtFixedRate(
            ContractStatusBatch::checkPaymentStatus,
            getMinuteInitialDelay(30),
            60 * 60 * 1000, // 1時間
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * バッチ処理を停止
     */
    public static void stopBatchProcessing() {
        System.out.println("契約ステータス更新バッチ処理を停止します...");
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
     * 契約ステータス更新バッチ実行
     */
    private static void executeStatusUpdate() {
        System.out.println("[" + new Date() + "] 契約ステータス更新バッチを実行します");
        
        try {
            // 審査期限切れの契約を取消
            int expiredCount = cancelExpiredContracts();
            System.out.println("取消した審査期限切れ契約: " + expiredCount + "件");
            
            // 支払い期限切れの契約を失効
            int lapsedCount = lapseOverdueContracts();
            System.out.println("失効した支払い期限切れ契約: " + lapsedCount + "件");
            
            // 満期契約を完了
            int maturedCount = completeMaturedContracts();
            System.out.println("完了した満期契約: " + maturedCount + "件");
            
            System.out.println("[" + new Date() + "] 契約ステータス更新バッチが正常に完了しました");
            
        } catch (Exception e) {
            System.err.println("[" + new Date() + "] バッチ処理中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 支払いステータスチェック
     */
    private static void checkPaymentStatus() {
        System.out.println("[" + new Date() + "] 支払いステータスチェックを実行します");
        
        try {
            // 支払い遅延の契約をチェック
            int overdueCount = checkOverduePayments();
            if (overdueCount > 0) {
                System.out.println("支払い遅延契約: " + overdueCount + "件");
            }
            
            // 支払い催促が必要な契約をチェック
            int reminderCount = checkPaymentReminders();
            if (reminderCount > 0) {
                System.out.println("支払い催促必要契約: " + reminderCount + "件");
            }
            
        } catch (Exception e) {
            System.err.println("[" + new Date() + "] 支払いチェック中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 審査期限切れの契約を取消
     */
    private static int cancelExpiredContracts() throws SQLException {
        String sql = "UPDATE contracts SET contract_status = '取消', cancellation_reason = '審査期限切れ', " +
                   "cancellation_date = CURDATE(), updated_at = CURRENT_TIMESTAMP " +
                   "WHERE contract_status = '審査中' " +
                   "AND created_at < DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 支払い期限切れの契約を失効
     */
    private static int lapseOverdueContracts() throws SQLException {
        String sql = "UPDATE contracts SET contract_status = '失効', lapse_date = CURDATE(), " +
                   "updated_at = CURRENT_TIMESTAMP " +
                   "WHERE contract_status = '承認' " +
                   "AND last_payment_date < DATE_SUB(CURDATE(), INTERVAL 60 DAY)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 満期契約を完了
     */
    private static int completeMaturedContracts() throws SQLException {
        String sql = "UPDATE contracts SET contract_status = '満期', maturity_date = CURDATE(), " +
                   "updated_at = CURRENT_TIMESTAMP " +
                   "WHERE contract_status = '承認' " +
                   "AND maturity_date <= CURDATE()";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 支払い遅延の契約をチェック
     */
    private static int checkOverduePayments() throws SQLException {
        String sql = "SELECT c.id, c.contract_number, c.customer_id, cu.customer_name, " +
                   "c.last_payment_date, DATEDIFF(CURDATE(), c.last_payment_date) as days_overdue " +
                   "FROM contracts c " +
                   "JOIN customers cu ON c.customer_id = cu.id " +
                   "WHERE c.contract_status = '承認' " +
                   "AND c.last_payment_date < DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        
        int overdueCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int contractId = rs.getInt("id");
                String contractNumber = rs.getString("contract_number");
                String customerName = rs.getString("customer_name");
                int daysOverdue = rs.getInt("days_overdue");
                
                System.out.println("支払い遅延契約: " + contractNumber + " - " + customerName + 
                                 " (遅延日数: " + daysOverdue + "日)");
                overdueCount++;
            }
        }
        
        return overdueCount;
    }
    
    /**
     * 支払い催促が必要な契約をチェック
     */
    private static int checkPaymentReminders() throws SQLException {
        String sql = "SELECT c.id, c.contract_number, c.customer_id, cu.customer_name, " +
                   "c.last_payment_date, DATEDIFF(CURDATE(), c.last_payment_date) as days_since_last_payment " +
                   "FROM contracts c " +
                   "JOIN customers cu ON c.customer_id = cu.id " +
                   "WHERE c.contract_status = '承認' " +
                   "AND c.last_payment_date < DATE_SUB(CURDATE(), INTERVAL 15 DAY)";
        
        int reminderCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int contractId = rs.getInt("id");
                String contractNumber = rs.getString("contract_number");
                String customerName = rs.getString("customer_name");
                int daysSincePayment = rs.getInt("days_since_last_payment");
                
                System.out.println("支払い催促必要契約: " + contractNumber + " - " + customerName + 
                                 " (最終支払いから: " + daysSincePayment + "日)");
                reminderCount++;
            }
        }
        
        return reminderCount;
    }
    
    /**
     * 初期遅延時間を計算（指定時間まで）
     */
    private static long getInitialDelay(int targetHour) {
        long now = System.currentTimeMillis();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, targetHour);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 分単位の初期遅延時間を計算（次の指定分まで）
     */
    private static long getMinuteInitialDelay(int targetMinute) {
        long now = System.currentTimeMillis();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.MINUTE, targetMinute);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.HOUR_OF_DAY, 1);
        }
        
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 手動でバッチ実行
     */
    public static void manualExecute() {
        System.out.println("手動で契約ステータス更新バッチを実行します...");
        executeStatusUpdate();
        checkPaymentStatus();
    }
}