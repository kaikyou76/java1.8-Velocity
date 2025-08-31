package com.insurance.api;

import com.insurance.model.Customer;
import com.insurance.service.CustomerService;
import com.insurance.util.LogUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户管理API
 * 提供客户数据的CRUD操作接口
 * 用于处理客户相关的API请求，包括客户的增删改查、搜索和统计等功能
 */
@WebServlet("/api/customers/*")
public class CustomerApiServlet extends ApiBaseServlet {
    
    // 客户服务对象，用于处理客户相关业务逻辑
    private CustomerService customerService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     */
    @Override
    public void init() {
        // 创建客户服务实例
        this.customerService = new CustomerService();
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
                    // 获取客户列表
                    getCustomers(request, response);
                } else if ("POST".equals(method)) {
                    // 创建新客户
                    createCustomer(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // ID路径处理（如/123）
                // 从路径中提取客户ID
                int customerId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    // 获取单个客户详情
                    getCustomer(request, response, customerId);
                } else if ("PUT".equals(method)) {
                    // 更新客户信息
                    updateCustomer(request, response, customerId);
                } else if ("DELETE".equals(method)) {
                    // 删除客户
                    deleteCustomer(request, response, customerId);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/search")) {
                // 搜索接口
                if ("GET".equals(method)) {
                    // 搜索客户
                    searchCustomers(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    // 获取客户统计信息
                    getCustomerStatistics(request, response);
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
            LogUtil.error("客户API处理异常: " + pathInfo, e);
            // 返回内部错误响应
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取客户列表
     * 获取所有客户信息，支持分页和排序
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int page = getIntParameter(request, "page", 1); // 页码，默认为1
            int size = getIntParameter(request, "size", 20); // 每页大小，默认为20
            String sortBy = getParameter(request, "sortBy", "id"); // 排序字段，默认为id
            String sortOrder = getParameter(request, "sortOrder", "asc"); // 排序顺序，默认为升序
            String status = getParameter(request, "status", ""); // 状态过滤条件
            
            // 调用服务层获取所有客户
            List<Customer> customers = customerService.getAllCustomers();
            
            // 简单分页处理
            int total = customers.size(); // 总记录数
            int from = (page - 1) * size; // 起始索引
            int to = Math.min(from + size, total); // 结束索引
            
            // 如果起始索引超出总记录数，则清空列表
            if (from >= total) {
                customers.clear();
            } else {
                // 截取当前页的数据
                customers = customers.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page); // 当前页码
            meta.put("size", size); // 每页大小
            meta.put("total", total); // 总记录数
            meta.put("pages", (int) Math.ceil((double) total / size)); // 总页数
            meta.put("sortBy", sortBy); // 排序字段
            meta.put("sortOrder", sortOrder); // 排序顺序
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(customers, meta));
            
        } catch (Exception e) {
            // 记录获取客户列表失败日志
            LogUtil.error("获取客户列表失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取客户列表失败"));
        }
    }
    
    /**
     * 获取单个客户详情
     * 根据客户ID获取客户详细信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层根据ID获取客户
            Customer customer = customerService.getCustomerById(customerId);
            
            // 如果客户存在，发送成功响应；否则发送未找到响应
            if (customer != null) {
                sendJsonResponse(response, ApiResponse.success(customer));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            // 记录获取客户详情失败日志
            LogUtil.error("获取客户详情失败: ID=" + customerId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取客户详情失败"));
        }
    }
    
    /**
     * 创建新客户
     * 根据请求参数创建新的客户
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void createCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "name", "email", "phone")) {
                // 缺少必需参数，发送错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String name = request.getParameter("name"); // 客户姓名
            String email = request.getParameter("email"); // 邮箱
            String phone = request.getParameter("phone"); // 电话
            String address = getParameter(request, "address", ""); // 地址
            String zipCode = getParameter(request, "zipCode", ""); // 邮政编码
            String birthDate = getParameter(request, "birthDate", ""); // 出生日期
            String gender = getParameter(request, "gender", "unknown"); // 性别
            String notes = getParameter(request, "notes", ""); // 备注
            
            // 创建客户对象
            Customer customer = new Customer();
            customer.setName(name); // 设置姓名
            customer.setEmail(email); // 设置邮箱
            customer.setPhone(phone); // 设置电话
            customer.setAddress(address); // 设置地址
            customer.setZipCode(zipCode); // 设置邮政编码
            customer.setBirthDate(birthDate); // 设置出生日期
            customer.setGender(gender); // 设置性别
            customer.setNotes(notes); // 设置备注
            customer.setStatus("active"); // 设置状态为活跃
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api"; // 创建者
            
            // 创建客户
            boolean success = customerService.createCustomer(customer, createdBy);
            
            // 根据创建结果发送相应响应
            if (success) {
                sendJsonResponse(response, ApiResponse.created(customer));
            } else {
                sendJsonResponse(response, ApiResponse.error("客户创建失败"));
            }
            
        } catch (Exception e) {
            // 记录创建客户失败日志
            LogUtil.error("创建客户失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("客户创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新客户信息
     * 根据客户ID和请求参数更新客户信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updateCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 检查客户是否存在
            Customer existingCustomer = customerService.getCustomerById(customerId);
            if (existingCustomer == null) {
                // 客户不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            String name = getParameter(request, "name", existingCustomer.getName()); // 姓名
            String email = getParameter(request, "email", existingCustomer.getEmail()); // 邮箱
            String phone = getParameter(request, "phone", existingCustomer.getPhone()); // 电话
            String address = getParameter(request, "address", existingCustomer.getAddress()); // 地址
            String zipCode = getParameter(request, "zipCode", existingCustomer.getZipCode()); // 邮政编码
            String birthDate = getParameter(request, "birthDate", existingCustomer.getBirthDate()); // 出生日期
            String gender = getParameter(request, "gender", existingCustomer.getGender()); // 性别
            String notes = getParameter(request, "notes", existingCustomer.getNotes()); // 备注
            String status = getParameter(request, "status", existingCustomer.getStatus()); // 状态
            
            // 更新客户对象
            Customer customer = new Customer();
            customer.setId(customerId); // 设置客户ID
            customer.setName(name); // 设置姓名
            customer.setEmail(email); // 设置邮箱
            customer.setPhone(phone); // 设置电话
            customer.setAddress(address); // 设置地址
            customer.setZipCode(zipCode); // 设置邮政编码
            customer.setBirthDate(birthDate); // 设置出生日期
            customer.setGender(gender); // 设置性别
            customer.setNotes(notes); // 设置备注
            customer.setStatus(status); // 设置状态
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api"; // 更新者
            
            // 更新客户
            boolean success = customerService.updateCustomer(customer, updatedBy);
            
            // 根据更新结果发送相应响应
            if (success) {
                // 更新成功，重新获取客户信息并发送成功响应
                Customer updatedCustomer = customerService.getCustomerById(customerId);
                sendJsonResponse(response, ApiResponse.success(updatedCustomer));
            } else {
                // 更新失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("客户更新失败"));
            }
            
        } catch (Exception e) {
            // 记录更新客户失败日志
            LogUtil.error("更新客户失败: ID=" + customerId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("客户更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除客户
     * 根据客户ID删除客户
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 检查客户是否存在
            Customer customer = customerService.getCustomerById(customerId);
            if (customer == null) {
                // 客户不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api"; // 删除者
            
            // 删除客户
            boolean success = customerService.deleteCustomer(customerId, deletedBy);
            
            // 根据删除结果发送相应响应
            if (success) {
                // 删除成功，发送成功响应
                sendJsonResponse(response, ApiResponse.success("客户删除成功"));
            } else {
                // 删除失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("客户删除失败"));
            }
            
        } catch (Exception e) {
            // 记录删除客户失败日志
            LogUtil.error("删除客户失败: ID=" + customerId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("客户删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索客户
     * 根据关键词搜索客户
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void searchCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取搜索关键词
            String keyword = getParameter(request, "keyword", "");
            if (keyword.trim().isEmpty()) {
                // 搜索关键词为空，发送错误响应
                sendJsonResponse(response, ApiResponse.error("搜索关键词不能为空"));
                return;
            }
            
            // 调用服务层搜索客户
            List<Customer> customers = customerService.searchCustomers(keyword);
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("keyword", keyword); // 搜索关键词
            meta.put("count", customers.size()); // 搜索结果数量
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(customers, meta));
            
        } catch (Exception e) {
            // 记录搜索客户失败日志
            LogUtil.error("搜索客户失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("搜索客户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取客户统计信息
     * 获取客户相关的统计信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getCustomerStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层获取所有客户
            List<Customer> customers = customerService.getAllCustomers();
            
            // 计算客户总数和活跃客户数
            int total = customers.size(); // 总客户数
            int active = (int) customers.stream().filter(c -> "active".equals(c.getStatus())).count(); // 活跃客户数
            int inactive = total - active; // 非活跃客户数
            
            // 按性别统计
            long male = customers.stream().filter(c -> "male".equals(c.getGender())).count(); // 男性客户数
            long female = customers.stream().filter(c -> "female".equals(c.getGender())).count(); // 女性客户数
            long unknown = total - male - female; // 性别未知客户数
            
            // 构建统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", total); // 总客户数
            statistics.put("active", active); // 活跃客户数
            statistics.put("inactive", inactive); // 非活跃客户数
            statistics.put("male", male); // 男性客户数
            statistics.put("female", female); // 女性客户数
            statistics.put("unknown", unknown); // 性别未知客户数
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            // 记录获取客户统计失败日志
            LogUtil.error("获取客户统计失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取客户统计失败"));
        }
    }
    
    /**
     * 检查是否需要权限
     * 客户API需要权限验证
     * @return 总是返回true，表示需要权限验证
     */
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    /**
     * 检查用户是否有权限访问
     * 只有管理员和销售人员才能访问客户API
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