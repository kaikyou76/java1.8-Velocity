package com.insurance.model;

import java.util.Date;

/**
 * 用户模型类
 * 定义系统用户的基本信息和权限
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String role;
    private String department;
    private String phoneNumber;
    private boolean active;
    private Date createdAt;
    private Date updatedAt;
    private Date lastLogin;
    private String createdBy;
    private String updatedBy;
    private int loginAttempts;
    private Date accountLockedUntil;
    
    // 用户角色定义
    public static final String ROLE_ADMIN = "ADMIN";      // 系统管理员
    public static final String ROLE_SALES = "SALES";      // 销售员
    public static final String ROLE_REVIEWER = "REVIEWER"; // 审查员
    public static final String ROLE_USER = "USER";        // 普通用户
    
    // 用户状态定义
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int ACCOUNT_LOCK_MINUTES = 30;
    
    // 默认构造函数
    public User() {
        this.active = true;
        this.loginAttempts = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // 全参数构造函数
    public User(int id, String username, String password, String email, String fullName, 
                String role, String department, String phoneNumber, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
        this.phoneNumber = phoneNumber;
        this.active = active;
        this.loginAttempts = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // Getter和Setter方法
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Date getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public int getLoginAttempts() {
        return loginAttempts;
    }
    
    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
    
    public Date getAccountLockedUntil() {
        return accountLockedUntil;
    }
    
    public void setAccountLockedUntil(Date accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }
    
    // 业务方法
    
    /**
     * 检查账户是否被锁定
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return new Date().before(accountLockedUntil);
    }
    
    /**
     * 检查账户是否需要解锁
     */
    public boolean needsAccountUnlock() {
        return isAccountLocked() && new Date().after(accountLockedUntil);
    }
    
    /**
     * 增加登录失败次数
     */
    public void incrementLoginAttempts() {
        this.loginAttempts++;
        this.updatedAt = new Date();
        
        // 如果达到最大尝试次数，锁定账户
        if (this.loginAttempts >= MAX_LOGIN_ATTEMPTS) {
            lockAccount();
        }
    }
    
    /**
     * 重置登录失败次数
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLockedUntil = null;
        this.updatedAt = new Date();
    }
    
    /**
     * 锁定账户
     */
    public void lockAccount() {
        long lockTime = System.currentTimeMillis() + (ACCOUNT_LOCK_MINUTES * 60 * 1000);
        this.accountLockedUntil = new Date(lockTime);
        this.updatedAt = new Date();
    }
    
    /**
     * 解锁账户
     */
    public void unlockAccount() {
        this.loginAttempts = 0;
        this.accountLockedUntil = null;
        this.updatedAt = new Date();
    }
    
    /**
     * 检查是否有管理员权限
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.role);
    }
    
    /**
     * 检查是否有销售员权限
     */
    public boolean isSales() {
        return ROLE_SALES.equals(this.role);
    }
    
    /**
     * 检查是否有审查员权限
     */
    public boolean isReviewer() {
        return ROLE_REVIEWER.equals(this.role);
    }
    
    /**
     * 检查是否为普通用户
     */
    public boolean isUser() {
        return ROLE_USER.equals(this.role);
    }
    
    /**
     * 获取角色显示名称
     */
    public String getRoleDisplayName() {
        switch (this.role) {
            case ROLE_ADMIN:
                return "系统管理员";
            case ROLE_SALES:
                return "销售员";
            case ROLE_REVIEWER:
                return "审查员";
            case ROLE_USER:
                return "普通用户";
            default:
                return "未知角色";
        }
    }
    
    /**
     * 获取账户状态显示名称
     */
    public String getStatusDisplayName() {
        if (!active) {
            return "已禁用";
        }
        if (isAccountLocked()) {
            return "已锁定";
        }
        return "正常";
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}