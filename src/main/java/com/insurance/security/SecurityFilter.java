package com.insurance.security;

import com.insurance.util.LogUtil;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 安全过滤器
 * 处理认证、授权、会话管理等安全相关功能
 */
@WebFilter(urlPatterns = {"/*"})
public class SecurityFilter implements Filter {
    
    // 不需要认证的路径
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/login.jsp",
        "/login",
        "/logout",
        "/css/",
        "/js/",
        "/images/",
        "/favicon.ico"
    );
    
    // 需要管理员权限的路径
    private static final List<String> ADMIN_PATHS = Arrays.asList(
        "/admin/",
        "/batch/",
        "/logs/",
        "/monitor/"
    );
    
    // 需要销售员权限的路径
    private static final List<String> SALES_PATHS = Arrays.asList(
        "/customer/",
        "/contract/",
        "/premium/"
    );
    
    // 需要审查员权限的路径
    private static final List<String> REVIEWER_PATHS = Arrays.asList(
        "/contract/review/",
        "/claim/"
    );
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LogUtil.info("安全过滤器初始化完成", "SecurityFilter");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // 设置安全响应头
            setSecurityHeaders(httpResponse);
            
            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
            
            // 检查是否为公共路径
            if (isPublicPath(path)) {
                chain.doFilter(request, response);
                return;
            }
            
            // 检查用户是否已登录
            if (!AuthUtil.isLoggedIn(httpRequest)) {
                LogUtil.warn("未授权访问尝试: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
                return;
            }
            
            // 检查会话是否超时
            if (AuthUtil.isSessionTimeout(httpRequest)) {
                LogUtil.info("会话超时: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                AuthUtil.logout(httpRequest);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?timeout=true");
                return;
            }
            
            // 更新会话活动时间
            AuthUtil.updateSessionActivity(httpRequest);
            
            // 检查权限
            if (!checkPermission(httpRequest, path)) {
                LogUtil.warn("权限不足: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
                return;
            }
            
            // 检查CSRF令牌
            if (!checkCSRFToken(httpRequest, httpResponse)) {
                LogUtil.warn("CSRF令牌验证失败: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF令牌验证失败");
                return;
            }
            
            // 防止会话固定攻击
            preventSessionFixation(httpRequest);
            
            // 所有检查通过，继续处理请求
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            LogUtil.error("安全过滤器处理异常: " + path, e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统错误");
        }
    }
    
    @Override
    public void destroy() {
        LogUtil.info("安全过滤器销毁", "SecurityFilter");
    }
    
    /**
     * 设置安全响应头
     */
    private void setSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }
    
    /**
     * 检查是否为公共路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查权限
     */
    private boolean checkPermission(HttpServletRequest request, String path) {
        // 管理员权限检查
        if (isAdminPath(path) && !AuthUtil.hasPermission(request, "ADMIN")) {
            return false;
        }
        
        // 销售员权限检查
        if (isSalesPath(path) && !AuthUtil.hasPermission(request, "SALES")) {
            return false;
        }
        
        // 审查员权限检查
        if (isReviewerPath(path) && !AuthUtil.hasPermission(request, "REVIEWER")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查是否为管理员路径
     */
    private boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查是否为销售员路径
     */
    private boolean isSalesPath(String path) {
        return SALES_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查是否为审查员路径
     */
    private boolean isReviewerPath(String path) {
        return REVIEWER_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查CSRF令牌
     */
    private boolean checkCSRFToken(HttpServletRequest request, HttpServletResponse response) {
        String method = request.getMethod();
        
        // 只对POST、PUT、DELETE方法检查CSRF令牌
        if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
            return true;
        }
        
        String sessionToken = (String) request.getSession().getAttribute("csrfToken");
        String requestToken = request.getParameter("csrfToken");
        
        if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 防止会话固定攻击
     */
    private void preventSessionFixation(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("sessionFixed") == null) {
            // 重新生成会话ID
            request.changeSessionId();
            session.setAttribute("sessionFixed", true);
        }
    }
    
    /**
     * 生成CSRF令牌
     */
    public static String generateCSRFToken(HttpServletRequest request) {
        String token = java.util.UUID.randomUUID().toString();
        request.getSession().setAttribute("csrfToken", token);
        return token;
    }
    
    /**
     * 获取CSRF令牌
     */
    public static String getCSRFToken(HttpServletRequest request) {
        return (String) request.getSession().getAttribute("csrfToken");
    }
}