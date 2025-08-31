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
 * 用于与银行转账、邮件服务、短信服务、支付网关、信用检查和文档归档等外部系统进行集成
 */
@WebServlet("/api/integration/*")
public class IntegrationControllerServlet extends ApiBaseServlet {
    
    // 外部系统集成对象，用于处理与各种外部系统的交互
    private ExternalSystemIntegration integration;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     */
    @Override
    public void init() {
        // 创建外部系统集成实例
        this.integration = new ExternalSystemIntegration();
    }
    
    /**
     * 处理HTTP请求
     * 根据请求路径和方法执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void handleRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求路径信息
        String pathInfo = request.getPathInfo();
        // 获取请求方法
        String method = request.getMethod();
        
        try {
            // 根据路径信息和请求方法执行相应的操作
            if (pathInfo == null || pathInfo.equals("/")) {
                // 根路径处理
                if ("GET".equals(method)) {
                    // 获取集成状态
                    getIntegrationStatus(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/bank-transfer")) {
                // 银行转账
                if ("POST".equals(method)) {
                    // 处理银行转账
                    processBankTransfer(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/email")) {
                // 邮件发送
                if ("POST".equals(method)) {
                    // 发送邮件
                    sendEmail(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/sms")) {
                // SMS发送
                if ("POST".equals(method)) {
                    // 发送SMS
                    sendSms(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/payment")) {
                // 支付处理
                if ("POST".equals(method)) {
                    // 处理支付
                    processPayment(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/credit-check")) {
                // 信用检查
                if ("POST".equals(method)) {
                    // 检查信用
                    checkCredit(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/document-archive")) {
                // 文档归档
                if ("POST".equals(method)) {
                    // 归档文档
                    archiveDocument(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/health")) {
                // 系统健康检查
                if ("GET".equals(method)) {
                    // 检查系统健康
                    checkSystemHealth(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else {
                // 路径未找到
                sendJsonResponse(response, ApiResponse.notFound());
            }
        } catch (Exception e) {
            // 记录API处理异常并返回内部错误响应
            logApiError("集成API处理异常: " + pathInfo, e);
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取集成状态
     * 检查各个外部系统的连接状态并返回整体健康状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void getIntegrationStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 创建状态映射，用于存储各个系统的状态信息
            Map<String, Object> status = new HashMap<>();
            
            // 检查各外部系统的连接状态
            // 检查银行转账系统健康状态
            boolean bankTransferOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.BANK_TRANSFER);
            // 检查邮件服务健康状态
            boolean emailServiceOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.EMAIL_SERVICE);
            // 检查短信服务健康状态
            boolean smsServiceOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.SMS_SERVICE);
            // 检查支付网关健康状态
            boolean paymentGatewayOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.PAYMENT_GATEWAY);
            // 检查信用检查系统健康状态
            boolean creditCheckOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.CREDIT_CHECK);
            // 检查文档归档系统健康状态
            boolean documentArchiveOk = integration.checkSystemHealth(
                ExternalSystemIntegration.SystemType.DOCUMENT_ARCHIVE);
            
            // 创建系统状态映射
            Map<String, Boolean> systemStatus = new HashMap<>();
            systemStatus.put("bankTransfer", bankTransferOk);
            systemStatus.put("emailService", emailServiceOk);
            systemStatus.put("smsService", smsServiceOk);
            systemStatus.put("paymentGateway", paymentGatewayOk);
            systemStatus.put("creditCheck", creditCheckOk);
            systemStatus.put("documentArchive", documentArchiveOk);
            
            // 设置状态信息
            status.put("timestamp", System.currentTimeMillis());
            status.put("systems", systemStatus);
            status.put("overallHealthy", bankTransferOk && emailServiceOk && smsServiceOk && 
                                           paymentGatewayOk && creditCheckOk && documentArchiveOk);
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(status));
            
        } catch (Exception e) {
            // 记录获取集成状态失败并返回错误响应
            logApiError("获取集成状态失败", e);
            sendJsonResponse(response, ApiResponse.error("获取集成状态失败"));
        }
    }
    
    /**
     * 处理银行转账
     * 根据请求参数执行银行转账操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void processBankTransfer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "accountNumber", "bankCode", "amount")) {
                // 缺少必需参数，返回错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String accountNumber = request.getParameter("accountNumber"); // 银行账户号码
            String bankCode = request.getParameter("bankCode"); // 银行代码
            double amount = getDoubleParameter(request, "amount", 0.0); // 转账金额
            String description = getParameter(request, "description", "保险系统转账"); // 转账描述
            
            // 验证参数
            if (amount <= 0) {
                // 转账金额必须大于0，返回错误响应
                sendJsonResponse(response, ApiResponse.error("转账金额必须大于0"));
                return;
            }
            
            // 创建转账请求
            ExternalSystemIntegration.BankTransferRequest transferRequest = 
                new ExternalSystemIntegration.BankTransferRequest(accountNumber, bankCode, amount, description);
            
            // 调用外部系统执行银行转账
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.processBankTransfer(transferRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess()); // 是否成功
            result.put("statusCode", integrationResponse.getStatusCode()); // 状态码
            result.put("accountNumber", accountNumber); // 银行账户号码
            result.put("amount", amount); // 转账金额
            result.put("description", description); // 转账描述
            result.put("timestamp", System.currentTimeMillis()); // 时间戳
            
            // 根据外部系统响应结果发送相应响应
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            // 记录银行转账处理失败并返回错误响应
            logApiError("银行转账处理失败", e);
            sendJsonResponse(response, ApiResponse.error("银行转账处理失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送邮件
     * 根据请求参数发送邮件
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void sendEmail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "to", "subject", "body")) {
                // 缺少必需参数，返回错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String to = request.getParameter("to"); // 收件人邮箱
            String subject = request.getParameter("subject"); // 邮件主题
            String body = request.getParameter("body"); // 邮件正文
            String from = getParameter(request, "from", "noreply@insurance-system.com"); // 发件人邮箱
            
            // 验证邮箱格式
            if (!isValidEmail(to) || !isValidEmail(from)) {
                // 邮箱格式不正确，返回错误响应
                sendJsonResponse(response, ApiResponse.error("邮箱格式不正确"));
                return;
            }
            
            // 创建邮件请求
            ExternalSystemIntegration.EmailRequest emailRequest = 
                new ExternalSystemIntegration.EmailRequest(to, subject, body, from);
            
            // 调用外部系统发送邮件
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.sendEmail(emailRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess()); // 是否成功
            result.put("statusCode", integrationResponse.getStatusCode()); // 状态码
            result.put("to", to); // 收件人邮箱
            result.put("subject", subject); // 邮件主题
            result.put("timestamp", System.currentTimeMillis()); // 时间戳
            
            // 根据外部系统响应结果发送相应响应
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            // 记录邮件发送失败并返回错误响应
            logApiError("邮件发送失败", e);
            sendJsonResponse(response, ApiResponse.error("邮件发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 发送SMS
     * 根据请求参数发送短信
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void sendSms(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "to", "message")) {
                // 缺少必需参数，返回错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String to = request.getParameter("to"); // 接收短信的手机号
            String message = request.getParameter("message"); // 短信内容
            String sender = getParameter(request, "sender", "Insurance"); // 发送者名称
            
            // 验证手机号格式
            if (!isValidPhoneNumber(to)) {
                // 手机号格式不正确，返回错误响应
                sendJsonResponse(response, ApiResponse.error("手机号格式不正确"));
                return;
            }
            
            // 验证消息长度
            if (message.length() > 160) {
                // 消息长度不能超过160字符，返回错误响应
                sendJsonResponse(response, ApiResponse.error("消息长度不能超过160字符"));
                return;
            }
            
            // 创建SMS请求
            ExternalSystemIntegration.SmsRequest smsRequest = 
                new ExternalSystemIntegration.SmsRequest(to, message, sender);
            
            // 调用外部系统发送短信
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.sendSms(smsRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess()); // 是否成功
            result.put("statusCode", integrationResponse.getStatusCode()); // 状态码
            result.put("to", to); // 接收短信的手机号
            result.put("message", message); // 短信内容
            result.put("sender", sender); // 发送者名称
            result.put("timestamp", System.currentTimeMillis()); // 时间戳
            
            // 根据外部系统响应结果发送相应响应
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            // 记录SMS发送失败并返回错误响应
            logApiError("SMS发送失败", e);
            sendJsonResponse(response, ApiResponse.error("SMS发送失败: " + e.getMessage()));
        }
    }
    
    /**
     * 处理支付
     * 根据请求参数处理支付操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void processPayment(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "cardNumber", "expiryDate", "cvv", "amount")) {
                // 缺少必需参数，返回错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String cardNumber = request.getParameter("cardNumber"); // 信用卡号
            String expiryDate = request.getParameter("expiryDate"); // 有效期
            String cvv = request.getParameter("cvv"); // CVV码
            double amount = getDoubleParameter(request, "amount", 0.0); // 支付金额
            String description = getParameter(request, "description", "保险系统支付"); // 支付描述
            
            // 验证参数
            if (amount <= 0) {
                // 支付金额必须大于0，返回错误响应
                sendJsonResponse(response, ApiResponse.error("支付金额必须大于0"));
                return;
            }
            
            // 验证信用卡号格式
            if (!isValidCreditCard(cardNumber)) {
                // 信用卡号格式不正确，返回错误响应
                sendJsonResponse(response, ApiResponse.error("信用卡号格式不正确"));
                return;
            }
            
            // 验证CVV格式
            if (!isValidCvv(cvv)) {
                // CVV格式不正确，返回错误响应
                sendJsonResponse(response, ApiResponse.error("CVV格式不正确"));
                return;
            }
            
            // 创建支付请求
            ExternalSystemIntegration.PaymentRequest paymentRequest = 
                new ExternalSystemIntegration.PaymentRequest(cardNumber, expiryDate, cvv, amount, description);
            
            // 调用外部系统处理支付
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.processPayment(paymentRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess()); // 是否成功
            result.put("statusCode", integrationResponse.getStatusCode()); // 状态码
            result.put("amount", amount); // 支付金额
            result.put("description", description); // 支付描述
            result.put("timestamp", System.currentTimeMillis()); // 时间戳
            // 注意：实际系统中不应该返回完整的信用卡信息
            result.put("cardNumberMasked", maskCreditCard(cardNumber)); // 屏蔽后的信用卡号
            
            // 根据外部系统响应结果发送相应响应
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            // 记录支付处理失败并返回错误响应
            logApiError("支付处理失败", e);
            sendJsonResponse(response, ApiResponse.error("支付处理失败: " + e.getMessage()));
        }
    }
    
    /**
     * 信用检查
     * 根据请求参数执行信用检查
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void checkCredit(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "customerId", "name", "birthDate", "annualIncome")) {
                // 缺少必需参数，返回错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            int customerId = getIntParameter(request, "customerId", 0); // 客户ID
            String name = request.getParameter("name"); // 客户姓名
            String birthDate = request.getParameter("birthDate"); // 出生日期
            double annualIncome = getDoubleParameter(request, "annualIncome", 0.0); // 年收入
            
            // 验证参数
            if (customerId <= 0) {
                // 客户ID必须大于0，返回错误响应
                sendJsonResponse(response, ApiResponse.error("客户ID必须大于0"));
                return;
            }
            
            if (annualIncome < 0) {
                // 年收入不能为负数，返回错误响应
                sendJsonResponse(response, ApiResponse.error("年收入不能为负数"));
                return;
            }
            
            // 创建信用检查请求
            ExternalSystemIntegration.CreditCheckRequest creditRequest = 
                new ExternalSystemIntegration.CreditCheckRequest(customerId, name, birthDate, annualIncome);
            
            // 调用外部系统执行信用检查
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.checkCredit(creditRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess()); // 是否成功
            result.put("statusCode", integrationResponse.getStatusCode()); // 状态码
            result.put("customerId", customerId); // 客户ID
            result.put("name", name); // 客户姓名
            result.put("annualIncome", annualIncome); // 年收入
            result.put("timestamp", System.currentTimeMillis()); // 时间戳
            
            // 如果信用检查成功，解析信用检查结果
            if (integrationResponse.isSuccess()) {
                // 解析信用检查结果
                // 这里简化处理，实际应该解析外部系统的响应
                result.put("creditScore", 750); // 示例值：信用评分
                result.put("creditLimit", 1000000.0); // 示例值：信用额度
                result.put("riskLevel", "low"); // 示例值：风险等级
            }
            
            // 根据外部系统响应结果发送相应响应
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            // 记录信用检查失败并返回错误响应
            logApiError("信用检查失败", e);
            sendJsonResponse(response, ApiResponse.error("信用检查失败: " + e.getMessage()));
        }
    }
    
    /**
     * 文档归档
     * 根据请求参数执行文档归档操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void archiveDocument(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "documentId", "documentType", "customerId")) {
                // 缺少必需参数，返回错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String documentId = request.getParameter("documentId"); // 文档ID
            String documentType = request.getParameter("documentType"); // 文档类型
            int customerId = getIntParameter(request, "customerId", 0); // 客户ID
            
            // 验证参数
            if (customerId <= 0) {
                // 客户ID必须大于0，返回错误响应
                sendJsonResponse(response, ApiResponse.error("客户ID必须大于0"));
                return;
            }
            
            // 创建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("archivedBy", getCurrentUser(request).getUsername()); // 归档者
            metadata.put("archivedAt", System.currentTimeMillis()); // 归档时间
            metadata.put("documentType", documentType); // 文档类型
            metadata.put("size", request.getContentLength()); // 文档大小
            
            // 创建文档归档请求
            ExternalSystemIntegration.DocumentArchiveRequest archiveRequest = 
                new ExternalSystemIntegration.DocumentArchiveRequest(documentId, documentType, customerId, metadata);
            
            // 调用外部系统执行文档归档
            ExternalSystemIntegration.IntegrationResponse integrationResponse = 
                integration.archiveDocument(archiveRequest);
            
            // 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("success", integrationResponse.isSuccess()); // 是否成功
            result.put("statusCode", integrationResponse.getStatusCode()); // 状态码
            result.put("documentId", documentId); // 文档ID
            result.put("documentType", documentType); // 文档类型
            result.put("customerId", customerId); // 客户ID
            result.put("timestamp", System.currentTimeMillis()); // 时间戳
            
            // 根据外部系统响应结果发送相应响应
            if (integrationResponse.isSuccess()) {
                sendJsonResponse(response, ApiResponse.success(result));
            } else {
                sendJsonResponse(response, ApiResponse.error(integrationResponse.getErrorMessage()));
            }
            
        } catch (Exception e) {
            // 记录文档归档失败并返回错误响应
            logApiError("文档归档失败", e);
            sendJsonResponse(response, ApiResponse.error("文档归档失败: " + e.getMessage()));
        }
    }
    
    /**
     * 系统健康检查
     * 检查各个外部系统的健康状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void checkSystemHealth(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 创建健康状态映射，用于存储各个系统的健康状态
            Map<String, Object> healthStatus = new HashMap<>();
            
            // 检查各个外部系统的健康状态
            // 检查银行转账系统健康状态
            healthStatus.put("bankTransfer", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.BANK_TRANSFER));
            // 检查邮件服务健康状态
            healthStatus.put("emailService", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.EMAIL_SERVICE));
            // 检查短信服务健康状态
            healthStatus.put("smsService", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.SMS_SERVICE));
            // 检查支付网关健康状态
            healthStatus.put("paymentGateway", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.PAYMENT_GATEWAY));
            // 检查信用检查系统健康状态
            healthStatus.put("creditCheck", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.CREDIT_CHECK));
            // 检查文档归档系统健康状态
            healthStatus.put("documentArchive", 
                integration.checkSystemHealth(ExternalSystemIntegration.SystemType.DOCUMENT_ARCHIVE));
            
            // 计算整体健康状态
            boolean allHealthy = healthStatus.values().stream()
                .allMatch(status -> (boolean) status);
            
            // 设置健康状态信息
            healthStatus.put("overallHealthy", allHealthy); // 整体健康状态
            healthStatus.put("timestamp", System.currentTimeMillis()); // 时间戳
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(healthStatus));
            
        } catch (Exception e) {
            // 记录系统健康检查失败并返回错误响应
            logApiError("系统健康检查失败", e);
            sendJsonResponse(response, ApiResponse.error("系统健康检查失败"));
        }
    }
    
    /**
     * 验证邮箱格式
     * 检查邮箱地址是否符合标准格式
     * @param email 邮箱地址
     * @return 如果邮箱格式正确返回true，否则返回false
     */
    private boolean isValidEmail(String email) {
        // 使用正则表达式验证邮箱格式
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
    
    /**
     * 验证手机号格式
     * 检查手机号是否符合标准格式
     * @param phone 手机号
     * @return 如果手机号格式正确返回true，否则返回false
     */
    private boolean isValidPhoneNumber(String phone) {
        // 使用正则表达式验证手机号格式
        return phone != null && phone.matches("^[\\d\\-\\+\\s()]+$");
    }
    
    /**
     * 验证信用卡号格式
     * 检查信用卡号是否符合标准格式
     * @param cardNumber 信用卡号
     * @return 如果信用卡号格式正确返回true，否则返回false
     */
    private boolean isValidCreditCard(String cardNumber) {
        // 移除空格和连字符后检查长度是否大于等于13
        return cardNumber != null && cardNumber.replaceAll("[\\s-]", "").length() >= 13;
    }
    
    /**
     * 验证CVV格式
     * 检查CVV码是否符合标准格式
     * @param cvv CVV码
     * @return 如果CVV格式正确返回true，否则返回false
     */
    private boolean isValidCvv(String cvv) {
        // 使用正则表达式验证CVV格式（3-4位数字）
        return cvv != null && cvv.matches("^\\d{3,4}$");
    }
    
    /**
     * 屏蔽信用卡号
     * 将信用卡号的前几位数字替换为星号，只显示后四位
     * @param cardNumber 信用卡号
     * @return 屏蔽后的信用卡号
     */
    private String maskCreditCard(String cardNumber) {
        // 移除空格和连字符
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        // 如果长度小于等于4，直接返回屏蔽格式
        if (cleaned.length() <= 4) {
            return "****-****-****-" + cleaned;
        }
        // 返回屏蔽格式，只显示后四位
        return "****-****-****-" + cleaned.substring(cleaned.length() - 4);
    }
    
    /**
     * 记录API错误
     * 将API错误信息输出到标准错误流并打印堆栈跟踪
     * @param message 错误消息
     * @param e 异常对象
     */
    private void logApiError(String message, Exception e) {
        // 输出错误消息到标准错误流
        System.err.println("API错误: " + message);
        // 打印异常堆栈跟踪
        e.printStackTrace();
    }
    
    /**
     * 检查是否需要权限
     * 集成API需要权限验证
     * @return 总是返回true，表示需要权限验证
     */
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    /**
     * 检查用户是否有权限访问
     * 只有管理员用户才能访问集成API
     * @param request HTTP请求对象
     * @return 如果用户是管理员返回true，否则返回false
     */
    @Override
    protected boolean hasPermission(HttpServletRequest request) {
        // 获取当前用户
        com.insurance.model.User user = getCurrentUser(request);
        // 检查用户是否为管理员
        return user != null && user.isAdmin();
    }
}