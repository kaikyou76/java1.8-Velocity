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
 * 用于定期更新保险费率的批处理作业类
 */
public class PremiumUpdateBatch {
    
    // 创建一个单线程的调度执行器服务，用于执行定时任务
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // 创建保险费率DAO实例，用于访问保险费率数据
    private static PremiumRateDAO premiumRateDAO = new PremiumRateDAO();
    
    /**
     * バッチ処理を開始
     * 启动批处理作业
     */
    public static void startBatchProcessing() {
        // 输出启动批处理的日志信息
        System.out.println("保険料更新バッチ処理を開始します...");
        
        // 毎日深夜2時に実行
        // 每天凌晨2点执行保险费更新任务
        scheduler.scheduleAtFixedRate(
            PremiumUpdateBatch::executePremiumUpdate, // 要执行的任务方法
            getInitialDelay(), // 初始延迟时间，到凌晨2点的时间差
            24 * 60 * 60 * 1000, // 24時間 // 24小时周期（毫秒）
            TimeUnit.MILLISECONDS // 时间单位为毫秒
        );
        
        // 毎時0分にステータスチェック
        // 每小时0分执行状态检查任务
        scheduler.scheduleAtFixedRate(
            PremiumUpdateBatch::checkRequestStatus, // 要执行的任务方法
            getHourlyInitialDelay(), // 初始延迟时间，到下一个整点的时间差
            60 * 60 * 1000, // 1時間 // 1小时周期（毫秒）
            TimeUnit.MILLISECONDS // 时间单位为毫秒
        );
    }
    
    /**
     * バッチ処理を停止
     * 停止批处理作业
     */
    public static void stopBatchProcessing() {
        // 输出停止批处理的日志信息
        System.out.println("保険料更新バッチ処理を停止します...");
        // 关闭调度器
        scheduler.shutdown();
        try {
            // 等待最多60秒让已提交的任务执行完毕
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                // 如果超时则强制关闭所有任务
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 如果等待过程中被中断，则强制关闭所有任务
            scheduler.shutdownNow();
            // 恢复中断状态
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 保険料更新バッチ実行
     * 执行保险费更新批处理
     */
    private static void executePremiumUpdate() {
        // 输出执行批处理的日志信息，包含当前时间
        System.out.println("[" + new Date() + "] 保険料更新バッチを実行します");
        
        try {
            // 有効期限切れの料率を無効化
            // 使过期的费率失效
            int expiredCount = disableExpiredRates();
            System.out.println("無効化した料率: " + expiredCount + "件");
            
            // 新しい料率を有効化
            // 激活新的费率
            int activatedCount = activateNewRates();
            System.out.println("有効化した料率: " + activatedCount + "件");
            
            // 契約の保険料再計算
            // 重新计算合同的保险费
            int updatedCount = updateContractPremiums();
            System.out.println("更新した契約: " + updatedCount + "件");
            
            // 输出批处理成功完成的日志信息
            System.out.println("[" + new Date() + "] 保険料更新バッチが正常に完了しました");
            
        } catch (Exception e) {
            // 输出批处理执行过程中发生错误的日志信息
            System.err.println("[" + new Date() + "] バッチ処理中にエラーが発生しました: " + e.getMessage());
            // 打印异常堆栈跟踪
            e.printStackTrace();
        }
    }
    
    /**
     * リクエストステータスチェック
     * 检查请求状态
     */
    private static void checkRequestStatus() {
        // 输出执行状态检查的日志信息，包含当前时间
        System.out.println("[" + new Date() + "] リクエストステータスチェックを実行します");
        
        try {
            // 長時間処理中のリクエストをチェック
            // 检查长时间处理中的请求
            int staleCount = checkStaleRequests();
            if (staleCount > 0) {
                System.out.println("長時間処理中のリクエスト: " + staleCount + "件");
            }
            
            // フォローアップ日が過ぎたリクエストをチェック
            // 检查跟进日期已过的请求
            int overdueCount = checkOverdueFollowups();
            if (overdueCount > 0) {
                System.out.println("フォローアップ期限切れリクエスト: " + overdueCount + "件");
            }
            
        } catch (Exception e) {
            // 输出状态检查过程中发生错误的日志信息
            System.err.println("[" + new Date() + "] ステータスチェック中にエラーが発生しました: " + e.getMessage());
            // 打印异常堆栈跟踪
            e.printStackTrace();
        }
    }
    
    /**
     * 有効期限切れの料率を無効化
     * 使过期的费率失效
     * @return 失效的费率数量
     * @throws SQLException SQL异常
     */
    private static int disableExpiredRates() throws SQLException {
        // 定义更新SQL语句，将有效期限已过且不是昨天的费率设置为昨天
        String sql = "UPDATE premium_rates SET valid_to = CURDATE() - INTERVAL 1 DAY " +
                   "WHERE valid_to IS NOT NULL AND valid_to < CURDATE() AND valid_to != CURDATE() - INTERVAL 1 DAY";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 执行更新操作并返回影响的行数
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 新しい料率を有効化
     * 激活新的费率
     * @return 激活的费率数量
     * @throws SQLException SQL异常
     */
    private static int activateNewRates() throws SQLException {
        // 定义更新SQL语句，将有效开始日期小于等于当前日期且未激活的费率设置为当前日期
        String sql = "UPDATE premium_rates SET valid_from = CURDATE() " +
                   "WHERE valid_from <= CURDATE() AND valid_from != CURDATE() " +
                   "AND (valid_to IS NULL OR valid_to >= CURDATE())";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 执行更新操作并返回影响的行数
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 契約の保険料を再計算
     * 重新计算合同的保险费
     * @return 更新的合同数量
     * @throws SQLException SQL异常
     */
    private static int updateContractPremiums() throws SQLException {
        // 定义查询SQL语句，获取需要重新计算保险费的合同信息
        String sql = "SELECT c.id, c.product_id, c.insured_amount, " +
                   "ip.gender, ip.entry_age, ip.insurance_period " +
                   "FROM contracts c " +
                   "JOIN insured_persons ip ON c.id = ip.contract_id " +
                   "WHERE ip.relationship = '本人' " +
                   "AND c.contract_status IN ('承認', '審査中')";
        
        // 初始化更新合同计数器
        int updatedCount = 0;
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 遍历查询结果
            while (rs.next()) {
                // 获取合同ID
                int contractId = rs.getInt("id");
                // 获取产品ID
                int productId = rs.getInt("product_id");
                // 获取保险金额
                double insuredAmount = rs.getDouble("insured_amount");
                // 获取性别
                String gender = rs.getString("gender");
                // 获取加入年龄
                int entryAge = rs.getInt("entry_age");
                // 获取保险期间
                int insurancePeriod = rs.getInt("insurance_period");
                
                // 新しい保険料を計算
                // 计算新的保险费
                double newPremium = calculateNewPremium(productId, gender, entryAge, insurancePeriod, insuredAmount);
                
                // 如果计算出的保险费大于0，则更新合同
                if (newPremium > 0) {
                    // 契約を更新
                    // 更新合同
                    if (updateContractPremium(contractId, newPremium)) {
                        updatedCount++;
                    }
                }
            }
        }
        
        // 返回更新的合同数量
        return updatedCount;
    }
    
    /**
     * 新しい保険料を計算
     * 计算新的保险费
     * @param productId 产品ID
     * @param gender 性别
     * @param entryAge 加入年龄
     * @param insurancePeriod 保险期间
     * @param insuredAmount 保险金额
     * @return 计算出的保险费
     */
    private static double calculateNewPremium(int productId, String gender, int entryAge, 
                                            int insurancePeriod, double insuredAmount) {
        try {
            // ストアドプロシージャを使用して計算
            // 使用存储过程进行计算
            String sql = "CALL calculate_premium(?, ?, ?, ?, ?, ?, ?)";
            
            // 使用try-with-resources自动关闭数据库连接和预编译语句
            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareCall(sql)) {
                
                // 设置存储过程参数
                pstmt.setInt(1, productId); // 产品ID
                pstmt.setString(2, gender); // 性别
                pstmt.setInt(3, entryAge); // 加入年龄
                pstmt.setInt(4, insurancePeriod); // 保险期间
                pstmt.setDouble(5, insuredAmount); // 保险金额
                pstmt.registerOutParameter(6, java.sql.Types.DECIMAL); // 注册输出参数6（月保险费）
                pstmt.registerOutParameter(7, java.sql.Types.DECIMAL); // 注册输出参数7（年保险费）
                
                // 执行存储过程
                pstmt.execute();
                
                // 返回月保险费
                return pstmt.getDouble(6); // 月々保険料
            }
        } catch (SQLException e) {
            // 输出保险费计算错误的日志信息
            System.err.println("保険料計算エラー: " + e.getMessage());
            // 返回-1表示计算失败
            return -1;
        }
    }
    
    /**
     * 契約保険料を更新
     * 更新合同保险费
     * @param contractId 合同ID
     * @param newPremium 新保险费
     * @return 更新是否成功
     */
    private static boolean updateContractPremium(int contractId, double newPremium) {
        // 定义更新SQL语句，更新合同的月保险费和年保险费
        String sql = "UPDATE contracts SET monthly_premium = ?, annual_premium = ?, " +
                   "updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置更新参数
            pstmt.setDouble(1, newPremium); // 月保险费
            pstmt.setDouble(2, newPremium * 12); // 年保险费
            pstmt.setInt(3, contractId); // 合同ID
            
            // 执行更新操作并返回是否成功
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // 输出合同更新错误的日志信息
            System.err.println("契約更新エラー: " + e.getMessage());
            // 返回false表示更新失败
            return false;
        }
    }
    
    /**
     * 長時間処理中のリクエストをチェック
     * 检查长时间处理中的请求
     * @return 长时间处理中的请求数量
     * @throws SQLException SQL异常
     */
    private static int checkStaleRequests() throws SQLException {
        // 定义查询SQL语句，获取处理中且创建时间超过7天的请求
        String sql = "SELECT id, request_number, created_at " +
                   "FROM document_requests " +
                   "WHERE request_status = '処理中' " +
                   "AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY)";
        
        // 初始化长时间处理中请求数量计数器
        int staleCount = 0;
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 遍历查询结果
            while (rs.next()) {
                // 获取请求ID
                int requestId = rs.getInt("id");
                // 获取请求编号
                String requestNumber = rs.getString("request_number");
                // 获取创建时间
                Date createdAt = rs.getDate("created_at");
                
                // 输出长时间处理中的请求信息
                System.out.println("長時間処理中のリクエスト: " + requestNumber + " (作成日: " + createdAt + ")");
                // 增加长时间处理中请求数量
                staleCount++;
            }
        }
        
        // 返回长时间处理中的请求数量
        return staleCount;
    }
    
    /**
     * フォローアップ期限切れリクエストをチェック
     * 检查跟进日期已过的请求
     * @return 跟进日期已过的请求数量
     * @throws SQLException SQL异常
     */
    private static int checkOverdueFollowups() throws SQLException {
        // 定义查询SQL语句，获取跟进日期已过且状态不是完成或取消的请求
        String sql = "SELECT id, request_number, follow_up_date " +
                   "FROM document_requests " +
                   "WHERE follow_up_date IS NOT NULL " +
                   "AND follow_up_date < CURDATE() " +
                   "AND request_status NOT IN ('完了', '取消')";
        
        // 初始化跟进日期已过请求数量计数器
        int overdueCount = 0;
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 遍历查询结果
            while (rs.next()) {
                // 获取请求ID
                int requestId = rs.getInt("id");
                // 获取请求编号
                String requestNumber = rs.getString("request_number");
                // 获取跟进日期
                Date followUpDate = rs.getDate("follow_up_date");
                
                // 输出跟进日期已过的请求信息
                System.out.println("フォローアップ期限切れ: " + requestNumber + " (期限: " + followUpDate + ")");
                // 增加跟进日期已过请求数量
                overdueCount++;
            }
        }
        
        // 返回跟进日期已过的请求数量
        return overdueCount;
    }
    
    /**
     * 初期遅延時間を計算（次の深夜2時まで）
     * 计算初始延迟时间（到下一个凌晨2点）
     * @return 到下一个凌晨2点的延迟毫秒数
     */
    private static long getInitialDelay() {
        // 获取当前时间毫秒数
        long now = System.currentTimeMillis();
        // 创建日历实例
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // 设置小时为2点
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 2);
        // 设置分钟为0
        calendar.set(java.util.Calendar.MINUTE, 0);
        // 设置秒为0
        calendar.set(java.util.Calendar.SECOND, 0);
        // 设置毫秒为0
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        // 如果目标时间已过，则设置为第二天的2点
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        
        // 返回到目标时间的延迟毫秒数
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 時間単位の初期遅延時間を計算（次の0分まで）
     * 计算小时级的初始延迟时间（到下一个整点）
     * @return 到下一个整点的延迟毫秒数
     */
    private static long getHourlyInitialDelay() {
        // 获取当前时间毫秒数
        long now = System.currentTimeMillis();
        // 创建日历实例
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // 设置分钟为0
        calendar.set(java.util.Calendar.MINUTE, 0);
        // 设置秒为0
        calendar.set(java.util.Calendar.SECOND, 0);
        // 设置毫秒为0
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        // 设置为下一个小时
        calendar.add(java.util.Calendar.HOUR_OF_DAY, 1);
        
        // 返回到目标时间的延迟毫秒数
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 手動でバッチ実行
     * 手动执行批处理
     */
    public static void manualExecute() {
        // 输出手动执行批处理的日志信息
        System.out.println("手動でバッチ処理を実行します...");
        // 执行保险费更新
        executePremiumUpdate();
        // 执行请求状态检查
        checkRequestStatus();
    }
}