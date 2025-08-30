package com.insurance.controller;

import com.insurance.api.ApiBaseServlet;
import com.insurance.integration.ExternalSystemIntegration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 集成控制器
 * 处理外部系统集成的API接口
 */
@WebServlet("/api/integration/*")
public class IntegrationControllerServlet extends ApiBaseServlet {
    
    private ExternalSystemIntegration integration;
    
    @Override
    public void init() {
        this.integration = new ExternalSystemIntegration();
    }
    
    @Override
    protected void handleRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String method = request.getMethod();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // 根路径处理
                if ("GET".equals(method)) {
                    getIntegrationStatus(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/bank-transfer")) {
                // 银行转账
                if ("POST".equals(method)) {
                    processBankTransfer(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/email")) {
                // 邮件发送
                if ("POST".equals(method)) {
                    sendEmail(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/sms")) {
                // SMS发送
                if ("POST".equals(method)) {
                    sendSms(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/payment")) {
                // 支付处理
                if ("POST".equals(method)) {
                    processPayment(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/credit-check")) {
                // 信用检查
                if ("POST".equals(method)) {
                    checkCredit(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/document-archive")) {
                // 文档归档
                if ("POST".equals(method)) {
                    archiveDocument(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/health")) {
                // 系统健康检查
                if ("GET".equals(method)) {
                    checkSystemHealth(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
        } catch (Exception e) {
            logApiError("集成API处理异常: " + pathInfo, e);
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取集成状态
     */
    private void getIntegrationStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 检查各外部系统的连接状态
            boolean bankTransferOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.BANK_TRANSFER);
            boolean emailServiceOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.EMAIL_SERVICE);
            boolean smsServiceOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.SMS_SERVICE);
            boolean paymentGatewayOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.PAYMENT_GATEWAY);
            boolean creditCheckOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.CREDIT_CHECK);
            boolean documentArchiveOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.DOCUMENT_ARCHIVE);
            
            Map<String, Boolean> systemStatus = new HashMap<>();
            systemStatus.put("bankTransfer", bankTransferOk);
            systemStatus.put("emailService", emailServiceOk);
            systemStatus.put("smsService", smsServiceOk);
            systemStatus.put("paymentGateway", paymentGatewayOk);
            systemStatus.put("creditCheck", creditCheckOk);
            systemStatus.put("documentArchive", documentArchiveOk);
            
            status.put("timestamp", System.currentTimeMillis());
            status.put("systems", systemStatus);
            status.put("overallHealthy", bankTransferOk && emailServiceOk && smsServiceOk && 
                                           paymentGatewayOk && creditCheckOk && documentArchiveOk);
            
            sendJsonResponse(response, ApiResponse.success(status));
            
        } catch (Exception e) {
            logApiError("获取集成状态失败", e);
            sendJsonResponse(response, ApiResponse.error("获取集成状态失败"));
        }
    }
    
    /**
     * 处理银行转账
     */
    private void processBankTransfer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "accountNumber", "bankCode", "amount")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String accountNumber = request.getParameter("accountNumber");
            String bankCode = request.getParameter("bankCode");
            double amount = getDoubleParameter(request, "amount", 0.0);
            String description = getParameter(request, "description", "保险系统转账");
            
            // 验证参数
            if (amount <= 0) {
                sendJsonResponse(response, ApiResponse.error("转账金额必须大于0"));
                return;
            }
            
            // 创建转账请求
            ExternalSystemIntegration.BankTransferRequest transferRequest = 
                new ExternalSystemIntegration.BankTransferRequest(accountNumber, bankCode, amount, description);
            
            // 调用外部系统
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.processBankTransfer(transferRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess());
            result.put("statusCode", integrationResponse.getStatusCode());
            result.put("accountNumber", accountNumber);
            result.put("amount", amount);
            result.put("description", description);
            result.put("timestamp", System.currentTimeMillis());
            
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            logApiError("银行转账处理失败", e);
            sendJsonResponse(response, ApiResponse.error("银行转账处理失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送邮件
     */
    private void sendEmail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "to", "subject", "body")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String to = request.getParameter("to");
            String subject = request.getParameter("subject");
            String body = request.getParameter("body");
            String from = getParameter(request, "from", "noreply@insurance-system.com");
            
            // 验证邮箱格式
            if (!isValidEmail(to) || !isValidEmail(from)) {
                sendJsonResponse(response, ApiResponse.error("邮箱格式不正确"));
                return;
            }
            
            // 创建邮件请求
            ExternalSystemIntegration.EmailRequest emailRequest = 
                new ExternalSystemIntegration.EmailRequest(to, subject, body, from);
            
            // 调用外部系统
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.sendEmail(emailRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess());
            result.put("statusCode", integrationResponse.getStatusCode());
            result.put("to", to);
            result.put("subject", subject);
            result.put("timestamp", System.currentTimeMillis());
            
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            logApiError("邮件发送失败", e);
            sendJsonResponse(response, ApiResponse.error("邮件发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送SMS
     */
    private void sendSms(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "to", "message")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String to = request.getParameter("to");
            String message = request.getParameter("message");
            String sender = getParameter(request, "sender", "Insurance");
            
            // 验证手机号格式
            if (!isValidPhoneNumber(to)) {
                sendJsonResponse(response, ApiResponse.error("手机号格式不正确"));
                return;
            }
            
            // 验证消息长度
            if (message.length() > 160) {
                sendJsonResponse(response, ApiResponse.error("消息长度不能超过160字符"));
                return;
            }
            
            // 创建SMS请求
            ExternalSystemIntegration.SmsRequest smsRequest = 
                new ExternalSystemIntegration.SmsRequest(to, message, sender);
            
            // 调用外部系统
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.sendSms(smsRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess());
            result.put("statusCode", integrationResponse.getStatusCode());
            result.put("to", to);
            result.put("message", message);
            result.put("sender", sender);
            result.put("timestamp", System.currentTimeMillis());
            
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            logApiError("SMS发送失败", e);
            sendJsonResponse(response, ApiResponse.error("SMS发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 处理支付
     */
    private void processPayment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "cardNumber", "expiryDate", "cvv", "amount")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String cardNumber = request.getParameter("cardNumber");
            String expiryDate = request.getParameter("expiryDate");
            String cvv = request.getParameter("cvv");
            double amount = getDoubleParameter(request, "amount", 0.0);
            String description = getParameter(request, "description", "保险系统支付");
            
            // 验证参数
            if (amount <= 0) {
                sendJsonResponse(response, ApiResponse.error("支付金额必须大于0"));
                return;
            }
            
            // 验证信用卡号格式
            if (!isValidCreditCard(cardNumber)) {
                sendJsonResponse(response, ApiResponse.error("信用卡号格式不正确"));
                return;
            }
            
            // 验证CVV格式
            if (!isValidCvv(cvv)) {
                sendJsonResponse(response, ApiResponse.error("CVV格式不正确"));
                return;
            }
            
            // 创建支付请求
            ExternalSystemIntegration.PaymentRequest paymentRequest = 
                new ExternalSystemIntegration.PaymentRequest(cardNumber, expiryDate, cvv, amount, description);
            
            // 调用外部系统
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.processPayment(paymentRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess());
            result.put("statusCode", integrationResponse.getStatusCode());
            result.put("amount", amount);
            result.put("description", description);
            result.put("timestamp", System.currentTimeMillis());
            // 注意：实际系统中不应该返回完整的信用卡信息
            result.put("cardNumberMasked", maskCreditCard(cardNumber));
            
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            logApiError("支付处理失败", e);
            sendJsonResponse(response, ApiResponse.error("支付处理失败: " + e.getMessage()));
        }
    }
    
    /**
     * 信用检查
     */
    private void checkCredit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "customerId", "name", "birthDate", "annualIncome")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            int customerId = getIntParameter(request, "customerId", 0);
            String name = request.getParameter("name");
            String birthDate = request.getParameter("birthDate");
            double annualIncome = getDoubleParameter(request, "annualIncome", 0.0);
            
            // 验证参数
            if (customerId <= 0) {
                sendJsonResponse(response, ApiResponse.error("客户ID必须大于0"));
                return;
            }
            
            if (annualIncome < 0) {
                sendJsonResponse(response, ApiResponse.error("年收入不能为负数"));
                return;
            }
            
            // 创建信用检查请求
            ExternalSystemIntegration.CreditCheckRequest creditRequest = 
                new ExternalSystemIntegration.CreditCheckRequest(customerId, name, birthDate, annualIncome);
            
            // 调用外部系统
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.checkCredit(creditRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess());
            result.put("statusCode", integrationResponse.getStatusCode());
            result.put("customerId", customerId);
            result.put("name", name);
            result.put("annualIncome", annualIncome);
            result.put("timestamp", System.currentTimeMillis());
            
            if (integrationResponse.isSuccess()) {
                // 解析信用检查结果
                // 这里简化处理，实际应该解析外部系统的响应
                result.put("creditScore", 750); // 示例值
                result.put("creditLimit", 1000000.0); // 示例值
                result.put("riskLevel", "low"); // 示例值
            }
            
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            logApiError("信用检查失败", e);
            sendJsonResponse(response, ApiResponse.error("信用检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 文档归档
     */
    private void archiveDocument(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "documentId", "documentType", "customerId")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String documentId = request.getParameter("documentId");
            String documentType = request.getParameter("documentType");
            int customerId = getIntParameter(request, "customerId", 0);
            
            // 验证参数
            if (customerId <= 0) {
                sendJsonResponse(response, ApiResponse.error("客户ID必须大于0"));
                return;
            }
            
            // 创建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("archivedBy", getCurrentUser(request).getUsername());
            metadata.put("archivedAt", System.currentTimeMillis());
            metadata.put("documentType", documentType);
            metadata.put("size", request.getContentLength());
            
            // 创建文档归档请求
            ExternalSystemIntegration.DocumentArchiveRequest archiveRequest = 
                new ExternalSystemIntegration.DocumentArchiveRequest(documentId, documentType, customerId, metadata);
            
            // 调用外部系统
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.archiveDocument(archiveRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess());
            result.put("statusCode", integrationResponse.getStatusCode());
            result.put("documentId", documentId);
            result.put("documentType", documentType);
            result.put("customerId", customerId);
            result.put("timestamp", System.currentTimeMillis());
            
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            logApiError("文档归档失败", e);
            sendJsonResponse(response, ApiResponse.error("文档归档失败: " + e.getMessage()));
        }
    }
    
    /**
     * 系统健康检查
     */
    private void checkSystemHealth(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            Map<String, Object> healthStatus = new HashMap<>();
            
            // 检查各个外部系统的健康状态
            healthStatus.put("bankTransfer", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.BANK_TRANSFER));
            healthStatus.put("emailService", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.EMAIL_SERVICE));
            healthStatus.put("smsService", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.SMS_SERVICE));
            healthStatus.put("paymentGateway", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.PAYMENT_GATEWAY));
            healthStatus.put("creditCheck", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.CREDIT_CHECK));
            healthStatus.put("documentArchive", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.DOCUMENT_ARCHIVE));
            
            // 计算整体健康状态
            boolean allHealthy = healthStatus.values().stream()
                .allMatch(status -> (boolean) status);
            
            healthStatus.put("overallHealthy", allHealthy);
            healthStatus.put("timestamp", System.currentTimeMillis());
            
            sendJsonResponse(response, ApiResponse.success(healthStatus));
            
        } catch (Exception e) {
            logApiError("系统健康检查失败", e);
            sendJsonResponse(response, ApiResponse.error("系统健康检查失败"));
        }
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * 验证手机号格式
     */
    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^[\\d\\-\\+\\s()]+$");
    }
    
    /**
     * 验证信用卡号格式
     */
    private boolean isValidCreditCard(String cardNumber) {
        return cardNumber != null && cardNumber.replaceAll("[\\s-]", "").length() >= 13;
    }
    
    /**
     * 验证CVV格式
     */
    private boolean isValidCvv(String cvv) {
        return cvv != null && cvv.matches("^\\d{3,4}$");
    }
    
    /**
     * 屏蔽信用卡号
     */
    private String maskCreditCard(String cardNumber) {
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        if (cleaned.length() <= 4) {
            return "****-****-****-" + cleaned;
        }
        return "****-****-****-" + cleaned.substring(cleaned.length() - 4);
    }
    
    /**
     * 记录API错误
     */
    private void logApiError(String message, Exception e) {
        System.err.println("API错误: " + message);
        e.printStackTrace();
    }
    
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    @Override
    protected boolean hasPermission(HttpServletRequest request) {
        com.insurance.model.User user = getCurrentUser(request);
        return user != null && user.isAdmin();
    }
}