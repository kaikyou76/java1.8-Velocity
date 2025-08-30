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
    
    // 外部系统配置
    private Properties config;
    private static final String CONFIG_FILE = "/config/external-systems.properties";
    
    // 系统类型枚举
    public enum SystemType {
        BANK_TRANSFER,    // 銀行振込システム
        EMAIL_SERVICE,    // メール配信システム
        SMS_SERVICE,      // SMS送信システム
        PAYMENT_GATEWAY, // 決済ゲートウェイ
        CREDIT_CHECK,     // 与信チェックシステム
        DOCUMENT_ARCHIVE  // 書類アーカイブシステム
    }
    
    public ExternalSystemIntegration() {
        loadConfig();
    }
    
    /**
     * 加载外部系统配置
     */
    private void loadConfig() {
        config = new Properties();
        try {
            String configPath = getClass().getResource(CONFIG_FILE).getPath();
            config.load(new FileInputStream(configPath));
            LogUtil.info("外部系统配置加载成功", "ExternalSystemIntegration");
        } catch (IOException e) {
            LogUtil.warn("外部系统配置文件未找到，使用默认配置", "ExternalSystemIntegration");
            // 设置默认配置
            setDefaultConfig();
        }
    }
    
    /**
     * 设置默认配置
     */
    private void setDefaultConfig() {
        config.setProperty("bank_transfer.url", "https://api.example-bank.com/transfers");
        config.setProperty("bank_transfer.timeout", "30000");
        config.setProperty("bank_transfer.api_key", "");
        
        config.setProperty("email_service.url", "https://api.example-email.com/send");
        config.setProperty("email_service.timeout", "10000");
        config.setProperty("email_service.api_key", "");
        
        config.setProperty("sms_service.url", "https://api.example-sms.com/send");
        config.setProperty("sms_service.timeout", "10000");
        config.setProperty("sms_service.api_key", "");
        
        config.setProperty("payment_gateway.url", "https://api.example-payment.com/charge");
        config.setProperty("payment_gateway.timeout", "15000");
        config.setProperty("payment_gateway.merchant_id", "");
        config.setProperty("payment_gateway.api_key", "");
    }
    
    /**
     * 发送HTTP请求
     */
    public IntegrationResponse sendHttpRequest(SystemType systemType, String method, 
                                        Map<String, String> headers, String requestBody) {
        try {
            String baseUrl = getSystemUrl(systemType);
            int timeout = getSystemTimeout(systemType);
            
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // 设置请求方法
            connection.setRequestMethod(method);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoOutput(true);
            
            // 设置请求头
            setDefaultHeaders(connection, systemType);
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
            
            // 记录日志
            logApiCall(systemType, method, baseUrl, responseCode);
            
            return new IntegrationResponse(responseCode, responseBody, null);
            
        } catch (Exception e) {
            LogUtil.error("外部系统请求失败: " + systemType, e);
            return new IntegrationResponse(500, null, e.getMessage());
        }
    }
    
    /**
     * 银行转账集成
     */
    public IntegrationResponse processBankTransfer(BankTransferRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.BANK_TRANSFER));
            
            String requestBody = String.format(
                "{\"account_number\":\"%s\",\"bank_code\":\"%s\",\"amount\":%.2f,\"currency\":\"JPY\",\"description\":\"%s\"}",
                request.getAccountNumber(), request.getBankCode(), 
                request.getAmount(), request.getDescription()
            );
            
            IntegrationResponse response = sendHttpRequest(
                SystemType.BANK_TRANSFER, "POST", headers, requestBody
            );
            
            if (response.getStatusCode() == 200) {
                LogUtil.info("银行转账成功: " + request.getAmount(), "ExternalSystemIntegration");
            }
            
            return response;
            
        } catch (Exception e) {
            LogUtil.error("银行转账处理失败", e);
            return new IntegrationResponse(500, null, "银行转账处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 邮件服务集成
     */
    public IntegrationResponse sendEmail(EmailRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.EMAIL_SERVICE));
            
            String requestBody = String.format(
                "{\"to\":\"%s\",\"subject\":\"%s\",\"body\":\"%s\",\"from\":\"%s\"}",
                request.getTo(), escapeJson(request.getSubject()), 
                escapeJson(request.getBody()), request.getFrom()
            );
            
            IntegrationResponse response = sendHttpRequest(
                SystemType.EMAIL_SERVICE, "POST", headers, requestBody
            );
            
            if (response.getStatusCode() == 200) {
                LogUtil.info("邮件发送成功: " + request.getTo(), "ExternalSystemIntegration");
            }
            
            return response;
            
        } catch (Exception e) {
            LogUtil.error("邮件发送失败", e);
            return new IntegrationResponse(500, null, "邮件发送失败: " + e.getMessage());
        }
    }
    
    /**
     * SMS服务集成
     */
    public IntegrationResponse sendSms(SmsRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.SMS_SERVICE));
            
            String requestBody = String.format(
                "{\"to\":\"%s\",\"message\":\"%s\",\"sender\":\"%s\"}",
                request.getTo(), escapeJson(request.getMessage()), request.getSender()
            );
            
            IntegrationResponse response = sendHttpRequest(
                SystemType.SMS_SERVICE, "POST", headers, requestBody
            );
            
            if (response.getStatusCode() == 200) {
                LogUtil.info("SMS发送成功: " + request.getTo(), "ExternalSystemIntegration");
            }
            
            return response;
            
        } catch (Exception e) {
            LogUtil.error("SMS发送失败", e);
            return new IntegrationResponse(500, null, "SMS发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 支付网关集成
     */
    public IntegrationResponse processPayment(PaymentRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.PAYMENT_GATEWAY));
            
            String requestBody = String.format(
                "{\"card_number\":\"%s\",\"expiry_date\":\"%s\",\"cvv\":\"%s\",\"amount\":%.2f,\"currency\":\"JPY\",\"description\":\"%s\"}",
                request.getCardNumber(), request.getExpiryDate(), 
                request.getCvv(), request.getAmount(), request.getDescription()
            );
            
            IntegrationResponse response = sendHttpRequest(
                SystemType.PAYMENT_GATEWAY, "POST", headers, requestBody
            );
            
            if (response.getStatusCode() == 200) {
                LogUtil.info("支付处理成功: " + request.getAmount(), "ExternalSystemIntegration");
            }
            
            return response;
            
        } catch (Exception e) {
            LogUtil.error("支付处理失败", e);
            return new IntegrationResponse(500, null, "支付处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 信用检查集成
     */
    public IntegrationResponse checkCredit(CreditCheckRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.CREDIT_CHECK));
            
            String requestBody = String.format(
                {"customer_id\":%d,\"name\":\"%s\",\"birth_date\":\"%s\",\"annual_income\":%.2f}",
                request.getCustomerId(), escapeJson(request.getName()), 
                request.getBirthDate(), request.getAnnualIncome()
            );
            
            IntegrationResponse response = sendHttpRequest(
                SystemType.CREDIT_CHECK, "POST", headers, requestBody
            );
            
            if (response.getStatusCode() == 200) {
                LogUtil.info("信用检查完成: " + request.getCustomerId(), "ExternalSystemIntegration");
            }
            
            return response;
            
        } catch (Exception e) {
            LogUtil.error("信用检查失败", e);
            return new IntegrationResponse(500, null, "信用检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 文档归档集成
     */
    public IntegrationResponse archiveDocument(DocumentArchiveRequest request) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "multipart/form-data");
            headers.put("Authorization", "Bearer " + getApiKey(SystemType.DOCUMENT_ARCHIVE));
            
            // 这里简化处理，实际需要处理文件上传
            String requestBody = String.format(
                "{\"document_id\":\"%s\",\"document_type\":\"%s\",\"customer_id\":%d,\"metadata\":%s}",
                request.getDocumentId(), request.getDocumentType(), 
                request.getCustomerId(), request.getMetadata().toString()
            );
            
            IntegrationResponse response = sendHttpRequest(
                SystemType.DOCUMENT_ARCHIVE, "POST", headers, requestBody
            );
            
            if (response.getStatusCode() == 200) {
                LogUtil.info("文档归档成功: " + request.getDocumentId(), "ExternalSystemIntegration");
            }
            
            return response;
            
        } catch (Exception e) {
            LogUtil.error("文档归档失败", e);
            return new IntegrationResponse(500, null, "文档归档失败: " + e.getMessage());
        }
    }
    
    /**
     * 系统健康检查
     */
    public boolean checkSystemHealth(SystemType systemType) {
        try {
            IntegrationResponse response = sendHttpRequest(systemType, "GET", null, null);
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            LogUtil.error("系统健康检查失败: " + systemType, e);
            return false;
        }
    }
    
    /**
     * 获取系统URL
     */
    private String getSystemUrl(SystemType systemType) {
        return config.getProperty(systemType.name().toLowerCase() + ".url", "");
    }
    
    /**
     * 获取系统超时时间
     */
    private int getSystemTimeout(SystemType systemType) {
        return Integer.parseInt(config.getProperty(systemType.name().toLowerCase() + ".timeout", "30000"));
    }
    
    /**
     * 获取API密钥
     */
    private String getApiKey(SystemType systemType) {
        String key = config.getProperty(systemType.name().toLowerCase() + ".api_key", "");
        if (key.isEmpty()) {
            LogUtil.warn("API密钥未配置: " + systemType, "ExternalSystemIntegration");
        }
        return key;
    }
    
    /**
     * 设置默认请求头
     */
    private void setDefaultHeaders(HttpURLConnection connection, SystemType systemType) {
        connection.setRequestProperty("User-Agent", "InsuranceSystem/1.0");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-System-Type", systemType.name());
        connection.setRequestProperty("X-Request-ID", java.util.UUID.randomUUID().toString());
    }
    
    /**
     * 读取响应
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    
    /**
     * 记录API调用日志
     */
    private void logApiCall(SystemType systemType, String method, String url, int responseCode) {
        LogUtil.info(String.format("外部API调用 - 系统: %s, 方法: %s, URL: %s, 状态码: %d", 
                              systemType, method, url, responseCode), 
                     "ExternalSystemIntegration");
    }
    
    /**
     * 转义JSON字符串
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\r", "\\r")
                  .replace("\n", "\\n")
                  .replace("\t", "\\t");
    }
    
    /**
     * 集成响应类
     */
    public static class IntegrationResponse {
        private int statusCode;
        private String responseBody;
        private String errorMessage;
        
        public IntegrationResponse(int statusCode, String responseBody, String errorMessage) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.errorMessage = errorMessage;
        }
        
        // Getter方法
        public int getStatusCode() { return statusCode; }
        public String getResponseBody() { return responseBody; }
        public String getErrorMessage() { return errorMessage; }
        
        // 业务方法
        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
        
        public String getErrorMessage() {
            return errorMessage != null ? errorMessage : "未知错误";
        }
    }
    
    /**
     * 银行转账请求类
     */
    public static class BankTransferRequest {
        private String accountNumber;
        private String bankCode;
        private double amount;
        private String description;
        
        // 构造函数和Getter/Setter方法
        public BankTransferRequest(String accountNumber, String bankCode, double amount, String description) {
            this.accountNumber = accountNumber;
            this.bankCode = bankCode;
            this.amount = amount;
            this.description = description;
        }
        
        public String getAccountNumber() { return accountNumber; }
        public String getBankCode() { return bankCode; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
    }
    
    /**
     * 邮件请求类
     */
    public static class EmailRequest {
        private String to;
        private String subject;
        private String body;
        private String from;
        
        public EmailRequest(String to, String subject, String body, String from) {
            this.to = to;
            this.subject = subject;
            this.body = body;
            this.from = from;
        }
        
        public String getTo() { return to; }
        public String getSubject() { return subject; }
        public String getBody() { return body; }
        public String getFrom() { return from; }
    }
    
    /**
     * SMS请求类
     */
    public static class SmsRequest {
        private String to;
        private String message;
        private String sender;
        
        public SmsRequest(String to, String message, String sender) {
            this.to = to;
            this.message = message;
            this.sender = sender;
        }
        
        public String getTo() { return to; }
        public String getMessage() { return message; }
        public String getSender() { return sender; }
    }
    
    /**
     * 支付请求类
     */
    public static class PaymentRequest {
        private String cardNumber;
        private String expiryDate;
        private String cvv;
        private double amount;
        private String description;
        
        public PaymentRequest(String cardNumber, String expiryDate, String cvv, double amount, String description) {
            this.cardNumber = cardNumber;
            this.expiryDate = expiryDate;
            this.cvv = cvv;
            this.amount = amount;
            this.description = description;
        }
        
        public String getCardNumber() { return cardNumber; }
        public String getExpiryDate() { return expiryDate; }
        public String getCvv() { return cvv; }
        public double getAmount() { return amount; }
        public String getDescription() { return description; }
    }
    
    /**
     * 信用检查请求类
     */
    public static class CreditCheckRequest {
        private int customerId;
        private String name;
        private String birthDate;
        private double annualIncome;
        
        public CreditCheckRequest(int customerId, String name, String birthDate, double annualIncome) {
            this.customerId = customerId;
            this.name = name;
            this.birthDate = birthDate;
            this.annualIncome = annualIncome;
        }
        
        public int getCustomerId() { return customerId; }
        public String getName() { return name; }
        public String getBirthDate() { return birthDate; }
        public double getAnnualIncome() { return annualIncome; }
    }
    
    /**
     * 文档归档请求类
     */
    public static class DocumentArchiveRequest {
        private String documentId;
        private String documentType;
        private int customerId;
        private Map<String, Object> metadata;
        
        public DocumentArchiveRequest(String documentId, String documentType, int customerId, Map<String, Object> metadata) {
            this.documentId = documentId;
            this.documentType = documentType;
            this.customerId = customerId;
            this.metadata = metadata;
        }
        
        public String getDocumentId() { return documentId; }
        public String getDocumentType() { return documentType; }
        public int getCustomerId() { return customerId; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
}