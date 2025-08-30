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
 */
@WebServlet("/api/customers/*")
public class CustomerApiServlet extends ApiBaseServlet {
    
    private CustomerService customerService;
    
    @Override
    public void init() {
        this.customerService = new CustomerService();
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
                    getCustomers(request, response);
                } else if ("POST".equals(method)) {
                    createCustomer(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // ID路径处理
                int customerId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    getCustomer(request, response, customerId);
                } else if ("PUT".equals(method)) {
                    updateCustomer(request, response, customerId);
                } else if ("DELETE".equals(method)) {
                    deleteCustomer(request, response, customerId);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/search")) {
                // 搜索接口
                if ("GET".equals(method)) {
                    searchCustomers(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    getCustomerStatistics(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
        } catch (Exception e) {
            LogUtil.error("客户API处理异常: " + pathInfo, e);
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取客户列表
     */
    private void getCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int page = getIntParameter(request, "page", 1);
            int size = getIntParameter(request, "size", 20);
            String sortBy = getParameter(request, "sortBy", "id");
            String sortOrder = getParameter(request, "sortOrder", "asc");
            String status = getParameter(request, "status", "");
            
            // 调用服务层
            List<Customer> customers = customerService.getAllCustomers();
            
            // 简单分页处理
            int total = customers.size();
            int from = (page - 1) * size;
            int to = Math.min(from + size, total);
            
            if (from >= total) {
                customers.clear();
            } else {
                customers = customers.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page);
            meta.put("size", size);
            meta.put("total", total);
            meta.put("pages", (int) Math.ceil((double) total / size));
            meta.put("sortBy", sortBy);
            meta.put("sortOrder", sortOrder);
            
            sendJsonResponse(response, ApiResponse.success(customers, meta));
            
        } catch (Exception e) {
            LogUtil.error("获取客户列表失败", e);
            sendJsonResponse(response, ApiResponse.error("获取客户列表失败"));
        }
    }
    
    /**
     * 获取单个客户详情
     */
    private void getCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            Customer customer = customerService.getCustomerById(customerId);
            
            if (customer != null) {
                sendJsonResponse(response, ApiResponse.success(customer));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            LogUtil.error("获取客户详情失败: ID=" + customerId, e);
            sendJsonResponse(response, ApiResponse.error("获取客户详情失败"));
        }
    }
    
    /**
     * 创建新客户
     */
    private void createCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "name", "email", "phone")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            String name = request.getParameter("name");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");
            String address = getParameter(request, "address", "");
            String zipCode = getParameter(request, "zipCode", "");
            String birthDate = getParameter(request, "birthDate", "");
            String gender = getParameter(request, "gender", "unknown");
            String notes = getParameter(request, "notes", "");
            
            // 创建客户对象
            Customer customer = new Customer();
            customer.setName(name);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAddress(address);
            customer.setZipCode(zipCode);
            customer.setBirthDate(birthDate);
            customer.setGender(gender);
            customer.setNotes(notes);
            customer.setStatus("active");
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 创建客户
            boolean success = customerService.createCustomer(customer, createdBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.created(customer));
            } else {
                sendJsonResponse(response, ApiResponse.error("客户创建失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("创建客户失败", e);
            sendJsonResponse(response, ApiResponse.error("客户创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新客户信息
     */
    private void updateCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 检查客户是否存在
            Customer existingCustomer = customerService.getCustomerById(customerId);
            if (existingCustomer == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            String name = getParameter(request, "name", existingCustomer.getName());
            String email = getParameter(request, "email", existingCustomer.getEmail());
            String phone = getParameter(request, "phone", existingCustomer.getPhone());
            String address = getParameter(request, "address", existingCustomer.getAddress());
            String zipCode = getParameter(request, "zipCode", existingCustomer.getZipCode());
            String birthDate = getParameter(request, "birthDate", existingCustomer.getBirthDate());
            String gender = getParameter(request, "gender", existingCustomer.getGender());
            String notes = getParameter(request, "notes", existingCustomer.getNotes());
            String status = getParameter(request, "status", existingCustomer.getStatus());
            
            // 更新客户对象
            Customer customer = new Customer();
            customer.setId(customerId);
            customer.setName(name);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAddress(address);
            customer.setZipCode(zipCode);
            customer.setBirthDate(birthDate);
            customer.setGender(gender);
            customer.setNotes(notes);
            customer.setStatus(status);
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 更新客户
            boolean success = customerService.updateCustomer(customer, updatedBy);
            
            if (success) {
                Customer updatedCustomer = customerService.getCustomerById(customerId);
                sendJsonResponse(response, ApiResponse.success(updatedCustomer));
            } else {
                sendJsonResponse(response, ApiResponse.error("客户更新失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("更新客户失败: ID=" + customerId, e);
            sendJsonResponse(response, ApiResponse.error("客户更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除客户
     */
    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 检查客户是否存在
            Customer customer = customerService.getCustomerById(customerId);
            if (customer == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 删除客户
            boolean success = customerService.deleteCustomer(customerId, deletedBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.success("客户删除成功"));
            } else {
                sendJsonResponse(response, ApiResponse.error("客户删除失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("删除客户失败: ID=" + customerId, e);
            sendJsonResponse(response, ApiResponse.error("客户删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索客户
     */
    private void searchCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String keyword = getParameter(request, "keyword", "");
            if (keyword.trim().isEmpty()) {
                sendJsonResponse(response, ApiResponse.error("搜索关键词不能为空"));
                return;
            }
            
            List<Customer> customers = customerService.searchCustomers(keyword);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("keyword", keyword);
            meta.put("count", customers.size());
            
            sendJsonResponse(response, ApiResponse.success(customers, meta));
            
        } catch (Exception e) {
            LogUtil.error("搜索客户失败", e);
            sendJsonResponse(response, ApiResponse.error("搜索客户失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取客户统计信息
     */
    private void getCustomerStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层获取统计信息
            List<Customer> customers = customerService.getAllCustomers();
            
            int total = customers.size();
            int active = (int) customers.stream().filter(c -> "active".equals(c.getStatus())).count();
            int inactive = total - active;
            
            // 按性别统计
            long male = customers.stream().filter(c -> "male".equals(c.getGender())).count();
            long female = customers.stream().filter(c -> "female".equals(c.getGender())).count();
            long unknown = total - male - female;
            
            // 构建统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", total);
            statistics.put("active", active);
            statistics.put("inactive", inactive);
            statistics.put("male", male);
            statistics.put("female", female);
            statistics.put("unknown", unknown);
            
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            LogUtil.error("获取客户统计失败", e);
            sendJsonResponse(response, ApiResponse.error("获取客户统计失败"));
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