package com.insurance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 数据库连接工具类
 */
public class DatabaseUtil {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/insurance_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";
    
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }
    
    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("useUnicode", "true");
        props.setProperty("serverTimezone", "Asia/Tokyo");
        
        return DriverManager.getConnection(DB_URL, props);
    }
    
    /**
     * 关闭数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }
}