package com.insurance.service;

import com.insurance.dao.UserDAO;
import com.insurance.model.User;
import com.insurance.security.AuthUtil;
import com.insurance.util.LogUtil;

import java.util.List;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑
 */
public class UserService {
    
    private UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        try {
            return userDAO.getUserByUsername(username);
        } catch (Exception e) {
            LogUtil.error("获取用户失败: " + username, e);
            return null;
        }
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(int id) {
        try {
            return userDAO.getUserById(id);
        } catch (Exception e) {
            LogUtil.error("获取用户失败: ID=" + id, e);
            return null;
        }
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (Exception e) {
            LogUtil.error("获取所有用户失败", e);
            return null;
        }
    }
    
    /**
     * 根据角色获取用户列表
     */
    public List<User> getUsersByRole(String role) {
        try {
            return userDAO.getUsersByRole(role);
        } catch (Exception e) {
            LogUtil.error("根据角色获取用户失败: " + role, e);
            return null;
        }
    }
    
    /**
     * 创建用户
     */
    public boolean createUser(User user, String createdBy) {
        try {
            // 验证用户名唯一性
            if (userDAO.getUserByUsername(user.getUsername()) != null) {
                LogUtil.warn("用户名已存在: " + user.getUsername());
                return false;
            }
            
            // 验证邮箱唯一性
            if (userDAO.getUserByEmail(user.getEmail()) != null) {
                LogUtil.warn("邮箱已存在: " + user.getEmail());
                return false;
            }
            
            // 验证必填字段
            if (!validateUser(user)) {
                return false;
            }
            
            // 加密密码
            user.setPassword(AuthUtil.encryptPassword(user.getPassword()));
            user.setCreatedBy(createdBy);
            user.setUpdatedBy(createdBy);
            
            return userDAO.createUser(user);
        } catch (Exception e) {
            LogUtil.error("创建用户失败", e);
            return false;
        }
    }
    
    /**
     * 更新用户信息
     */
    public boolean updateUser(User user, String updatedBy) {
        try {
            // 验证用户存在
            User existingUser = userDAO.getUserById(user.getId());
            if (existingUser == null) {
                LogUtil.warn("用户不存在: ID=" + user.getId());
                return false;
            }
            
            // 验证用户名唯一性（排除自己）
            User usernameCheck = userDAO.getUserByUsername(user.getUsername());
            if (usernameCheck != null && usernameCheck.getId() != user.getId()) {
                LogUtil.warn("用户名已存在: " + user.getUsername());
                return false;
            }
            
            // 验证邮箱唯一性（排除自己）
            User emailCheck = userDAO.getUserByEmail(user.getEmail());
            if (emailCheck != null && emailCheck.getId() != user.getId()) {
                LogUtil.warn("邮箱已存在: " + user.getEmail());
                return false;
            }
            
            // 验证必填字段
            if (!validateUser(user)) {
                return false;
            }
            
            // 如果密码不为空，则加密密码
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(AuthUtil.encryptPassword(user.getPassword()));
            } else {
                // 保留原密码
                user.setPassword(existingUser.getPassword());
            }
            
            user.setUpdatedBy(updatedBy);
            
            return userDAO.updateUser(user);
        } catch (Exception e) {
            LogUtil.error("更新用户失败", e);
            return false;
        }
    }
    
    /**
     * 删除用户
     */
    public boolean deleteUser(int id, String deletedBy) {
        try {
            // 验证用户存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 不能删除管理员账户
            if (User.ROLE_ADMIN.equals(user.getRole())) {
                LogUtil.warn("不能删除管理员账户: " + user.getUsername());
                return false;
            }
            
            return userDAO.deleteUser(id);
        } catch (Exception e) {
            LogUtil.error("删除用户失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 启用/禁用用户
     */
    public boolean toggleUserStatus(int id, String updatedBy) {
        try {
            // 验证用户存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 不能禁用管理员账户
            if (User.ROLE_ADMIN.equals(user.getRole()) && user.isActive()) {
                LogUtil.warn("不能禁用管理员账户: " + user.getUsername());
                return false;
            }
            
            // 切换状态
            user.setActive(!user.isActive());
            user.setUpdatedBy(updatedBy);
            
            return userDAO.updateUser(user);
        } catch (Exception e) {
            LogUtil.error("切换用户状态失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 重置用户密码
     */
    public boolean resetPassword(int id, String newPassword, String updatedBy) {
        try {
            // 验证用户存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 验证密码强度
            if (!validatePassword(newPassword)) {
                return false;
            }
            
            // 加密新密码
            user.setPassword(AuthUtil.encryptPassword(newPassword));
            user.setUpdatedBy(updatedBy);
            
            return userDAO.updateUser(user);
        } catch (Exception e) {
            LogUtil.error("重置用户密码失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 解锁用户账户
     */
    public boolean unlockUserAccount(int id, String updatedBy) {
        try {
            // 验证用户存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 解锁账户
            user.unlockAccount();
            user.setUpdatedBy(updatedBy);
            
            return userDAO.updateUser(user);
        } catch (Exception e) {
            LogUtil.error("解锁用户账户失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 更新用户最后登录时间
     */
    public boolean updateLastLogin(int id) {
        try {
            return userDAO.updateLastLogin(id);
        } catch (Exception e) {
            LogUtil.error("更新用户最后登录时间失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 获取用户统计信息
     */
    public UserStatistics getUserStatistics() {
        try {
            return userDAO.getUserStatistics();
        } catch (Exception e) {
            LogUtil.error("获取用户统计信息失败", e);
            return null;
        }
    }
    
    /**
     * 验证用户信息
     */
    private boolean validateUser(User user) {
        // 验证用户名
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            LogUtil.warn("用户名不能为空");
            return false;
        }
        
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            LogUtil.warn("用户名长度必须在3-20字符之间");
            return false;
        }
        
        // 验证密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!validatePassword(user.getPassword())) {
                return false;
            }
        }
        
        // 验证邮箱
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            LogUtil.warn("邮箱格式不正确");
            return false;
        }
        
        // 验证全名
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            LogUtil.warn("全名不能为空");
            return false;
        }
        
        // 验证角色
        if (user.getRole() == null || !isValidRole(user.getRole())) {
            LogUtil.warn("无效的用户角色");
            return false;
        }
        
        // 验证电话号码
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
            if (!user.getPhoneNumber().matches("^[\\d\\-\\+\\s()]+$")) {
                LogUtil.warn("电话号码格式不正确");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 验证密码强度
     */
    private boolean validatePassword(String password) {
        if (password == null || password.length() < 8) {
            LogUtil.warn("密码长度至少8位");
            return false;
        }
        
        if (password.length() > 20) {
            LogUtil.warn("密码长度不能超过20位");
            return false;
        }
        
        // 至少包含一个大写字母、一个小写字母和一个数字
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            LogUtil.warn("密码必须包含至少一个大写字母、一个小写字母和一个数字");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证角色有效性
     */
    private boolean isValidRole(String role) {
        return User.ROLE_ADMIN.equals(role) || 
               User.ROLE_SALES.equals(role) || 
               User.ROLE_REVIEWER.equals(role) || 
               User.ROLE_USER.equals(role);
    }
    
    /**
     * 用户统计信息类
     */
    public static class UserStatistics {
        private int totalUsers;
        private int activeUsers;
        private int inactiveUsers;
        private int lockedUsers;
        private int adminCount;
        private int salesCount;
        private int reviewerCount;
        private int userCount;
        
        // 构造函数和Getter/Setter方法
        public UserStatistics() {}
        
        public UserStatistics(int totalUsers, int activeUsers, int inactiveUsers, 
                             int lockedUsers, int adminCount, int salesCount, 
                             int reviewerCount, int userCount) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
            this.lockedUsers = lockedUsers;
            this.adminCount = adminCount;
            this.salesCount = salesCount;
            this.reviewerCount = reviewerCount;
            this.userCount = userCount;
        }
        
        // Getter方法
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        public int getActiveUsers() { return activeUsers; }
        public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
        
        public int getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(int inactiveUsers) { this.inactiveUsers = inactiveUsers; }
        
        public int getLockedUsers() { return lockedUsers; }
        public void setLockedUsers(int lockedUsers) { this.lockedUsers = lockedUsers; }
        
        public int getAdminCount() { return adminCount; }
        public void setAdminCount(int adminCount) { this.adminCount = adminCount; }
        
        public int getSalesCount() { return salesCount; }
        public void setSalesCount(int salesCount) { this.salesCount = salesCount; }
        
        public int getReviewerCount() { return reviewerCount; }
        public void setReviewerCount(int reviewerCount) { this.reviewerCount = reviewerCount; }
        
        public int getUserCount() { return userCount; }
        public void setUserCount(int userCount) { this.userCount = userCount; }
    }
}