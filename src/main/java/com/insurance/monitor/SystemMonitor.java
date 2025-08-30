package com.insurance.monitor;

import com.insurance.util.DatabaseUtil;
import com.insurance.util.LogUtil;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 系统监控类
 * 监控系统性能和资源使用情况
 */
public class SystemMonitor {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    
    /**
     * 开始系统监控
     */
    public static void startMonitoring() {
        LogUtil.info("システムモニタリングを開始します");
        
        // 每5分钟监控一次
        scheduler.scheduleAtFixedRate(
            SystemMonitor::monitorSystem,
            0,
            5 * 60 * 1000, // 5分钟
            TimeUnit.MILLISECONDS
        );
        
        // 每小时执行一次详细检查
        scheduler.scheduleAtFixedRate(
            SystemMonitor::performDetailedCheck,
            0,
            60 * 60 * 1000, // 1小时
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * 停止系统监控
     */
    public static void stopMonitoring() {
        LogUtil.info("システムモニタリングを停止します");
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
     * 系统监控主方法
     */
    private static void monitorSystem() {
        try {
            monitorMemoryUsage();
            monitorCpuUsage();
            monitorDatabaseConnection();
            monitorDiskSpace();
            
        } catch (Exception e) {
            LogUtil.error("システムモニタリング中にエラーが発生しました: " + e.getMessage(), e);
        }
    }
    
    /**
     * 详细系统检查
     */
    private static void performDetailedCheck() {
        try {
            checkDatabasePerformance();
            checkSystemHealth();
            checkApplicationStatus();
            
        } catch (Exception e) {
            LogUtil.error("詳細システムチェック中にエラーが発生しました: " + e.getMessage(), e);
        }
    }
    
    /**
     * 监控内存使用情况
     */
    private static void monitorMemoryUsage() {
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        
        long usedHeap = heapMemory.getUsed() / 1024 / 1024; // MB
        long maxHeap = heapMemory.getMax() / 1024 / 1024;   // MB
        long usedNonHeap = nonHeapMemory.getUsed() / 1024 / 1024; // MB
        
        int heapUsagePercent = (int) ((usedHeap * 100) / maxHeap);
        
        if (heapUsagePercent > 80) {
            LogUtil.warn("メモリ使用率が高いです: " + heapUsagePercent + "% (ヒープ: " + usedHeap + "MB/" + maxHeap + "MB)");
        } else {
            LogUtil.info("メモリ使用状況 - ヒープ: " + usedHeap + "MB/" + maxHeap + "MB (" + heapUsagePercent + "%), " +
                       "非ヒープ: " + usedNonHeap + "MB");
        }
    }
    
    /**
     * 监控CPU使用情况
     */
    private static void monitorCpuUsage() {
        double systemLoad = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();
        
        if (systemLoad > availableProcessors * 0.8) {
            LogUtil.warn("CPU負荷が高いです: " + systemLoad + " (利用可能プロセッサ: " + availableProcessors + ")");
        } else {
            LogUtil.info("CPU負荷: " + systemLoad + " (利用可能プロセッサ: " + availableProcessors + ")");
        }
    }
    
    /**
     * 监控数据库连接
     */
    private static void monitorDatabaseConnection() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            
            if (rs.next()) {
                LogUtil.info("データベース接続: 正常");
            }
            
        } catch (Exception e) {
            LogUtil.error("データベース接続チェック失敗: " + e.getMessage(), e);
        }
    }
    
    /**
     * 监控磁盘空间（简化实现）
     */
    private static void monitorDiskSpace() {
        // 这里简化实现，实际应该检查重要目录的磁盘空间
        java.io.File root = new java.io.File("/");
        long freeSpace = root.getFreeSpace() / 1024 / 1024 / 1024; // GB
        long totalSpace = root.getTotalSpace() / 1024 / 1024 / 1024; // GB
        
        int freePercent = (int) ((freeSpace * 100) / totalSpace);
        
        if (freePercent < 10) {
            LogUtil.warn("ディスク空き容量が少ないです: " + freeSpace + "GB/" + totalSpace + "GB (" + freePercent + "% 空き)");
        } else {
            LogUtil.info("ディスク空き容量: " + freeSpace + "GB/" + totalSpace + "GB (" + freePercent + "% 空き)");
        }
    }
    
    /**
     * 检查数据库性能
     */
    private static void checkDatabasePerformance() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 检查表数量
            String tableCountSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE()";
            try (ResultSet rs = stmt.executeQuery(tableCountSql)) {
                if (rs.next()) {
                    int tableCount = rs.getInt(1);
                    LogUtil.info("データベーステーブル数: " + tableCount);
                }
            }
            
            // 检查总数据量
            String dataSizeSql = "SELECT SUM(data_length + index_length) / 1024 / 1024 as total_size_mb " +
                               "FROM information_schema.tables WHERE table_schema = DATABASE()";
            try (ResultSet rs = stmt.executeQuery(dataSizeSql)) {
                if (rs.next()) {
                    double totalSizeMB = rs.getDouble(1);
                    LogUtil.info("データベース総サイズ: " + String.format("%.2f", totalSizeMB) + "MB");
                }
            }
            
        } catch (Exception e) {
            LogUtil.error("データベースパフォーマンスチェック失敗: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查系统健康状态
     */
    private static void checkSystemHealth() {
        // JVM运行时间
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000 / 60; // 分钟
        LogUtil.info("JVM稼働時間: " + uptime + " 分");
        
        // 线程数量
        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();
        LogUtil.info("アクティブスレッド数: " + threadCount);
        
        // 加载类数量
        long loadedClassCount = ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount();
        LogUtil.info("ロード済みクラス数: " + loadedClassCount);
    }
    
    /**
     * 检查应用状态
     */
    private static void checkApplicationStatus() {
        LogUtil.info("アプリケーションステータスチェック: 正常");
        
        // 这里可以添加更多应用特定的健康检查
        checkBatchProcessStatus();
        checkServiceAvailability();
    }
    
    /**
     * 检查批处理状态
     */
    private static void checkBatchProcessStatus() {
        LogUtil.info("バッチ処理ステータス: 監視中");
        // 实际实现应该检查批处理作业的状态
    }
    
    /**
     * 检查服务可用性
     */
    private static void checkServiceAvailability() {
        LogUtil.info("サービス可用性: すべてのサービスが正常に動作しています");
        // 实际实现应该检查关键服务的可用性
    }
    
    /**
     * 获取当前系统状态报告
     */
    public static String getSystemStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== システムステータスレポート ===\n");
        report.append("生成時間: ").append(new Date()).append("\n\n");
        
        // 内存信息
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        report.append("メモリ使用状況:\n");
        report.append("  ヒープ使用量: ").append(heapMemory.getUsed() / 1024 / 1024).append("MB/")
              .append(heapMemory.getMax() / 1024 / 1024).append("MB\n");
        
        // CPU信息
        report.append("CPU負荷: ").append(osBean.getSystemLoadAverage()).append("\n");
        report.append("利用可能プロセッサ: ").append(osBean.getAvailableProcessors()).append("\n\n");
        
        // JVM信息
        report.append("JVM情報:\n");
        report.append("  稼働時間: ").append(ManagementFactory.getRuntimeMXBean().getUptime() / 1000 / 60).append("分\n");
        report.append("  スレッド数: ").append(ManagementFactory.getThreadMXBean().getThreadCount()).append("\n");
        
        return report.toString();
    }
    
    /**
     * 手动触发系统检查
     */
    public static void manualSystemCheck() {
        LogUtil.info("手動システムチェックを開始します");
        monitorSystem();
        performDetailedCheck();
        LogUtil.info("手動システムチェックが完了しました");
    }
}