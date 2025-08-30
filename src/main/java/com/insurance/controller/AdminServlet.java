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
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    
    private CustomerService customerService;
    private DocumentRequestService documentRequestService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        customerService = new CustomerService();
        documentRequestService = new DocumentRequestService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 管理者権限チェック（仮実装）
        if (!isAdminUser(request)) {
            response.sendRedirect("login?error=管理者権限が必要です");
            return;
        }
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "dashboard";
        }
        
        try {
            switch (action) {
                case "dashboard":
                    showAdminDashboard(request, response);
                    break;
                case "customers":
                    showCustomerManagement(request, response);
                    break;
                case "requests":
                    showRequestManagement(request, response);
                    break;
                case "reports":
                    showReports(request, response);
                    break;
                case "users":
                    showUserManagement(request, response);
                    break;
                case "settings":
                    showSystemSettings(request, response);
                    break;
                default:
                    showAdminDashboard(request, response);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 管理者権限チェック
        if (!isAdminUser(request)) {
            response.sendRedirect("login?error=管理者権限が必要です");
            return;
        }
        
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect("admin?action=dashboard");
            return;
        }
        
        try {
            switch (action) {
                case "updateCustomerStatus":
                    updateCustomerStatus(request, response);
                    break;
                case "updateRequestStatus":
                    updateRequestStatus(request, response);
                    break;
                case "generateReport":
                    generateReport(request, response);
                    break;
                case "updateSystemSettings":
                    updateSystemSettings(request, response);
                    break;
                default:
                    response.sendRedirect("admin?action=dashboard");
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    /**
     * 管理者ダッシュボード表示
     */
    private void showAdminDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 統計情報の取得
        int totalCustomers = customerService.getAllCustomers().size();
        List<DocumentRequest> allRequests = documentRequestService.getAllDocumentRequests();
        Map<String, Integer> requestStats = documentRequestService.getRequestStatistics();
        
        request.setAttribute("totalCustomers", totalCustomers);
        request.setAttribute("totalRequests", allRequests.size());
        request.setAttribute("requestStats", requestStats);
        request.setAttribute("activeTab", "dashboard");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 顧客管理画面表示
     */
    private void showCustomerManagement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Customer> customers = customerService.getAllCustomers();
        request.setAttribute("customers", customers);
        request.setAttribute("activeTab", "customers");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/customers.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 資料請求管理画面表示
     */
    private void showRequestManagement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
        request.setAttribute("requests", requests);
        request.setAttribute("activeTab", "requests");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/requests.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * レポート画面表示
     */
    private void showReports(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Map<String, Integer> stats = documentRequestService.getRequestStatistics();
        request.setAttribute("stats", stats);
        request.setAttribute("activeTab", "reports");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/reports.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * ユーザー管理画面表示
     */
    private void showUserManagement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // ユーザーリストの取得（仮実装）
        List<User> users = getUsers();
        request.setAttribute("users", users);
        request.setAttribute("activeTab", "users");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * システム設定画面表示
     */
    private void showSystemSettings(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // システム設定の取得（仮実装）
        Map<String, String> settings = getSystemSettings();
        request.setAttribute("settings", settings);
        request.setAttribute("activeTab", "settings");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 顧客ステータス更新
     */
    private void updateCustomerStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        String status = request.getParameter("status");
        
        if (idStr == null || status == null) {
            response.sendRedirect("admin?action=customers&error=パラメータが不足しています");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            Customer customer = customerService.getCustomerById(id);
            
            if (customer == null) {
                response.sendRedirect("admin?action=customers&error=顧客が見つかりません");
                return;
            }
            
            // ステータス更新ロジック（仮実装）
            boolean success = true; // 実際の更新処理
            
            if (success) {
                response.sendRedirect("admin?action=customers&message=顧客ステータスを更新しました");
            } else {
                response.sendRedirect("admin?action=customers&error=ステータス更新に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect("admin?action=customers&error=無効なIDです");
        }
    }
    
    /**
     * 資料請求ステータス更新
     */
    private void updateRequestStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        String status = request.getParameter("status");
        
        if (idStr == null || status == null) {
            response.sendRedirect("admin?action=requests&error=パラメータが不足しています");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            boolean success = documentRequestService.updateRequestStatus(id, status);
            
            if (success) {
                response.sendRedirect("admin?action=requests&message=リクエストステータスを更新しました");
            } else {
                response.sendRedirect("admin?action=requests&error=ステータス更新に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect("admin?action=requests&error=無効なIDです");
        }
    }
    
    /**
     * レポート生成
     */
    private void generateReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String reportType = request.getParameter("reportType");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        
        // レポート生成ロジック（仮実装）
        if (reportType != null) {
            response.sendRedirect("admin?action=reports&message=レポートを生成しました");
        } else {
            response.sendRedirect("admin?action=reports&error=レポートタイプを指定してください");
        }
    }
    
    /**
     * システム設定更新
     */
    private void updateSystemSettings(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 設定更新ロジック（仮実装）
        String settingName = request.getParameter("settingName");
        String settingValue = request.getParameter("settingValue");
        
        if (settingName != null && settingValue != null) {
            response.sendRedirect("admin?action=settings&message=設定を更新しました");
        } else {
            response.sendRedirect("admin?action=settings&error=設定の更新に失敗しました");
        }
    }
    
    /**
     * 管理者権限チェック
     */
    private boolean isAdminUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String userRole = (String) session.getAttribute("userRole");
        return "admin".equals(userRole);
    }
    
    /**
     * ユーザーリスト取得（仮実装）
     */
    private List<User> getUsers() {
        // 実際の実装ではデータベースからユーザーリストを取得
        return List.of(); // 空のリストを返す
    }
    
    /**
     * システム設定取得（仮実装）
     */
    private Map<String, String> getSystemSettings() {
        // 実際の実装ではデータベースから設定を取得
        return Map.of(
            "system_name", "保険管理システム",
            "version", "1.0.0",
            "maintenance_mode", "false"
        );
    }
}