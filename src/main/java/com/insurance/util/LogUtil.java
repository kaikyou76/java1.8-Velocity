package com.insurance.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日志工具类
 * 提供统一的日志记录功能
 */
public class LogUtil {
    
    private static final String LOG_DIR = "logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 记录信息日志
     */
    public static void info(String message) {
        log("INFO", message, null);
    }
    
    /**
     * 记录警告日志
     */
    public static void warn(String message) {
        log("WARN", message, null);
    }
    
    /**
     * 记录错误日志
     */
    public static void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }
    
    /**
     * 记录调试日志
     */
    public static void debug(String message) {
        log("DEBUG", message, null);
    }
    
    /**
     * 核心日志记录方法
     */
    private static void log(String level, String message, Throwable throwable) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // 控制台输出
        System.out.println(logMessage);
        
        if (throwable != null) {
            throwable.printStackTrace();
        }
        
        // 文件日志记录
        writeToFile(logMessage, throwable);
    }
    
    /**
     * 写入日志文件
     */
    private static void writeToFile(String message, Throwable throwable) {
        try {
            // 创建日志目录
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // 每日日志文件
            String fileName = "insurance_system_" + FILE_DATE_FORMAT.format(new Date()) + ".log";
            File logFile = new File(logDir, fileName);
            
            try (FileWriter writer = new FileWriter(logFile, true)) {
                writer.write(message + "\n");
                
                if (throwable != null) {
                    writer.write("Exception: " + throwable.getMessage() + "\n");
                    for (StackTraceElement element : throwable.getStackTrace()) {
                        writer.write("\tat " + element.toString() + "\n");
                    }
                }
                
                writer.write("\n");
            }
            
        } catch (IOException e) {
            System.err.println("日志文件写入失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录系统启动日志
     */
    public static void logSystemStart() {
        info("保険システムが起動しました");
        info("システムバージョン: 1.0.0");
        info("Javaバージョン: " + System.getProperty("java.version"));
    }
    
    /**
     * 记录系统关闭日志
     */
    public static void logSystemShutdown() {
        info("保険システムが正常に終了しました");
    }
    
    /**
     * 记录用户操作日志
     */
    public static void logUserAction(String username, String action, String details) {
        info(String.format("ユーザー操作 - ユーザー: %s, アクション: %s, 詳細: %s", 
            username, action, details));
    }
    
    /**
     * 记录数据库操作日志
     */
    public static void logDatabaseOperation(String operation, String table, int affectedRows) {
        info(String.format("データベース操作 - 操作: %s, テーブル: %s, 影響行数: %d", 
            operation, table, affectedRows));
    }
    
    /**
     * 记录性能日志
     */
    public static void logPerformance(String operation, long durationMs) {
        info(String.format("パフォーマンス - 操作: %s, 実行時間: %dms", operation, durationMs));
    }
}