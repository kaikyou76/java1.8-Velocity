package com.insurance.batch;

import com.insurance.dao.PremiumRateDAO;
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
 * 保険料更新バッチ処理クラス
 * 定期的に保険料率を更新するバッチジョブ
 */
public class PremiumUpdateBatch {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static PremiumRateDAO premiumRateDAO = new PremiumRateDAO();
    
    /**
     * バッチ処理を開始
     */
    public static void startBatchProcessing() {
        System.out.println("保険料更新バッチ処理を開始します...");
        
        // 毎日深夜2時に実行
        scheduler.scheduleAtFixedRate(
            PremiumUpdateBatch::executePremiumUpdate,
            getInitialDelay(),
            24 * 60 * 60 * 1000, // 24時間
            TimeUnit.MILLISECONDS
        );
        
        // 毎時0分にステータスチェック
        scheduler.scheduleAtFixedRate(
            PremiumUpdateBatch::checkRequestStatus,
            getHourlyInitialDelay(),
            60 * 60 * 1000, // 1時間
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * バッチ処理を停止
     */
    public static void stopBatchProcessing() {
        System.out.println("保険料更新バッチ処理を停止します...");
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
     * 保険料更新バッチ実行
     */
    private static void executePremiumUpdate() {
        System.out.println("[" + new Date() + "] 保険料更新バッチを実行します");
        
        try {
            // 有効期限切れの料率を無効化
            int expiredCount = disableExpiredRates();
            System.out.println("無効化した料率: " + expiredCount + "件");
            
            // 新しい料率を有効化
            int activatedCount = activateNewRates();
            System.out.println("有効化した料率: " + activatedCount + "件");
            
            // 契約の保険料再計算
            int updatedCount = updateContractPremiums();
            System.out.println("更新した契約: " + updatedCount + "件");
            
            System.out.println("[" + new Date() + "] 保険料更新バッチが正常に完了しました");
            
        } catch (Exception e) {
            System.err.println("[" + new Date() + "] バッチ処理中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * リクエストステータスチェック
     */
    private static void checkRequestStatus() {
        System.out.println("[" + new Date() + "] リクエストステータスチェックを実行します");
        
        try {
            // 長時間処理中のリクエストをチェック
            int staleCount = checkStaleRequests();
            if (staleCount > 0) {
                System.out.println("長時間処理中のリクエスト: " + staleCount + "件");
            }
            
            // フォローアップ日が過ぎたリクエストをチェック
            int overdueCount = checkOverdueFollowups();
            if (overdueCount > 0) {
                System.out.println("フォローアップ期限切れリクエスト: " + overdueCount + "件");
            }
            
        } catch (Exception e) {
            System.err.println("[" + new Date() + "] ステータスチェック中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 有効期限切れの料率を無効化
     */
    private static int disableExpiredRates() throws SQLException {
        String sql = "UPDATE premium_rates SET valid_to = CURDATE() - INTERVAL 1 DAY " +
                   "WHERE valid_to IS NOT NULL AND valid_to < CURDATE() AND valid_to != CURDATE() - INTERVAL 1 DAY";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 新しい料率を有効化
     */
    private static int activateNewRates() throws SQLException {
        String sql = "UPDATE premium_rates SET valid_from = CURDATE() " +
                   "WHERE valid_from <= CURDATE() AND valid_from != CURDATE() " +
                   "AND (valid_to IS NULL OR valid_to >= CURDATE())";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 契約の保険料を再計算
     */
    private static int updateContractPremiums() throws SQLException {
        String sql = "SELECT c.id, c.product_id, c.insured_amount, " +
                   "ip.gender, ip.entry_age, ip.insurance_period " +
                   "FROM contracts c " +
                   "JOIN insured_persons ip ON c.id = ip.contract_id " +
                   "WHERE ip.relationship = '本人' " +
                   "AND c.contract_status IN ('承認', '審査中')";
        
        int updatedCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int contractId = rs.getInt("id");
                int productId = rs.getInt("product_id");
                double insuredAmount = rs.getDouble("insured_amount");
                String gender = rs.getString("gender");
                int entryAge = rs.getInt("entry_age");
                int insurancePeriod = rs.getInt("insurance_period");
                
                // 新しい保険料を計算
                double newPremium = calculateNewPremium(productId, gender, entryAge, insurancePeriod, insuredAmount);
                
                if (newPremium > 0) {
                    // 契約を更新
                    if (updateContractPremium(contractId, newPremium)) {
                        updatedCount++;
                    }
                }
            }
        }
        
        return updatedCount;
    }
    
    /**
     * 新しい保険料を計算
     */
    private static double calculateNewPremium(int productId, String gender, int entryAge, 
                                            int insurancePeriod, double insuredAmount) {
        try {
            // ストアドプロシージャを使用して計算
            String sql = "CALL calculate_premium(?, ?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareCall(sql)) {
                
                pstmt.setInt(1, productId);
                pstmt.setString(2, gender);
                pstmt.setInt(3, entryAge);
                pstmt.setInt(4, insurancePeriod);
                pstmt.setDouble(5, insuredAmount);
                pstmt.registerOutParameter(6, java.sql.Types.DECIMAL);
                pstmt.registerOutParameter(7, java.sql.Types.DECIMAL);
                
                pstmt.execute();
                
                return pstmt.getDouble(6); // 月々保険料
            }
        } catch (SQLException e) {
            System.err.println("保険料計算エラー: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * 契約保険料を更新
     */
    private static boolean updateContractPremium(int contractId, double newPremium) {
        String sql = "UPDATE contracts SET monthly_premium = ?, annual_premium = ?, " +
                   "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newPremium);
            pstmt.setDouble(2, newPremium * 12);
            pstmt.setInt(3, contractId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("契約更新エラー: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 長時間処理中のリクエストをチェック
     */
    private static int checkStaleRequests() throws SQLException {
        String sql = "SELECT id, request_number, created_at " +
                   "FROM document_requests " +
                   "WHERE request_status = '処理中' " +
                   "AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY)";
        
        int staleCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int requestId = rs.getInt("id");
                String requestNumber = rs.getString("request_number");
                Date createdAt = rs.getDate("created_at");
                
                System.out.println("長時間処理中のリクエスト: " + requestNumber + " (作成日: " + createdAt + ")");
                staleCount++;
            }
        }
        
        return staleCount;
    }
    
    /**
     * フォローアップ期限切れリクエストをチェック
     */
    private static int checkOverdueFollowups() throws SQLException {
        String sql = "SELECT id, request_number, follow_up_date " +
                   "FROM document_requests " +
                   "WHERE follow_up_date IS NOT NULL " +
                   "AND follow_up_date < CURDATE() " +
                   "AND request_status NOT IN ('完了', '取消')";
        
        int overdueCount = 0;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                int requestId = rs.getInt("id");
                String requestNumber = rs.getString("request_number");
                Date followUpDate = rs.getDate("follow_up_date");
                
                System.out.println("フォローアップ期限切れ: " + requestNumber + " (期限: " + followUpDate + ")");
                overdueCount++;
            }
        }
        
        return overdueCount;
    }
    
    /**
     * 初期遅延時間を計算（次の深夜2時まで）
     */
    private static long getInitialDelay() {
        long now = System.currentTimeMillis();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 2);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 時間単位の初期遅延時間を計算（次の0分まで）
     */
    private static long getHourlyInitialDelay() {
        long now = System.currentTimeMillis();
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        calendar.add(java.util.Calendar.HOUR_OF_DAY, 1);
        
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 手動でバッチ実行
     */
    public static void manualExecute() {
        System.out.println("手動でバッチ処理を実行します...");
        executePremiumUpdate();
        checkRequestStatus();
    }
}