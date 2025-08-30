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
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND deleted = false";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 根据邮箱获取用户
     */
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND deleted = false";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? AND deleted = false";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        
        return null;
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT * FROM users WHERE deleted = false ORDER BY id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        
        return users;
    }
    
    /**
     * 根据角色获取用户列表
     */
    public List<User> getUsersByRole(String role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role = ? AND deleted = false ORDER BY id";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        
        return users;
    }
    
    /**
     * 创建用户
     */
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password, email, full_name, role, department, phone_number, " +
                    "active, created_at, updated_at, created_by, updated_by, login_attempts, account_locked_until) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, NULL)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getDepartment());
            pstmt.setString(7, user.getPhoneNumber());
            pstmt.setBoolean(8, user.isActive());
            pstmt.setTimestamp(9, new Timestamp(user.getCreatedAt().getTime()));
            pstmt.setTimestamp(10, new Timestamp(user.getUpdatedAt().getTime()));
            pstmt.setString(11, user.getCreatedBy());
            pstmt.setString(12, user.getUpdatedBy());
            
            int result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 获取生成的ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 更新用户
     */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, full_name = ?, " +
                    "role = ?, department = ?, phone_number = ?, active = ?, updated_at = ?, " +
                    "updated_by = ?, login_attempts = ?, account_locked_until = ?, last_login = ? " +
                    "WHERE id = ? AND deleted = false";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole());
            pstmt.setString(6, user.getDepartment());
            pstmt.setString(7, user.getPhoneNumber());
            pstmt.setBoolean(8, user.isActive());
            pstmt.setTimestamp(9, new Timestamp(user.getUpdatedAt().getTime()));
            pstmt.setString(10, user.getUpdatedBy());
            pstmt.setInt(11, user.getLoginAttempts());
            
            if (user.getAccountLockedUntil() != null) {
                pstmt.setTimestamp(12, new Timestamp(user.getAccountLockedUntil().getTime()));
            } else {
                pstmt.setNull(12, Types.TIMESTAMP);
            }
            
            if (user.getLastLogin() != null) {
                pstmt.setTimestamp(13, new Timestamp(user.getLastLogin().getTime()));
            } else {
                pstmt.setNull(13, Types.TIMESTAMP);
            }
            
            pstmt.setInt(14, user.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 删除用户（软删除）
     */
    public boolean deleteUser(int id) throws SQLException {
        String sql = "UPDATE users SET deleted = true, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 更新最后登录时间
     */
    public boolean updateLastLogin(int id) throws SQLException {
        String sql = "UPDATE users SET last_login = ?, login_attempts = 0, account_locked_until = NULL " +
                    "WHERE id = ? AND deleted = false";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 增加登录失败次数
     */
    public boolean incrementLoginAttempts(int id) throws SQLException {
        String sql = "UPDATE users SET login_attempts = login_attempts + 1, updated_at = ? " +
                    "WHERE id = ? AND deleted = false";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 重置登录失败次数
     */
    public boolean resetLoginAttempts(int id) throws SQLException {
        String sql = "UPDATE users SET login_attempts = 0, account_locked_until = NULL, updated_at = ? " +
                    "WHERE id = ? AND deleted = false";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * 获取用户统计信息
     */
    public UserStatistics getUserStatistics() throws SQLException {
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
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new UserStatistics(
                    rs.getInt("total_users"),
                    rs.getInt("active_users"),
                    rs.getInt("inactive_users"),
                    rs.getInt("locked_users"),
                    rs.getInt("admin_count"),
                    rs.getInt("sales_count"),
                    rs.getInt("reviewer_count"),
                    rs.getInt("user_count")
                );
            }
        }
        
        return null;
    }
    
    /**
     * 搜索用户
     */
    public List<User> searchUsers(String keyword) throws SQLException {
        String sql = "SELECT * FROM users WHERE deleted = false AND " +
                    "(username LIKE ? OR email LIKE ? OR full_name LIKE ? OR department LIKE ?) " +
                    "ORDER BY id";
        
        List<User> users = new ArrayList<>();
        String searchPattern = "%" + keyword + "%";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        
        return users;
    }
    
    /**
     * 映射ResultSet到User对象
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setDepartment(rs.getString("department"));
        user.setPhoneNumber(rs.getString("phone_number"));
        user.setActive(rs.getBoolean("active"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        user.setCreatedBy(rs.getString("created_by"));
        user.setUpdatedBy(rs.getString("updated_by"));
        user.setLoginAttempts(rs.getInt("login_attempts"));
        
        Timestamp lockedUntil = rs.getTimestamp("account_locked_until");
        if (lockedUntil != null) {
            user.setAccountLockedUntil(new Date(lockedUntil.getTime()));
        }
        
        return user;
    }
}