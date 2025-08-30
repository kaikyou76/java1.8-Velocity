package com.insurance.service;

import com.insurance.dao.SystemLogDAO;
import com.insurance.model.SystemLog;
import java.util.Date;
import java.util.List;

/**
 * 系统日志业务逻辑服务类
 */
public class SystemLogService {
    
    private SystemLogDAO systemLogDAO;
    
    public SystemLogService() {
        this.systemLogDAO = new SystemLogDAO();
    }
    
    /**
     * 记录系统日志
     */
    public boolean logSystemEvent(String level, String message, String module) {
        SystemLog log = new SystemLog(level, message, module);
        return systemLogDAO.insertLog(log);
    }
    
    /**
     * 记录用户操作日志
     */
    public boolean logUserAction(String username, String action, String details, 
                               String ipAddress, String userAgent) {
        SystemLog log = new SystemLog("INFO", 
            "ユーザー操作: " + action + " - " + details, "USER_ACTION");
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        return systemLogDAO.insertLog(log);
    }
    
    /**
     * 记录错误日志
     */
    public boolean logError(String message, String module, String username) {
        SystemLog log = new SystemLog("ERROR", message, module);
        log.setUsername(username);
        return systemLogDAO.insertLog(log);
    }
    
    /**
     * 获取所有日志
     */
    public List<SystemLog> getAllLogs() {
        return systemLogDAO.getAllLogs();
    }
    
    /**
     * 搜索日志
     */
    public List<SystemLog> searchLogs(String level, String module, String keyword, 
                                    Date startDate, Date endDate) {
        return systemLogDAO.searchLogs(level, module, keyword, startDate, endDate);
    }
    
    /**
     * 获取日志统计
     */
    public List<Object[]> getLogStatistics() {
        return systemLogDAO.getLogStatistics();
    }
    
    /**
     * 清理旧日志
     */
    public int cleanupOldLogs(int daysToKeep) {
        return systemLogDAO.cleanupOldLogs(daysToKeep);
    }
    
    /**
     * 获取日志总数
     */
    public int getTotalLogCount() {
        return systemLogDAO.getLogCount();
    }
    
    /**
     * 获取最近日志
     */
    public List<SystemLog> getRecentLogs(int limit) {
        // 这里简化实现，实际应该使用分页查询
        List<SystemLog> allLogs = systemLogDAO.getAllLogs();
        return allLogs.subList(0, Math.min(limit, allLogs.size()));
    }
    
    /**
     * 获取模块使用统计
     */
    public List<Object[]> getModuleStatistics() {
        // 这里简化实现，实际应该在DAO中实现
        List<SystemLog> allLogs = systemLogDAO.getAllLogs();
        java.util.Map<String, Integer> moduleCounts = new java.util.HashMap<>();
        
        for (SystemLog log : allLogs) {
            String module = log.getModule();
            moduleCounts.put(module, moduleCounts.getOrDefault(module, 0) + 1);
        }
        
        return moduleCounts.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(java.util.stream.Collectors.toList());
    }
}