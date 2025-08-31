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
 * 用于处理用户个人主页相关的HTTP请求，包括仪表板、个人资料、合同信息、文档请求、保险费支付和设置等功能
 */
@WebServlet("/mypage")
public class MyPageServlet extends HttpServlet {
    
    // 客户服务对象，用于处理客户相关业务逻辑
    private CustomerService customerService;
    // 文档请求服务对象，用于处理文档请求相关业务逻辑
    private DocumentRequestService documentRequestService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     * @throws ServletException 当初始化过程中发生错误时抛出
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
     * 处理HTTP GET请求
     * 根据请求参数中的action值执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的action值，用于确定要执行的操作
        String action = request.getParameter("action");
        
        // 如果action参数为空，则默认设置为"dashboard"
        if (action == null) {
            action = "dashboard";
        }
        
        try {
            // セッションから顧客IDを取得（仮実装）
            // 从会话中获取客户ID（临时实现）
            HttpSession session = request.getSession();
            Integer customerId = (Integer) session.getAttribute("customerId");
            
            // デモ用：顧客IDがなければ最初の顧客を使用
            // 演示用：如果没有客户ID，则使用第一个客户
            if (customerId == null) {
                List<Customer> customers = customerService.getAllCustomers();
                if (!customers.isEmpty()) {
                    customerId = customers.get(0).getId();
                    session.setAttribute("customerId", customerId);
                }
            }
            
            // 如果客户ID仍然为空，则重定向到登录页面
            if (customerId == null) {
                response.sendRedirect("login"); // ログインページへリダイレクト // 重定向到登录页面
                return;
            }
            
            // 将客户ID设置为请求属性，供JSP页面使用
            request.setAttribute("customerId", customerId);
            
            // 根据action值执行相应的操作
            switch (action) {
                case "dashboard":
                    // 显示仪表板
                    showDashboard(request, response, customerId);
                    break;
                case "profile":
                    // 显示个人资料
                    showProfile(request, response, customerId);
                    break;
                case "contracts":
                    // 显示合同信息
                    showContracts(request, response, customerId);
                    break;
                case "documents":
                    // 显示文档请求
                    showDocumentRequests(request, response, customerId);
                    break;
                case "premiums":
                    // 显示保险费支付信息
                    showPremiumPayments(request, response, customerId);
                    break;
                case "settings":
                    // 显示设置页面
                    showSettings(request, response, customerId);
                    break;
                default:
                    // 默认显示仪表板
                    showDashboard(request, response, customerId);
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 处理HTTP POST请求
     * 根据请求参数中的action值执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的action值，用于确定要执行的操作
        String action = request.getParameter("action");
        
        // 如果action参数为空，则重定向到仪表板页面
        if (action == null) {
            response.sendRedirect("mypage?action=dashboard");
            return;
        }
        
        try {
            // 从会话中获取客户ID
            HttpSession session = request.getSession();
            Integer customerId = (Integer) session.getAttribute("customerId");
            
            // 如果客户ID为空，则重定向到登录页面
            if (customerId == null) {
                response.sendRedirect("login");
                return;
            }
            
            // 根据action值执行相应的操作
            switch (action) {
                case "updateProfile":
                    // 更新个人资料
                    updateProfile(request, response, customerId);
                    break;
                case "changePassword":
                    // 更改密码
                    changePassword(request, response, customerId);
                    break;
                case "updateNotification":
                    // 更新通知设置
                    updateNotificationSettings(request, response, customerId);
                    break;
                default:
                    // 默认重定向到仪表板页面
                    response.sendRedirect("mypage?action=dashboard");
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * ダッシュボード表示
     * 显示仪表板页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showDashboard(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 根据客户ID获取客户信息
        Customer customer = customerService.getCustomerById(customerId);
        // 如果客户不存在，则重定向到登录页面
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 資料請求の取得
        // 获取资料请求信息
        List<DocumentRequest> documentRequests = documentRequestService.getRequestsByCustomerId(customerId);
        
        // 契約情報の取得（仮実装）
        // 获取合同信息（临时实现）
        List<Contract> contracts = getContractsByCustomerId(customerId);
        
        // 将客户信息、资料请求和合同信息设置为请求属性，供JSP页面使用
        request.setAttribute("customer", customer);
        request.setAttribute("documentRequests", documentRequests);
        request.setAttribute("contracts", contracts);
        request.setAttribute("activeTab", "dashboard");
        
        // 获取请求转发器，指向仪表板页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/dashboard.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * プロフィール表示
     * 显示个人资料页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showProfile(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 根据客户ID获取客户信息
        Customer customer = customerService.getCustomerById(customerId);
        // 如果客户不存在，则重定向到登录页面
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 将客户信息设置为请求属性，供JSP页面使用
        request.setAttribute("customer", customer);
        request.setAttribute("activeTab", "profile");
        
        // 获取请求转发器，指向个人资料页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/profile.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 契約情報表示
     * 显示合同信息页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showContracts(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 根据客户ID获取客户信息
        Customer customer = customerService.getCustomerById(customerId);
        // 如果客户不存在，则重定向到登录页面
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 契約情報の取得（仮実装）
        // 获取合同信息（临时实现）
        List<Contract> contracts = getContractsByCustomerId(customerId);
        
        // 将客户信息和合同信息设置为请求属性，供JSP页面使用
        request.setAttribute("customer", customer);
        request.setAttribute("contracts", contracts);
        request.setAttribute("activeTab", "contracts");
        
        // 获取请求转发器，指向合同信息页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/contracts.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 資料請求表示
     * 显示文档请求页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showDocumentRequests(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 根据客户ID获取客户信息
        Customer customer = customerService.getCustomerById(customerId);
        // 如果客户不存在，则重定向到登录页面
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 获取客户的资料请求信息
        List<DocumentRequest> documentRequests = documentRequestService.getRequestsByCustomerId(customerId);
        
        // 将客户信息和资料请求信息设置为请求属性，供JSP页面使用
        request.setAttribute("customer", customer);
        request.setAttribute("documentRequests", documentRequests);
        request.setAttribute("activeTab", "documents");
        
        // 获取请求转发器，指向文档请求页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/documents.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 保険料支払い情報表示
     * 显示保险费支付信息页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showPremiumPayments(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 根据客户ID获取客户信息
        Customer customer = customerService.getCustomerById(customerId);
        // 如果客户不存在，则重定向到登录页面
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 支払い情報の取得（仮実装）
        // 获取支付信息（临时实现）
        List<?> payments = getPremiumPaymentsByCustomerId(customerId);
        
        // 将客户信息和支付信息设置为请求属性，供JSP页面使用
        request.setAttribute("customer", customer);
        request.setAttribute("payments", payments);
        request.setAttribute("activeTab", "premiums");
        
        // 获取请求转发器，指向保险费支付信息页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/premiums.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 設定画面表示
     * 显示设置页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showSettings(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 根据客户ID获取客户信息
        Customer customer = customerService.getCustomerById(customerId);
        // 如果客户不存在，则重定向到登录页面
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // 将客户信息设置为请求属性，供JSP页面使用
        request.setAttribute("customer", customer);
        request.setAttribute("activeTab", "settings");
        
        // 获取请求转发器，指向设置页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/mypage/settings.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * プロフィール更新
     * 更新个人资料
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void updateProfile(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 根据客户ID获取客户信息
        Customer customer = customerService.getCustomerById(customerId);
        // 如果客户不存在，则重定向到登录页面
        if (customer == null) {
            response.sendRedirect("login");
            return;
        }
        
        // パラメータから顧客情報を更新
        // 从参数中更新客户信息
        customer.setPhoneNumber(request.getParameter("phoneNumber")); // 设置电话号码
        customer.setEmail(request.getParameter("email")); // 设置邮箱
        customer.setPostalCode(request.getParameter("postalCode")); // 设置邮政编码
        customer.setPrefecture(request.getParameter("prefecture")); // 设置都道府县
        customer.setCity(request.getParameter("city")); // 设置市区町村
        customer.setAddressLine1(request.getParameter("addressLine1")); // 设置地址1
        customer.setAddressLine2(request.getParameter("addressLine2")); // 设置地址2
        
        // 调用服务层方法更新客户信息
        boolean success = customerService.updateCustomer(customer);
        
        // 根据更新结果进行相应处理
        if (success) {
            // 更新成功，重定向到个人资料页面并显示成功消息
            response.sendRedirect("mypage?action=profile&message=プロフィールを更新しました");
        } else {
            // 更新失败，重定向到个人资料页面并显示错误消息
            response.sendRedirect("mypage?action=profile&error=プロフィールの更新に失敗しました");
        }
    }
    
    /**
     * パスワード変更
     * 更改密码
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void changePassword(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 获取请求参数
        String currentPassword = request.getParameter("currentPassword"); // 当前密码
        String newPassword = request.getParameter("newPassword"); // 新密码
        String confirmPassword = request.getParameter("confirmPassword"); // 确认密码
        
        // パスワード変更ロジック（仮実装）
        // 密码更改逻辑（临时实现）
        if (newPassword.equals(confirmPassword)) {
            // パスワード変更処理
            // 密码更改处理
            response.sendRedirect("mypage?action=settings&message=パスワードを変更しました");
        } else {
            response.sendRedirect("mypage?action=settings&error=パスワードが一致しません");
        }
    }
    
    /**
     * 通知設定更新
     * 更新通知设置
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void updateNotificationSettings(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        // 通知設定の更新ロジック（仮実装）
        // 通知设置更新逻辑（临时实现）
        String emailNotifications = request.getParameter("emailNotifications"); // 邮件通知设置
        String smsNotifications = request.getParameter("smsNotifications"); // 短信通知设置
        
        // 重定向到设置页面并显示成功消息
        response.sendRedirect("mypage?action=settings&message=通知設定を更新しました");
    }
    
    /**
     * 顧客IDに基づく契約情報取得（仮実装）
     * 根据客户ID获取合同信息（临时实现）
     * @param customerId 客户ID
     * @return 合同信息列表
     */
    private List<Contract> getContractsByCustomerId(int customerId) {
        // 実際の実装ではデータベースから契約情報を取得
        // 实际实现中从数据库获取合同信息
        return List.of(); // 空のリストを返す // 返回空列表
    }
    
    /**
     * 顧客IDに基づく保険料支払い情報取得（仮実装）
     * 根据客户ID获取保险费支付信息（临时实现）
     * @param customerId 客户ID
     * @return 支付信息列表
     */
    private List<?> getPremiumPaymentsByCustomerId(int customerId) {
        // 実際の実装ではデータベースから支払い情報を取得
        // 实际实现中从数据库获取支付信息
        return List.of(); // 空のリストを返す // 返回空列表
    }
}