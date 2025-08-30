package com.insurance.security;

import com.insurance.model.User;
import com.insurance.service.UserService;
import com.insurance.util.LogUtil;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * 认证工具类
 * 处理用户登录、登出、权限验证等安全相关功能
 */
public class AuthUtil {
    
    private static final UserService userService = new UserService();
    
    /**
     * 密码加密
     */
    public static String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LogUtil.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 用户登录验证
     */
    public static boolean authenticate(String username, String password, HttpServletRequest request) {
        try {
            User user = userService.getUserByUsername(username);
            if (user != null && user.isActive() && 
                user.getPassword().equals(encryptPassword(password))) {
                
                // 创建会话
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("loginTime", new Date());
                session.setAttribute("userRole", user.getRole());
                
                // 记录登录日志
                LogUtil.info("用户登录成功: " + username, 
                            "AuthUtil", 
                            getRemoteAddr(request));
                
                return true;
            }
            
            // 记录登录失败日志
            LogUtil.warn("用户登录失败: " + username, 
                         "AuthUtil", 
                         getRemoteAddr(request));
            
            return false;
        } catch (Exception e) {
            LogUtil.error("用户认证异常: " + username, e);
            return false;
        }
    }
    
    /**
     * 用户登出
     */
    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                LogUtil.info("用户登出: " + user.getUsername(), 
                            "AuthUtil", 
                            getRemoteAddr(request));
            }
            session.invalidate();
        }
    }
    
    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }
    
    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null ? (User) session.getAttribute("user") : null;
    }
    
    /**
     * 检查用户权限
     */
    public static boolean hasPermission(HttpServletRequest request, String requiredRole) {
        User user = getCurrentUser(request);
        if (user == null) {
            return false;
        }
        
        // 管理员拥有所有权限
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        
        return requiredRole.equals(user.getRole());
    }
    
    /**
     * 检查模块访问权限
     */
    public static boolean hasModuleAccess(HttpServletRequest request, String module) {
        User user = getCurrentUser(request);
        if (user == null) {
            return false;
        }
        
        // 管理员可以访问所有模块
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        
        // 根据角色和模块权限定义
        switch (user.getRole()) {
            case "SALES":
                return "CUSTOMER".equals(module) || "CONTRACT".equals(module) || "PREMIUM".equals(module);
            case "REVIEWER":
                return "CONTRACT".equals(module) || "CLAIM".equals(module);
            case "USER":
                return "MYPAGE".equals(module);
            default:
                return false;
        }
    }
    
    /**
     * 获取客户端IP地址
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 检查会话是否超时
     */
    public static boolean isSessionTimeout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }
        
        // 检查会话超时（30分钟）
        Date loginTime = (Date) session.getAttribute("loginTime");
        if (loginTime == null) {
            return true;
        }
        
        long timeout = 30 * 60 * 1000; // 30分钟
        return (System.currentTimeMillis() - loginTime.getTime()) > timeout;
    }
    
    /**
     * 更新会话活动时间
     */
    public static void updateSessionActivity(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("lastActivity", new Date());
        }
    }
}