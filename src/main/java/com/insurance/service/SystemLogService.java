package com.insurance.service;

import com.insurance.dao.SystemLogDAO;
import com.insurance.model.SystemLog;
import java.util.Date;
import java.util.List;

/**
 * 系统日志业务逻辑服务类
 */
public class SystemLogService {
    
    // 系统日志数据访问对象，用于与数据库交互
    private SystemLogDAO systemLogDAO;
    
    // 构造方法，初始化系统日志数据访问对象
    public SystemLogService() {
        this.systemLogDAO = new SystemLogDAO();
    }
    
    /**
     * 记录系统日志
     * 创建并保存系统日志记录
     * @param level 日志级别 (INFO, WARN, ERROR等)
     * @param message 日志消息
     * @param module 模块名称
     * @return boolean 记录成功返回true，否则返回false
     */
    public boolean logSystemEvent(String level, String message, String module) {
        // 创建系统日志对象
        SystemLog log = new SystemLog(level, message, module);
        // 调用DAO层方法插入日志记录
        return systemLogDAO.insertLog(log);
    }
    
    /**
     * 记录用户操作日志
     * 记录用户的操作行为日志
     * @param username 用户名
     * @param action 操作类型
     * @param details 操作详情
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return boolean 记录成功返回true，否则返回false
     */
    public boolean logUserAction(String username, String action, String details, 
                               String ipAddress, String userAgent) {
        // 创建用户操作日志对象，级别为INFO，模块为USER_ACTION
        SystemLog log = new SystemLog("INFO", 
            "ユーザー操作: " + action + " - " + details, "USER_ACTION");
        // 设置用户名
        log.setUsername(username);
        // 设置IP地址
        log.setIpAddress(ipAddress);
        // 设置用户代理
        log.setUserAgent(userAgent);
        // 调用DAO层方法插入日志记录
        return systemLogDAO.insertLog(log);
    }
    
    /**
     * 记录错误日志
     * 记录系统错误日志
     * @param message 错误消息
     * @param module 模块名称
     * @param username 用户名
     * @return boolean 记录成功返回true，否则返回false
     */
    public boolean logError(String message, String module, String username) {
        // 创建错误日志对象，级别为ERROR
        SystemLog log = new SystemLog("ERROR", message, module);
        // 设置用户名
        log.setUsername(username);
        // 调用DAO层方法插入日志记录
        return systemLogDAO.insertLog(log);
    }
    
    /**
     * 获取所有日志
     * 从数据库获取所有系统日志记录
     * @return List<SystemLog> 日志列表
     */
    public List<SystemLog> getAllLogs() {
        // 调用DAO层方法获取所有日志记录
        return systemLogDAO.getAllLogs();
    }
    
    /**
     * 搜索日志
     * 根据条件搜索系统日志记录
     * @param level 日志级别
     * @param module 模块名称
     * @param keyword 关键字
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return List<SystemLog> 符合条件的日志列表
     */
    public List<SystemLog> searchLogs(String level, String module, String keyword, 
                                    Date startDate, Date endDate) {
        // 调用DAO层方法根据条件搜索日志记录
        return systemLogDAO.searchLogs(level, module, keyword, startDate, endDate);
    }
    
    /**
     * 获取日志统计
     * 获取日志的统计信息
     * @return List<Object[]> 日志统计信息
     */
    public List<Object[]> getLogStatistics() {
        // 调用DAO层方法获取日志统计信息
        return systemLogDAO.getLogStatistics();
    }
    
    /**
     * 清理旧日志
     * 删除指定天数之前的旧日志记录
     * @param daysToKeep 保留天数
     * @return int 删除的日志记录数
     */
    public int cleanupOldLogs(int daysToKeep) {
        // 调用DAO层方法清理旧日志记录
        return systemLogDAO.cleanupOldLogs(daysToKeep);
    }
    
    /**
     * 获取日志总数
     * 获取系统中日志记录的总数量
     * @return int 日志总数
     */
    public int getTotalLogCount() {
        // 调用DAO层方法获取日志记录总数
        return systemLogDAO.getLogCount();
    }
    
    /**
     * 获取最近日志
     * 获取最近的指定数量的日志记录
     * @param limit 获取的日志记录数量
     * @return List<SystemLog> 最近的日志列表
     */
    public List<SystemLog> getRecentLogs(int limit) {
        // 这里简化实现，实际应该使用分页查询
        // 获取所有日志记录
        List<SystemLog> allLogs = systemLogDAO.getAllLogs();
        // 返回前limit条记录
        return allLogs.subList(0, Math.min(limit, allLogs.size()));
    }
    
    /**
     * 获取模块使用统计
     * 统计各个模块的使用情况
     * @return List<Object[]> 模块使用统计信息
     */
    public List<Object[]> getModuleStatistics() {
        // 这里简化实现，实际应该在DAO中实现
        // 获取所有日志记录
        List<SystemLog> allLogs = systemLogDAO.getAllLogs();
        // 创建模块计数映射
        java.util.Map<String, Integer> moduleCounts = new java.util.HashMap<>();
        
        // 遍历所有日志记录，统计各模块使用次数
        for (SystemLog log : allLogs) {
            String module = log.getModule();
            // 累加模块使用次数
            moduleCounts.put(module, moduleCounts.getOrDefault(module, 0) + 1);
        }
        
        // 将统计结果转换为List<Object[]>格式返回
        return moduleCounts.entrySet().stream()
                .map(entry -> new Object[]{entry.getKey(), entry.getValue()})
                .collect(java.util.stream.Collectors.toList());
    }
}