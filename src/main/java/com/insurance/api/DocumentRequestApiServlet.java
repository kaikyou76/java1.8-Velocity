package com.insurance.api;

import com.insurance.model.DocumentRequest;
import com.insurance.service.DocumentRequestService;
import com.insurance.util.LogUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资料请求管理API
 * 提供资料请求和仮申込的CRUD操作接口
 * 用于处理资料请求相关的API请求，包括资料请求的增删改查、搜索、统计和状态更新等功能
 */
@WebServlet("/api/document-requests/*")
public class DocumentRequestApiServlet extends ApiBaseServlet {
    
    // 资料请求服务对象，用于处理资料请求相关业务逻辑
    private DocumentRequestService documentRequestService;
    // 日期格式化对象，用于解析和格式化日期字符串
    private SimpleDateFormat dateFormat;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     */
    @Override
    public void init() {
        // 创建资料请求服务实例
        this.documentRequestService = new DocumentRequestService();
        // 创建日期格式化对象，指定日期格式为"yyyy-MM-dd"
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }
    
    /**
     * 处理API请求
     * 根据请求路径和方法执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
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
                    // 获取资料请求列表
                    getDocumentRequests(request, response);
                } else if ("POST".equals(method)) {
                    // 创建新资料请求
                    createDocumentRequest(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // ID路径处理（如/123）
                // 从路径中提取请求ID
                int requestId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    // 获取单个资料请求详情
                    getDocumentRequest(request, response, requestId);
                } else if ("PUT".equals(method)) {
                    // 更新资料请求
                    updateDocumentRequest(request, response, requestId);
                } else if ("DELETE".equals(method)) {
                    // 删除资料请求
                    deleteDocumentRequest(request, response, requestId);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/search")) {
                // 搜索接口
                if ("GET".equals(method)) {
                    // 搜索资料请求
                    searchDocumentRequests(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    // 获取资料请求统计信息
                    getDocumentRequestStatistics(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/customer/\\d+$")) {
                // 客户相关资料请求（如/customer/123）
                // 从路径中提取客户ID
                int customerId = Integer.parseInt(pathInfo.substring(10));
                if ("GET".equals(method)) {
                    // 获取客户相关资料请求
                    getDocumentRequestsByCustomer(request, response, customerId);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/types")) {
                // 请求类型列表
                if ("GET".equals(method)) {
                    // 获取请求类型列表
                    getRequestTypes(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+/status")) {
                // 状态更新（如/123/status）
                // 从路径中提取请求ID
                int requestId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/status")));
                if ("PUT".equals(method)) {
                    // 更新资料请求状态
                    updateDocumentRequestStatus(request, response, requestId);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else {
                // 路径未找到
                sendJsonResponse(response, ApiResponse.notFound());
            }
        } catch (Exception e) {
            // 记录API处理异常日志
            LogUtil.error("资料请求API处理异常: " + pathInfo, e);
            // 返回内部错误响应
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取资料请求列表
     * 获取所有资料请求信息，支持分页、状态过滤、请求类型过滤和排序
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int page = getIntParameter(request, "page", 1); // 页码，默认为1
            int size = getIntParameter(request, "size", 20); // 每页大小，默认为20
            String status = getParameter(request, "status", ""); // 状态过滤条件
            String requestType = getParameter(request, "requestType", ""); // 请求类型过滤条件
            String sortBy = getParameter(request, "sortBy", "id"); // 排序字段，默认为id
            String sortOrder = getParameter(request, "sortOrder", "desc"); // 排序顺序，默认为降序
            
            // 调用服务层获取所有资料请求
            List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
            
            // 状态过滤
            if (!status.isEmpty()) {
                // 移除不匹配状态的资料请求
                requests.removeIf(request -> !status.equals(request.getStatus()));
            }
            
            // 请求类型过滤
            if (!requestType.isEmpty()) {
                // 移除不匹配请求类型的资料请求
                requests.removeIf(request -> !requestType.equals(request.getRequestType()));
            }
            
            // 简单分页处理
            int total = requests.size(); // 总记录数
            int from = (page - 1) * size; // 起始索引
            int to = Math.min(from + size, total); // 结束索引
            
            // 如果起始索引超出总记录数，则清空列表
            if (from >= total) {
                requests.clear();
            } else {
                // 截取当前页的数据
                requests = requests.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page); // 当前页码
            meta.put("size", size); // 每页大小
            meta.put("total", total); // 总记录数
            meta.put("pages", (int) Math.ceil((double) total / size)); // 总页数
            meta.put("status", status); // 状态过滤条件
            meta.put("requestType", requestType); // 请求类型过滤条件
            meta.put("sortBy", sortBy); // 排序字段
            meta.put("sortOrder", sortOrder); // 排序顺序
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(requests, meta));
            
        } catch (Exception e) {
            // 记录获取资料请求列表失败日志
            LogUtil.error("获取资料请求列表失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取资料请求列表失败"));
        }
    }
    
    /**
     * 获取单个资料请求详情
     * 根据请求ID获取资料请求详细信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param requestId 请求ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getDocumentRequest(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层根据ID获取资料请求
            DocumentRequest documentRequest = documentRequestService.getDocumentRequestById(requestId);
            
            // 如果资料请求存在，发送成功响应；否则发送未找到响应
            if (documentRequest != null) {
                sendJsonResponse(response, ApiResponse.success(documentRequest));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            // 记录获取资料请求详情失败日志
            LogUtil.error("获取资料请求详情失败: ID=" + requestId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取资料请求详情失败"));
        }
    }
    
    /**
     * 创建新资料请求
     * 根据请求参数创建新的资料请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void createDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "customerName", "email", "phone", "requestType")) {
                // 缺少必需参数，发送错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String customerName = request.getParameter("customerName"); // 客户姓名
            String email = request.getParameter("email"); // 邮箱
            String phone = request.getParameter("phone"); // 电话
            String address = getParameter(request, "address", ""); // 地址
            String requestType = request.getParameter("requestType"); // 请求类型
            String details = getParameter(request, "details", ""); // 详细信息
            String preferredContactMethod = getParameter(request, "preferredContactMethod", "email"); // 偏好联系方式
            String preferredContactTime = getParameter(request, "preferredContactTime", ""); // 偏好联系时间
            String notes = getParameter(request, "notes", ""); // 备注
            String status = getParameter(request, "status", "pending"); // 状态
            String requestedDateStr = getParameter(request, "requestedDate", ""); // 请求日期字符串
            
            // 解析请求日期
            java.util.Date requestedDate = null;
            if (!requestedDateStr.isEmpty()) {
                // 如果提供了请求日期，则解析日期
                requestedDate = parseDate(requestedDateStr);
                if (requestedDate == null) {
                    // 请求日期格式不正确，发送错误响应
                    sendJsonResponse(response, ApiResponse.error("请求日期格式不正确，请使用yyyy-MM-dd格式"));
                    return;
                }
            }
            
            // 创建资料请求对象
            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setCustomerName(customerName); // 设置客户姓名
            documentRequest.setEmail(email); // 设置邮箱
            documentRequest.setPhone(phone); // 设置电话
            documentRequest.setAddress(address); // 设置地址
            documentRequest.setRequestType(requestType); // 设置请求类型
            documentRequest.setDetails(details); // 设置详细信息
            documentRequest.setPreferredContactMethod(preferredContactMethod); // 设置偏好联系方式
            documentRequest.setPreferredContactTime(preferredContactTime); // 设置偏好联系时间
            documentRequest.setNotes(notes); // 设置备注
            documentRequest.setStatus(status); // 设置状态
            
            // 如果解析出请求日期，则设置请求日期
            if (requestedDate != null) {
                documentRequest.setRequestedDate(new java.sql.Date(requestedDate.getTime()));
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api"; // 创建者
            
            // 创建资料请求
            boolean success = documentRequestService.createDocumentRequest(documentRequest, createdBy);
            
            // 根据创建结果发送相应响应
            if (success) {
                sendJsonResponse(response, ApiResponse.created(documentRequest));
            } else {
                sendJsonResponse(response, ApiResponse.error("资料请求创建失败"));
            }
            
        } catch (Exception e) {
            // 记录创建资料请求失败日志
            LogUtil.error("创建资料请求失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("资料请求创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新资料请求
     * 根据请求ID和请求参数更新资料请求信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param requestId 请求ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updateDocumentRequest(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            // 检查资料请求是否存在
            DocumentRequest existingRequest = documentRequestService.getDocumentRequestById(requestId);
            if (existingRequest == null) {
                // 资料请求不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            String customerName = getParameter(request, "customerName", existingRequest.getCustomerName()); // 客户姓名
            String email = getParameter(request, "email", existingRequest.getEmail()); // 邮箱
            String phone = getParameter(request, "phone", existingRequest.getPhone()); // 电话
            String address = getParameter(request, "address", existingRequest.getAddress()); // 地址
            String requestType = getParameter(request, "requestType", existingRequest.getRequestType()); // 请求类型
            String details = getParameter(request, "details", existingRequest.getDetails()); // 详细信息
            String preferredContactMethod = getParameter(request, "preferredContactMethod", existingRequest.getPreferredContactMethod()); // 偏好联系方式
            String preferredContactTime = getParameter(request, "preferredContactTime", existingRequest.getPreferredContactTime()); // 偏好联系时间
            String notes = getParameter(request, "notes", existingRequest.getNotes()); // 备注
            String status = getParameter(request, "status", existingRequest.getStatus()); // 状态
            String requestedDateStr = getParameter(request, "requestedDate", ""); // 请求日期字符串
            
            // 更新资料请求对象
            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setId(requestId); // 设置请求ID
            documentRequest.setCustomerName(customerName); // 设置客户姓名
            documentRequest.setEmail(email); // 设置邮箱
            documentRequest.setPhone(phone); // 设置电话
            documentRequest.setAddress(address); // 设置地址
            documentRequest.setRequestType(requestType); // 设置请求类型
            documentRequest.setDetails(details); // 设置详细信息
            documentRequest.setPreferredContactMethod(preferredContactMethod); // 设置偏好联系方式
            documentRequest.setPreferredContactTime(preferredContactTime); // 设置偏好联系时间
            documentRequest.setNotes(notes); // 设置备注
            documentRequest.setStatus(status); // 设置状态
            
            // 处理请求日期更新
            if (!requestedDateStr.isEmpty()) {
                // 如果提供了请求日期，则解析日期
                java.util.Date requestedDate = parseDate(requestedDateStr);
                if (requestedDate == null) {
                    // 请求日期格式不正确，发送错误响应
                    sendJsonResponse(response, ApiResponse.error("请求日期格式不正确"));
                    return;
                }
                documentRequest.setRequestedDate(new java.sql.Date(requestedDate.getTime()));
            } else {
                // 如果未提供请求日期，则使用原有日期
                documentRequest.setRequestedDate(existingRequest.getRequestedDate());
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api"; // 更新者
            
            // 更新资料请求
            boolean success = documentRequestService.updateDocumentRequest(documentRequest, updatedBy);
            
            // 根据更新结果发送相应响应
            if (success) {
                // 更新成功，重新获取资料请求信息并发送成功响应
                DocumentRequest updatedRequest = documentRequestService.getDocumentRequestById(requestId);
                sendJsonResponse(response, ApiResponse.success(updatedRequest));
            } else {
                // 更新失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("资料请求更新失败"));
            }
            
        } catch (Exception e) {
            // 记录更新资料请求失败日志
            LogUtil.error("更新资料请求失败: ID=" + requestId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("资料请求更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除资料请求
     * 根据请求ID删除资料请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param requestId 请求ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void deleteDocumentRequest(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            // 检查资料请求是否存在
            DocumentRequest documentRequest = documentRequestService.getDocumentRequestById(requestId);
            if (documentRequest == null) {
                // 资料请求不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api"; // 删除者
            
            // 删除资料请求
            boolean success = documentRequestService.deleteDocumentRequest(requestId, deletedBy);
            
            // 根据删除结果发送相应响应
            if (success) {
                // 删除成功，发送成功响应
                sendJsonResponse(response, ApiResponse.success("资料请求删除成功"));
            } else {
                // 删除失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("资料请求删除失败"));
            }
            
        } catch (Exception e) {
            // 记录删除资料请求失败日志
            LogUtil.error("删除资料请求失败: ID=" + requestId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("资料请求删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索资料请求
     * 根据关键词搜索资料请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void searchDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取搜索关键词
            String keyword = getParameter(request, "keyword", "");
            if (keyword.trim().isEmpty()) {
                // 搜索关键词为空，发送错误响应
                sendJsonResponse(response, ApiResponse.error("搜索关键词不能为空"));
                return;
            }
            
            // 调用服务层搜索资料请求
            List<DocumentRequest> requests = documentRequestService.searchDocumentRequests(keyword);
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("keyword", keyword); // 搜索关键词
            meta.put("count", requests.size()); // 搜索结果数量
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(requests, meta));
            
        } catch (Exception e) {
            // 记录搜索资料请求失败日志
            LogUtil.error("搜索资料请求失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("搜索资料请求失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取资料请求统计信息
     * 获取资料请求相关的统计信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getDocumentRequestStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层获取所有资料请求
            List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
            
            // 基本统计
            int total = requests.size(); // 总请求数
            int pending = (int) requests.stream().filter(r -> "pending".equals(r.getStatus())).count(); // 待处理请求数
            int processing = (int) requests.stream().filter(r -> "processing".equals(r.getStatus())).count(); // 处理中请求数
            int completed = (int) requests.stream().filter(r -> "completed".equals(r.getStatus())).count(); // 已完成请求数
            int cancelled = (int) requests.stream().filter(r -> "cancelled".equals(r.getStatus())).count(); // 已取消请求数
            
            // 按请求类型统计
            Map<String, Long> typeCount = requests.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    DocumentRequest::getRequestType, 
                    java.util.stream.Collectors.counting()
                ));
            
            // 按状态统计
            Map<String, Long> statusCount = requests.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    DocumentRequest::getStatus, 
                    java.util.stream.Collectors.counting()
                ));
            
            // 按联系方法统计
            Map<String, Long> contactMethodCount = requests.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    DocumentRequest::getPreferredContactMethod, 
                    java.util.stream.Collectors.counting()
                ));
            
            // 构建统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", total); // 总请求数
            statistics.put("pending", pending); // 待处理请求数
            statistics.put("processing", processing); // 处理中请求数
            statistics.put("completed", completed); // 已完成请求数
            statistics.put("cancelled", cancelled); // 已取消请求数
            statistics.put("typeDistribution", typeCount); // 请求类型分布
            statistics.put("statusDistribution", statusCount); // 状态分布
            statistics.put("contactMethodDistribution", contactMethodCount); // 联系方法分布
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            // 记录获取资料请求统计失败日志
            LogUtil.error("获取资料请求统计失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取资料请求统计失败"));
        }
    }
    
    /**
     * 获取客户相关资料请求
     * 根据客户ID获取该客户的所有资料请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getDocumentRequestsByCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 这里需要实现根据客户ID获取资料请求的方法
            // 暂时返回空列表
            // 调用服务层获取所有资料请求
            List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
            // 注意：这里需要根据实际业务逻辑来过滤客户相关的请求
            // 暂时不过滤
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("customerId", customerId); // 客户ID
            meta.put("count", requests.size()); // 资料请求数量
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(requests, meta));
            
        } catch (Exception e) {
            // 记录获取客户资料请求失败日志
            LogUtil.error("获取客户资料请求失败: customerId=" + customerId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取客户资料请求失败"));
        }
    }
    
    /**
     * 获取请求类型列表
     * 获取所有可用的请求类型
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getRequestTypes(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 创建请求类型列表
            List<Map<String, Object>> types = new java.util.ArrayList<>();
            
            // 添加资料請求类型
            Map<String, Object> type1 = new HashMap<>();
            type1.put("code", "document"); // 类型代码
            type1.put("name", "资料請求"); // 类型名称
            type1.put("description", "商品資料や会社案内の請求"); // 类型描述
            types.add(type1);
            
            // 添加仮申込类型
            Map<String, Object> type2 = new HashMap<>();
            type2.put("code", "temporary"); // 类型代码
            type2.put("name", "仮申込"); // 类型名称
            type2.put("description", "本申込前の仮の申込手続き"); // 类型描述
            types.add(type2);
            
            // 添加相談类型
            Map<String, Object> type3 = new HashMap<>();
            type3.put("code", "consultation"); // 类型代码
            type3.put("name", "相談"); // 类型名称
            type3.put("description", "商品やサービスに関する相談"); // 类型描述
            types.add(type3);
            
            // 添加その他类型
            Map<String, Object> type4 = new HashMap<>();
            type4.put("code", "other"); // 类型代码
            type4.put("name", "その他"); // 类型名称
            type4.put("description", "その他のお問い合わせ"); // 类型描述
            types.add(type4);
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(types));
            
        } catch (Exception e) {
            // 记录获取请求类型列表失败日志
            LogUtil.error("获取请求类型列表失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取请求类型列表失败"));
        }
    }
    
    /**
     * 更新资料请求状态
     * 根据请求ID更新资料请求的状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param requestId 请求ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updateDocumentRequestStatus(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            // 检查资料请求是否存在
            DocumentRequest existingRequest = documentRequestService.getDocumentRequestById(requestId);
            if (existingRequest == null) {
                // 资料请求不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取状态参数
            String status = request.getParameter("status");
            if (status == null || status.trim().isEmpty()) {
                // 状态参数为空，发送错误响应
                sendJsonResponse(response, ApiResponse.error("状态参数不能为空"));
                return;
            }
            
            // 验证状态值
            if (!isValidStatus(status)) {
                // 状态值无效，发送错误响应
                sendJsonResponse(response, ApiResponse.error("无效的状态值"));
                return;
            }
            
            // 更新资料请求状态
            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setId(requestId); // 设置请求ID
            documentRequest.setStatus(status); // 设置状态
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api"; // 更新者
            
            // 更新资料请求
            boolean success = documentRequestService.updateDocumentRequest(documentRequest, updatedBy);
            
            // 根据更新结果发送相应响应
            if (success) {
                // 更新成功，重新获取资料请求信息并发送成功响应
                DocumentRequest updatedRequest = documentRequestService.getDocumentRequestById(requestId);
                sendJsonResponse(response, ApiResponse.success(updatedRequest));
            } else {
                // 更新失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("状态更新失败"));
            }
            
        } catch (Exception e) {
            // 记录更新资料请求状态失败日志
            LogUtil.error("更新资料请求状态失败: ID=" + requestId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("状态更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 验证状态值是否有效
     * 检查给定的状态值是否为有效的状态
     * @param status 状态值
     * @return 如果状态值有效返回true，否则返回false
     */
    private boolean isValidStatus(String status) {
        // 检查状态值是否为以下有效值之一
        return "pending".equals(status) || 
               "processing".equals(status) || 
               "completed".equals(status) || 
               "cancelled".equals(status);
    }
    
    /**
     * 解析日期字符串
     * 将日期字符串解析为Date对象
     * @param dateStr 日期字符串
     * @return 解析出的Date对象，如果解析失败则返回null
     */
    private java.util.Date parseDate(String dateStr) {
        try {
            // 使用dateFormat解析日期字符串
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            // 解析失败返回null
            return null;
        }
    }
    
    /**
     * 检查是否需要权限
     * 资料请求API需要权限验证
     * @return 总是返回true，表示需要权限验证
     */
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    /**
     * 检查用户是否有权限访问
     * 只有管理员和销售人员才能访问资料请求API
     * @param request HTTP请求对象
     * @return 如果用户有权限返回true，否则返回false
     */
    @Override
    protected boolean hasPermission(HttpServletRequest request) {
        // 获取当前用户
        com.insurance.model.User user = getCurrentUser(request);
        // 检查用户是否为管理员或销售人员
        return user != null && (user.isAdmin() || user.isSales());
    }
}