package com.insurance.model;

import java.util.Date;

/**
 * 系统日志模型类
 */
public class SystemLog {
    // 日志ID，主键
    private int id;
    // 日志级别（INFO, WARN, ERROR等）
    private String logLevel;
    // 日志消息内容
    private String logMessage;
    // 模块名称（哪个模块产生的日志）
    private String module;
    // 用户名（哪个用户操作产生的日志）
    private String username;
    // IP地址（客户端IP）
    private String ipAddress;
    // 日志时间
    private Date logTime;
    // 请求ID（用于追踪请求）
    private String requestId;
    // 会话ID（用户会话标识）
    private String sessionId;
    // 用户代理（浏览器信息）
    private String userAgent;
    
    // 构造函数
    // 无参构造函数
    public SystemLog() {}
    
    // 有参构造函数，用于创建新的系统日志对象
    // @param logLevel 日志级别
    // @param logMessage 日志消息
    // @param module 模块名称
    public SystemLog(String logLevel, String logMessage, String module) {
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.module = module;
        this.logTime = new Date();
    }
    
    // Getter和Setter方法
    // 获取日志ID
    public int getId() { return id; }
    // 设置日志ID
    public void setId(int id) { this.id = id; }
    
    // 获取日志级别
    public String getLogLevel() { return logLevel; }
    // 设置日志级别
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    
    // 获取日志消息
    public String getLogMessage() { return logMessage; }
    // 设置日志消息
    public void setLogMessage(String logMessage) { this.logMessage = logMessage; }
    
    // 获取模块名称
    public String getModule() { return module; }
    // 设置模块名称
    public void setModule(String module) { this.module = module; }
    
    // 获取用户名
    public String getUsername() { return username; }
    // 设置用户名
    public void setUsername(String username) { this.username = username; }
    
    // 获取IP地址
    public String getIpAddress() { return ipAddress; }
    // 设置IP地址
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    // 获取日志时间
    public Date getLogTime() { return logTime; }
    // 设置日志时间
    public void setLogTime(Date logTime) { this.logTime = logTime; }
    
    // 获取请求ID
    public String getRequestId() { return requestId; }
    // 设置请求ID
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    // 获取会话ID
    public String getSessionId() { return sessionId; }
    // 设置会话ID
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    // 获取用户代理
    public String getUserAgent() { return userAgent; }
    // 设置用户代理
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    @Override
    public String toString() {
        return "SystemLog{" +
                "id=" + id +
                ", logLevel='" + logLevel + '\'' +
                ", logMessage='" + logMessage + '\'' +
                ", module='" + module + '\'' +
                ", username='" + username + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", logTime=" + logTime +
                ", requestId='" + requestId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", userAgent='" + userAgent + '\'' +
                '}';
    }
}