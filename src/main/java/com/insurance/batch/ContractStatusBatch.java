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
 * 用于定期更新合同状态的批处理作业类
 */
public class ContractStatusBatch {
    
    // 创建一个单线程的调度执行器服务，用于执行定时任务
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * バッチ処理を開始
     * 启动批处理作业
     */
    public static void startBatchProcessing() {
        // 输出启动批处理的日志信息
        System.out.println("契約ステータス更新バッチ処理を開始します...");
        
        // 毎日深夜3時に実行
        // 每天凌晨3点执行合同状态更新任务
        scheduler.scheduleAtFixedRate(
            ContractStatusBatch::executeStatusUpdate, // 要执行的任务方法
            getInitialDelay(3), // 3时 // 初始延迟时间，到凌晨3点的时间差
            24 * 60 * 60 * 1000, // 24時間 // 24小时周期（毫秒）
            TimeUnit.MILLISECONDS // 时间单位为毫秒
        );
        
        // 毎時30分に支払いチェック
        // 每小时30分执行付款状态检查任务
        scheduler.scheduleAtFixedRate(
            ContractStatusBatch::checkPaymentStatus, // 要执行的任务方法
            getMinuteInitialDelay(30), // 初始延迟时间，到下一个30分的时间差
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
        System.out.println("契約ステータス更新バッチ処理を停止します...");
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
     * 契約ステータス更新バッチ実行
     * 执行合同状态更新批处理
     */
    private static void executeStatusUpdate() {
        // 输出执行批处理的日志信息，包含当前时间
        System.out.println("[" + new Date() + "] 契約ステータス更新バッチを実行します");
        
        try {
            // 審査期限切れの契約を取消
            // 取消审查期限已过的合同
            int expiredCount = cancelExpiredContracts();
            System.out.println("取消した審査期限切れ契約: " + expiredCount + "件");
            
            // 支払い期限切れの契約を失効
            // 使付款期限已过的合同失效
            int lapsedCount = lapseOverdueContracts();
            System.out.println("失効した支払い期限切れ契約: " + lapsedCount + "件");
            
            // 満期契約を完了
            // 完成到期的合同
            int maturedCount = completeMaturedContracts();
            System.out.println("完了した満期契約: " + maturedCount + "件");
            
            // 输出批处理成功完成的日志信息
            System.out.println("[" + new Date() + "] 契約ステータス更新バッチが正常に完了しました");
            
        } catch (Exception e) {
            // 输出批处理执行过程中发生错误的日志信息
            System.err.println("[" + new Date() + "] バッチ処理中にエラーが発生しました: " + e.getMessage());
            // 打印异常堆栈跟踪
            e.printStackTrace();
        }
    }
    
    /**
     * 支払いステータスチェック
     * 检查付款状态
     */
    private static void checkPaymentStatus() {
        // 输出执行付款状态检查的日志信息，包含当前时间
        System.out.println("[" + new Date() + "] 支払いステータスチェックを実行します");
        
        try {
            // 支払い遅延の契約をチェック
            // 检查付款延迟的合同
            int overdueCount = checkOverduePayments();
            if (overdueCount > 0) {
                System.out.println("支払い遅延契約: " + overdueCount + "件");
            }
            
            // 支払い催促が必要な契約をチェック
            // 检查需要付款催促的合同
            int reminderCount = checkPaymentReminders();
            if (reminderCount > 0) {
                System.out.println("支払い催促必要契約: " + reminderCount + "件");
            }
            
        } catch (Exception e) {
            // 输出付款检查过程中发生错误的日志信息
            System.err.println("[" + new Date() + "] 支払いチェック中にエラーが発生しました: " + e.getMessage());
            // 打印异常堆栈跟踪
            e.printStackTrace();
        }
    }
    
    /**
     * 審査期限切れの契約を取消
     * 取消审查期限已过的合同
     * @return 取消的合同数量
     * @throws SQLException SQL异常
     */
    private static int cancelExpiredContracts() throws SQLException {
        // 定义更新SQL语句，将审查中且创建时间超过30天的合同状态更新为取消
        String sql = "UPDATE contracts SET contract_status = '取消', cancellation_reason = '審査期限切れ', " +
                   "cancellation_date = CURDATE(), updated_at = CURRENT_TIMESTAMP " +
                   "WHERE contract_status = '審査中' " +
                   "AND created_at < DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 执行更新操作并返回影响的行数
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 支払い期限切れの契約を失効
     * 使付款期限已过的合同失效
     * @return 失效的合同数量
     * @throws SQLException SQL异常
     */
    private static int lapseOverdueContracts() throws SQLException {
        // 定义更新SQL语句，将承认可且最后付款日期超过60天的合同状态更新为失效
        String sql = "UPDATE contracts SET contract_status = '失効', lapse_date = CURDATE(), " +
                   "updated_at = CURRENT_TIMESTAMP " +
                   "WHERE contract_status = '承認' " +
                   "AND last_payment_date < DATE_SUB(CURDATE(), INTERVAL 60 DAY)";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 执行更新操作并返回影响的行数
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 満期契約を完了
     * 完成到期的合同
     * @return 完成的合同数量
     * @throws SQLException SQL异常
     */
    private static int completeMaturedContracts() throws SQLException {
        // 定义更新SQL语句，将承认可且到期日小于等于当前日期的合同状态更新为满期
        String sql = "UPDATE contracts SET contract_status = '満期', maturity_date = CURDATE(), " +
                   "updated_at = CURRENT_TIMESTAMP " +
                   "WHERE contract_status = '承認' " +
                   "AND maturity_date <= CURDATE()";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 执行更新操作并返回影响的行数
            return pstmt.executeUpdate();
        }
    }
    
    /**
     * 支払い遅延の契約をチェック
     * 检查付款延迟的合同
     * @return 付款延迟的合同数量
     * @throws SQLException SQL异常
     */
    private static int checkOverduePayments() throws SQLException {
        // 定义查询SQL语句，获取承认可且最后付款日期超过30天的合同信息
        String sql = "SELECT c.id, c.contract_number, c.customer_id, cu.customer_name, " +
                   "c.last_payment_date, DATEDIFF(CURDATE(), c.last_payment_date) as days_overdue " +
                   "FROM contracts c " +
                   "JOIN customers cu ON c.customer_id = cu.id " +
                   "WHERE c.contract_status = '承認' " +
                   "AND c.last_payment_date < DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        
        // 初始化延迟合同计数器
        int overdueCount = 0;
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 遍历查询结果
            while (rs.next()) {
                // 获取合同ID
                int contractId = rs.getInt("id");
                // 获取合同编号
                String contractNumber = rs.getString("contract_number");
                // 获取客户名称
                String customerName = rs.getString("customer_name");
                // 获取延迟天数
                int daysOverdue = rs.getInt("days_overdue");
                
                // 输出延迟合同信息
                System.out.println("支払い遅延契約: " + contractNumber + " - " + customerName + 
                                 " (遅延日数: " + daysOverdue + "日)");
                // 增加延迟合同计数
                overdueCount++;
            }
        }
        
        // 返回延迟合同数量
        return overdueCount;
    }
    
    /**
     * 支払い催促が必要な契約をチェック
     * 检查需要付款催促的合同
     * @return 需要付款催促的合同数量
     * @throws SQLException SQL异常
     */
    private static int checkPaymentReminders() throws SQLException {
        // 定义查询SQL语句，获取承认可且最后付款日期超过15天的合同信息
        String sql = "SELECT c.id, c.contract_number, c.customer_id, cu.customer_name, " +
                   "c.last_payment_date, DATEDIFF(CURDATE(), c.last_payment_date) as days_since_last_payment " +
                   "FROM contracts c " +
                   "JOIN customers cu ON c.customer_id = cu.id " +
                   "WHERE c.contract_status = '承認' " +
                   "AND c.last_payment_date < DATE_SUB(CURDATE(), INTERVAL 15 DAY)";
        
        // 初始化催促合同计数器
        int reminderCount = 0;
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 遍历查询结果
            while (rs.next()) {
                // 获取合同ID
                int contractId = rs.getInt("id");
                // 获取合同编号
                String contractNumber = rs.getString("contract_number");
                // 获取客户名称
                String customerName = rs.getString("customer_name");
                // 获取自最后付款以来的天数
                int daysSincePayment = rs.getInt("days_since_last_payment");
                
                // 输出需要催促的合同信息
                System.out.println("支払い催促必要契約: " + contractNumber + " - " + customerName + 
                                 " (最終支払いから: " + daysSincePayment + "日)");
                // 增加催促合同计数
                reminderCount++;
            }
        }
        
        // 返回需要催促的合同数量
        return reminderCount;
    }
    
    /**
     * 初期遅延時間を計算（指定時間まで）
     * 计算初始延迟时间（到指定小时）
     * @param targetHour 目标小时
     * @return 到目标时间的延迟毫秒数
     */
    private static long getInitialDelay(int targetHour) {
        // 获取当前时间毫秒数
        long now = System.currentTimeMillis();
        // 创建日历实例
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // 设置目标小时
        calendar.set(java.util.Calendar.HOUR_OF_DAY, targetHour);
        // 设置分钟为0
        calendar.set(java.util.Calendar.MINUTE, 0);
        // 设置秒为0
        calendar.set(java.util.Calendar.SECOND, 0);
        // 设置毫秒为0
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        // 如果目标时间已过，则设置为第二天的目标时间
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        
        // 返回到目标时间的延迟毫秒数
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 分単位の初期遅延時間を計算（次の指定分まで）
     * 计算分钟级的初始延迟时间（到下一个指定分钟）
     * @param targetMinute 目标分钟
     * @return 到目标时间的延迟毫秒数
     */
    private static long getMinuteInitialDelay(int targetMinute) {
        // 获取当前时间毫秒数
        long now = System.currentTimeMillis();
        // 创建日历实例
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // 设置目标分钟
        calendar.set(java.util.Calendar.MINUTE, targetMinute);
        // 设置秒为0
        calendar.set(java.util.Calendar.SECOND, 0);
        // 设置毫秒为0
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        // 如果目标时间已过，则设置为下一个小时的目标时间
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.HOUR_OF_DAY, 1);
        }
        
        // 返回到目标时间的延迟毫秒数
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 手動でバッチ実行
     * 手动执行批处理
     */
    public static void manualExecute() {
        // 输出手动执行批处理的日志信息
        System.out.println("手動で契約ステータス更新バッチを実行します...");
        // 执行合同状态更新
        executeStatusUpdate();
        // 执行付款状态检查
        checkPaymentStatus();
    }
}