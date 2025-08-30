package com.insurance.controller;

import com.insurance.security.AuthUtil;
import com.insurance.security.SecurityFilter;
import com.insurance.util.LogUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证控制器
 * 处理用户登录、登出等操作
 */
@WebServlet("/login")
public class AuthController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 如果已登录，重定向到主页
        if (AuthUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/mypage/dashboard");
            return;
        }
        
        // 检查是否为会话超时
        boolean timeout = "true".equals(request.getParameter("timeout"));
        if (timeout) {
            request.setAttribute("timeout", true);
        }
        
        // 生成CSRF令牌
        String csrfToken = SecurityFilter.generateCSRFToken(request);
        request.setAttribute("csrfToken", csrfToken);
        
        // 转发到登录页面
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置请求字符编码
        request.setCharacterEncoding("UTF-8");
        
        // 获取表单参数
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String csrfToken = request.getParameter("csrfToken");
        
        // 验证CSRF令牌
        String sessionToken = SecurityFilter.getCSRFToken(request);
        if (sessionToken == null || !sessionToken.equals(csrfToken)) {
            LogUtil.warn("CSRF令牌验证失败: " + username, "AuthController", AuthUtil.getRemoteAddr(request));
            request.setAttribute("error", "CSRF令牌验证失败");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // 验证输入参数
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "用户名和密码不能为空");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // 认证用户
        boolean authenticated = AuthUtil.authenticate(username, password, request);
        
        if (authenticated) {
            // 登录成功，根据用户角色重定向到相应页面
            String userRole = (String) request.getSession().getAttribute("userRole");
            String redirectUrl = getRedirectUrl(userRole);
            
            LogUtil.info("用户登录成功，重定向到: " + redirectUrl, 
                        "AuthController", AuthUtil.getRemoteAddr(request));
            
            response.sendRedirect(request.getContextPath() + redirectUrl);
        } else {
            // 登录失败
            LogUtil.warn("用户登录失败: " + username, "AuthController", AuthUtil.getRemoteAddr(request));
            request.setAttribute("error", "用户名或密码错误");
            request.setAttribute("username", username);
            
            // 重新生成CSRF令牌
            String newCsrfToken = SecurityFilter.generateCSRFToken(request);
            request.setAttribute("csrfToken", newCsrfToken);
            
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
    /**
     * 根据用户角色获取重定向URL
     */
    private String getRedirectUrl(String role) {
        switch (role) {
            case "ADMIN":
                return "/admin/dashboard";
            case "SALES":
                return "/customer/list";
            case "REVIEWER":
                return "/contract/review";
            case "USER":
                return "/mypage/dashboard";
            default:
                return "/mypage/dashboard";
        }
    }
}

/**
 * 登出控制器
 */
@WebServlet("/logout")
class LogoutController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 记录用户登出
        if (AuthUtil.isLoggedIn(request)) {
            LogUtil.info("用户登出", "LogoutController", AuthUtil.getRemoteAddr(request));
            AuthUtil.logout(request);
        }
        
        // 重定向到登录页面
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}

/**
 * 访问拒绝控制器
 */
@WebServlet("/access-denied")
class AccessDeniedController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 记录访问拒绝
        LogUtil.warn("访问被拒绝", "AccessDeniedController", AuthUtil.getRemoteAddr(request));
        
        // 设置响应状态码
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        // 转发到访问拒绝页面
        request.getRequestDispatcher("/WEB-INF/views/error/access-denied.jsp").forward(request, response);
    }
}

/**
 * 会话超时控制器
 */
@WebServlet("/session-timeout")
class SessionTimeoutController extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 记录会话超时
        LogUtil.info("会话超时", "SessionTimeoutController", AuthUtil.getRemoteAddr(request));
        
        // 设置响应状态码
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 转发到会话超时页面
        request.getRequestDispatcher("/WEB-INF/views/error/session-timeout.jsp").forward(request, response);
    }
}