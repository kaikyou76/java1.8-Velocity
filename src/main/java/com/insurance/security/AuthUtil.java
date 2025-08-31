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
    
    // 用户服务实例，用于处理用户相关的业务逻辑
    private static final UserService userService = new UserService();
    
    /**
     * 密码加密
     * 使用SHA-256算法对密码进行加密处理
     * @param password 原始密码
     * @return String 加密后的密码
     */
    public static String encryptPassword(String password) {
        try {
            // 获取SHA-256消息摘要实例
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 对密码进行摘要计算
            byte[] hashedBytes = md.digest(password.getBytes());
            // 将字节数组转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                // 格式化每个字节为两位十六进制数
                sb.append(String.format("%02x", b));
            }
            // 返回加密后的密码字符串
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // 记录加密失败日志并抛出运行时异常
            LogUtil.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 用户登录验证
     * 验证用户提供的用户名和密码是否正确
     * @param username 用户名
     * @param password 密码
     * @param request HTTP请求对象
     * @return boolean 登录成功返回true，否则返回false
     */
    public static boolean authenticate(String username, String password, HttpServletRequest request) {
        try {
            // 根据用户名获取用户信息
            User user = userService.getUserByUsername(username);
            // 验证用户是否存在、账户是否激活以及密码是否正确
            if (user != null && user.isActive() && 
                user.getPassword().equals(encryptPassword(password))) {
                
                // 创建会话并存储用户信息
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("loginTime", new Date());
                session.setAttribute("userRole", user.getRole());
                
                // 记录登录成功日志
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
            // 记录认证异常日志并返回false
            LogUtil.error("用户认证异常: " + username, e);
            return false;
        }
    }
    
    /**
     * 用户登出
     * 销毁用户的会话信息
     * @param request HTTP请求对象
     */
    public static void logout(HttpServletRequest request) {
        // 获取当前会话（如果不存在则返回null）
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 获取会话中的用户信息
            User user = (User) session.getAttribute("user");
            if (user != null) {
                // 记录用户登出日志
                LogUtil.info("用户登出: " + user.getUsername(), 
                            "AuthUtil", 
                            getRemoteAddr(request));
            }
            // 销毁会话
            session.invalidate();
        }
    }
    
    /**
     * 检查用户是否已登录
     * 通过检查会话中是否存在用户信息来判断用户是否已登录
     * @param request HTTP请求对象
     * @return boolean 已登录返回true，否则返回false
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        // 获取当前会话（如果不存在则返回null）
        HttpSession session = request.getSession(false);
        // 检查会话是否存在且包含用户信息
        return session != null && session.getAttribute("user") != null;
    }
    
    /**
     * 获取当前登录用户
     * 从会话中获取当前登录的用户对象
     * @param request HTTP请求对象
     * @return User 当前登录用户对象，未登录则返回null
     */
    public static User getCurrentUser(HttpServletRequest request) {
        // 获取当前会话（如果不存在则返回null）
        HttpSession session = request.getSession(false);
        // 如果会话存在，返回会话中的用户对象，否则返回null
        return session != null ? (User) session.getAttribute("user") : null;
    }
    
    /**
     * 检查用户权限
     * 验证当前用户是否具有指定的角色权限
     * @param request HTTP请求对象
     * @param requiredRole 所需的角色
     * @return boolean 具有权限返回true，否则返回false
     */
    public static boolean hasPermission(HttpServletRequest request, String requiredRole) {
        // 获取当前登录用户
        User user = getCurrentUser(request);
        // 如果用户未登录，返回false
        if (user == null) {
            return false;
        }
        
        // 管理员拥有所有权限
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        
        // 检查用户角色是否与所需角色匹配
        return requiredRole.equals(user.getRole());
    }
    
    /**
     * 检查模块访问权限
     * 根据用户角色验证其对特定模块的访问权限
     * @param request HTTP请求对象
     * @param module 模块名称
     * @return boolean 具有访问权限返回true，否则返回false
     */
    public static boolean hasModuleAccess(HttpServletRequest request, String module) {
        // 获取当前登录用户
        User user = getCurrentUser(request);
        // 如果用户未登录，返回false
        if (user == null) {
            return false;
        }
        
        // 管理员可以访问所有模块
        if ("ADMIN".equals(user.getRole())) {
            return true;
        }
        
        // 根据角色和模块权限定义进行检查
        switch (user.getRole()) {
            // 销售员可以访问客户、合同和保险模块
            case "SALES":
                return "CUSTOMER".equals(module) || "CONTRACT".equals(module) || "PREMIUM".equals(module);
            // 审查员可以访问合同和理赔模块
            case "REVIEWER":
                return "CONTRACT".equals(module) || "CLAIM".equals(module);
            // 普通用户可以访问个人主页模块
            case "USER":
                return "MYPAGE".equals(module);
            // 其他角色无权限
            default:
                return false;
        }
    }
    
    /**
     * 获取客户端IP地址
     * 处理代理服务器情况，获取真实的客户端IP地址
     * @param request HTTP请求对象
     * @return String 客户端IP地址
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        // 尝试从X-Forwarded-For头获取IP
        String ip = request.getHeader("X-Forwarded-For");
        // 如果获取不到或为空或为unknown，尝试从Proxy-Client-IP头获取
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        // 如果获取不到或为空或为unknown，尝试从WL-Proxy-Client-IP头获取
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        // 如果获取不到或为空或为unknown，直接从请求中获取
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 返回客户端IP地址
        return ip;
    }
    
    /**
     * 检查会话是否超时
     * 检查用户会话是否已超过30分钟未活动
     * @param request HTTP请求对象
     * @return boolean 会话超时返回true，否则返回false
     */
    public static boolean isSessionTimeout(HttpServletRequest request) {
        // 获取当前会话
        HttpSession session = request.getSession(false);
        // 如果会话不存在，认为已超时
        if (session == null) {
            return true;
        }
        
        // 检查会话超时（30分钟）
        // 获取登录时间
        Date loginTime = (Date) session.getAttribute("loginTime");
        // 如果登录时间不存在，认为已超时
        if (loginTime == null) {
            return true;
        }
        
        // 设置超时时间为30分钟
        long timeout = 30 * 60 * 1000; // 30分钟
        // 检查当前时间与登录时间的差值是否超过超时时间
        return (System.currentTimeMillis() - loginTime.getTime()) > timeout;
    }
    
    /**
     * 更新会话活动时间
     * 更新会话中的最后活动时间
     * @param request HTTP请求对象
     */
    public static void updateSessionActivity(HttpServletRequest request) {
        // 获取当前会话
        HttpSession session = request.getSession(false);
        // 如果会话存在，更新最后活动时间
        if (session != null) {
            session.setAttribute("lastActivity", new Date());
        }
    }
}