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
 */
@WebServlet("/api/document-requests/*")
public class DocumentRequestApiServlet extends ApiBaseServlet {
    
    private DocumentRequestService documentRequestService;
    private SimpleDateFormat dateFormat;
    
    @Override
    public void init() {
        this.documentRequestService = new DocumentRequestService();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
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
                    getDocumentRequests(request, response);
                } else if ("POST".equals(method)) {
                    createDocumentRequest(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // ID路径处理
                int requestId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    getDocumentRequest(request, response, requestId);
                } else if ("PUT".equals(method)) {
                    updateDocumentRequest(request, response, requestId);
                } else if ("DELETE".equals(method)) {
                    deleteDocumentRequest(request, response, requestId);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/search")) {
                // 搜索接口
                if ("GET".equals(method)) {
                    searchDocumentRequests(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    getDocumentRequestStatistics(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/customer/\\d+$")) {
                // 客户相关资料请求
                int customerId = Integer.parseInt(pathInfo.substring(10));
                if ("GET".equals(method)) {
                    getDocumentRequestsByCustomer(request, response, customerId);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/types")) {
                // 请求类型列表
                if ("GET".equals(method)) {
                    getRequestTypes(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+/status")) {
                // 状态更新
                int requestId = Integer.parseInt(pathInfo.substring(1, pathInfo.indexOf("/status")));
                if ("PUT".equals(method)) {
                    updateDocumentRequestStatus(request, response, requestId);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
        } catch (Exception e) {
            LogUtil.error("资料请求API处理异常: " + pathInfo, e);
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取资料请求列表
     */
    private void getDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int page = getIntParameter(request, "page", 1);
            int size = getIntParameter(request, "size", 20);
            String status = getParameter(request, "status", "");
            String requestType = getParameter(request, "requestType", "");
            String sortBy = getParameter(request, "sortBy", "id");
            String sortOrder = getParameter(request, "sortOrder", "desc");
            
            // 调用服务层
            List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
            
            // 状态过滤
            if (!status.isEmpty()) {
                requests.removeIf(request -> !status.equals(request.getStatus()));
            }
            
            // 请求类型过滤
            if (!requestType.isEmpty()) {
                requests.removeIf(request -> !requestType.equals(request.getRequestType()));
            }
            
            // 简单分页处理
            int total = requests.size();
            int from = (page - 1) * size;
            int to = Math.min(from + size, total);
            
            if (from >= total) {
                requests.clear();
            } else {
                requests = requests.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page);
            meta.put("size", size);
            meta.put("total", total);
            meta.put("pages", (int) Math.ceil((double) total / size));
            meta.put("status", status);
            meta.put("requestType", requestType);
            meta.put("sortBy", sortBy);
            meta.put("sortOrder", sortOrder);
            
            sendJsonResponse(response, ApiResponse.success(requests, meta));
            
        } catch (Exception e) {
            LogUtil.error("获取资料请求列表失败", e);
            sendJsonResponse(response, ApiResponse.error("获取资料请求列表失败"));
        }
    }
    
    /**
     * 获取单个资料请求详情
     */
    private void getDocumentRequest(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            DocumentRequest documentRequest = documentRequestService.getDocumentRequestById(requestId);
            
            if (documentRequest != null) {
                sendJsonResponse(response, ApiResponse.success(documentRequest));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            LogUtil.error("获取资料请求详情失败: ID=" + requestId, e);
            sendJsonResponse(response, ApiResponse.error("获取资料请求详情失败"));
        }
    }
    
    /**
     * 创建新资料请求
     */
    private void createDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "customerName", "email", "phone", "requestType")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String customerName = request.getParameter("customerName");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String address = getParameter(request, "address", "");
            String requestType = request.getParameter("requestType");
            String details = getParameter(request, "details", "");
            String preferredContactMethod = getParameter(request, "preferredContactMethod", "email");
            String preferredContactTime = getParameter(request, "preferredContactTime", "");
            String notes = getParameter(request, "notes", "");
            String status = getParameter(request, "status", "pending");
            String requestedDateStr = getParameter(request, "requestedDate", "");
            
            // 解析请求日期
            java.util.Date requestedDate = null;
            if (!requestedDateStr.isEmpty()) {
                requestedDate = parseDate(requestedDateStr);
                if (requestedDate == null) {
                    sendJsonResponse(response, ApiResponse.error("请求日期格式不正确，请使用yyyy-MM-dd格式"));
                    return;
                }
            }
            
            // 创建资料请求对象
            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setCustomerName(customerName);
            documentRequest.setEmail(email);
            documentRequest.setPhone(phone);
            documentRequest.setAddress(address);
            documentRequest.setRequestType(requestType);
            documentRequest.setDetails(details);
            documentRequest.setPreferredContactMethod(preferredContactMethod);
            documentRequest.setPreferredContactTime(preferredContactTime);
            documentRequest.setNotes(notes);
            documentRequest.setStatus(status);
            
            if (requestedDate != null) {
                documentRequest.setRequestedDate(new java.sql.Date(requestedDate.getTime()));
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 创建资料请求
            boolean success = documentRequestService.createDocumentRequest(documentRequest, createdBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.created(documentRequest));
            } else {
                sendJsonResponse(response, ApiResponse.error("资料请求创建失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("创建资料请求失败", e);
            sendJsonResponse(response, ApiResponse.error("资料请求创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新资料请求
     */
    private void updateDocumentRequest(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            // 检查资料请求是否存在
            DocumentRequest existingRequest = documentRequestService.getDocumentRequestById(requestId);
            if (existingRequest == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            String customerName = getParameter(request, "customerName", existingRequest.getCustomerName());
            String email = getParameter(request, "email", existingRequest.getEmail());
            String phone = getParameter(request, "phone", existingRequest.getPhone());
            String address = getParameter(request, "address", existingRequest.getAddress());
            String requestType = getParameter(request, "requestType", existingRequest.getRequestType());
            String details = getParameter(request, "details", existingRequest.getDetails());
            String preferredContactMethod = getParameter(request, "preferredContactMethod", existingRequest.getPreferredContactMethod());
            String preferredContactTime = getParameter(request, "preferredContactTime", existingRequest.getPreferredContactTime());
            String notes = getParameter(request, "notes", existingRequest.getNotes());
            String status = getParameter(request, "status", existingRequest.getStatus());
            String requestedDateStr = getParameter(request, "requestedDate", "");
            
            // 更新资料请求对象
            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setId(requestId);
            documentRequest.setCustomerName(customerName);
            documentRequest.setEmail(email);
            documentRequest.setPhone(phone);
            documentRequest.setAddress(address);
            documentRequest.setRequestType(requestType);
            documentRequest.setDetails(details);
            documentRequest.setPreferredContactMethod(preferredContactMethod);
            documentRequest.setPreferredContactTime(preferredContactTime);
            documentRequest.setNotes(notes);
            documentRequest.setStatus(status);
            
            // 处理请求日期更新
            if (!requestedDateStr.isEmpty()) {
                java.util.Date requestedDate = parseDate(requestedDateStr);
                if (requestedDate == null) {
                    sendJsonResponse(response, ApiResponse.error("请求日期格式不正确"));
                    return;
                }
                documentRequest.setRequestedDate(new java.sql.Date(requestedDate.getTime()));
            } else {
                documentRequest.setRequestedDate(existingRequest.getRequestedDate());
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 更新资料请求
            boolean success = documentRequestService.updateDocumentRequest(documentRequest, updatedBy);
            
            if (success) {
                DocumentRequest updatedRequest = documentRequestService.getDocumentRequestById(requestId);
                sendJsonResponse(response, ApiResponse.success(updatedRequest));
            } else {
                sendJsonResponse(response, ApiResponse.error("资料请求更新失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("更新资料请求失败: ID=" + requestId, e);
            sendJsonResponse(response, ApiResponse.error("资料请求更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除资料请求
     */
    private void deleteDocumentRequest(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            // 检查资料请求是否存在
            DocumentRequest documentRequest = documentRequestService.getDocumentRequestById(requestId);
            if (documentRequest == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 删除资料请求
            boolean success = documentRequestService.deleteDocumentRequest(requestId, deletedBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.success("资料请求删除成功"));
            } else {
                sendJsonResponse(response, ApiResponse.error("资料请求删除失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("删除资料请求失败: ID=" + requestId, e);
            sendJsonResponse(response, ApiResponse.error("资料请求删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索资料请求
     */
    private void searchDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String keyword = getParameter(request, "keyword", "");
            if (keyword.trim().isEmpty()) {
                sendJsonResponse(response, ApiResponse.error("搜索关键词不能为空"));
                return;
            }
            
            List<DocumentRequest> requests = documentRequestService.searchDocumentRequests(keyword);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("keyword", keyword);
            meta.put("count", requests.size());
            
            sendJsonResponse(response, ApiResponse.success(requests, meta));
            
        } catch (Exception e) {
            LogUtil.error("搜索资料请求失败", e);
            sendJsonResponse(response, ApiResponse.error("搜索资料请求失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取资料请求统计信息
     */
    private void getDocumentRequestStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层获取统计信息
            List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
            
            // 基本统计
            int total = requests.size();
            int pending = (int) requests.stream().filter(r -> "pending".equals(r.getStatus())).count();
            int processing = (int) requests.stream().filter(r -> "processing".equals(r.getStatus())).count();
            int completed = (int) requests.stream().filter(r -> "completed".equals(r.getStatus())).count();
            int cancelled = (int) requests.stream().filter(r -> "cancelled".equals(r.getStatus())).count();
            
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
            statistics.put("total", total);
            statistics.put("pending", pending);
            statistics.put("processing", processing);
            statistics.put("completed", completed);
            statistics.put("cancelled", cancelled);
            statistics.put("typeDistribution", typeCount);
            statistics.put("statusDistribution", statusCount);
            statistics.put("contactMethodDistribution", contactMethodCount);
            
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            LogUtil.error("获取资料请求统计失败", e);
            sendJsonResponse(response, ApiResponse.error("获取资料请求统计失败"));
        }
    }
    
    /**
     * 获取客户相关资料请求
     */
    private void getDocumentRequestsByCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 这里需要实现根据客户ID获取资料请求的方法
            // 暂时返回空列表
            List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
            // 注意：这里需要根据实际业务逻辑来过滤客户相关的请求
            // 暂时不过滤
            Map<String, Object> meta = new HashMap<>();
            meta.put("customerId", customerId);
            meta.put("count", requests.size());
            
            sendJsonResponse(response, ApiResponse.success(requests, meta));
            
        } catch (Exception e) {
            LogUtil.error("获取客户资料请求失败: customerId=" + customerId, e);
            sendJsonResponse(response, ApiResponse.error("获取客户资料请求失败"));
        }
    }
    
    /**
     * 获取请求类型列表
     */
    private void getRequestTypes(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            List<Map<String, Object>> types = new java.util.ArrayList<>();
            
            Map<String, Object> type1 = new HashMap<>();
            type1.put("code", "document");
            type1.put("name", "资料請求");
            type1.put("description", "商品資料や会社案内の請求");
            types.add(type1);
            
            Map<String, Object> type2 = new HashMap<>();
            type2.put("code", "temporary");
            type2.put("name", "仮申込");
            type2.put("description", "本申込前の仮の申込手続き");
            types.add(type2);
            
            Map<String, Object> type3 = new HashMap<>();
            type3.put("code", "consultation");
            type3.put("code", "相談");
            type3.put("description", "商品やサービスに関する相談");
            types.add(type3);
            
            Map<String, Object> type4 = new HashMap<>();
            type4.put("code", "other");
            type4.put("name", "その他");
            type4.put("description", "その他のお問い合わせ");
            types.add(type4);
            
            sendJsonResponse(response, ApiResponse.success(types));
            
        } catch (Exception e) {
            LogUtil.error("获取请求类型列表失败", e);
            sendJsonResponse(response, ApiResponse.error("获取请求类型列表失败"));
        }
    }
    
    /**
     * 更新资料请求状态
     */
    private void updateDocumentRequestStatus(HttpServletRequest request, HttpServletResponse response, int requestId) 
            throws ServletException, IOException {
        
        try {
            // 检查资料请求是否存在
            DocumentRequest existingRequest = documentRequestService.getDocumentRequestById(requestId);
            if (existingRequest == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取状态参数
            String status = request.getParameter("status");
            if (status == null || status.trim().isEmpty()) {
                sendJsonResponse(response, ApiResponse.error("状态参数不能为空"));
                return;
            }
            
            // 验证状态值
            if (!isValidStatus(status)) {
                sendJsonResponse(response, ApiResponse.error("无效的状态值"));
                return;
            }
            
            // 更新资料请求状态
            DocumentRequest documentRequest = new DocumentRequest();
            documentRequest.setId(requestId);
            documentRequest.setStatus(status);
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            boolean success = documentRequestService.updateDocumentRequest(documentRequest, updatedBy);
            
            if (success) {
                DocumentRequest updatedRequest = documentRequestService.getDocumentRequestById(requestId);
                sendJsonResponse(response, ApiResponse.success(updatedRequest));
            } else {
                sendJsonResponse(response, ApiResponse.error("状态更新失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("更新资料请求状态失败: ID=" + requestId, e);
            sendJsonResponse(response, ApiResponse.error("状态更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 验证状态值是否有效
     */
    private boolean isValidStatus(String status) {
        return "pending".equals(status) || 
               "processing".equals(status) || 
               "completed".equals(status) || 
               "cancelled".equals(status);
    }
    
    /**
     * 解析日期字符串
     */
    private java.util.Date parseDate(String dateStr) {
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
    
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    @Override
    protected boolean hasPermission(HttpServletRequest request) {
        com.insurance.model.User user = getCurrentUser(request);
        return user != null && (user.isAdmin() || user.isSales());
    }
}