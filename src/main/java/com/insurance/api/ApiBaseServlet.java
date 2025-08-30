package com.insurance.api;

import com.insurance.security.AuthUtil;
import com.insurance.util.LogUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * API基础Servlet类
 * 提供API请求的通用功能
 */
@WebServlet("/api/*")
public abstract class ApiBaseServlet extends HttpServlet {
    
    /**
     * 请求方法枚举
     */
    protected enum HttpMethod {
        GET, POST, PUT, DELETE, OPTIONS
    }
    
    /**
     * 响应状态码
     */
    protected static class ResponseStatus {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_ERROR = 500;
    }
    
    /**
     * 响应消息
     */
    protected static class ResponseMessage {
        public static final String SUCCESS = "success";
        public static final String ERROR = "error";
        public static final String INVALID_REQUEST = "invalid_request";
        public static final String UNAUTHORIZED = "unauthorized";
        public static final String FORBIDDEN = "forbidden";
        public static final String NOT_FOUND = "not_found";
        public static final String INTERNAL_ERROR = "internal_error";
    }
    
    /**
     * API响应格式
     */
    protected static class ApiResponse {
        private int status;
        private String message;
        private Object data;
        private Map<String, Object> meta;
        
        public ApiResponse(int status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
            this.meta = new HashMap<>();
        }
        
        public ApiResponse(int status, String message, Object data, Map<String, Object> meta) {
            this.status = status;
            this.message = message;
            this.data = data;
            this.meta = meta;
        }
        
        // Getter方法
        public int getStatus() { return status; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
        public Map<String, Object> getMeta() { return meta; }
        
        // Setter方法
        public void setStatus(int status) { this.status = status; }
        public void setMessage(String message) { this.message = message; }
        public void setData(Object data) { this.data = data; }
        public void setMeta(Map<String, Object> meta) { this.meta = meta; }
        
        /**
         * 添加元数据
         */
        public void addMeta(String key, Object value) {
            this.meta.put(key, value);
        }
        
        /**
         * 创建成功响应
         */
        public static ApiResponse success(Object data) {
            return new ApiResponse(ResponseStatus.OK, ResponseMessage.SUCCESS, data);
        }
        
        /**
         * 创建成功响应（带元数据）
         */
        public static ApiResponse success(Object data, Map<String, Object> meta) {
            return new ApiResponse(ResponseStatus.OK, ResponseMessage.SUCCESS, data, meta);
        }
        
        /**
         * 创建创建成功响应
         */
        public static ApiResponse created(Object data) {
            return new ApiResponse(ResponseStatus.CREATED, ResponseMessage.SUCCESS, data);
        }
        
        /**
         * 创建错误响应
         */
        public static ApiResponse error(String message) {
            return new ApiResponse(ResponseStatus.BAD_REQUEST, message, null);
        }
        
        /**
         * 创建未授权响应
         */
        public static ApiResponse unauthorized() {
            return new ApiResponse(ResponseStatus.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED, null);
        }
        
        /**
         * 创建禁止访问响应
         */
        public static ApiResponse forbidden() {
            return new ApiResponse(ResponseStatus.FORBIDDEN, ResponseMessage.FORBIDDEN, null);
        }
        
        /**
         * 创建未找到响应
         */
        public static ApiResponse notFound() {
            return new ApiResponse(ResponseStatus.NOT_FOUND, ResponseMessage.NOT_FOUND, null);
        }
        
        /**
         * 创建内部错误响应
         */
        public static ApiResponse internalError() {
            return new ApiResponse(ResponseStatus.INTERNAL_ERROR, ResponseMessage.INTERNAL_ERROR, null);
        }
    }
    
    /**
     * 处理API请求
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置CORS响应头
        setCorsHeaders(response);
        
        // 处理OPTIONS请求（预检请求）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // 检查认证（如果需要）
        if (requiresAuthentication() && !isAuthenticated(request)) {
            sendJsonResponse(response, ApiResponse.unauthorized());
            return;
        }
        
        // 检查权限（如果需要）
        if (requiresPermission() && !hasPermission(request)) {
            sendJsonResponse(response, ApiResponse.forbidden());
            return;
        }
        
        // 设置内容类型
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // 调用具体的API处理方法
            handleRequest(request, response);
        } catch (Exception e) {
            LogUtil.error("API处理异常: " + request.getRequestURI(), e);
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 抽象方法：处理具体的API请求
     */
    protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException;
    
    /**
     * 子类可以重写此方法来设置是否需要认证
     */
    protected boolean requiresAuthentication() {
        return true;
    }
    
    /**
     * 子类可以重写此方法来设置是否需要权限检查
     */
    protected boolean requiresPermission() {
        return false;
    }
    
    /**
     * 子类可以重写此方法来检查权限
     */
    protected boolean hasPermission(HttpServletRequest request) {
        return true;
    }
    
    /**
     * 检查用户是否已认证
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        return AuthUtil.isLoggedIn(request);
    }
    
    /**
     * 获取当前用户
     */
    protected com.insurance.model.User getCurrentUser(HttpServletRequest request) {
        return AuthUtil.getCurrentUser(request);
    }
    
    /**
     * 设置CORS响应头
     */
    protected void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
    
    /**
     * 发送JSON响应
     */
    protected void sendJsonResponse(HttpServletResponse response, ApiResponse apiResponse) 
            throws IOException {
        response.setStatus(apiResponse.getStatus());
        PrintWriter out = response.getWriter();
        out.print(toJson(apiResponse));
        out.flush();
    }
    
    /**
     * 将ApiResponse转换为JSON字符串
     */
    protected String toJson(ApiResponse apiResponse) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\":").append(apiResponse.getStatus()).append(",");
        json.append("\"message\":\"").append(escapeJson(apiResponse.getMessage())).append("\"");
        
        if (apiResponse.getData() != null) {
            json.append(",\"data\":");
            if (apiResponse.getData() instanceof String) {
                json.append("\"").append(escapeJson(apiResponse.getData().toString())).append("\"");
            } else {
                json.append(objectToJson(apiResponse.getData()));
            }
        }
        
        if (apiResponse.getMeta() != null && !apiResponse.getMeta().isEmpty()) {
            json.append(",\"meta\":{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : apiResponse.getMeta().entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(escapeJson(entry.getKey())).append("\":");
                if (entry.getValue() instanceof String) {
                    json.append("\"").append(escapeJson(entry.getValue().toString())).append("\"");
                } else {
                    json.append(objectToJson(entry.getValue()));
                }
                first = false;
            }
            json.append("}");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * 将对象转换为JSON字符串（简单实现）
     */
    protected String objectToJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        // 这里简化处理，实际项目中建议使用JSON库
        return obj.toString();
    }
    
    /**
     * 转义JSON字符串
     */
    protected String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\r", "\\r")
                 .replace("\n", "\\n")
                 .replace("\t", "\\t");
    }
    
    /**
     * 获取请求参数（带默认值）
     */
    protected String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value != null ? value : defaultValue;
    }
    
    /**
     * 获取整数参数
     */
    protected int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        try {
            return Integer.parseInt(request.getParameter(name));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取长整数参数
     */
    protected long getLongParameter(HttpServletRequest request, String name, long defaultValue) {
        try {
            return Long.parseLong(request.getParameter(name));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔参数
     */
    protected boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
        String value = request.getParameter(name);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * 验证请求参数
     */
    protected boolean validateRequiredParameters(HttpServletRequest request, String... requiredParams) {
        for (String param : requiredParams) {
            String value = request.getParameter(param);
            if (value == null || value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取请求体内容
     */
    protected String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        java.io.BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    /**
     * 解析JSON请求体（简单实现）
     */
    protected Map<String, String> parseJsonBody(String jsonBody) {
        Map<String, String> params = new HashMap<>();
        // 这里简化处理，实际项目中建议使用JSON库
        return params;
    }
}