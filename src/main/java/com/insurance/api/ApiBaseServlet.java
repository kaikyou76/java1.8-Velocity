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
 * 用于处理API请求的基础Servlet类，提供认证、权限检查、CORS设置、JSON响应等通用功能
 */
@WebServlet("/api/*")
public abstract class ApiBaseServlet extends HttpServlet {
    
    /**
     * 请求方法枚举
     * 定义HTTP请求方法的枚举类型
     */
    protected enum HttpMethod {
        GET, POST, PUT, DELETE, OPTIONS
    }
    
    /**
     * 响应状态码
     * 定义API响应的状态码常量
     */
    protected static class ResponseStatus {
        public static final int OK = 200; // 请求成功
        public static final int CREATED = 201; // 创建成功
        public static final int BAD_REQUEST = 400; // 请求错误
        public static final int UNAUTHORIZED = 401; // 未授权
        public static final int FORBIDDEN = 403; // 禁止访问
        public static final int NOT_FOUND = 404; // 未找到
        public static final int INTERNAL_ERROR = 500; // 内部错误
    }
    
    /**
     * 响应消息
     * 定义API响应的消息常量
     */
    protected static class ResponseMessage {
        public static final String SUCCESS = "success"; // 成功
        public static final String ERROR = "error"; // 错误
        public static final String INVALID_REQUEST = "invalid_request"; // 无效请求
        public static final String UNAUTHORIZED = "unauthorized"; // 未授权
        public static final String FORBIDDEN = "forbidden"; // 禁止访问
        public static final String NOT_FOUND = "not_found"; // 未找到
        public static final String INTERNAL_ERROR = "internal_error"; // 内部错误
    }
    
    /**
     * API响应格式
     * 定义API响应的数据结构
     */
    protected static class ApiResponse {
        private int status; // 响应状态码
        private String message; // 响应消息
        private Object data; // 响应数据
        private Map<String, Object> meta; // 元数据
        
        public ApiResponse(int status, String message, Object data) {
            this.status = status; // 设置状态码
            this.message = message; // 设置消息
            this.data = data; // 设置数据
            this.meta = new HashMap<>(); // 初始化元数据
        }
        
        public ApiResponse(int status, String message, Object data, Map<String, Object> meta) {
            this.status = status; // 设置状态码
            this.message = message; // 设置消息
            this.data = data; // 设置数据
            this.meta = meta; // 设置元数据
        }
        
        // Getter方法
        public int getStatus() { return status; } // 获取状态码
        public String getMessage() { return message; } // 获取消息
        public Object getData() { return data; } // 获取数据
        public Map<String, Object> getMeta() { return meta; } // 获取元数据
        
        // Setter方法
        public void setStatus(int status) { this.status = status; } // 设置状态码
        public void setMessage(String message) { this.message = message; } // 设置消息
        public void setData(Object data) { this.data = data; } // 设置数据
        public void setMeta(Map<String, Object> meta) { this.meta = meta; } // 设置元数据
        
        /**
         * 添加元数据
         * @param key 元数据键
         * @param value 元数据值
         */
        public void addMeta(String key, Object value) {
            this.meta.put(key, value); // 将键值对添加到元数据中
        }
        
        /**
         * 创建成功响应
         * @param data 响应数据
         * @return ApiResponse对象
         */
        public static ApiResponse success(Object data) {
            return new ApiResponse(ResponseStatus.OK, ResponseMessage.SUCCESS, data);
        }
        
        /**
         * 创建成功响应（带元数据）
         * @param data 响应数据
         * @param meta 元数据
         * @return ApiResponse对象
         */
        public static ApiResponse success(Object data, Map<String, Object> meta) {
            return new ApiResponse(ResponseStatus.OK, ResponseMessage.SUCCESS, data, meta);
        }
        
        /**
         * 创建创建成功响应
         * @param data 响应数据
         * @return ApiResponse对象
         */
        public static ApiResponse created(Object data) {
            return new ApiResponse(ResponseStatus.CREATED, ResponseMessage.SUCCESS, data);
        }
        
        /**
         * 创建错误响应
         * @param message 错误消息
         * @return ApiResponse对象
         */
        public static ApiResponse error(String message) {
            return new ApiResponse(ResponseStatus.BAD_REQUEST, message, null);
        }
        
        /**
         * 创建未授权响应
         * @return ApiResponse对象
         */
        public static ApiResponse unauthorized() {
            return new ApiResponse(ResponseStatus.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED, null);
        }
        
        /**
         * 创建禁止访问响应
         * @return ApiResponse对象
         */
        public static ApiResponse forbidden() {
            return new ApiResponse(ResponseStatus.FORBIDDEN, ResponseMessage.FORBIDDEN, null);
        }
        
        /**
         * 创建未找到响应
         * @return ApiResponse对象
         */
        public static ApiResponse notFound() {
            return new ApiResponse(ResponseStatus.NOT_FOUND, ResponseMessage.NOT_FOUND, null);
        }
        
        /**
         * 创建内部错误响应
         * @return ApiResponse对象
         */
        public static ApiResponse internalError() {
            return new ApiResponse(ResponseStatus.INTERNAL_ERROR, ResponseMessage.INTERNAL_ERROR, null);
        }
    }
    
    /**
     * 处理API请求
     * 重写service方法以提供统一的API处理逻辑
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置CORS响应头
        // 设置跨域资源共享响应头，允许前端应用访问API
        setCorsHeaders(response);
        
        // 处理OPTIONS请求（预检请求）
        // 如果是OPTIONS请求（CORS预检请求），直接返回200状态码
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // 检查认证（如果需要）
        // 如果API需要认证且用户未认证，则返回未授权响应
        if (requiresAuthentication() && !isAuthenticated(request)) {
            sendJsonResponse(response, ApiResponse.unauthorized());
            return;
        }
        
        // 检查权限（如果需要）
        // 如果API需要权限检查且用户没有相应权限，则返回禁止访问响应
        if (requiresPermission() && !hasPermission(request)) {
            sendJsonResponse(response, ApiResponse.forbidden());
            return;
        }
        
        // 设置内容类型
        // 设置响应内容类型为JSON，字符编码为UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // 调用具体的API处理方法
            // 调用子类实现的handleRequest方法处理具体业务逻辑
            handleRequest(request, response);
        } catch (Exception e) {
            // 记录API处理异常日志
            LogUtil.error("API处理异常: " + request.getRequestURI(), e);
            // 返回内部错误响应
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 抽象方法：处理具体的API请求
     * 子类必须实现此方法来处理具体的API请求逻辑
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException;
    
    /**
     * 子类可以重写此方法来设置是否需要认证
     * 默认需要认证
     * @return 是否需要认证
     */
    protected boolean requiresAuthentication() {
        return true; // 默认需要认证
    }
    
    /**
     * 子类可以重写此方法来设置是否需要权限检查
     * 默认不需要权限检查
     * @return 是否需要权限检查
     */
    protected boolean requiresPermission() {
        return false; // 默认不需要权限检查
    }
    
    /**
     * 子类可以重写此方法来检查权限
     * 默认有权限
     * @param request HTTP请求对象
     * @return 是否有权限
     */
    protected boolean hasPermission(HttpServletRequest request) {
        return true; // 默认有权限
    }
    
    /**
     * 检查用户是否已认证
     * 使用AuthUtil工具类检查用户是否已登录
     * @param request HTTP请求对象
     * @return 用户是否已认证
     */
    protected boolean isAuthenticated(HttpServletRequest request) {
        return AuthUtil.isLoggedIn(request); // 调用认证工具类检查用户是否已登录
    }
    
    /**
     * 获取当前用户
     * 使用AuthUtil工具类获取当前登录用户信息
     * @param request HTTP请求对象
     * @return 当前用户对象
     */
    protected com.insurance.model.User getCurrentUser(HttpServletRequest request) {
        return AuthUtil.getCurrentUser(request); // 调用认证工具类获取当前用户
    }
    
    /**
     * 设置CORS响应头
     * 设置跨域资源共享相关的响应头
     * @param response HTTP响应对象
     */
    protected void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*"); // 允许所有域访问
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS"); // 允许的HTTP方法
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With"); // 允许的请求头
        response.setHeader("Access-Control-Allow-Credentials", "true"); // 允许携带凭证
        response.setHeader("Access-Control-Max-Age", "3600"); // 预检请求缓存时间（秒）
    }
    
    /**
     * 发送JSON响应
     * 将ApiResponse对象转换为JSON格式并发送给客户端
     * @param response HTTP响应对象
     * @param apiResponse API响应对象
     * @throws IOException IO异常
     */
    protected void sendJsonResponse(HttpServletResponse response, ApiResponse apiResponse) 
            throws IOException {
        response.setStatus(apiResponse.getStatus()); // 设置响应状态码
        PrintWriter out = response.getWriter(); // 获取响应输出流
        out.print(toJson(apiResponse)); // 将ApiResponse转换为JSON并输出
        out.flush(); // 刷新输出流
    }
    
    /**
     * 将ApiResponse转换为JSON字符串
     * 将ApiResponse对象转换为JSON格式的字符串
     * @param apiResponse API响应对象
     * @return JSON字符串
     */
    protected String toJson(ApiResponse apiResponse) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"status\":").append(apiResponse.getStatus()).append(","); // 添加状态码
        json.append("\"message\":\"").append(escapeJson(apiResponse.getMessage())).append("\""); // 添加消息
        
        // 如果有数据，则添加数据部分
        if (apiResponse.getData() != null) {
            json.append(",\"data\":");
            if (apiResponse.getData() instanceof String) {
                // 如果数据是字符串，则添加引号
                json.append("\"").append(escapeJson(apiResponse.getData().toString())).append("\"");
            } else {
                // 如果数据不是字符串，则转换为JSON
                json.append(objectToJson(apiResponse.getData()));
            }
        }
        
        // 如果有元数据且不为空，则添加元数据部分
        if (apiResponse.getMeta() != null && !apiResponse.getMeta().isEmpty()) {
            json.append(",\"meta\":{");
            boolean first = true;
            // 遍历元数据键值对
            for (Map.Entry<String, Object> entry : apiResponse.getMeta().entrySet()) {
                if (!first) json.append(",");
                // 添加键
                json.append("\"").append(escapeJson(entry.getKey())).append("\":");
                // 添加值
                if (entry.getValue() instanceof String) {
                    // 如果值是字符串，则添加引号
                    json.append("\"").append(escapeJson(entry.getValue().toString())).append("\"");
                } else {
                    // 如果值不是字符串，则转换为JSON
                    json.append(objectToJson(entry.getValue()));
                }
                first = false;
            }
            json.append("}");
        }
        
        json.append("}");
        return json.toString(); // 返回JSON字符串
    }
    
    /**
     * 将对象转换为JSON字符串（简单实现）
     * 简单地将对象转换为JSON字符串（实际项目中建议使用JSON库）
     * @param obj 对象
     * @return JSON字符串
     */
    protected String objectToJson(Object obj) {
        if (obj == null) {
            return "null"; // 如果对象为空，返回null字符串
        }
        
        // 这里简化处理，实际项目中建议使用JSON库
        return obj.toString(); // 返回对象的字符串表示
    }
    
    /**
     * 转义JSON字符串
     * 对JSON字符串中的特殊字符进行转义
     * @param text 原始字符串
     * @return 转义后的字符串
     */
    protected String escapeJson(String text) {
        if (text == null) {
            return ""; // 如果字符串为空，返回空字符串
        }
        // 对特殊字符进行转义
        return text.replace("\\", "\\\\") // 转义反斜杠
                 .replace("\"", "\\\"") // 转义双引号
                 .replace("\r", "\\r") // 转义回车符
                 .replace("\n", "\\n") // 转义换行符
                 .replace("\t", "\\t"); // 转义制表符
    }
    
    /**
     * 获取请求参数（带默认值）
     * 获取请求参数，如果参数不存在则返回默认值
     * @param request HTTP请求对象
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    protected String getParameter(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name); // 获取参数值
        return value != null ? value : defaultValue; // 如果参数值不为空则返回参数值，否则返回默认值
    }
    
    /**
     * 获取整数参数
     * 获取请求参数并转换为整数，如果转换失败则返回默认值
     * @param request HTTP请求对象
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 整数参数值或默认值
     */
    protected int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        try {
            return Integer.parseInt(request.getParameter(name)); // 尝试将参数值转换为整数
        } catch (NumberFormatException e) {
            return defaultValue; // 如果转换失败，返回默认值
        }
    }
    
    /**
     * 获取长整数参数
     * 获取请求参数并转换为长整数，如果转换失败则返回默认值
     * @param request HTTP请求对象
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 长整数参数值或默认值
     */
    protected long getLongParameter(HttpServletRequest request, String name, long defaultValue) {
        try {
            return Long.parseLong(request.getParameter(name)); // 尝试将参数值转换为长整数
        } catch (NumberFormatException e) {
            return defaultValue; // 如果转换失败，返回默认值
        }
    }
    
    /**
     * 获取布尔参数
     * 获取请求参数并转换为布尔值，如果参数不存在则返回默认值
     * @param request HTTP请求对象
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 布尔参数值或默认值
     */
    protected boolean getBooleanParameter(HttpServletRequest request, String name, boolean defaultValue) {
        String value = request.getParameter(name); // 获取参数值
        return value != null ? Boolean.parseBoolean(value) : defaultValue; // 如果参数值不为空则转换为布尔值，否则返回默认值
    }
    
    /**
     * 验证请求参数
     * 验证请求中是否包含所有必需的参数
     * @param request HTTP请求对象
     * @param requiredParams 必需的参数名称数组
     * @return 如果所有必需参数都存在且不为空则返回true，否则返回false
     */
    protected boolean validateRequiredParameters(HttpServletRequest request, String... requiredParams) {
        // 遍历所有必需的参数
        for (String param : requiredParams) {
            String value = request.getParameter(param); // 获取参数值
            // 如果参数值为空或仅包含空格，则验证失败
            if (value == null || value.trim().isEmpty()) {
                return false;
            }
        }
        return true; // 所有必需参数都存在且不为空，验证成功
    }
    
    /**
     * 获取请求体内容
     * 读取HTTP请求体的内容
     * @param request HTTP请求对象
     * @return 请求体内容字符串
     * @throws IOException IO异常
     */
    protected String getRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        java.io.BufferedReader reader = request.getReader(); // 获取请求体读取器
        // 逐行读取请求体内容
        while ((line = reader.readLine()) != null) {
            sb.append(line); // 将每行内容添加到StringBuilder中
        }
        return sb.toString(); // 返回请求体内容字符串
    }
    
    /**
     * 解析JSON请求体（简单实现）
     * 简单地解析JSON请求体（实际项目中建议使用JSON库）
     * @param jsonBody JSON请求体字符串
     * @return 参数映射
     */
    protected Map<String, String> parseJsonBody(String jsonBody) {
        Map<String, String> params = new HashMap<>();
        // 这里简化处理，实际项目中建议使用JSON库
        return params; // 返回空的参数映射
    }
}