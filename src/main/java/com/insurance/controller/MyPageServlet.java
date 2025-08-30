package com.insurance.controller;

import com.insurance.model.Contract;
import com.insurance.model.Customer;
import com.insurance.model.DocumentRequest;
import com.insurance.service.CustomerService;
import com.insurance.service.DocumentRequestService;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * マイページ機能Servlet
 */
@WebServlet("/mypage")
public class MyPageServlet extends HttpServlet {
    
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
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "dashboard";
        }
        
        try {
            // セッションから顧客IDを取得（仮実装）
            HttpSession session = request.getSession();
            Integer customerId = (Integer) session.getAttribute("customerId");
            
            // デモ用：顧客IDがなければ最初の顧客を使用
            if (customerId == null) {
                List<Customer> customers = customerService.getAllCustomers();
                if (!customers.isEmpty()) {
                    customerId = customers.get(0).getId();
                    session.setAttribute("customerId", customerId);
                }
            }
            
            if (customerId == null) {
                response.sendRedirect("login"); // ログインページへリダイレクト
                return;
            }
            
            request.setAttribute("customerId", customerId);
            
            switch (action) {
                case "dashboard":
                    showDashboard(request, response, customerId);
                    break;
                case "profile":
                    showProfile(request, response, customerId);
                    break;
                case "contracts":
                    showContracts(request, response, customerId);
                    break;
                case "documents":
                    showDocumentRequests(request, response, customerId);
                    break;
                case "premiums":
                    showPremiumPayments(request, response, customerId);
                    break;
                case "settings":
                    showSettings(request, response, customerId);
                    break;
                default:
                    showDashboard(request, response, customerId);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect("mypage?action=dashboard");
            return;
        }
        
        try {
            HttpSession session = request.getSession();
            Integer customerId = (Integer) session.getAttribute("customerId");
            
            if (customerId == null) {
                response.sendRedirect("login");
                return;
            }
            
            switch (action) {
                case "updateProfile":
                    updateProfile(request, response, customerId);
                    break;
                case "changePassword":
                    changePassword(request, response, customerId);
                    break;
                case "updateNotification":
                    updateNotificationSettings(request, response, customerId);
                    break;
                default:
                    response.sendRedirect("mypage?action=dashboard");
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    /**
     * ダッシュボード表示
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 資料請求の取得
        List<DocumentRequest> documentRequests = documentRequestService.getRequestsByCustomerId(customerId);
        
        // 契約情報の取得（仮実装）
        List<Contract> contracts = getContractsByCustomerId(customerId);
        
        request.setAttribute("customer", customer);
        request.setAttribute("documentRequests", documentRequests);
        request.setAttribute("contracts", contracts);
        request.setAttribute("activeTab", "dashboard");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/dashboard.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * プロフィール表示
     */
    private void showProfile(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        request.setAttribute("customer", customer);
        request.setAttribute("activeTab", "profile");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/profile.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 契約情報表示
     */
    private void showContracts(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 契約情報の取得（仮実装）
        List<Contract> contracts = getContractsByCustomerId(customerId);
        
        request.setAttribute("customer", customer);
        request.setAttribute("contracts", contracts);
        request.setAttribute("activeTab", "contracts");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/contracts.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 資料請求表示
     */
    private void showDocumentRequests(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        List<DocumentRequest> documentRequests = documentRequestService.getRequestsByCustomerId(customerId);
        
        request.setAttribute("customer", customer);
        request.setAttribute("documentRequests", documentRequests);
        request.setAttribute("activeTab", "documents");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/documents.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 保険料支払い情報表示
     */
    private void showPremiumPayments(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 支払い情報の取得（仮実装）
        List<?> payments = getPremiumPaymentsByCustomerId(customerId);
        
        request.setAttribute("customer", customer);
        request.setAttribute("payments", payments);
        request.setAttribute("activeTab", "premiums");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/premiums.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 設定画面表示
     */
    private void showSettings(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        request.setAttribute("customer", customer);
        request.setAttribute("activeTab", "settings");
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/settings.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * プロフィール更新
     */
    private void updateProfile(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        Customer customer = customerService.getCustomerById(customerId);
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // パラメータから顧客情報を更新
        customer.setPhoneNumber(request.getParameter("phoneNumber"));
        customer.setEmail(request.getParameter("email"));
        customer.setPostalCode(request.getParameter("postalCode"));
        customer.setPrefecture(request.getParameter("prefecture"));
        customer.setCity(request.getParameter("city"));
        customer.setAddressLine1(request.getParameter("addressLine1"));
        customer.setAddressLine2(request.getParameter("addressLine2"));
        
        boolean success = customerService.updateCustomer(customer);
        
        if (success) {
            response.sendRedirect("mypage?action=profile&message=プロフィールを更新しました");
        } else {
            response.sendRedirect("mypage?action=profile&error=プロフィールの更新に失敗しました");
        }
    }
    
    /**
     * パスワード変更
     */
    private void changePassword(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // パスワード変更ロジック（仮実装）
        if (newPassword.equals(confirmPassword)) {
            // パスワード変更処理
            response.sendRedirect("mypage?action=settings&message=パスワードを変更しました");
        } else {
            response.sendRedirect("mypage?action=settings&error=パスワードが一致しません");
        }
    }
    
    /**
     * 通知設定更新
     */
    private void updateNotificationSettings(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 通知設定の更新ロジック（仮実装）
        String emailNotifications = request.getParameter("emailNotifications");
        String smsNotifications = request.getParameter("smsNotifications");
        
        response.sendRedirect("mypage?action=settings&message=通知設定を更新しました");
    }
    
    /**
     * 顧客IDに基づく契約情報取得（仮実装）
     */
    private List<Contract> getContractsByCustomerId(int customerId) {
        // 実際の実装ではデータベースから契約情報を取得
        return List.of(); // 空のリストを返す
    }
    
    /**
     * 顧客IDに基づく保険料支払い情報取得（仮実装）
     */
    private List<?> getPremiumPaymentsByCustomerId(int customerId) {
        // 実際の実装ではデータベースから支払い情報を取得
        return List.of(); // 空のリストを返す
    }
}