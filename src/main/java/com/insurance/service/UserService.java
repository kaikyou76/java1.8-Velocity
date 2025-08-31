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
    
    // 用户数据访问对象，用于与数据库交互
    private UserDAO userDAO;
    
    // 构造方法，初始化用户数据访问对象
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * 根据用户名获取用户
     * 通过用户名从数据库中查找用户信息
     * @param username 用户名
     * @return User 用户对象，如果未找到返回null
     */
    public User getUserByUsername(String username) {
        try {
            // 调用DAO层方法根据用户名获取用户
            return userDAO.getUserByUsername(username);
        } catch (Exception e) {
            // 记录错误日志并返回null
            LogUtil.error("获取用户失败: " + username, e);
            return null;
        }
    }
    
    /**
     * 根据ID获取用户
     * 通过用户ID从数据库中查找用户信息
     * @param id 用户ID
     * @return User 用户对象，如果未找到返回null
     */
    public User getUserById(int id) {
        try {
            // 调用DAO层方法根据ID获取用户
            return userDAO.getUserById(id);
        } catch (Exception e) {
            // 记录错误日志并返回null
            LogUtil.error("获取用户失败: ID=" + id, e);
            return null;
        }
    }
    
    /**
     * 获取所有用户
     * 从数据库中获取所有用户信息
     * @return List<User> 用户列表，如果发生错误返回null
     */
    public List<User> getAllUsers() {
        try {
            // 调用DAO层方法获取所有用户
            return userDAO.getAllUsers();
        } catch (Exception e) {
            // 记录错误日志并返回null
            LogUtil.error("获取所有用户失败", e);
            return null;
        }
    }
    
    /**
     * 根据角色获取用户列表
     * 通过角色从数据库中查找用户信息
     * @param role 用户角色
     * @return List<User> 用户列表，如果发生错误返回null
     */
    public List<User> getUsersByRole(String role) {
        try {
            // 调用DAO层方法根据角色获取用户列表
            return userDAO.getUsersByRole(role);
        } catch (Exception e) {
            // 记录错误日志并返回null
            LogUtil.error("根据角色获取用户失败: " + role, e);
            return null;
        }
    }
    
    /**
     * 创建用户
     * 验证用户信息并创建新用户
     * @param user 用户对象
     * @param createdBy 创建者用户名
     * @return boolean 创建成功返回true，否则返回false
     */
    public boolean createUser(User user, String createdBy) {
        try {
            // 验证用户名唯一性
            // 检查用户名是否已存在
            if (userDAO.getUserByUsername(user.getUsername()) != null) {
                LogUtil.warn("用户名已存在: " + user.getUsername());
                return false;
            }
            
            // 验证邮箱唯一性
            // 检查邮箱是否已存在
            if (userDAO.getUserByEmail(user.getEmail()) != null) {
                LogUtil.warn("邮箱已存在: " + user.getEmail());
                return false;
            }
            
            // 验证必填字段
            // 验证用户信息是否符合要求
            if (!validateUser(user)) {
                return false;
            }
            
            // 加密密码
            // 使用AuthUtil工具类对密码进行加密
            user.setPassword(AuthUtil.encryptPassword(user.getPassword()));
            // 设置创建者
            user.setCreatedBy(createdBy);
            // 设置更新者
            user.setUpdatedBy(createdBy);
            
            // 调用DAO层方法创建用户
            return userDAO.createUser(user);
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("创建用户失败", e);
            return false;
        }
    }
    
    /**
     * 更新用户信息
     * 验证用户信息并更新用户
     * @param user 用户对象
     * @param updatedBy 更新者用户名
     * @return boolean 更新成功返回true，否则返回false
     */
    public boolean updateUser(User user, String updatedBy) {
        try {
            // 验证用户存在
            // 检查要更新的用户是否存在
            User existingUser = userDAO.getUserById(user.getId());
            if (existingUser == null) {
                LogUtil.warn("用户不存在: ID=" + user.getId());
                return false;
            }
            
            // 验证用户名唯一性（排除自己）
            // 检查用户名是否已存在（排除当前用户自己）
            User usernameCheck = userDAO.getUserByUsername(user.getUsername());
            if (usernameCheck != null && usernameCheck.getId() != user.getId()) {
                LogUtil.warn("用户名已存在: " + user.getUsername());
                return false;
            }
            
            // 验证邮箱唯一性（排除自己）
            // 检查邮箱是否已存在（排除当前用户自己）
            User emailCheck = userDAO.getUserByEmail(user.getEmail());
            if (emailCheck != null && emailCheck.getId() != user.getId()) {
                LogUtil.warn("邮箱已存在: " + user.getEmail());
                return false;
            }
            
            // 验证必填字段
            // 验证用户信息是否符合要求
            if (!validateUser(user)) {
                return false;
            }
            
            // 如果密码不为空，则加密密码
            // 如果提供了新密码，则对密码进行加密
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(AuthUtil.encryptPassword(user.getPassword()));
            } else {
                // 保留原密码
                // 如果未提供新密码，则保留原有密码
                user.setPassword(existingUser.getPassword());
            }
            
            // 设置更新者
            user.setUpdatedBy(updatedBy);
            
            // 调用DAO层方法更新用户
            return userDAO.updateUser(user);
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("更新用户失败", e);
            return false;
        }
    }
    
    /**
     * 删除用户
     * 根据用户ID删除指定用户，但不能删除管理员账户
     * @param id 用户ID
     * @param deletedBy 删除操作的执行者
     * @return boolean 删除成功返回true，否则返回false
     */
    public boolean deleteUser(int id, String deletedBy) {
        try {
            // 验证用户存在
            // 根据ID获取用户信息，检查用户是否存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                // 如果用户不存在，记录警告日志并返回false
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 不能删除管理员账户
            // 检查用户角色是否为管理员，如果是则不允许删除
            if (User.ROLE_ADMIN.equals(user.getRole())) {
                // 记录警告日志并返回false
                LogUtil.warn("不能删除管理员账户: " + user.getUsername());
                return false;
            }
            
            // 调用DAO层方法删除用户
            return userDAO.deleteUser(id);
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("删除用户失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 启用/禁用用户
     * 切换用户的激活状态，但不能禁用管理员账户
     * @param id 用户ID
     * @param updatedBy 更新操作的执行者
     * @return boolean 更新成功返回true，否则返回false
     */
    public boolean toggleUserStatus(int id, String updatedBy) {
        try {
            // 验证用户存在
            // 根据ID获取用户信息，检查用户是否存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                // 如果用户不存在，记录警告日志并返回false
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 不能禁用管理员账户
            // 检查用户角色是否为管理员且当前处于激活状态，如果是则不允许禁用
            if (User.ROLE_ADMIN.equals(user.getRole()) && user.isActive()) {
                // 记录警告日志并返回false
                LogUtil.warn("不能禁用管理员账户: " + user.getUsername());
                return false;
            }
            
            // 切换状态
            // 反转用户的激活状态
            user.setActive(!user.isActive());
            // 设置更新者
            user.setUpdatedBy(updatedBy);
            
            // 调用DAO层方法更新用户信息
            return userDAO.updateUser(user);
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("切换用户状态失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 重置用户密码
     * 为指定用户设置新密码，会先验证密码强度再加密存储
     * @param id 用户ID
     * @param newPassword 新密码
     * @param updatedBy 更新操作的执行者
     * @return boolean 重置成功返回true，否则返回false
     */
    public boolean resetPassword(int id, String newPassword, String updatedBy) {
        try {
            // 验证用户存在
            // 根据ID获取用户信息，检查用户是否存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                // 如果用户不存在，记录警告日志并返回false
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 验证密码强度
            // 检查新密码是否符合强度要求
            if (!validatePassword(newPassword)) {
                // 如果密码不符合要求，直接返回false
                return false;
            }
            
            // 加密新密码
            // 使用AuthUtil工具类对新密码进行加密
            user.setPassword(AuthUtil.encryptPassword(newPassword));
            // 设置更新者
            user.setUpdatedBy(updatedBy);
            
            // 调用DAO层方法更新用户信息
            return userDAO.updateUser(user);
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("重置用户密码失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 解锁用户账户
     * 解锁被锁定的用户账户
     * @param id 用户ID
     * @param updatedBy 更新操作的执行者
     * @return boolean 解锁成功返回true，否则返回false
     */
    public boolean unlockUserAccount(int id, String updatedBy) {
        try {
            // 验证用户存在
            // 根据ID获取用户信息，检查用户是否存在
            User user = userDAO.getUserById(id);
            if (user == null) {
                // 如果用户不存在，记录警告日志并返回false
                LogUtil.warn("用户不存在: ID=" + id);
                return false;
            }
            
            // 解锁账户
            // 调用User对象的解锁方法
            user.unlockAccount();
            // 设置更新者
            user.setUpdatedBy(updatedBy);
            
            // 调用DAO层方法更新用户信息
            return userDAO.updateUser(user);
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("解锁用户账户失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 更新用户最后登录时间
     * 更新指定用户的最后登录时间戳
     * @param id 用户ID
     * @return boolean 更新成功返回true，否则返回false
     */
    public boolean updateLastLogin(int id) {
        try {
            // 调用DAO层方法更新用户最后登录时间
            return userDAO.updateLastLogin(id);
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("更新用户最后登录时间失败: ID=" + id, e);
            return false;
        }
    }
    
    /**
     * 获取用户统计信息
     * 获取系统中用户的统计信息，包括总数、活跃数等
     * @return UserStatistics 用户统计信息对象，如果发生错误返回null
     */
    public UserStatistics getUserStatistics() {
        try {
            // 调用DAO层方法获取用户统计信息
            return userDAO.getUserStatistics();
        } catch (Exception e) {
            // 记录错误日志并返回null
            LogUtil.error("获取用户统计信息失败", e);
            return null;
        }
    }
    
    /**
     * 验证用户信息
     * 验证用户对象的各个字段是否符合要求
     * @param user 用户对象
     * @return boolean 验证通过返回true，否则返回false
     */
    private boolean validateUser(User user) {
        // 验证用户名
        // 检查用户名是否为空或只包含空格
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            // 记录警告日志并返回false
            LogUtil.warn("用户名不能为空");
            return false;
        }
        
        // 检查用户名长度是否在3-20字符之间
        if (user.getUsername().length() < 3 || user.getUsername().length() > 20) {
            // 记录警告日志并返回false
            LogUtil.warn("用户名长度必须在3-20字符之间");
            return false;
        }
        
        // 验证密码
        // 如果密码不为空，验证密码强度
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 检查密码是否符合强度要求
            if (!validatePassword(user.getPassword())) {
                // 如果密码不符合要求，直接返回false
                return false;
            }
        }
        
        // 验证邮箱
        // 检查邮箱是否为空或格式不正确
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            // 记录警告日志并返回false
            LogUtil.warn("邮箱格式不正确");
            return false;
        }
        
        // 验证全名
        // 检查全名是否为空或只包含空格
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            // 记录警告日志并返回false
            LogUtil.warn("全名不能为空");
            return false;
        }
        
        // 验证角色
        // 检查角色是否为空或不是有效角色
        if (user.getRole() == null || !isValidRole(user.getRole())) {
            // 记录警告日志并返回false
            LogUtil.warn("无效的用户角色");
            return false;
        }
        
        // 验证电话号码
        // 如果电话号码不为空，检查格式是否正确
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().trim().isEmpty()) {
            // 使用正则表达式验证电话号码格式
            if (!user.getPhoneNumber().matches("^[\\d\\-\\+\\s()]+$")) {
                // 记录警告日志并返回false
                LogUtil.warn("电话号码格式不正确");
                return false;
            }
        }
        
        // 所有验证通过，返回true
        return true;
    }
    
    /**
     * 验证密码强度
     * 验证密码是否符合安全要求
     * @param password 密码
     * @return boolean 密码符合要求返回true，否则返回false
     */
    private boolean validatePassword(String password) {
        // 检查密码是否为空或长度小于8位
        if (password == null || password.length() < 8) {
            // 记录警告日志并返回false
            LogUtil.warn("密码长度至少8位");
            return false;
        }
        
        // 检查密码长度是否超过20位
        if (password.length() > 20) {
            // 记录警告日志并返回false
            LogUtil.warn("密码长度不能超过20位");
            return false;
        }
        
        // 至少包含一个大写字母、一个小写字母和一个数字
        // 检查密码是否包含至少一个大写字母、一个小写字母和一个数字
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            // 记录警告日志并返回false
            LogUtil.warn("密码必须包含至少一个大写字母、一个小写字母和一个数字");
            return false;
        }
        
        // 密码符合所有要求，返回true
        return true;
    }
    
    /**
     * 验证角色有效性
     * 检查给定的角色是否为系统支持的有效角色
     * @param role 用户角色
     * @return boolean 角色有效返回true，否则返回false
     */
    private boolean isValidRole(String role) {
        // 检查角色是否为系统支持的有效角色之一
        return User.ROLE_ADMIN.equals(role) || 
               User.ROLE_SALES.equals(role) || 
               User.ROLE_REVIEWER.equals(role) || 
               User.ROLE_USER.equals(role);
    }
    
    /**
     * 用户统计信息类
     * 用于封装用户统计信息的数据结构
     */
    public static class UserStatistics {
        // 用户总数
        private int totalUsers;
        // 活跃用户数
        private int activeUsers;
        // 非活跃用户数
        private int inactiveUsers;
        // 被锁定用户数
        private int lockedUsers;
        // 管理员数量
        private int adminCount;
        // 销售人员数量
        private int salesCount;
        // 审核人员数量
        private int reviewerCount;
        // 普通用户数量
        private int userCount;
        
        // 构造函数和Getter/Setter方法
        // 默认构造函数
        public UserStatistics() {}
        
        // 带参数的构造函数，用于初始化所有统计字段
        public UserStatistics(int totalUsers, int activeUsers, int inactiveUsers, 
                             int lockedUsers, int adminCount, int salesCount, 
                             int reviewerCount, int userCount) {
            // 初始化用户总数
            this.totalUsers = totalUsers;
            // 初始化活跃用户数
            this.activeUsers = activeUsers;
            // 初始化非活跃用户数
            this.inactiveUsers = inactiveUsers;
            // 初始化被锁定用户数
            this.lockedUsers = lockedUsers;
            // 初始化管理员数量
            this.adminCount = adminCount;
            // 初始化销售人员数量
            this.salesCount = salesCount;
            // 初始化审核人员数量
            this.reviewerCount = reviewerCount;
            // 初始化普通用户数量
            this.userCount = userCount;
        }
        
        // Getter方法
        // 获取用户总数
        public int getTotalUsers() { return totalUsers; }
        // 设置用户总数
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        // 获取活跃用户数
        public int getActiveUsers() { return activeUsers; }
        // 设置活跃用户数
        public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
        
        // 获取非活跃用户数
        public int getInactiveUsers() { return inactiveUsers; }
        // 设置非活跃用户数
        public void setInactiveUsers(int inactiveUsers) { this.inactiveUsers = inactiveUsers; }
        
        // 获取被锁定用户数
        public int getLockedUsers() { return lockedUsers; }
        // 设置被锁定用户数
        public void setLockedUsers(int lockedUsers) { this.lockedUsers = lockedUsers; }
        
        // 获取管理员数量
        public int getAdminCount() { return adminCount; }
        // 设置管理员数量
        public void setAdminCount(int adminCount) { this.adminCount = adminCount; }
        
        // 获取销售人员数量
        public int getSalesCount() { return salesCount; }
        // 设置销售人员数量
        public void setSalesCount(int salesCount) { this.salesCount = salesCount; }
        
        // 获取审核人员数量
        public int getReviewerCount() { return reviewerCount; }
        // 设置审核人员数量
        public void setReviewerCount(int reviewerCount) { this.reviewerCount = reviewerCount; }
        
        // 获取普通用户数量
        public int getUserCount() { return userCount; }
        // 设置普通用户数量
        public void setUserCount(int userCount) { this.userCount = userCount; }
    }
}