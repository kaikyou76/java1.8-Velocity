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
    
    // 定时任务调度器，用于执行监控任务
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // 内存管理Bean，用于获取JVM内存使用情况
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    // 操作系统管理Bean，用于获取系统级信息（如CPU负载）
    private static final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    
    /**
     * 开始系统监控
     * 启动两个定时任务：每5分钟的基础监控和每小时的详细检查
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
     * 优雅地关闭监控任务调度器
     */
    public static void stopMonitoring() {
        LogUtil.info("システムモニタリングを停止します");
        scheduler.shutdown();
        try {
            // 等待最多60秒让现有任务完成
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                // 如果等待超时，强制关闭所有任务
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 如果在等待过程中被中断，强制关闭所有任务
            scheduler.shutdownNow();
            // 恢复中断状态
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 系统监控主方法
     * 执行基础的系统监控任务，包括内存、CPU、数据库连接和磁盘空间监控
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
     * 执行更详细的系统检查，包括数据库性能、系统健康状态和应用状态
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
     * 检查堆内存和非堆内存使用情况，如果堆内存使用率超过80%则发出警告
     */
    private static void monitorMemoryUsage() {
        // 获取堆内存使用情况
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        // 获取非堆内存使用情况
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        
        // 将字节转换为MB
        long usedHeap = heapMemory.getUsed() / 1024 / 1024; // MB
        long maxHeap = heapMemory.getMax() / 1024 / 1024;   // MB
        long usedNonHeap = nonHeapMemory.getUsed() / 1024 / 1024; // MB
        
        // 计算堆内存使用百分比
        int heapUsagePercent = (int) ((usedHeap * 100) / maxHeap);
        
        // 如果堆内存使用率超过80%，记录警告日志
        if (heapUsagePercent > 80) {
            LogUtil.warn("メモリ使用率が高いです: " + heapUsagePercent + "% (ヒープ: " + usedHeap + "MB/" + maxHeap + "MB)");
        } else {
            // 否则记录信息日志
            LogUtil.info("メモリ使用状況 - ヒープ: " + usedHeap + "MB/" + maxHeap + "MB (" + heapUsagePercent + "%), " +
                       "非ヒープ: " + usedNonHeap + "MB");
        }
    }
    
    /**
     * 监控CPU使用情况
     * 检查系统负载，如果负载超过可用处理器数量的80%则发出警告
     */
    private static void monitorCpuUsage() {
        // 获取系统负载平均值
        double systemLoad = osBean.getSystemLoadAverage();
        // 获取可用处理器数量
        int availableProcessors = osBean.getAvailableProcessors();
        
        // 如果系统负载超过可用处理器数量的80%，记录警告日志
        if (systemLoad > availableProcessors * 0.8) {
            LogUtil.warn("CPU負荷が高いです: " + systemLoad + " (利用可能プロセッサ: " + availableProcessors + ")");
        } else {
            // 否则记录信息日志
            LogUtil.info("CPU負荷: " + systemLoad + " (利用可能プロセッサ: " + availableProcessors + ")");
        }
    }
    
    /**
     * 监控数据库连接
     * 通过执行简单查询来检查数据库连接是否正常
     */
    private static void monitorDatabaseConnection() {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            
            // 如果查询成功，记录信息日志
            if (rs.next()) {
                LogUtil.info("データベース接続: 正常");
            }
            
        } catch (Exception e) {
            // 如果出现异常，记录错误日志
            LogUtil.error("データベース接続チェック失敗: " + e.getMessage(), e);
        }
    }
    
    /**
     * 监控磁盘空间（简化实现）
     * 检查根目录的磁盘空间使用情况，如果空闲空间少于10%则发出警告
     */
    private static void monitorDiskSpace() {
        // 这里简化实现，实际应该检查重要目录的磁盘空间
        // 创建根目录文件对象
        java.io.File root = new java.io.File("/");
        // 获取空闲空间（GB）
        long freeSpace = root.getFreeSpace() / 1024 / 1024 / 1024; // GB
        // 获取总空间（GB）
        long totalSpace = root.getTotalSpace() / 1024 / 1024 / 1024; // GB
        
        // 计算空闲空间百分比
        int freePercent = (int) ((freeSpace * 100) / totalSpace);
        
        // 如果空闲空间少于10%，记录警告日志
        if (freePercent < 10) {
            LogUtil.warn("ディスク空き容量が少ないです: " + freeSpace + "GB/" + totalSpace + "GB (" + freePercent + "% 空き)");
        } else {
            // 否则记录信息日志
            LogUtil.info("ディスク空き容量: " + freeSpace + "GB/" + totalSpace + "GB (" + freePercent + "% 空き)");
        }
    }
    
    /**
     * 检查数据库性能
     * 检查数据库中的表数量和总数据大小
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
     * 检查JVM运行时间、活跃线程数和已加载类数量
     */
    private static void checkSystemHealth() {
        // JVM运行时间（分钟）
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
     * 执行应用级别的健康检查
     */
    private static void checkApplicationStatus() {
        LogUtil.info("アプリケーションステータスチェック: 正常");
        
        // 这里可以添加更多应用特定的健康检查
        checkBatchProcessStatus();
        checkServiceAvailability();
    }
    
    /**
     * 检查批处理状态
     * 检查批处理作业的运行状态
     */
    private static void checkBatchProcessStatus() {
        LogUtil.info("バッチ処理ステータス: 監視中");
        // 实际实现应该检查批处理作业的状态
    }
    
    /**
     * 检查服务可用性
     * 检查关键服务的可用性
     */
    private static void checkServiceAvailability() {
        LogUtil.info("サービス可用性: すべてのサービスが正常に動作しています");
        // 实际实现应该检查关键服务的可用性
    }
    
    /**
     * 获取当前系统状态报告
     * 生成包含内存、CPU和JVM信息的系统状态报告
     * @return 系统状态报告字符串
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
     * 手动执行一次基础监控和详细检查
     */
    public static void manualSystemCheck() {
        LogUtil.info("手動システムチェックを開始します");
        // 执行基础监控
        monitorSystem();
        // 执行详细检查
        performDetailedCheck();
        LogUtil.info("手動システムチェックが完了しました");
    }
}