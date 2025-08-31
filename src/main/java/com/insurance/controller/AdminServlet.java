package com.insurance.controller;

import com.insurance.model.Customer;
import com.insurance.model.DocumentRequest;
import com.insurance.model.User;
import com.insurance.service.CustomerService;
import com.insurance.service.DocumentRequestService;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 管理画面機能Servlet
 * 处理管理员相关的请求和操作
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    
    // 客户服务对象，用于处理客户相关业务逻辑
    private CustomerService customerService;
    // 文档请求服务对象，用于处理文档请求相关业务逻辑
    private DocumentRequestService documentRequestService;
    
    /**
     * 初始化Servlet，创建服务对象实例
     * @throws ServletException Servlet异常
     */
    @Override
    public void init() throws ServletException {
        // 调用父类的初始化方法
        super.init();
        // 创建客户服务实例
        customerService = new CustomerService();
        // 创建文档请求服务实例
        documentRequestService = new DocumentRequestService();
    }
    
    /**
     * 处理GET请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 管理者権限チェック（仮実装）
        // 检查用户是否具有管理员权限，如果没有则重定向到登录页面
        if (!isAdminUser(request)) {
            response.sendRedirect("login?error=管理者権限が必要です");
            return;
        }
        
        // 获取请求参数中的操作类型
        String action = request.getParameter("action");
        
        // 如果没有指定操作类型，默认为dashboard
        if (action == null) {
            action = "dashboard";
        }
        
        // 使用try-catch处理可能的异常
        try {
            // 根据操作类型执行相应的处理方法
            switch (action) {
                case "dashboard":
                    // 显示管理员仪表板
                    showAdminDashboard(request, response);
                    break;
                case "customers":
                    // 显示客户管理页面
                    showCustomerManagement(request, response);
                    break;
                case "requests":
                    // 显示请求管理页面
                    showRequestManagement(request, response);
                    break;
                case "reports":
                    // 显示报告页面
                    showReports(request, response);
                    break;
                case "users":
                    // 显示用户管理页面
                    showUserManagement(request, response);
                    break;
                case "settings":
                    // 显示系统设置页面
                    showSystemSettings(request, response);
                    break;
                default:
                    // 默认显示管理员仪表板
                    showAdminDashboard(request, response);
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并抛出ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 处理POST请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 管理者権限チェック
        // 检查用户是否具有管理员权限，如果没有则重定向到登录页面
        if (!isAdminUser(request)) {
            response.sendRedirect("login?error=管理者権限が必要です");
            return;
        }
        
        // 获取请求参数中的操作类型
        String action = request.getParameter("action");
        
        // 如果没有指定操作类型，重定向到管理员仪表板
        if (action == null) {
            response.sendRedirect("admin?action=dashboard");
            return;
        }
        
        // 使用try-catch处理可能的异常
        try {
            // 根据操作类型执行相应的处理方法
            switch (action) {
                case "updateCustomerStatus":
                    // 更新客户状态
                    updateCustomerStatus(request, response);
                    break;
                case "updateRequestStatus":
                    // 更新请求状态
                    updateRequestStatus(request, response);
                    break;
                case "generateReport":
                    // 生成报告
                    generateReport(request, response);
                    break;
                case "updateSystemSettings":
                    // 更新系统设置
                    updateSystemSettings(request, response);
                    break;
                default:
                    // 默认重定向到管理员仪表板
                    response.sendRedirect("admin?action=dashboard");
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并抛出ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 管理者ダッシュボード表示
     * 显示管理员仪表板页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void showAdminDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 統計情報の取得
        // 获取统计信息
        int totalCustomers = customerService.getAllCustomers().size(); // 获取客户总数
        List<DocumentRequest> allRequests = documentRequestService.getAllDocumentRequests(); // 获取所有文档请求
        Map<String, Integer> requestStats = documentRequestService.getRequestStatistics(); // 获取请求统计信息
        
        // 将统计信息设置为请求属性，供JSP页面使用
        request.setAttribute("totalCustomers", totalCustomers);
        request.setAttribute("totalRequests", allRequests.size());
        request.setAttribute("requestStats", requestStats);
        request.setAttribute("activeTab", "dashboard");
        
        // 获取请求转发器并转发请求到JSP页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 顧客管理画面表示
     * 显示客户管理页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void showCustomerManagement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取所有客户信息
        List<Customer> customers = customerService.getAllCustomers();
        // 将客户列表设置为请求属性，供JSP页面使用
        request.setAttribute("customers", customers);
        request.setAttribute("activeTab", "customers");
        
        // 获取请求转发器并转发请求到JSP页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/customers.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 資料請求管理画面表示
     * 显示资料请求管理页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void showRequestManagement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取所有文档请求信息
        List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
        // 将请求列表设置为请求属性，供JSP页面使用
        request.setAttribute("requests", requests);
        request.setAttribute("activeTab", "requests");
        
        // 获取请求转发器并转发请求到JSP页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/requests.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * レポート画面表示
     * 显示报告页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void showReports(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求统计信息
        Map<String, Integer> stats = documentRequestService.getRequestStatistics();
        // 将统计信息设置为请求属性，供JSP页面使用
        request.setAttribute("stats", stats);
        request.setAttribute("activeTab", "reports");
        
        // 获取请求转发器并转发请求到JSP页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * ユーザー管理画面表示
     * 显示用户管理页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void showUserManagement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // ユーザーリストの取得（仮実装）
        // 获取用户列表（临时实现）
        List<User> users = getUsers();
        // 将用户列表设置为请求属性，供JSP页面使用
        request.setAttribute("users", users);
        request.setAttribute("activeTab", "users");
        
        // 获取请求转发器并转发请求到JSP页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * システム設定画面表示
     * 显示系统设置页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void showSystemSettings(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // システム設定の取得（仮実装）
        // 获取系统设置（临时实现）
        Map<String, String> settings = getSystemSettings();
        // 将设置信息设置为请求属性，供JSP页面使用
        request.setAttribute("settings", settings);
        request.setAttribute("activeTab", "settings");
        
        // 获取请求转发器并转发请求到JSP页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 顧客ステータス更新
     * 更新客户状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updateCustomerStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数
        String idStr = request.getParameter("id"); // 客户ID
        String status = request.getParameter("status"); // 新状态
        
        // 检查参数是否完整
        if (idStr == null || status == null) {
            response.sendRedirect("admin?action=customers&error=パラメータが不足しています");
            return;
        }
        
        // 使用try-catch处理可能的NumberFormatException异常
        try {
            // 将ID字符串转换为整数
            int id = Integer.parseInt(idStr);
            // 根据ID获取客户信息
            Customer customer = customerService.getCustomerById(id);
            
            // 检查客户是否存在
            if (customer == null) {
                response.sendRedirect("admin?action=customers&error=顧客が見つかりません");
                return;
            }
            
            // ステータス更新ロジック（仮実装）
            // 状态更新逻辑（临时实现）
            boolean success = true; // 实际的更新处理
            
            // 根据更新结果重定向到相应页面
            if (success) {
                response.sendRedirect("admin?action=customers&message=顧客ステータスを更新しました");
            } else {
                response.sendRedirect("admin?action=customers&error=ステータス更新に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            // 捕获NumberFormatException并重定向到错误页面
            response.sendRedirect("admin?action=customers&error=無効なIDです");
        }
    }
    
    /**
     * 資料請求ステータス更新
     * 更新资料请求状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updateRequestStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数
        String idStr = request.getParameter("id"); // 请求ID
        String status = request.getParameter("status"); // 新状态
        
        // 检查参数是否完整
        if (idStr == null || status == null) {
            response.sendRedirect("admin?action=requests&error=パラメータが不足しています");
            return;
        }
        
        // 使用try-catch处理可能的NumberFormatException异常
        try {
            // 将ID字符串转换为整数
            int id = Integer.parseInt(idStr);
            // 调用服务方法更新请求状态
            boolean success = documentRequestService.updateRequestStatus(id, status);
            
            // 根据更新结果重定向到相应页面
            if (success) {
                response.sendRedirect("admin?action=requests&message=リクエストステータスを更新しました");
            } else {
                response.sendRedirect("admin?action=requests&error=ステータス更新に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            // 捕获NumberFormatException并重定向到错误页面
            response.sendRedirect("admin?action=requests&error=無効なIDです");
        }
    }
    
    /**
     * レポート生成
     * 生成报告
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void generateReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数
        String reportType = request.getParameter("reportType"); // 报告类型
        String startDate = request.getParameter("startDate"); // 开始日期
        String endDate = request.getParameter("endDate"); // 结束日期
        
        // レポート生成ロジック（仮実装）
        // 报告生成逻辑（临时实现）
        if (reportType != null) {
            response.sendRedirect("admin?action=reports&message=レポートを生成しました");
        } else {
            response.sendRedirect("admin?action=reports&error=レポートタイプを指定してください");
        }
    }
    
    /**
     * システム設定更新
     * 更新系统设置
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updateSystemSettings(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 設定更新ロジック（仮実装）
        // 设置更新逻辑（临时实现）
        String settingName = request.getParameter("settingName"); // 设置名称
        String settingValue = request.getParameter("settingValue"); // 设置值
        
        // 检查参数是否完整
        if (settingName != null && settingValue != null) {
            response.sendRedirect("admin?action=settings&message=設定を更新しました");
        } else {
            response.sendRedirect("admin?action=settings&error=設定の更新に失敗しました");
        }
    }
    
    /**
     * 管理者権限チェック
     * 检查管理员权限
     * @param request HTTP请求对象
     * @return 如果是管理员返回true，否则返回false
     */
    private boolean isAdminUser(HttpServletRequest request) {
        // 获取会话对象
        HttpSession session = request.getSession();
        // 从会话中获取用户角色
        String userRole = (String) session.getAttribute("userRole");
        // 检查用户角色是否为管理员
        return "admin".equals(userRole);
    }
    
    /**
     * ユーザーリスト取得（仮実装）
     * 获取用户列表（临时实现）
     * @return 用户列表
     */
    private List<User> getUsers() {
        // 実際の実装ではデータベースからユーザーリストを取得
        // 实际实现中从数据库获取用户列表
        return List.of(); // 空のリストを返す // 返回空列表
    }
    
    /**
     * システム設定取得（仮実装）
     * 获取系统设置（临时实现）
     * @return 系统设置映射
     */
    private Map<String, String> getSystemSettings() {
        // 実際の実装ではデータベースから設定を取得
        // 实际实现中从数据库获取设置
        return Map.of(
            "system_name", "保険管理システム", // 系统名称
            "version", "1.0.0", // 版本号
            "maintenance_mode", "false" // 维护模式状态
        );
    }
}