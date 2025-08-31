package com.insurance.util;

// 导入Java文件操作类，用于创建和操作日志文件
import java.io.File;
// 导入Java文件写入器类，用于将日志写入文件
import java.io.FileWriter;
// 导入Java IO异常类，用于处理文件操作异常
import java.io.IOException;
// 导入Java日期格式化类，用于格式化日志时间戳
import java.text.SimpleDateFormat;
// 导入Java日期类，用于获取当前时间
import java.util.Date;

/**
 * 日志工具类
 * 提供统一的日志记录功能
 */
public class LogUtil {
    
    // 定义日志文件存储目录
    private static final String LOG_DIR = "logs";
    // 定义日志时间戳格式（年-月-日 时:分:秒）
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 定义日志文件名日期格式（年-月-日）
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 记录信息日志
     * 记录系统运行过程中的普通信息
     * @param message 日志消息内容
     */
    public static void info(String message) {
        // 调用核心日志记录方法，级别为INFO
        log("INFO", message, null);
    }
    
    /**
     * 记录警告日志
     * 记录系统运行过程中的警告信息
     * @param message 日志消息内容
     */
    public static void warn(String message) {
        // 调用核心日志记录方法，级别为WARN
        log("WARN", message, null);
    }
    
    /**
     * 记录错误日志
     * 记录系统运行过程中的错误信息
     * @param message 日志消息内容
     * @param throwable 异常对象
     */
    public static void error(String message, Throwable throwable) {
        // 调用核心日志记录方法，级别为ERROR
        log("ERROR", message, throwable);
    }
    
    /**
     * 记录调试日志
     * 记录系统调试信息
     * @param message 日志消息内容
     */
    public static void debug(String message) {
        // 调用核心日志记录方法，级别为DEBUG
        log("DEBUG", message, null);
    }
    
    /**
     * 核心日志记录方法
     * 实现日志记录的核心逻辑，包括控制台输出和文件写入
     * @param level 日志级别（INFO、WARN、ERROR、DEBUG）
     * @param message 日志消息内容
     * @param throwable 异常对象（可为null）
     */
    private static void log(String level, String message, Throwable throwable) {
        // 获取当前时间并格式化为指定格式
        String timestamp = DATE_FORMAT.format(new Date());
        // 构造完整的日志消息格式：[时间戳] [级别] 消息内容
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);
        
        // 控制台输出
        // 将日志消息输出到控制台
        System.out.println(logMessage);
        
        // 如果有异常对象，则打印异常堆栈信息
        if (throwable != null) {
            throwable.printStackTrace();
        }
        
        // 文件日志记录
        // 调用文件写入方法将日志保存到文件
        writeToFile(logMessage, throwable);
    }
    
    /**
     * 写入日志文件
     * 将日志消息写入到文件中
     * @param message 日志消息内容
     * @param throwable 异常对象（可为null）
     */
    private static void writeToFile(String message, Throwable throwable) {
        try {
            // 创建日志目录
            // 创建File对象表示日志目录
            File logDir = new File(LOG_DIR);
            // 检查日志目录是否存在，如果不存在则创建
            if (!logDir.exists()) {
                // 创建日志目录（包括必要的父目录）
                logDir.mkdirs();
            }
            
            // 每日日志文件
            // 根据当前日期生成日志文件名
            String fileName = "insurance_system_" + FILE_DATE_FORMAT.format(new Date()) + ".log";
            // 创建File对象表示日志文件
            File logFile = new File(logDir, fileName);
            
            // 使用try-with-resources语句自动关闭文件写入器
            try (FileWriter writer = new FileWriter(logFile, true)) {
                // 将日志消息写入文件，并添加换行符
                writer.write(message + "\n");
                
                // 如果有异常对象，则将异常信息写入文件
                if (throwable != null) {
                    // 写入异常消息
                    writer.write("Exception: " + throwable.getMessage() + "\n");
                    // 遍历异常堆栈元素并写入文件
                    for (StackTraceElement element : throwable.getStackTrace()) {
                        // 写入每个堆栈元素信息
                        writer.write("\tat " + element.toString() + "\n");
                    }
                }
                
                // 写入空行分隔不同的日志记录
                writer.write("\n");
            }
            
        } catch (IOException e) {
            // 如果文件写入过程中发生IO异常，则输出错误信息到标准错误流
            System.err.println("日志文件写入失败: " + e.getMessage());
        }
    }
    
    /**
     * 记录系统启动日志
     * 记录系统启动时的相关信息
     */
    public static void logSystemStart() {
        // 记录系统启动信息
        info("保険システムが起動しました");
        // 记录系统版本信息
        info("システムバージョン: 1.0.0");
        // 记录Java版本信息
        info("Javaバージョン: " + System.getProperty("java.version"));
    }
    
    /**
     * 记录系统关闭日志
     * 记录系统正常关闭的信息
     */
    public static void logSystemShutdown() {
        // 记录系统关闭信息
        info("保険システムが正常に終了しました");
    }
    
    /**
     * 记录用户操作日志
     * 记录用户的操作行为
     * @param username 用户名
     * @param action 操作类型
     * @param details 操作详情
     */
    public static void logUserAction(String username, String action, String details) {
        // 格式化用户操作日志信息并记录
        info(String.format("ユーザー操作 - ユーザー: %s, アクション: %s, 詳細: %s", 
            username, action, details));
    }
    
    /**
     * 记录数据库操作日志
     * 记录数据库操作的相关信息
     * @param operation 操作类型
     * @param table 数据库表名
     * @param affectedRows 受影响的行数
     */
    public static void logDatabaseOperation(String operation, String table, int affectedRows) {
        // 格式化数据库操作日志信息并记录
        info(String.format("データベース操作 - 操作: %s, テーブル: %s, 影響行数: %d", 
            operation, table, affectedRows));
    }
    
    /**
     * 记录性能日志
     * 记录系统性能相关的信息
     * @param operation 操作类型
     * @param durationMs 操作执行时间（毫秒）
     */
    public static void logPerformance(String operation, long durationMs) {
        // 格式化性能日志信息并记录
        info(String.format("パフォーマンス - 操作: %s, 実行時間: %dms", operation, durationMs));
    }
}