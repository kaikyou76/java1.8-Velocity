package com.insurance.dao;

import com.insurance.model.SystemLog;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统日志数据访问对象
 */
public class SystemLogDAO {
    
    /**
     * 插入系统日志
     */
    public boolean insertLog(SystemLog log) {
        String sql = "INSERT INTO system_logs (log_level, log_message, module, username, " +
                   "ip_address, log_time, request_id, session_id, user_agent) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, log.getLogLevel());
            pstmt.setString(2, log.getLogMessage());
            pstmt.setString(3, log.getModule());
            pstmt.setString(4, log.getUsername());
            pstmt.setString(5, log.getIpAddress());
            pstmt.setTimestamp(6, new Timestamp(log.getLogTime().getTime()));
            pstmt.setString(7, log.getRequestId());
            pstmt.setString(8, log.getSessionId());
            pstmt.setString(9, log.getUserAgent());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("系统日志插入失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据ID获取日志
     */
    public SystemLog getLogById(int id) {
        String sql = "SELECT * FROM system_logs WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToLog(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("获取系统日志失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 获取所有日志
     */
    public List<SystemLog> getAllLogs() {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_logs ORDER BY log_time DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("获取所有系统日志失败: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * 根据条件搜索日志
     */
    public List<SystemLog> searchLogs(String level, String module, String keyword, 
                                    Date startDate, Date endDate) {
        List<SystemLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM system_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (level != null && !level.isEmpty()) {
            sql.append(" AND log_level = ?");
            params.add(level);
        }
        
        if (module != null && !module.isEmpty()) {
            sql.append(" AND module = ?");
            params.add(module);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (log_message LIKE ? OR username LIKE ?)");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        
        if (startDate != null) {
            sql.append(" AND log_time >= ?");
            params.add(new Timestamp(startDate.getTime()));
        }
        
        if (endDate != null) {
            sql.append(" AND log_time <= ?");
            params.add(new Timestamp(endDate.getTime()));
        }
        
        sql.append(" ORDER BY log_time DESC");
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToLog(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("搜索系统日志失败: " + e.getMessage());
        }
        
        return logs;
    }
    
    /**
     * 获取日志统计信息
     */
    public List<Object[]> getLogStatistics() {
        List<Object[]> statistics = new ArrayList<>();
        String sql = "SELECT log_level, COUNT(*) as count FROM system_logs " +
                   "GROUP BY log_level ORDER BY count DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String level = rs.getString("log_level");
                int count = rs.getInt("count");
                statistics.add(new Object[]{level, count});
            }
            
        } catch (SQLException e) {
            System.err.println("获取日志统计信息失败: " + e.getMessage());
        }
        
        return statistics;
    }
    
    /**
     * 清理旧日志
     */
    public int cleanupOldLogs(int daysToKeep) {
        String sql = "DELETE FROM system_logs WHERE log_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, daysToKeep);
            return pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("清理旧日志失败: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * 获取日志数量
     */
    public int getLogCount() {
        String sql = "SELECT COUNT(*) FROM system_logs";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("获取日志数量失败: " + e.getMessage());
        }
        
        return 0;
    }
    
    /**
     * 将ResultSet映射到SystemLog对象
     */
    private SystemLog mapResultSetToLog(ResultSet rs) throws SQLException {
        SystemLog log = new SystemLog();
        log.setId(rs.getInt("id"));
        log.setLogLevel(rs.getString("log_level"));
        log.setLogMessage(rs.getString("log_message"));
        log.setModule(rs.getString("module"));
        log.setUsername(rs.getString("username"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setLogTime(rs.getTimestamp("log_time"));
        log.setRequestId(rs.getString("request_id"));
        log.setSessionId(rs.getString("session_id"));
        log.setUserAgent(rs.getString("user_agent"));
        return log;
    }
}