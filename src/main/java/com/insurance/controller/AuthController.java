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
    
    /**
     * 处理GET请求（显示登录页面）
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 如果已登录，重定向到主页
        // 检查用户是否已经登录，如果已登录则重定向到用户主页
        if (AuthUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/mypage/dashboard");
            return;
        }
        
        // 检查是否为会话超时
        // 检查请求参数中是否包含timeout=true，表示会话超时
        boolean timeout = "true".equals(request.getParameter("timeout"));
        if (timeout) {
            // 如果是会话超时，设置请求属性timeout为true
            request.setAttribute("timeout", true);
        }
        
        // 生成CSRF令牌
        // 生成CSRF令牌以防止跨站请求伪造攻击
        String csrfToken = SecurityFilter.generateCSRFToken(request);
        // 将CSRF令牌设置为请求属性，供JSP页面使用
        request.setAttribute("csrfToken", csrfToken);
        
        // 转发到登录页面
        // 将请求转发到登录页面JSP
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }
    
    /**
     * 处理POST请求（处理登录表单提交）
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置请求字符编码
        // 设置请求的字符编码为UTF-8，以正确处理中文字符
        request.setCharacterEncoding("UTF-8");
        
        // 获取表单参数
        // 从请求中获取用户名、密码和CSRF令牌参数
        String username = request.getParameter("username"); // 用户名
        String password = request.getParameter("password"); // 密码
        String csrfToken = request.getParameter("csrfToken"); // CSRF令牌
        
        // 验证CSRF令牌
        // 获取会话中的CSRF令牌并与表单提交的令牌进行比较
        String sessionToken = SecurityFilter.getCSRFToken(request);
        // 如果令牌为空或不匹配，则记录警告日志并返回错误信息
        if (sessionToken == null || !sessionToken.equals(csrfToken)) {
            LogUtil.warn("CSRF令牌验证失败: " + username, "AuthController", AuthUtil.getRemoteAddr(request));
            request.setAttribute("error", "CSRF令牌验证失败");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // 验证输入参数
        // 检查用户名和密码是否为空或仅包含空格
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            // 如果输入参数无效，设置错误信息并转发回登录页面
            request.setAttribute("error", "用户名和密码不能为空");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }
        
        // 认证用户
        // 调用认证工具类验证用户名和密码
        boolean authenticated = AuthUtil.authenticate(username, password, request);
        
        // 根据认证结果进行相应处理
        if (authenticated) {
            // 登录成功，根据用户角色重定向到相应页面
            // 从会话中获取用户角色
            String userRole = (String) request.getSession().getAttribute("userRole");
            // 根据用户角色获取重定向URL
            String redirectUrl = getRedirectUrl(userRole);
            
            // 记录登录成功日志
            LogUtil.info("用户登录成功，重定向到: " + redirectUrl, 
                        "AuthController", AuthUtil.getRemoteAddr(request));
            
            // 重定向到相应页面
            response.sendRedirect(request.getContextPath() + redirectUrl);
        } else {
            // 登录失败
            // 记录登录失败日志
            LogUtil.warn("用户登录失败: " + username, "AuthController", AuthUtil.getRemoteAddr(request));
            // 设置错误信息和用户名属性
            request.setAttribute("error", "用户名或密码错误");
            request.setAttribute("username", username);
            
            // 重新生成CSRF令牌
            // 登录失败后重新生成CSRF令牌以提高安全性
            String newCsrfToken = SecurityFilter.generateCSRFToken(request);
            request.setAttribute("csrfToken", newCsrfToken);
            
            // 转发回登录页面
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
    
    /**
     * 根据用户角色获取重定向URL
     * @param role 用户角色
     * @return 重定向URL
     */
    private String getRedirectUrl(String role) {
        // 根据用户角色返回相应的重定向URL
        switch (role) {
            case "ADMIN":
                // 管理员重定向到管理仪表板
                return "/admin/dashboard";
            case "SALES":
                // 销售人员重定向到客户列表
                return "/customer/list";
            case "REVIEWER":
                // 审核人员重定向到合同审核页面
                return "/contract/review";
            case "USER":
                // 普通用户重定向到个人主页仪表板
                return "/mypage/dashboard";
            default:
                // 默认重定向到个人主页仪表板
                return "/mypage/dashboard";
        }
    }
}

/**
 * 登出控制器
 * 处理用户登出操作
 */
@WebServlet("/logout")
class LogoutController extends HttpServlet {
    
    /**
     * 处理GET请求（处理用户登出）
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 记录用户登出
        // 检查用户是否已登录，如果已登录则记录登出日志并执行登出操作
        if (AuthUtil.isLoggedIn(request)) {
            LogUtil.info("用户登出", "LogoutController", AuthUtil.getRemoteAddr(request));
            AuthUtil.logout(request);
        }
        
        // 重定向到登录页面
        // 登出后重定向到登录页面
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }
}

/**
 * 访问拒绝控制器
 * 处理用户访问被拒绝的情况
 */
@WebServlet("/access-denied")
class AccessDeniedController extends HttpServlet {
    
    /**
     * 处理GET请求（显示访问拒绝页面）
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 记录访问拒绝
        // 记录访问被拒绝的日志
        LogUtil.warn("访问被拒绝", "AccessDeniedController", AuthUtil.getRemoteAddr(request));
        
        // 设置响应状态码
        // 设置HTTP响应状态码为403（禁止访问）
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        // 转发到访问拒绝页面
        // 将请求转发到访问拒绝的JSP页面
        request.getRequestDispatcher("/WEB-INF/views/error/access-denied.jsp").forward(request, response);
    }
}

/**
 * 会话超时控制器
 * 处理用户会话超时的情况
 */
@WebServlet("/session-timeout")
class SessionTimeoutController extends HttpServlet {
    
    /**
     * 处理GET请求（显示会话超时页面）
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 记录会话超时
        // 记录会话超时的日志
        LogUtil.info("会话超时", "SessionTimeoutController", AuthUtil.getRemoteAddr(request));
        
        // 设置响应状态码
        // 设置HTTP响应状态码为401（未授权）
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 转发到会话超时页面
        // 将请求转发到会话超时的JSP页面
        request.getRequestDispatcher("/WEB-INF/views/error/session-timeout.jsp").forward(request, response);
    }
}