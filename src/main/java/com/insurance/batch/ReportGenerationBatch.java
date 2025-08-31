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
 * 用于定期生成各种报告的批处理作业类
 */
public class ReportGenerationBatch {
    
    // 创建一个单线程的调度执行器服务，用于执行定时任务
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * バッチ処理を開始
     * 启动批处理作业
     */
    public static void startBatchProcessing() {
        // 输出启动批处理的日志信息
        System.out.println("レポート生成バッチ処理を開始します...");
        
        // 毎週月曜日深夜4時に実行
        // 每周周一凌晨4点执行报告生成任务
        scheduler.scheduleAtFixedRate(
            ReportGenerationBatch::executeReportGeneration, // 要执行的任务方法
            getWeeklyInitialDelay(), // 初始延迟时间，到下一个周一凌晨4点的时间差
            7 * 24 * 60 * 60 * 1000, // 1週間 // 1周周期（毫秒）
            TimeUnit.MILLISECONDS // 时间单位为毫秒
        );
        
        // 毎月1日深夜5時に月次レポート
        // 每月1日凌晨5点执行月度报告生成任务
        scheduler.scheduleAtFixedRate(
            ReportGenerationBatch::executeMonthlyReports, // 要执行的任务方法
            getMonthlyInitialDelay(), // 初始延迟时间，到下个月1日凌晨5点的时间差
            30 * 24 * 60 * 60 * 1000L, // 約1ヶ月 // 约1个月周期（毫秒）
            TimeUnit.MILLISECONDS // 时间单位为毫秒
        );
    }
    
    /**
     * バッチ処理を停止
     * 停止批处理作业
     */
    public static void stopBatchProcessing() {
        // 输出停止批处理的日志信息
        System.out.println("レポート生成バッチ処理を停止します...");
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
     * レポート生成バッチ実行
     * 执行报告生成批处理
     */
    private static void executeReportGeneration() {
        // 输出执行批处理的日志信息，包含当前时间
        System.out.println("[" + new Date() + "] 週次レポート生成バッチを実行します");
        
        try {
            // 契約統計レポート
            // 生成合同统计报告
            generateContractStatsReport();
            System.out.println("契約統計レポートを生成しました");
            
            // 資料請求統計レポート
            // 生成资料请求统计报告
            generateRequestStatsReport();
            System.out.println("資料請求統計レポートを生成しました");
            
            // 売上統計レポート
            // 生成销售统计报告
            generateSalesReport();
            System.out.println("売上統計レポートを生成しました");
            
            // 输出批处理成功完成的日志信息
            System.out.println("[" + new Date() + "] 週次レポート生成バッチが正常に完了しました");
            
        } catch (Exception e) {
            // 输出批处理执行过程中发生错误的日志信息
            System.err.println("[" + new Date() + "] レポート生成中にエラーが発生しました: " + e.getMessage());
            // 打印异常堆栈跟踪
            e.printStackTrace();
        }
    }
    
    /**
     * 月次レポート生成
     * 生成月度报告
     */
    private static void executeMonthlyReports() {
        // 输出执行月度报告生成的日志信息，包含当前时间
        System.out.println("[" + new Date() + "] 月次レポート生成バッチを実行します");
        
        try {
            // 月次契約レポート
            // 生成月度合同报告
            generateMonthlyContractReport();
            System.out.println("月次契約レポートを生成しました");
            
            // 月次売上レポート
            // 生成月度销售报告
            generateMonthlySalesReport();
            System.out.println("月次売上レポートを生成しました");
            
            // 月次顧客分析レポート
            // 生成月度客户分析报告
            generateMonthlyCustomerAnalysis();
            System.out.println("月次顧客分析レポートを生成しました");
            
            // 输出月度报告生成成功完成的日志信息
            System.out.println("[" + new Date() + "] 月次レポート生成バッチが正常に完了しました");
            
        } catch (Exception e) {
            // 输出月度报告生成过程中发生错误的日志信息
            System.err.println("[" + new Date() + "] 月次レポート生成中にエラーが発生しました: " + e.getMessage());
            // 打印异常堆栈跟踪
            e.printStackTrace();
        }
    }
    
    /**
     * 契約統計レポート生成
     * 生成合同统计报告
     * @throws SQLException SQL异常
     */
    private static void generateContractStatsReport() throws SQLException {
        // 生成报告日期字符串
        String reportDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 生成文件名
        String fileName = "contract_stats_" + reportDate + ".csv";
        
        // 定义查询SQL语句，获取合同统计数据
        String sql = "SELECT " +
                   "COUNT(*) as total_contracts, " +
                   "SUM(CASE WHEN contract_status = '承認' THEN 1 ELSE 0 END) as approved_count, " +
                   "SUM(CASE WHEN contract_status = '審査中' THEN 1 ELSE 0 END) as pending_count, " +
                   "SUM(CASE WHEN contract_status = '取消' THEN 1 ELSE 0 END) as cancelled_count, " +
                   "SUM(CASE WHEN contract_status = '失効' THEN 1 ELSE 0 END) as lapsed_count, " +
                   "AVG(monthly_premium) as avg_monthly_premium, " +
                   "SUM(insured_amount) as total_insured_amount " +
                   "FROM contracts";
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 如果有查询结果
            if (rs.next()) {
                // 获取总合同数
                int totalContracts = rs.getInt("total_contracts");
                // 获取已批准合同数
                int approvedCount = rs.getInt("approved_count");
                // 获取审查中合同数
                int pendingCount = rs.getInt("pending_count");
                // 获取已取消合同数
                int cancelledCount = rs.getInt("cancelled_count");
                // 获取已失效合同数
                int lapsedCount = rs.getInt("lapsed_count");
                // 获取平均月保险费
                double avgPremium = rs.getDouble("avg_monthly_premium");
                // 获取总保险金额
                double totalInsured = rs.getDouble("total_insured_amount");
                
                // ここでCSVファイルやデータベースに保存
                // 在这里保存到CSV文件或数据库
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
     * 生成资料请求统计报告
     * @throws SQLException SQL异常
     */
    private static void generateRequestStatsReport() throws SQLException {
        // 生成报告日期字符串
        String reportDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 生成文件名
        String fileName = "request_stats_" + reportDate + ".csv";
        
        // 定义查询SQL语句，获取资料请求统计数据
        String sql = "SELECT " +
                   "COUNT(*) as total_requests, " +
                   "SUM(CASE WHEN request_status = '新規' THEN 1 ELSE 0 END) as new_count, " +
                   "SUM(CASE WHEN request_status = '処理中' THEN 1 ELSE 0 END) as processing_count, " +
                   "SUM(CASE WHEN request_status = '完了' THEN 1 ELSE 0 END) as completed_count, " +
                   "SUM(CASE WHEN request_status = '取消' THEN 1 ELSE 0 END) as cancelled_count, " +
                   "AVG(TIMESTAMPDIFF(DAY, created_at, COALESCE(completed_date, NOW()))) as avg_processing_days " +
                   "FROM document_requests";
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 如果有查询结果
            if (rs.next()) {
                // 获取总请求数
                int totalRequests = rs.getInt("total_requests");
                // 获取新请求数量
                int newCount = rs.getInt("new_count");
                // 获取处理中请求数量
                int processingCount = rs.getInt("processing_count");
                // 获取已完成请求数量
                int completedCount = rs.getInt("completed_count");
                // 获取已取消请求数量
                int cancelledCount = rs.getInt("cancelled_count");
                // 获取平均处理天数
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
     * 生成销售统计报告
     * @throws SQLException SQL异常
     */
    private static void generateSalesReport() throws SQLException {
        // 生成报告日期字符串
        String reportDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // 生成文件名
        String fileName = "sales_report_" + reportDate + ".csv";
        
        // 定义查询SQL语句，获取销售统计数据
        String sql = "SELECT " +
                   "SUM(pr.amount) as total_sales, " +
                   "COUNT(pr.id) as total_payments, " +
                   "AVG(pr.amount) as avg_payment_amount, " +
                   "MIN(pr.payment_date) as first_payment_date, " +
                   "MAX(pr.payment_date) as last_payment_date " +
                   "FROM payment_records pr " +
                   "WHERE pr.payment_status = '完了' " +
                   "AND pr.payment_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 如果有查询结果
            if (rs.next()) {
                // 获取总销售额
                double totalSales = rs.getDouble("total_sales");
                // 获取总付款笔数
                int totalPayments = rs.getInt("total_payments");
                // 获取平均付款金额
                double avgPayment = rs.getDouble("avg_payment_amount");
                // 获取首次付款日期
                Date firstPayment = rs.getDate("first_payment_date");
                // 获取最后付款日期
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
     * 生成月度合同报告
     * @throws SQLException SQL异常
     */
    private static void generateMonthlyContractReport() throws SQLException {
        // 生成月份字符串
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        // 生成文件名
        String fileName = "monthly_contracts_" + month + ".csv";
        
        // 定义查询SQL语句，获取月度合同数据
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
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("月次契約レポート: " + fileName);
            // 遍历查询结果
            while (rs.next()) {
                // 获取产品名称
                String productName = rs.getString("product_name");
                // 获取合同数量
                int contractCount = rs.getInt("contract_count");
                // 获取总保险金额
                double totalInsured = rs.getDouble("total_insured_amount");
                // 获取总月保险费
                double totalPremium = rs.getDouble("total_monthly_premium");
                // 获取平均月保险费
                double avgPremium = rs.getDouble("avg_monthly_premium");
                
                System.out.println(productName + ": " + contractCount + "件, " + 
                                 totalInsured + "円, " + totalPremium + "円/月");
            }
        }
    }
    
    /**
     * 月次売上レポート生成
     * 生成月度销售报告
     * @throws SQLException SQL异常
     */
    private static void generateMonthlySalesReport() throws SQLException {
        // 生成月份字符串
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        // 生成文件名
        String fileName = "monthly_sales_" + month + ".csv";
        
        // 定义查询SQL语句，获取月度销售数据
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
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("月次売上レポート: " + fileName);
            // 遍历查询结果
            while (rs.next()) {
                // 获取付款日期
                String paymentDay = rs.getString("payment_day");
                // 获取日付款笔数
                int dailyPayments = rs.getInt("daily_payments");
                // 获取日销售额
                double dailySales = rs.getDouble("daily_sales");
                // 获取日平均付款金额
                double avgPayment = rs.getDouble("avg_daily_payment");
                
                System.out.println(paymentDay + ": " + dailyPayments + "件, " + 
                                 dailySales + "円, 平均: " + avgPayment + "円");
            }
        }
    }
    
    /**
     * 月次顧客分析レポート生成
     * 生成月度客户分析报告
     * @throws SQLException SQL异常
     */
    private static void generateMonthlyCustomerAnalysis() throws SQLException {
        // 生成月份字符串
        String month = new SimpleDateFormat("yyyy-MM").format(new Date());
        // 生成文件名
        String fileName = "customer_analysis_" + month + ".csv";
        
        // 定义查询SQL语句，获取月度客户分析数据
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
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            System.out.println("月次顧客分析レポート: " + fileName);
            // 遍历查询结果
            while (rs.next()) {
                // 获取性别
                String gender = rs.getString("gender");
                // 获取年龄组
                int ageGroup = rs.getInt("age_group");
                // 获取客户数量
                int customerCount = rs.getInt("customer_count");
                // 获取平均保险费
                double avgPremium = rs.getDouble("avg_premium");
                // 获取平均保险金额
                double avgInsured = rs.getDouble("avg_insured_amount");
                
                System.out.println(gender + " " + ageGroup + "代: " + customerCount + "人, " +
                                 "平均保険料: " + avgPremium + "円, " +
                                 "平均保険金額: " + avgInsured + "円");
            }
        }
    }
    
    /**
     * 週次初期遅延時間を計算（次の月曜日4時まで）
     * 计算周次初始延迟时间（到下一个周一凌晨4点）
     * @return 到下一个周一凌晨4点的延迟毫秒数
     */
    private static long getWeeklyInitialDelay() {
        // 获取当前时间毫秒数
        long now = System.currentTimeMillis();
        // 创建日历实例
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // 设置为周一
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        // 设置小时为4点
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 4);
        // 设置分钟为0
        calendar.set(java.util.Calendar.MINUTE, 0);
        // 设置秒为0
        calendar.set(java.util.Calendar.SECOND, 0);
        // 设置毫秒为0
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        // 如果目标时间已过，则设置为下一周的周一4点
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1);
        }
        
        // 返回到目标时间的延迟毫秒数
        return calendar.getTimeInMillis() - now;
    }
    
    /**
     * 月次初期遅延時間を計算（次の1日5時まで）
     * 计算月次初始延迟时间（到下个月1日凌晨5点）
     * @return 到下个月1日凌晨5点的延迟毫秒数
     */
    private static long getMonthlyInitialDelay() {
        // 获取当前时间毫秒数
        long now = System.currentTimeMillis();
        // 创建日历实例
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        // 设置为1日
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        // 设置小时为5点
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 5);
        // 设置分钟为0
        calendar.set(java.util.Calendar.MINUTE, 0);
        // 设置秒为0
        calendar.set(java.util.Calendar.SECOND, 0);
        // 设置毫秒为0
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        
        // 如果目标时间已过，则设置为下个月的1日5点
        if (calendar.getTimeInMillis() <= now) {
            calendar.add(java.util.Calendar.MONTH, 1);
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
        System.out.println("手動でレポート生成バッチを実行します...");
        // 执行周次报告生成
        executeReportGeneration();
        // 执行月度报告生成
        executeMonthlyReports();
    }
}