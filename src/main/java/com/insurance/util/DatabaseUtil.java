package com.insurance.util;

// 导入Java SQL连接接口，用于数据库连接操作
import java.sql.Connection;
// 导入Java SQL驱动管理器，用于获取数据库连接
import java.sql.DriverManager;
// 导入Java SQL异常类，用于处理数据库相关异常
import java.sql.SQLException;
// 导入Java属性类，用于设置数据库连接属性
import java.util.Properties;

/**
 * 数据库连接工具类
 * 提供数据库连接的获取和关闭功能
 */
public class DatabaseUtil {
    
    // 数据库连接URL，指定连接到insurance_system数据库
    private static final String DB_URL = "jdbc:mysql://localhost:3306/insurance_system";
    // 数据库用户名
    private static final String DB_USER = "root";
    // 数据库密码
    private static final String DB_PASSWORD = "password";
    
    // 静态代码块，在类加载时执行一次
    static {
        try {
            // 加载MySQL JDBC驱动程序
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // 如果找不到驱动程序，抛出运行时异常
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }
    
    /**
     * 获取数据库连接
     * 建立并返回一个新的数据库连接
     * @return Connection 数据库连接对象
     * @throws SQLException 数据库访问错误时抛出
     */
    public static Connection getConnection() throws SQLException {
        // 创建属性对象，用于设置连接参数
        Properties props = new Properties();
        // 设置数据库用户名
        props.setProperty("user", DB_USER);
        // 设置数据库密码
        props.setProperty("password", DB_PASSWORD);
        // 设置不使用SSL连接
        props.setProperty("useSSL", "false");
        // 设置字符编码为UTF-8
        props.setProperty("characterEncoding", "UTF-8");
        // 设置使用Unicode编码
        props.setProperty("useUnicode", "true");
        // 设置服务器时区为东京时区
        props.setProperty("serverTimezone", "Asia/Tokyo");
        
        // 通过驱动管理器获取数据库连接并返回
        return DriverManager.getConnection(DB_URL, props);
    }
    
    /**
     * 关闭数据库连接
     * 安全地关闭数据库连接，避免资源泄露
     * @param conn 要关闭的数据库连接对象
     */
    public static void closeConnection(Connection conn) {
        // 检查连接对象是否为null
        if (conn != null) {
            try {
                // 尝试关闭数据库连接
                conn.close();
            } catch (SQLException e) {
                // 如果关闭连接时发生异常，输出错误信息到标准错误流
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }
}