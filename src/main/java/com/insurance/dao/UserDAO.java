package com.insurance.dao;

import com.insurance.model.User;
import com.insurance.service.UserService.UserStatistics;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户数据访问对象
 * 处理用户相关的数据库操作
 */
public class UserDAO {
    
    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return User对象，如果未找到返回null
     * @throws SQLException SQL异常
     */
    public User getUserByUsername(String username) throws SQLException {
        // 定义根据用户名查询用户的SQL语句，只查询未删除的用户
        String sql = "SELECT * FROM users WHERE username = ? AND deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setString(1, username);
            
            // 执行查询并处理结果集
            try (ResultSet rs = pstmt.executeQuery()) {
                // 如果结果集中有数据，映射为User对象
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        // 未找到对应用户名的用户时返回null
        return null;
    }
    
    /**
     * 根据邮箱获取用户
     * @param email 用户邮箱
     * @return User对象，如果未找到返回null
     * @throws SQLException SQL异常
     */
    public User getUserByEmail(String email) throws SQLException {
        // 定义根据邮箱查询用户的SQL语句，只查询未删除的用户
        String sql = "SELECT * FROM users WHERE email = ? AND deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setString(1, email);
            
            // 执行查询并处理结果集
            try (ResultSet rs = pstmt.executeQuery()) {
                // 如果结果集中有数据，映射为User对象
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        // 未找到对应邮箱的用户时返回null
        return null;
    }
    
    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return User对象，如果未找到返回null
     * @throws SQLException SQL异常
     */
    public User getUserById(int id) throws SQLException {
        // 定义根据ID查询用户的SQL语句，只查询未删除的用户
        String sql = "SELECT * FROM users WHERE id = ? AND deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setInt(1, id);
            
            // 执行查询并处理结果集
            try (ResultSet rs = pstmt.executeQuery()) {
                // 如果结果集中有数据，映射为User对象
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        // 未找到对应ID的用户时返回null
        return null;
    }
    
    /**
     * 获取所有用户
     * @return 包含所有未删除用户的列表
     * @throws SQLException SQL异常
     */
    public List<User> getAllUsers() throws SQLException {
        // 定义查询所有未删除用户的SQL语句，按ID排序
        String sql = "SELECT * FROM users WHERE deleted = false ORDER BY id";
        // 创建用于存储用户的列表
        List<User> users = new ArrayList<>();
        
        // 使用try-with-resources自动关闭数据库连接、语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // 遍历结果集，将每条记录映射为User对象并添加到列表中
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        
        // 返回用户列表
        return users;
    }
    
    /**
     * 根据角色获取用户列表
     * @param role 用户角色
     * @return 包含指定角色的未删除用户的列表
     * @throws SQLException SQL异常
     */
    public List<User> getUsersByRole(String role) throws SQLException {
        // 定义根据角色查询用户的SQL语句，只查询未删除的用户
        String sql = "SELECT * FROM users WHERE role = ? AND deleted = false ORDER BY id";
        // 创建用于存储用户的列表
        List<User> users = new ArrayList<>();
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setString(1, role);
            
            // 执行查询并处理结果集
            try (ResultSet rs = pstmt.executeQuery()) {
                // 遍历结果集，将每条记录映射为User对象并添加到列表中
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        
        // 返回用户列表
        return users;
    }
    
    /**
     * 创建用户
     * @param user 要创建的用户对象
     * @return 创建成功返回true，失败返回false
     * @throws SQLException SQL异常
     */
    public boolean createUser(User user) throws SQLException {
        // 定义插入用户的SQL语句，包含所有用户字段
        String sql = "INSERT INTO users (username, password, email, full_name, role, department, phone_number, " +
                    "active, created_at, updated_at, created_by, updated_by, login_attempts, account_locked_until) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, NULL)";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // 设置SQL参数，对应用户对象的各个属性
            pstmt.setString(1, user.getUsername());       // 用户名
            pstmt.setString(2, user.getPassword());       // 密码
            pstmt.setString(3, user.getEmail());          // 邮箱
            pstmt.setString(4, user.getFullName());       // 全名
            pstmt.setString(5, user.getRole());           // 角色
            pstmt.setString(6, user.getDepartment());     // 部门
            pstmt.setString(7, user.getPhoneNumber());    // 电话号码
            pstmt.setBoolean(8, user.isActive());         // 是否激活
            pstmt.setTimestamp(9, new Timestamp(user.getCreatedAt().getTime()));  // 创建时间
            pstmt.setTimestamp(10, new Timestamp(user.getUpdatedAt().getTime())); // 更新时间
            pstmt.setString(11, user.getCreatedBy());     // 创建者
            pstmt.setString(12, user.getUpdatedBy());     // 更新者
            
            // 执行插入操作并获取影响的行数
            int result = pstmt.executeUpdate();
            
            // 如果插入成功，获取生成的ID
            if (result > 0) {
                // 获取生成的ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));  // 设置用户ID
                    }
                }
                return true;  // 返回创建成功
            }
        }
        
        // 创建失败返回false
        return false;
    }
    
    /**
     * 更新用户
     * @param user 要更新的用户对象
     * @return 更新成功返回true，失败返回false
     * @throws SQLException SQL异常
     */
    public boolean updateUser(User user) throws SQLException {
        // 定义更新用户的SQL语句，更新所有用户字段
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, " +
                    "role = ?, department = ?, phone_number = ?, active = ?, updated_at = ?, " +
                    "updated_by = ?, login_attempts = ?, account_locked_until = ?, last_login = ? " +
                    "WHERE id = ? AND deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数，对应用户对象的各个属性
            pstmt.setString(1, user.getUsername());       // 用户名
            pstmt.setString(2, user.getPassword());       // 密码
            pstmt.setString(3, user.getEmail());          // 邮箱
            pstmt.setString(4, user.getFullName());       // 全名
            pstmt.setString(5, user.getRole());           // 角色
            pstmt.setString(6, user.getDepartment());     // 部门
            pstmt.setString(7, user.getPhoneNumber());    // 电话号码
            pstmt.setBoolean(8, user.isActive());         // 是否激活
            pstmt.setTimestamp(9, new Timestamp(user.getUpdatedAt().getTime())); // 更新时间
            pstmt.setString(10, user.getUpdatedBy());     // 更新者
            pstmt.setInt(11, user.getLoginAttempts());    // 登录失败次数
            
            // 设置账户锁定时间，如果为null则设置为NULL
            if (user.getAccountLockedUntil() != null) {
                pstmt.setTimestamp(12, new Timestamp(user.getAccountLockedUntil().getTime()));
            } else {
                pstmt.setNull(12, Types.TIMESTAMP);
            }
            
            // 设置最后登录时间，如果为null则设置为NULL
            if (user.getLastLogin() != null) {
                pstmt.setTimestamp(13, new Timestamp(user.getLastLogin().getTime()));
            } else {
                pstmt.setNull(13, Types.TIMESTAMP);
            }
            
            // 设置用户ID
            pstmt.setInt(14, user.getId());
            
            // 执行更新操作并返回是否成功
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除用户（软删除）
     * @param id 要删除的用户ID
     * @return 删除成功返回true，失败返回false
     * @throws SQLException SQL异常
     */
    public boolean deleteUser(int id) throws SQLException {
        // 定义软删除用户的SQL语句，将deleted字段设置为true
        String sql = "UPDATE users SET deleted = true, updated_at = ? WHERE id = ?";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime())); // 更新时间
            pstmt.setInt(2, id);  // 用户ID
            
            // 执行更新操作并返回是否成功
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 更新最后登录时间
     * @param id 用户ID
     * @return 更新成功返回true，失败返回false
     * @throws SQLException SQL异常
     */
    public boolean updateLastLogin(int id) throws SQLException {
        // 定义更新最后登录时间的SQL语句，同时重置登录失败次数和账户锁定时间
        String sql = "UPDATE users SET last_login = ?, login_attempts = 0, account_locked_until = NULL " +
                    "WHERE id = ? AND deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime())); // 最后登录时间
            pstmt.setInt(2, id);  // 用户ID
            
            // 执行更新操作并返回是否成功
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 增加登录失败次数
     * @param id 用户ID
     * @return 更新成功返回true，失败返回false
     * @throws SQLException SQL异常
     */
    public boolean incrementLoginAttempts(int id) throws SQLException {
        // 定义增加登录失败次数的SQL语句
        String sql = "UPDATE users SET login_attempts = login_attempts + 1, updated_at = ? " +
                    "WHERE id = ? AND deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime())); // 更新时间
            pstmt.setInt(2, id);  // 用户ID
            
            // 执行更新操作并返回是否成功
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 重置登录失败次数
     * @param id 用户ID
     * @return 更新成功返回true，失败返回false
     * @throws SQLException SQL异常
     */
    public boolean resetLoginAttempts(int id) throws SQLException {
        // 定义重置登录失败次数的SQL语句，同时重置账户锁定时间
        String sql = "UPDATE users SET login_attempts = 0, account_locked_until = NULL, updated_at = ? " +
                    "WHERE id = ? AND deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime())); // 更新时间
            pstmt.setInt(2, id);  // 用户ID
            
            // 执行更新操作并返回是否成功
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 获取用户统计信息
     * @return UserStatistics对象，包含用户统计信息
     * @throws SQLException SQL异常
     */
    public UserStatistics getUserStatistics() throws SQLException {
        // 定义获取用户统计信息的SQL语句，统计各种用户状态和角色的数量
        String sql = "SELECT " +
                    "COUNT(*) as total_users, " +
                    "SUM(CASE WHEN active = true THEN 1 ELSE 0 END) as active_users, " +
                    "SUM(CASE WHEN active = false THEN 1 ELSE 0 END) as inactive_users, " +
                    "SUM(CASE WHEN account_locked_until > NOW() THEN 1 ELSE 0 END) as locked_users, " +
                    "SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) as admin_count, " +
                    "SUM(CASE WHEN role = 'SALES' THEN 1 ELSE 0 END) as sales_count, " +
                    "SUM(CASE WHEN role = 'REVIEWER' THEN 1 ELSE 0 END) as reviewer_count, " +
                    "SUM(CASE WHEN role = 'USER' THEN 1 ELSE 0 END) as user_count " +
                    "FROM users WHERE deleted = false";
        
        // 使用try-with-resources自动关闭数据库连接、语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // 处理查询结果
            if (rs.next()) {
                // 创建并返回UserStatistics对象
                return new UserStatistics(
                    rs.getInt("total_users"),      // 总用户数
                    rs.getInt("active_users"),     // 激活用户数
                    rs.getInt("inactive_users"),   // 未激活用户数
                    rs.getInt("locked_users"),     // 锁定用户数
                    rs.getInt("admin_count"),      // 管理员数
                    rs.getInt("sales_count"),      // 销售人员数
                    rs.getInt("reviewer_count"),   // 审核人员数
                    rs.getInt("user_count")        // 普通用户数
                );
            }
        }
        
        // 查询失败时返回null
        return null;
    }
    
    /**
     * 搜索用户
     * @param keyword 搜索关键词
     * @return 包含符合条件的未删除用户的列表
     * @throws SQLException SQL异常
     */
    public List<User> searchUsers(String keyword) throws SQLException {
        // 定义搜索用户的SQL语句，在用户名、邮箱、全名和部门中搜索关键词
        String sql = "SELECT * FROM users WHERE deleted = false AND " +
                    "(username LIKE ? OR email LIKE ? OR full_name LIKE ? OR department LIKE ?) " +
                    "ORDER BY id";
        
        // 创建用于存储用户的列表
        List<User> users = new ArrayList<>();
        // 构造搜索模式
        String searchPattern = "%" + keyword + "%";
        
        // 使用try-with-resources自动关闭数据库连接和预编译语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置SQL参数
            pstmt.setString(1, searchPattern);  // 用户名搜索模式
            pstmt.setString(2, searchPattern);  // 邮箱搜索模式
            pstmt.setString(3, searchPattern);  // 全名搜索模式
            pstmt.setString(4, searchPattern);  // 部门搜索模式
            
            // 执行查询并处理结果集
            try (ResultSet rs = pstmt.executeQuery()) {
                // 遍历结果集，将每条记录映射为User对象并添加到列表中
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        
        // 返回用户列表
        return users;
    }
    
    /**
     * 映射ResultSet到User对象
     * @param rs 数据库查询结果集
     * @return 映射后的User对象
     * @throws SQLException SQL异常
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        // 创建新的User对象
        User user = new User();
        // 从结果集中获取各字段值并设置到User对象中
        user.setId(rs.getInt("id"));                    // 用户ID
        user.setUsername(rs.getString("username"));     // 用户名
        user.setPassword(rs.getString("password"));     // 密码
        user.setEmail(rs.getString("email"));           // 邮箱
        user.setFullName(rs.getString("full_name"));    // 全名
        user.setRole(rs.getString("role"));             // 角色
        user.setDepartment(rs.getString("department")); // 部门
        user.setPhoneNumber(rs.getString("phone_number")); // 电话号码
        user.setActive(rs.getBoolean("active"));        // 是否激活
        user.setCreatedAt(rs.getTimestamp("created_at"));  // 创建时间
        user.setUpdatedAt(rs.getTimestamp("updated_at"));  // 更新时间
        user.setLastLogin(rs.getTimestamp("last_login"));  // 最后登录时间
        user.setCreatedBy(rs.getString("created_by"));  // 创建者
        user.setUpdatedBy(rs.getString("updated_by"));  // 更新者
        user.setLoginAttempts(rs.getInt("login_attempts")); // 登录失败次数
        
        // 获取账户锁定时间，如果为null则不设置
        Timestamp lockedUntil = rs.getTimestamp("account_locked_until");
        if (lockedUntil != null) {
            user.setAccountLockedUntil(new Date(lockedUntil.getTime()));
        }
        
        // 返回映射完成的User对象
        return user;
    }
}