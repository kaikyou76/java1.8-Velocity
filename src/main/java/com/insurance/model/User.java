package com.insurance.model;

import java.util.Date;

/**
 * 用户模型类
 * 定义系统用户的基本信息和权限
 */
public class User {
    // 用户ID，主键
    private int id;
    // 用户名，用于登录
    private String username;
    // 密码，加密存储
    private String password;
    // 电子邮件
    private String email;
    // 用户全名
    private String fullName;
    // 用户角色（ADMIN, SALES, REVIEWER, USER）
    private String role;
    // 所属部门
    private String department;
    // 电话号码
    private String phoneNumber;
    // 账户是否激活
    private boolean active;
    // 账户创建时间
    private Date createdAt;
    // 账户更新时间
    private Date updatedAt;
    // 最后登录时间
    private Date lastLogin;
    // 创建者
    private String createdBy;
    // 更新者
    private String updatedBy;
    // 登录失败次数
    private int loginAttempts;
    // 账户锁定截止时间
    private Date accountLockedUntil;
    
    // 用户角色定义
    // 系统管理员角色
    public static final String ROLE_ADMIN = "ADMIN";      // 系统管理员
    // 销售员角色
    public static final String ROLE_SALES = "SALES";      // 销售员
    // 审查员角色
    public static final String ROLE_REVIEWER = "REVIEWER"; // 审查员
    // 普通用户角色
    public static final String ROLE_USER = "USER";        // 普通用户
    
    // 用户状态定义
    // 最大登录尝试次数
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    // 账户锁定时间（分钟）
    public static final int ACCOUNT_LOCK_MINUTES = 30;
    
    // 默认构造函数
    // 创建新用户时使用，初始化默认值
    public User() {
        this.active = true;
        this.loginAttempts = 0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }
    
    // 全参数构造函数
    // @param id 用户ID
    // @param username 用户名
    // @param password 密码
    // @param email 电子邮件
    // @param fullName 用户全名
    // @param role 用户角色
    // @param department 所属部门
    // @param phoneNumber 电话号码
    // @param active 账户是否激活
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
    // 获取用户ID
    public int getId() {
        return id;
    }
    
    // 设置用户ID
    public void setId(int id) {
        this.id = id;
    }
    
    // 获取用户名
    public String getUsername() {
        return username;
    }
    
    // 设置用户名
    public void setUsername(String username) {
        this.username = username;
    }
    
    // 获取密码
    public String getPassword() {
        return password;
    }
    
    // 设置密码
    public void setPassword(String password) {
        this.password = password;
    }
    
    // 获取电子邮件
    public String getEmail() {
        return email;
    }
    
    // 设置电子邮件
    public void setEmail(String email) {
        this.email = email;
    }
    
    // 获取用户全名
    public String getFullName() {
        return fullName;
    }
    
    // 设置用户全名
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    // 获取用户角色
    public String getRole() {
        return role;
    }
    
    // 设置用户角色
    public void setRole(String role) {
        this.role = role;
    }
    
    // 获取所属部门
    public String getDepartment() {
        return department;
    }
    
    // 设置所属部门
    public void setDepartment(String department) {
        this.department = department;
    }
    
    // 获取电话号码
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    // 设置电话号码
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    // 检查账户是否激活
    public boolean isActive() {
        return active;
    }
    
    // 设置账户激活状态
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // 获取账户创建时间
    public Date getCreatedAt() {
        return createdAt;
    }
    
    // 设置账户创建时间
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    // 获取账户更新时间
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    // 设置账户更新时间
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // 获取最后登录时间
    public Date getLastLogin() {
        return lastLogin;
    }
    
    // 设置最后登录时间
    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    // 获取创建者
    public String getCreatedBy() {
        return createdBy;
    }
    
    // 设置创建者
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    // 获取更新者
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    // 设置更新者
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    // 获取登录失败次数
    public int getLoginAttempts() {
        return loginAttempts;
    }
    
    // 设置登录失败次数
    public void setLoginAttempts(int loginAttempts) {
        this.loginAttempts = loginAttempts;
    }
    
    // 获取账户锁定截止时间
    public Date getAccountLockedUntil() {
        return accountLockedUntil;
    }
    
    // 设置账户锁定截止时间
    public void setAccountLockedUntil(Date accountLockedUntil) {
        this.accountLockedUntil = accountLockedUntil;
    }
    
    // 业务方法
    
    /**
     * 检查账户是否被锁定
     * @return 如果账户被锁定返回true，否则返回false
     */
    public boolean isAccountLocked() {
        if (accountLockedUntil == null) {
            return false;
        }
        return new Date().before(accountLockedUntil);
    }
    
    /**
     * 检查账户是否需要解锁
     * @return 如果账户需要解锁返回true，否则返回false
     */
    public boolean needsAccountUnlock() {
        return isAccountLocked() && new Date().after(accountLockedUntil);
    }
    
    /**
     * 增加登录失败次数
     * 当用户登录失败时调用此方法，如果失败次数达到上限则锁定账户
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
     * 当用户成功登录时调用此方法，重置失败次数并解锁账户
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLockedUntil = null;
        this.updatedAt = new Date();
    }
    
    /**
     * 锁定账户
     * 将账户锁定指定的时间
     */
    public void lockAccount() {
        long lockTime = System.currentTimeMillis() + (ACCOUNT_LOCK_MINUTES * 60 * 1000);
        this.accountLockedUntil = new Date(lockTime);
        this.updatedAt = new Date();
    }
    
    /**
     * 解锁账户
     * 手动解锁账户，重置失败次数和锁定时间
     */
    public void unlockAccount() {
        this.loginAttempts = 0;
        this.accountLockedUntil = null;
        this.updatedAt = new Date();
    }
    
    /**
     * 检查是否有管理员权限
     * @return 如果用户是管理员返回true，否则返回false
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.role);
    }
    
    /**
     * 检查是否有销售员权限
     * @return 如果用户是销售员返回true，否则返回false
     */
    public boolean isSales() {
        return ROLE_SALES.equals(this.role);
    }
    
    /**
     * 检查是否有审查员权限
     * @return 如果用户是审查员返回true，否则返回false
     */
    public boolean isReviewer() {
        return ROLE_REVIEWER.equals(this.role);
    }
    
    /**
     * 检查是否为普通用户
     * @return 如果用户是普通用户返回true，否则返回false
     */
    public boolean isUser() {
        return ROLE_USER.equals(this.role);
    }
    
    /**
     * 获取角色显示名称
     * 根据用户角色返回对应的中文显示名称
     * @return 角色显示名称
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
     * 根据账户状态返回对应的中文显示名称
     * @return 账户状态显示名称
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