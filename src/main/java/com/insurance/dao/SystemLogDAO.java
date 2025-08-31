package com.insurance.dao;

import com.insurance.model.SystemLog;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统日志数据访问对象
 * 负责系统日志的增删改查操作
 */
public class SystemLogDAO {
    
    /**
     * 插入系统日志
     * @param log 要插入的系统日志对象
     * @return 插入成功返回true，失败返回false
     */
    public boolean insertLog(SystemLog log) {
        // 定义插入日志的SQL语句，包含所有日志字段
        String sql = "INSERT INTO system_logs (log_level, log_message, module, username, " +
                   "ip_address, log_time, request_id, session_id, user_agent) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数，对应日志对象的各个属性
            pstmt.setString(1, log.getLogLevel());        // 日志级别
            pstmt.setString(2, log.getLogMessage());      // 日志消息
            pstmt.setString(3, log.getModule());          // 模块名称
            pstmt.setString(4, log.getUsername());        // 用户名
            pstmt.setString(5, log.getIpAddress());       // IP地址
            pstmt.setTimestamp(6, new Timestamp(log.getLogTime().getTime())); // 日志时间
            pstmt.setString(7, log.getRequestId());       // 请求ID
            pstmt.setString(8, log.getSessionId());       // 会话ID
            pstmt.setString(9, log.getUserAgent());       // 用户代理
            
            // 执行插入操作并获取影响的行数
            int affectedRows = pstmt.executeUpdate();
            // 如果影响行数大于0，表示插入成功
            return affectedRows > 0;
            
        } catch (SQLException e) {
            // 捕获SQL异常并打印错误信息
            System.err.println("系统日志插入失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据ID获取日志
     * @param id 日志ID
     * @return SystemLog对象，如果未找到返回null
     */
    public SystemLog getLogById(int id) {
        // 定义根据ID查询日志的SQL语句
        String sql = "SELECT * FROM system_logs WHERE id = ?";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setInt(1, id);
            
            // 执行查询并处理结果集
            try (ResultSet rs = pstmt.executeQuery()) {
                // 如果结果集中有数据，映射为SystemLog对象
                if (rs.next()) {
                    return mapResultSetToLog(rs);
                }
            }
            
        } catch (SQLException e) {
            // 捕获SQL异常并打印错误信息
            System.err.println("获取系统日志失败: " + e.getMessage());
        }
        
        // 未找到对应ID的日志时返回null
        return null;
    }
    
    /**
     * 获取所有日志
     * @return 包含所有系统日志的列表
     */
    public List<SystemLog> getAllLogs() {
        // 创建用于存储日志的列表
        List<SystemLog> logs = new ArrayList<>();
        // 定义查询所有日志的SQL语句，按时间倒序排列
        String sql = "SELECT * FROM system_logs ORDER BY log_time DESC";
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 遍历结果集，将每条记录映射为SystemLog对象并添加到列表中
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
            
        } catch (SQLException e) {
            // 捕获SQL异常并打印错误信息
            System.err.println("获取所有系统日志失败: " + e.getMessage());
        }
        
        // 返回日志列表
        return logs;
    }
    
    /**
     * 根据条件搜索日志
     * @param level 日志级别
     * @param module 模块名称
     * @param keyword 关键词
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 符合条件的日志列表
     */
    public List<SystemLog> searchLogs(String level, String module, String keyword, 
                                    Date startDate, Date endDate) {
        // 创建用于存储搜索结果的列表
        List<SystemLog> logs = new ArrayList<>();
        // 使用StringBuilder构建动态SQL语句
        StringBuilder sql = new StringBuilder("SELECT * FROM system_logs WHERE 1=1");
        // 创建用于存储SQL参数的列表
        List<Object> params = new ArrayList<>();
        
        // 如果指定了日志级别，则添加相应条件
        if (level != null && !level.isEmpty()) {
            sql.append(" AND log_level = ?");
            params.add(level);
        }
        
        // 如果指定了模块名称，则添加相应条件
        if (module != null && !module.isEmpty()) {
            sql.append(" AND module = ?");
            params.add(module);
        }
        
        // 如果指定了关键词，则添加相应条件（在消息和用户名中搜索）
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (log_message LIKE ? OR username LIKE ?)");
            params.add("%" + keyword + "%");  // 消息中包含关键词
            params.add("%" + keyword + "%");  // 用户名中包含关键词
        }
        
        // 如果指定了开始日期，则添加相应条件
        if (startDate != null) {
            sql.append(" AND log_time >= ?");
            params.add(new Timestamp(startDate.getTime()));
        }
        
        // 如果指定了结束日期，则添加相应条件
        if (endDate != null) {
            sql.append(" AND log_time <= ?");
            params.add(new Timestamp(endDate.getTime()));
        }
        
        // 按时间倒序排列结果
        sql.append(" ORDER BY log_time DESC");
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // 设置SQL参数
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            // 执行查询并处理结果集
            try (ResultSet rs = pstmt.executeQuery()) {
                // 遍历结果集，将每条记录映射为SystemLog对象并添加到列表中
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
            
        } catch (SQLException e) {
            // 捕获SQL异常并打印错误信息
            System.err.println("搜索系统日志失败: " + e.getMessage());
        }
        
        // 返回搜索结果列表
        return logs;
    }
    
    /**
     * 获取日志统计信息
     * @return 包含日志级别和对应数量的统计信息列表
     */
    public List<Object[]> getLogStatistics() {
        // 创建用于存储统计信息的列表
        List<Object[]> statistics = new ArrayList<>();
        // 定义获取日志统计信息的SQL语句，按日志级别分组并统计数量
        String sql = "SELECT log_level, COUNT(*) as count FROM system_logs " +
                   "GROUP BY log_level ORDER BY count DESC";
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 遍历结果集，将每条记录添加到统计信息列表中
            while (rs.next()) {
                String level = rs.getString("log_level");  // 日志级别
                int count = rs.getInt("count");            // 对应数量
                statistics.add(new Object[]{level, count});
            }
            
        } catch (SQLException e) {
            // 捕获SQL异常并打印错误信息
            System.err.println("获取日志统计信息失败: " + e.getMessage());
        }
        
        // 返回统计信息列表
        return statistics;
    }
    
    /**
     * 清理旧日志
     * @param daysToKeep 保留天数
     * @return 删除的日志数量
     */
    public int cleanupOldLogs(int daysToKeep) {
        // 定义删除旧日志的SQL语句，删除指定天数之前的日志
        String sql = "DELETE FROM system_logs WHERE log_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setInt(1, daysToKeep);
            // 执行删除操作并返回影响的行数
            return pstmt.executeUpdate();
            
        } catch (SQLException e) {
            // 捕获SQL异常并打印错误信息
            System.err.println("清理旧日志失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 获取日志数量
     * @return 日志总数量
     */
    public int getLogCount() {
        // 定义获取日志数量的SQL语句
        String sql = "SELECT COUNT(*) FROM system_logs";
        
        // 使用try-with-resources自动关闭数据库连接、预编译语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            // 获取查询结果
            if (rs.next()) {
                return rs.getInt(1);  // 返回日志总数量
            }
            
        } catch (SQLException e) {
            // 捕获SQL异常并打印错误信息
            System.err.println("获取日志数量失败: " + e.getMessage());
        }
        
        // 查询失败时返回0
        return 0;
    }
    
    /**
     * 将ResultSet映射到SystemLog对象
     * @param rs 数据库查询结果集
     * @return 映射后的SystemLog对象
     * @throws SQLException SQL异常
     */
    private SystemLog mapResultSetToLog(ResultSet rs) throws SQLException {
        // 创建新的SystemLog对象
        SystemLog log = new SystemLog();
        // 从结果集中获取各字段值并设置到SystemLog对象中
        log.setId(rs.getInt("id"));                    // 日志ID
        log.setLogLevel(rs.getString("log_level"));    // 日志级别
        log.setLogMessage(rs.getString("log_message")); // 日志消息
        log.setModule(rs.getString("module"));         // 模块名称
        log.setUsername(rs.getString("username"));     // 用户名
        log.setIpAddress(rs.getString("ip_address"));  // IP地址
        log.setLogTime(rs.getTimestamp("log_time"));   // 日志时间
        log.setRequestId(rs.getString("request_id"));  // 请求ID
        log.setSessionId(rs.getString("session_id"));  // 会话ID
        log.setUserAgent(rs.getString("user_agent"));  // 用户代理
        // 返回映射完成的SystemLog对象
        return log;
    }
}