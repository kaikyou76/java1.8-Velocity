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
    
    // 不需要认证的公共路径列表
    // 这些路径可以被未登录用户访问
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/login.jsp",      // 登录页面
        "/login",          // 登录处理接口
        "/logout",         // 登出处理接口
        "/css/",           // CSS样式文件目录
        "/js/",            // JavaScript文件目录
        "/images/",        // 图片文件目录
        "/favicon.ico"     // 网站图标
    );
    
    // 需要管理员权限的路径列表
    // 只有管理员角色可以访问这些路径
    private static final List<String> ADMIN_PATHS = Arrays.asList(
        "/admin/",         // 管理员功能模块
        "/batch/",         // 批处理功能模块
        "/logs/",          // 日志查看模块
        "/monitor/"        // 系统监控模块
    );
    
    // 需要销售员权限的路径列表
    // 只有销售员角色可以访问这些路径
    private static final List<String> SALES_PATHS = Arrays.asList(
        "/customer/",      // 客户管理模块
        "/contract/",      // 合同管理模块
        "/premium/"        // 保险费管理模块
    );
    
    // 需要审查员权限的路径列表
    // 只有审查员角色可以访问这些路径
    private static final List<String> REVIEWER_PATHS = Arrays.asList(
        "/contract/review/",  // 合同审查模块
        "/claim/"             // 理赔处理模块
    );
    
    /**
     * 初始化过滤器
     * @param filterConfig 过滤器配置对象
     * @throws ServletException Servlet异常
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 记录安全过滤器初始化完成日志
        LogUtil.info("安全过滤器初始化完成", "SecurityFilter");
    }
    
    /**
     * 执行过滤逻辑
     * 对每个请求进行安全检查，包括认证、授权、CSRF防护等
     * @param request Servlet请求对象
     * @param response Servlet响应对象
     * @param chain 过滤器链
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // 将请求和响应对象转换为HTTP类型
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // 设置安全响应头，增强安全性
            setSecurityHeaders(httpResponse);
            
            // 获取请求路径
            String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
            
            // 检查是否为公共路径，如果是则直接放行
            if (isPublicPath(path)) {
                chain.doFilter(request, response);
                return;
            }
            
            // 检查用户是否已登录，未登录则重定向到登录页面
            if (!AuthUtil.isLoggedIn(httpRequest)) {
                LogUtil.warn("未授权访问尝试: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
                return;
            }
            
            // 检查会话是否超时，超时则注销用户并重定向到登录页面
            if (AuthUtil.isSessionTimeout(httpRequest)) {
                LogUtil.info("会话超时: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                AuthUtil.logout(httpRequest);
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp?timeout=true");
                return;
            }
            
            // 更新会话活动时间，防止会话超时
            AuthUtil.updateSessionActivity(httpRequest);
            
            // 检查用户权限，权限不足则返回403错误
            if (!checkPermission(httpRequest, path)) {
                LogUtil.warn("权限不足: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足");
                return;
            }
            
            // 检查CSRF令牌，验证失败则返回403错误
            if (!checkCSRFToken(httpRequest, httpResponse)) {
                LogUtil.warn("CSRF令牌验证失败: " + path, "SecurityFilter", AuthUtil.getRemoteAddr(httpRequest));
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF令牌验证失败");
                return;
            }
            
            // 防止会话固定攻击
            preventSessionFixation(httpRequest);
            
            // 所有安全检查通过，继续处理请求
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            // 记录异常日志并返回500错误
            LogUtil.error("安全过滤器处理异常: " + path, e);
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统错误");
        }
    }
    
    /**
     * 销毁过滤器
     */
    @Override
    public void destroy() {
        // 记录安全过滤器销毁日志
        LogUtil.info("安全过滤器销毁", "SecurityFilter");
    }
    
    /**
     * 设置安全响应头
     * 添加各种安全相关的HTTP响应头，增强应用安全性
     * @param response HttpServletResponse对象
     */
    private void setSecurityHeaders(HttpServletResponse response) {
        // 防止MIME类型嗅探
        response.setHeader("X-Content-Type-Options", "nosniff");
        // 防止页面被嵌入到iframe中
        response.setHeader("X-Frame-Options", "DENY");
        // 启用XSS保护
        response.setHeader("X-XSS-Protection", "1; mode=block");
        // 启用HSTS（HTTP严格传输安全）
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        // 设置内容安全策略
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'");
        // 禁止缓存敏感信息
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        // 禁止缓存（兼容HTTP/1.0）
        response.setHeader("Pragma", "no-cache");
        // 设置过期时间为过去的时间
        response.setHeader("Expires", "0");
    }
    
    /**
     * 检查是否为公共路径
     * 判断请求路径是否为不需要认证的公共路径
     * @param path 请求路径
     * @return boolean 是公共路径返回true，否则返回false
     */
    private boolean isPublicPath(String path) {
        // 检查路径是否以公共路径列表中的任意一个开头
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查权限
     * 根据请求路径检查用户是否具有相应的访问权限
     * @param request HttpServletRequest对象
     * @param path 请求路径
     * @return boolean 具有权限返回true，否则返回false
     */
    private boolean checkPermission(HttpServletRequest request, String path) {
        // 管理员权限检查
        // 如果是管理员路径但用户没有管理员权限，返回false
        if (isAdminPath(path) && !AuthUtil.hasPermission(request, "ADMIN")) {
            return false;
        }
        
        // 销售员权限检查
        // 如果是销售员路径但用户没有销售员权限，返回false
        if (isSalesPath(path) && !AuthUtil.hasPermission(request, "SALES")) {
            return false;
        }
        
        // 审查员权限检查
        // 如果是审查员路径但用户没有审查员权限，返回false
        if (isReviewerPath(path) && !AuthUtil.hasPermission(request, "REVIEWER")) {
            return false;
        }
        
        // 所有权限检查通过，返回true
        return true;
    }
    
    /**
     * 检查是否为管理员路径
     * 判断请求路径是否需要管理员权限
     * @param path 请求路径
     * @return boolean 是管理员路径返回true，否则返回false
     */
    private boolean isAdminPath(String path) {
        // 检查路径是否以管理员路径列表中的任意一个开头
        return ADMIN_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查是否为销售员路径
     * 判断请求路径是否需要销售员权限
     * @param path 请求路径
     * @return boolean 是销售员路径返回true，否则返回false
     */
    private boolean isSalesPath(String path) {
        // 检查路径是否以销售员路径列表中的任意一个开头
        return SALES_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查是否为审查员路径
     * 判断请求路径是否需要审查员权限
     * @param path 请求路径
     * @return boolean 是审查员路径返回true，否则返回false
     */
    private boolean isReviewerPath(String path) {
        // 检查路径是否以审查员路径列表中的任意一个开头
        return REVIEWER_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 检查CSRF令牌
     * 验证请求中的CSRF令牌是否与会话中的令牌匹配
     * @param request HttpServletRequest对象
     * @param response HttpServletResponse对象
     * @return boolean CSRF令牌验证通过返回true，否则返回false
     */
    private boolean checkCSRFToken(HttpServletRequest request, HttpServletResponse response) {
        // 获取请求方法
        String method = request.getMethod();
        
        // 只对POST、PUT、DELETE方法检查CSRF令牌，GET等安全方法不需要检查
        if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
            return true;
        }
        
        // 获取会话中的CSRF令牌
        String sessionToken = (String) request.getSession().getAttribute("csrfToken");
        // 获取请求参数中的CSRF令牌
        String requestToken = request.getParameter("csrfToken");
        
        // 如果会话令牌或请求令牌为空，或者两者不匹配，返回false
        if (sessionToken == null || requestToken == null || !sessionToken.equals(requestToken)) {
            return false;
        }
        
        // CSRF令牌验证通过，返回true
        return true;
    }
    
    /**
     * 防止会话固定攻击
     * 通过重新生成会话ID来防止会话固定攻击
     * @param request HttpServletRequest对象
     */
    private void preventSessionFixation(HttpServletRequest request) {
        // 获取当前会话
        HttpSession session = request.getSession(false);
        // 如果会话存在且尚未进行会话固定防护
        if (session != null && session.getAttribute("sessionFixed") == null) {
            // 重新生成会话ID，防止会话固定攻击
            request.changeSessionId();
            // 标记会话固定防护已完成
            session.setAttribute("sessionFixed", true);
        }
    }
    
    /**
     * 生成CSRF令牌
     * 生成一个新的CSRF令牌并存储在会话中
     * @param request HttpServletRequest对象
     * @return String 生成的CSRF令牌
     */
    public static String generateCSRFToken(HttpServletRequest request) {
        // 生成UUID作为CSRF令牌
        String token = java.util.UUID.randomUUID().toString();
        // 将令牌存储在会话中
        request.getSession().setAttribute("csrfToken", token);
        // 返回生成的令牌
        return token;
    }
    
    /**
     * 获取CSRF令牌
     * 从会话中获取CSRF令牌
     * @param request HttpServletRequest对象
     * @return String CSRF令牌
     */
    public static String getCSRFToken(HttpServletRequest request) {
        // 从会话中获取CSRF令牌并返回
        return (String) request.getSession().getAttribute("csrfToken");
    }
}