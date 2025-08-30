package com.insurance.model;

import java.util.Date;

/**
 * 系统日志模型类
 */
public class SystemLog {
    private int id;
    private String logLevel;
    private String logMessage;
    private String module;
    private String username;
    private String ipAddress;
    private Date logTime;
    private String requestId;
    private String sessionId;
    private String userAgent;
    
    // 构造函数
    public SystemLog() {}
    
    public SystemLog(String logLevel, String logMessage, String module) {
        this.logLevel = logLevel;
        this.logMessage = logMessage;
        this.module = module;
        this.logTime = new Date();
    }
    
    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }
    
    public String getLogMessage() { return logMessage; }
    public void setLogMessage(String logMessage) { this.logMessage = logMessage; }
    
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public Date getLogTime() { return logTime; }
    public void setLogTime(Date logTime) { this.logTime = logTime; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getUserAgent() { return userAgent; }
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