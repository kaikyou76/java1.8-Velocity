package com.insurance.integration;

import com.insurance.util.LogUtil;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 外部系统集成接口
 * 处理与外部系统的数据交换和集成
 */
public class ExternalSystemIntegration {
    
    // 外部系统配置属性，存储从配置文件加载的配置信息
    private Properties config;
    // 外部系统配置文件路径
    private static final String CONFIG_FILE = "/config/external-systems.properties";
    
    // 系统类型枚举，定义了支持的外部系统类型
    public enum SystemType {
        // 银行转账系统
        BANK_TRANSFER,    // 銀行振込システム
        // 邮件服务系统
        EMAIL_SERVICE,    // メール配信システム
        // 短信服务系统
        SMS_SERVICE,      // SMS送信システム
        // 支付网关系统
        PAYMENT_GATEWAY, // 決済ゲートウェイ
        // 信用检查系统
        CREDIT_CHECK,     // 与信チェックシステム
        // 文档归档系统
        DOCUMENT_ARCHIVE  // 書類アーカイブシステム
    }
    
    // 构造函数，初始化外部系统集成类
    // 在创建实例时自动加载配置
    public ExternalSystemIntegration() {
        loadConfig();
    }
    
    /**
     * 加载外部系统配置
     * 从配置文件加载外部系统的URL、超时时间等配置信息
     * 如果配置文件不存在，则使用默认配置
     */
    private void loadConfig() {
        // 初始化配置属性对象
        config = new Properties();
        try {
            // 获取配置文件路径并加载配置
            String configPath = getClass().getResource(CONFIG_FILE).getPath();
            config.load(new FileInputStream(configPath));
            LogUtil.info("外部系统配置加载成功", "ExternalSystemIntegration");
        } catch (IOException e) {
            // 如果配置文件不存在，记录警告日志并使用默认配置
            LogUtil.warn("外部系统配置文件未找到，使用默认配置", "ExternalSystemIntegration");
            // 设置默认配置
            setDefaultConfig();
        }
    }
    
    /**
     * 设置默认配置
     * 当配置文件不存在时，为各个外部系统设置默认的URL和超时时间
     */
    private void setDefaultConfig() {
        // 银行转账系统默认配置
        config.setProperty("bank_transfer.url", "https://api.example-bank.com/transfers");
        config.setProperty("bank_transfer.timeout", "30000");
        config.setProperty("bank_transfer.api_key", "");
        
        // 邮件服务系统默认配置
        config.setProperty("email_service.url", "https://api.example-email.com/send");
        config.setProperty("email_service.timeout", "10000");
        config.setProperty("email_service.api_key", "");
        
        // 短信服务系统默认配置
        config.setProperty("sms_service.url", "https://api.example-sms.com/send");
        config.setProperty("sms_service.timeout", "10000");
        config.setProperty("sms_service.api_key", "");
        
        // 支付网关系统默认配置
        config.setProperty("payment_gateway.url", "https://api.example-payment.com/charge");
        config.setProperty("payment_gateway.timeout", "15000");
        config.setProperty("payment_gateway.merchant_id", "");
        config.setProperty("payment_gateway.api_key", "");
    }
    
    /**
     * 发送HTTP请求
     * 向指定的外部系统发送HTTP请求，并返回响应结果
     * @param systemType 系统类型枚举值
     * @param method HTTP方法（GET、POST等）
     * @param headers 请求头信息
     * @param requestBody 请求体内容
     * @return IntegrationResponse 集成响应对象
     */
    public IntegrationResponse sendHttpRequest(SystemType systemType, String method, 
                                        Map<String, String> headers, String requestBody) {
        try {
            // 获取系统URL和超时时间
            String baseUrl = getSystemUrl(systemType);
            int timeout = getSystemTimeout(systemType);
            
            // 创建URL连接
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法和超时时间
            connection.setRequestMethod(method);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            // 设置允许输出（用于发送请求体）
            connection.setDoOutput(true);
            
            // 设置默认请求头
            setDefaultHeaders(connection, systemType);
            // 设置额外的请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            // 发送请求体
            if (requestBody != null && !requestBody.isEmpty()) {
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            // 获取响应
            int responseCode = connection.getResponseCode();
            String responseBody = readResponse(connection);
            
            // 记录API调用日志
            logApiCall(systemType, method, baseUrl, responseCode);
            
            // 返回成功响应
            return new IntegrationResponse(responseCode, responseBody, null);
            
        } catch (Exception e) {
            // 记录错误日志并返回错误响应
            LogUtil.error("外部系统请求失败: " + systemType, e);
            return new IntegrationResponse(500, null, e.getMessage());
        }
    }
    
    /**
     * 银行转账集成
     * 处理向银行系统的转账请求
     * @param request 银行转账请求对象
     * @return IntegrationResponse 集成响应对象
     */
    public IntegrationResponse processBankTransfer(BankTransferRequest request) {
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.BANK_TRANSFER));
            
            // 构造请求体
            String requestBody = String.format(
                "{\"account_number\":\"%s\",\"bank_code\":\"%s\",\"amount\":%.2f,\"currency\":\"JPY\",\"description\":\"%s\"}",
                request.getAccountNumber(), request.getBankCode(), 
                request.getAmount(), request.getDescription()
            );
            
            // 发送HTTP请求
            IntegrationResponse response = sendHttpRequest(
                SystemType.BANK_TRANSFER, "POST", headers, requestBody
            );
            
            // 如果响应状态码为200，记录成功日志
            if (response.getStatusCode() == 200) {
                LogUtil.info("银行转账成功: " + request.getAmount(), "ExternalSystemIntegration");
            }
            
            // 返回响应结果
            return response;
            
        } catch (Exception e) {
            // 记录错误日志并返回错误响应
            LogUtil.error("银行转账处理失败", e);
            return new IntegrationResponse(500, null, "银行转账处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 邮件服务集成
     * 发送邮件到指定的邮件服务系统
     * @param request 邮件请求对象
     * @return IntegrationResponse 集成响应对象
     */
    public IntegrationResponse sendEmail(EmailRequest request) {
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.EMAIL_SERVICE));
            
            // 构造请求体，对主题和正文进行JSON转义
            String requestBody = String.format(
                "{\"to\":\"%s\",\"subject\":\"%s\",\"body\":\"%s\",\"from\":\"%s\"}",
                request.getTo(), escapeJson(request.getSubject()), 
                escapeJson(request.getBody()), request.getFrom()
            );
            
            // 发送HTTP请求
            IntegrationResponse response = sendHttpRequest(
                SystemType.EMAIL_SERVICE, "POST", headers, requestBody
            );
            
            // 如果响应状态码为200，记录成功日志
            if (response.getStatusCode() == 200) {
                LogUtil.info("邮件发送成功: " + request.getTo(), "ExternalSystemIntegration");
            }
            
            // 返回响应结果
            return response;
            
        } catch (Exception e) {
            // 记录错误日志并返回错误响应
            LogUtil.error("邮件发送失败", e);
            return new IntegrationResponse(500, null, "邮件发送失败: " + e.getMessage());
        }
    }
    
    /**
     * SMS服务集成
     * 发送短信到指定的SMS服务系统
     * @param request SMS请求对象
     * @return IntegrationResponse 集成响应对象
     */
    public IntegrationResponse sendSms(SmsRequest request) {
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.SMS_SERVICE));
            
            // 构造请求体，对消息内容进行JSON转义
            String requestBody = String.format(
                "{\"to\":\"%s\",\"message\":\"%s\",\"sender\":\"%s\"}",
                request.getTo(), escapeJson(request.getMessage()), request.getSender()
            );
            
            // 发送HTTP请求
            IntegrationResponse response = sendHttpRequest(
                SystemType.SMS_SERVICE, "POST", headers, requestBody
            );
            
            // 如果响应状态码为200，记录成功日志
            if (response.getStatusCode() == 200) {
                LogUtil.info("SMS发送成功: " + request.getTo(), "ExternalSystemIntegration");
            }
            
            // 返回响应结果
            return response;
            
        } catch (Exception e) {
            // 记录错误日志并返回错误响应
            LogUtil.error("SMS发送失败", e);
            return new IntegrationResponse(500, null, "SMS发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 支付网关集成
     * 处理支付请求到支付网关系统
     * @param request 支付请求对象
     * @return IntegrationResponse 集成响应对象
     */
    public IntegrationResponse processPayment(PaymentRequest request) {
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.PAYMENT_GATEWAY));
            
            // 构造请求体
            String requestBody = String.format(
                "{\"card_number\":\"%s\",\"expiry_date\":\"%s\",\"cvv\":\"%s\",\"amount\":%.2f,\"currency\":\"JPY\",\"description\":\"%s\"}",
                request.getCardNumber(), request.getExpiryDate(), 
                request.getCvv(), request.getAmount(), request.getDescription()
            );
            
            // 发送HTTP请求
            IntegrationResponse response = sendHttpRequest(
                SystemType.PAYMENT_GATEWAY, "POST", headers, requestBody
            );
            
            // 如果响应状态码为200，记录成功日志
            if (response.getStatusCode() == 200) {
                LogUtil.info("支付处理成功: " + request.getAmount(), "ExternalSystemIntegration");
            }
            
            // 返回响应结果
            return response;
            
        } catch (Exception e) {
            // 记录错误日志并返回错误响应
            LogUtil.error("支付处理失败", e);
            return new IntegrationResponse(500, null, "支付处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 信用检查集成
     * 向信用检查系统发送信用检查请求
     * @param request 信用检查请求对象
     * @return IntegrationResponse 集成响应对象
     */
    public IntegrationResponse checkCredit(CreditCheckRequest request) {
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.CREDIT_CHECK));
            
            // 构造请求体，对姓名进行JSON转义
            String requestBody = String.format(
                "{\"customer_id\":%d,\"name\":\"%s\",\"birth_date\":\"%s\",\"annual_income\":%.2f}",
                request.getCustomerId(), escapeJson(request.getName()), 
                request.getBirthDate(), request.getAnnualIncome()
            );
            
            // 发送HTTP请求
            IntegrationResponse response = sendHttpRequest(
                SystemType.CREDIT_CHECK, "POST", headers, requestBody
            );
            
            // 如果响应状态码为200，记录成功日志
            if (response.getStatusCode() == 200) {
                LogUtil.info("信用检查完成: " + request.getCustomerId(), "ExternalSystemIntegration");
            }
            
            // 返回响应结果
            return response;
            
        } catch (Exception e) {
            // 记录错误日志并返回错误响应
            LogUtil.error("信用检查失败", e);
            return new IntegrationResponse(500, null, "信用检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 文档归档集成
     * 将文档发送到文档归档系统
     * @param request 文档归档请求对象
     * @return IntegrationResponse 集成响应对象
     */
    public IntegrationResponse archiveDocument(DocumentArchiveRequest request) {
        try {
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "multipart/form-data");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.DOCUMENT_ARCHIVE));
            
            // 这里简化处理，实际需要处理文件上传
            // 构造请求体
            String requestBody = String.format(
                "{\"document_id\":\"%s\",\"document_type\":\"%s\",\"customer_id\":%d,\"metadata\":%s}",
                request.getDocumentId(), request.getDocumentType(), 
                request.getCustomerId(), request.getMetadata().toString()
            );
            
            // 发送HTTP请求
            IntegrationResponse response = sendHttpRequest(
                SystemType.DOCUMENT_ARCHIVE, "POST", headers, requestBody
            );
            
            // 如果响应状态码为200，记录成功日志
            if (response.getStatusCode() == 200) {
                LogUtil.info("文档归档成功: " + request.getDocumentId(), "ExternalSystemIntegration");
            }
            
            // 返回响应结果
            return response;
            
        } catch (Exception e) {
            // 记录错误日志并返回错误响应
            LogUtil.error("文档归档失败", e);
            return new IntegrationResponse(500, null, "文档归档失败: " + e.getMessage());
        }
    }
    
    /**
     * 系统健康检查
     * 检查指定外部系统的健康状态
     * @param systemType 系统类型枚举值
     * @return boolean 如果系统健康返回true，否则返回false
     */
    public boolean checkSystemHealth(SystemType systemType) {
        try {
            // 发送GET请求检查系统健康状态
            IntegrationResponse response = sendHttpRequest(systemType, "GET", null, null);
            // 如果响应状态码为200，表示系统健康
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            // 记录错误日志并返回false
            LogUtil.error("系统健康检查失败: " + systemType, e);
            return false;
        }
    }
    
    /**
     * 获取系统URL
     * 根据系统类型从配置中获取对应的URL
     * @param systemType 系统类型枚举值
     * @return String 系统URL
     */
    private String getSystemUrl(SystemType systemType) {
        // 从配置中获取系统URL，如果没有配置则返回空字符串
        return config.getProperty(systemType.name().toLowerCase() + ".url", "");
    }
    
    /**
     * 获取系统超时时间
     * 根据系统类型从配置中获取对应的超时时间
     * @param systemType 系统类型枚举值
     * @return int 超时时间（毫秒）
     */
    private int getSystemTimeout(SystemType systemType) {
        // 从配置中获取系统超时时间，如果没有配置则返回默认值30000毫秒
        return Integer.parseInt(config.getProperty(systemType.name().toLowerCase() + ".timeout", "30000"));
    }
    
    /**
     * 获取API密钥
     * 根据系统类型从配置中获取对应的API密钥
     * @param systemType 系统类型枚举值
     * @return String API密钥
     */
    private String getApiKey(SystemType systemType) {
        // 从配置中获取API密钥
        String key = config.getProperty(systemType.name().toLowerCase() + ".api_key", "");
        // 如果密钥为空，记录警告日志
        if (key.isEmpty()) {
            LogUtil.warn("API密钥未配置: " + systemType, "ExternalSystemIntegration");
        }
        return key;
    }
    
    /**
     * 设置默认请求头
     * 为HTTP连接设置默认的请求头信息
     * @param connection HttpURLConnection对象
     * @param systemType 系统类型枚举值
     */
    private void setDefaultHeaders(HttpURLConnection connection, SystemType systemType) {
        // 设置用户代理
        connection.setRequestProperty("User-Agent", "InsuranceSystem/1.0");
        // 设置接受的内容类型
        connection.setRequestProperty("Accept", "application/json");
        // 设置系统类型头
        connection.setRequestProperty("X-System-Type", systemType.name());
        // 设置请求ID头
        connection.setRequestProperty("X-Request-ID", java.util.UUID.randomUUID().toString());
    }
    
    /**
     * 读取响应
     * 从HTTP连接中读取响应内容
     * @param connection HttpURLConnection对象
     * @return String 响应内容
     * @throws IOException IO异常
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        // 创建StringBuilder用于存储响应内容
        StringBuilder response = new StringBuilder();
        // 使用BufferedReader读取响应流
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            // 逐行读取响应内容
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        // 返回响应内容
        return response.toString();
    }
    
    /**
     * 记录API调用日志
     * 记录外部API调用的相关信息
     * @param systemType 系统类型枚举值
     * @param method HTTP方法
     * @param url 请求URL
     * @param responseCode 响应状态码
     */
    private void logApiCall(SystemType systemType, String method, String url, int responseCode) {
        // 记录API调用信息日志
        LogUtil.info(String.format("外部API调用 - 系统: %s, 方法: %s, URL: %s, 状态码: %d", 
                              systemType, method, url, responseCode), 
                     "ExternalSystemIntegration");
    }
    
    /**
     * 转义JSON字符串
     * 对字符串进行JSON转义处理
     * @param text 原始字符串
     * @return String 转义后的字符串
     */
    private String escapeJson(String text) {
        // 对字符串中的特殊字符进行转义
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\r", "\\r")
                  .replace("\n", "\\n")
                  .replace("\t", "\\t");
    }
    
    /**
     * 集成响应类
     * 封装外部系统集成的响应信息
     */
    public static class IntegrationResponse {
        // 响应状态码
        private int statusCode;
        // 响应体内容
        private String responseBody;
        // 错误信息
        private String errorMessage;
        
        // 构造函数
        public IntegrationResponse(int statusCode, String responseBody, String errorMessage) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.errorMessage = errorMessage;
        }
        
        // Getter方法
        // 获取响应状态码
        public int getStatusCode() { return statusCode; }
        // 获取响应体内容
        public String getResponseBody() { return responseBody; }
        // 获取错误信息
        public String getErrorMessage() { return errorMessage; }
        
        // 业务方法
        // 检查响应是否成功（状态码在200-299之间）
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        // 获取错误信息，如果为空则返回默认错误信息
        public String getErrorMessage() {
            return errorMessage != null ? errorMessage : "未知错误";
        }
    }
    
    /**
     * 银行转账请求类
     * 封装银行转账请求的相关信息
     */
    public static class BankTransferRequest {
        // 账户号码
        private String accountNumber;
        // 银行代码
        private String bankCode;
        // 转账金额
        private double amount;
        // 转账描述
        private String description;
        
        // 构造函数和Getter/Setter方法
        // 构造函数
        public BankTransferRequest(String accountNumber, String bankCode, double amount, String description) {
            this.accountNumber = accountNumber;
            this.bankCode = bankCode;
            this.amount = amount;
            this.description = description;
        }
        
        // 获取账户号码
        public String getAccountNumber() { return accountNumber; }
        // 获取银行代码
        public String getBankCode() { return bankCode; }
        // 获取转账金额
        public double getAmount() { return amount; }
        // 获取转账描述
        public String getDescription() { return description; }
    }
    
    /**
     * 邮件请求类
     * 封装邮件发送请求的相关信息
     */
    public static class EmailRequest {
        // 收件人邮箱
        private String to;
        // 邮件主题
        private String subject;
        // 邮件正文
        private String body;
        // 发件人邮箱
        private String from;
        
        // 构造函数
        public EmailRequest(String to, String subject, String body, String from) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.from = from;
        }
        
        // 获取收件人邮箱
        public String getTo() { return to; }
        // 获取邮件主题
        public String getSubject() { return subject; }
        // 获取邮件正文
        public String getBody() { return body; }
        // 获取发件人邮箱
        public String getFrom() { return from; }
    }
    
    /**
     * SMS请求类
     * 封装短信发送请求的相关信息
     */
    public static class SmsRequest {
        // 接收者手机号
        private String to;
        // 短信内容
        private String message;
        // 发送者标识
        private String sender;
        
        // 构造函数
        public SmsRequest(String to, String message, String sender) {
            this.to = to;
            this.message = message;
            this.sender = sender;
        }
        
        // 获取接收者手机号
        public String getTo() { return to; }
        // 获取短信内容
        public String getMessage() { return message; }
        // 获取发送者标识
        public String getSender() { return sender; }
    }
    
    /**
     * 支付请求类
     * 封装支付请求的相关信息
     */
    public static class PaymentRequest {
        // 卡号
        private String cardNumber;
        // 有效期
        private String expiryDate;
        // CVV码
        private String cvv;
        // 支付金额
        private double amount;
        // 支付描述
        private String description;
        
        // 构造函数
        public PaymentRequest(String cardNumber, String expiryDate, String cvv, double amount, String description) {
            this.cardNumber = cardNumber;
            this.expiryDate = expiryDate;
            this.cvv = cvv;
            this.amount = amount;
            this.description = description;
        }
        
        // 获取卡号
        public String getCardNumber() { return cardNumber; }
        // 获取有效期
        public String getExpiryDate() { return expiryDate; }
        // 获取CVV码
        public String getCvv() { return cvv; }
        // 获取支付金额
        public double getAmount() { return amount; }
        // 获取支付描述
        public String getDescription() { return description; }
    }
    
    /**
     * 信用检查请求类
     * 封装信用检查请求的相关信息
     */
    public static class CreditCheckRequest {
        // 客户ID
        private int customerId;
        // 客户姓名
        private String name;
        // 出生日期
        private String birthDate;
        // 年收入
        private double annualIncome;
        
        // 构造函数
        public CreditCheckRequest(int customerId, String name, String birthDate, double annualIncome) {
            this.customerId = customerId;
            this.name = name;
            this.birthDate = birthDate;
            this.annualIncome = annualIncome;
        }
        
        // 获取客户ID
        public int getCustomerId() { return customerId; }
        // 获取客户姓名
        public String getName() { return name; }
        // 获取出生日期
        public String getBirthDate() { return birthDate; }
        // 获取年收入
        public double getAnnualIncome() { return annualIncome; }
    }
    
    /**
     * 文档归档请求类
     * 封装文档归档请求的相关信息
     */
    public static class DocumentArchiveRequest {
        // 文档ID
        private String documentId;
        // 文档类型
        private String documentType;
        // 客户ID
        private int customerId;
        // 元数据
        private Map<String, Object> metadata;
        
        // 构造函数
        public DocumentArchiveRequest(String documentId, String documentType, int customerId, Map<String, Object> metadata) {
            this.documentId = documentId;
            this.documentType = documentType;
            this.customerId = customerId;
            this.metadata = metadata;
        }
        
        // 获取文档ID
        public String getDocumentId() { return documentId; }
        // 获取文档类型
        public String getDocumentType() { return documentType; }
        // 获取客户ID
        public int getCustomerId() { return customerId; }
        // 获取元数据
        public Map<String, Object> getMetadata() { return metadata; }
    }
}