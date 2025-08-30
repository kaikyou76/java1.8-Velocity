package com.insurance.api;

import com.insurance.model.Contract;
import com.insurance.service.ContractService;
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
 * 保险契约管理API
 * 提供保险契约的CRUD操作接口
 */
@WebServlet("/api/contracts/*")
public class ContractApiServlet extends ApiBaseServlet {
    
    private ContractService contractService;
    private SimpleDateFormat dateFormat;
    
    @Override
    public void init() {
        this.contractService = new ContractService();
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
                    getContracts(request, response);
                } else if ("POST".equals(method)) {
                    createContract(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // ID路径处理
                int contractId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    getContract(request, response, contractId);
                } else if ("PUT".equals(method)) {
                    updateContract(request, response, contractId);
                } else if ("DELETE".equals(method)) {
                    deleteContract(request, response, contractId);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/search")) {
                // 搜索接口
                if ("GET".equals(method)) {
                    searchContracts(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    getContractStatistics(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/customer/\\d+$")) {
                // 客户相关契约
                int customerId = Integer.parseInt(pathInfo.substring(10));
                if ("GET".equals(method)) {
                    getContractsByCustomer(request, response, customerId);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/premium")) {
                // 保险费计算接口
                if ("POST".equals(method)) {
                    calculatePremium(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
        } catch (Exception e) {
            LogUtil.error("契约API处理异常: " + pathInfo, e);
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取契约列表
     */
    private void getContracts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int page = getIntParameter(request, "page", 1);
            int size = getIntParameter(request, "size", 20);
            String status = getParameter(request, "status", "");
            String sortBy = getParameter(request, "sortBy", "id");
            String sortOrder = getParameter(request, "sortOrder", "desc");
            
            // 调用服务层
            List<Contract> contracts = contractService.getAllContracts();
            
            // 状态过滤
            if (!status.isEmpty()) {
                contracts.removeIf(contract -> !status.equals(contract.getStatus()));
            }
            
            // 简单分页处理
            int total = contracts.size();
            int from = (page - 1) * size;
            int to = Math.min(from + size, total);
            
            if (from >= total) {
                contracts.clear();
            } else {
                contracts = contracts.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page);
            meta.put("size", size);
            meta.put("total", total);
            meta.put("pages", (int) Math.ceil((double) total / size));
            meta.put("status", status);
            meta.put("sortBy", sortBy);
            meta.put("sortOrder", sortOrder);
            
            sendJsonResponse(response, ApiResponse.success(contracts, meta));
            
        } catch (Exception e) {
            LogUtil.error("获取契约列表失败", e);
            sendJsonResponse(response, ApiResponse.error("获取契约列表失败"));
        }
    }
    
    /**
     * 获取单个契约详情
     */
    private void getContract(HttpServletRequest request, HttpServletResponse response, int contractId) 
            throws ServletException, IOException {
        
        try {
            Contract contract = contractService.getContractById(contractId);
            
            if (contract != null) {
                sendJsonResponse(response, ApiResponse.success(contract));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            LogUtil.error("获取契约详情失败: ID=" + contractId, e);
            sendJsonResponse(response, ApiResponse.error("获取契约详情失败"));
        }
    }
    
    /**
     * 创建新契约
     */
    private void createContract(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "customerId", "productId", "contractNumber", "startDate", "endDate")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            int customerId = getIntParameter(request, "customerId", 0);
            int productId = getIntParameter(request, "productId", 0);
            String contractNumber = request.getParameter("contractNumber");
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            double premiumAmount = getDoubleParameter(request, "premiumAmount", 0.0);
            String paymentMethod = getParameter(request, "paymentMethod", "bank_transfer");
            String status = getParameter(request, "status", "active");
            String notes = getParameter(request, "notes", "");
            
            // 验证日期格式
            java.util.Date startDate = parseDate(startDateStr);
            java.util.Date endDate = parseDate(endDateStr);
            if (startDate == null || endDate == null) {
                sendJsonResponse(response, ApiResponse.error("日期格式不正确，请使用yyyy-MM-dd格式"));
                return;
            }
            
            // 创建契约对象
            Contract contract = new Contract();
            contract.setCustomerId(customerId);
            contract.setProductId(productId);
            contract.setContractNumber(contractNumber);
            contract.setStartDate(new java.sql.Date(startDate.getTime()));
            contract.setEndDate(new java.sql.Date(endDate.getTime()));
            contract.setPremiumAmount(premiumAmount);
            contract.setPaymentMethod(paymentMethod);
            contract.setStatus(status);
            contract.setNotes(notes);
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 创建契约
            boolean success = contractService.createContract(contract, createdBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.created(contract));
            } else {
                sendJsonResponse(response, ApiResponse.error("契约创建失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("创建契约失败", e);
            sendJsonResponse(response, ApiResponse.error("契约创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新契约信息
     */
    private void updateContract(HttpServletRequest request, HttpServletResponse response, int contractId) 
            throws ServletException, IOException {
        
        try {
            // 检查契约是否存在
            Contract existingContract = contractService.getContractById(contractId);
            if (existingContract == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            int customerId = getIntParameter(request, "customerId", existingContract.getCustomerId());
            int productId = getIntParameter(request, "productId", existingContract.getProductId());
            String contractNumber = getParameter(request, "contractNumber", existingContract.getContractNumber());
            String startDateStr = getParameter(request, "startDate", "");
            String endDateStr = getParameter(request, "endDate", "");
            double premiumAmount = getDoubleParameter(request, "premiumAmount", existingContract.getPremiumAmount());
            String paymentMethod = getParameter(request, "paymentMethod", existingContract.getPaymentMethod());
            String status = getParameter(request, "status", existingContract.getStatus());
            String notes = getParameter(request, "notes", existingContract.getNotes());
            
            // 更新契约对象
            Contract contract = new Contract();
            contract.setId(contractId);
            contract.setCustomerId(customerId);
            contract.setProductId(productId);
            contract.setContractNumber(contractNumber);
            
            // 处理日期更新
            if (!startDateStr.isEmpty()) {
                java.util.Date startDate = parseDate(startDateStr);
                if (startDate == null) {
                    sendJsonResponse(response, ApiResponse.error("开始日期格式不正确"));
                    return;
                }
                contract.setStartDate(new java.sql.Date(startDate.getTime()));
            } else {
                contract.setStartDate(existingContract.getStartDate());
            }
            
            if (!endDateStr.isEmpty()) {
                java.util.Date endDate = parseDate(endDateStr);
                if (endDate == null) {
                    sendJsonResponse(response, ApiResponse.error("结束日期格式不正确"));
                    return;
                }
                contract.setEndDate(new java.sql.Date(endDate.getTime()));
            } else {
                contract.setEndDate(existingContract.getEndDate());
            }
            
            contract.setPremiumAmount(premiumAmount);
            contract.setPaymentMethod(paymentMethod);
            contract.setStatus(status);
            contract.setNotes(notes);
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 更新契约
            boolean success = contractService.updateContract(contract, updatedBy);
            
            if (success) {
                Contract updatedContract = contractService.getContractById(contractId);
                sendJsonResponse(response, ApiResponse.success(updatedContract));
            } else {
                sendJsonResponse(response, ApiResponse.error("契约更新失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("更新契约失败: ID=" + contractId, e);
            sendJsonResponse(response, ApiResponse.error("契约更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除契约
     */
    private void deleteContract(HttpServletRequest request, HttpServletResponse response, int contractId) 
            throws ServletException, IOException {
        
        try {
            // 检查契约是否存在
            Contract contract = contractService.getContractById(contractId);
            if (contract == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 删除契约
            boolean success = contractService.deleteContract(contractId, deletedBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.success("契约删除成功"));
            } else {
                sendJsonResponse(response, ApiResponse.error("契约删除失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("删除契约失败: ID=" + contractId, e);
            sendJsonResponse(response, ApiResponse.error("契约删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索契约
     */
    private void searchContracts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String keyword = getParameter(request, "keyword", "");
            if (keyword.trim().isEmpty()) {
                sendJsonResponse(response, ApiResponse.error("搜索关键词不能为空"));
                return;
            }
            
            List<Contract> contracts = contractService.searchContracts(keyword);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("keyword", keyword);
            meta.put("count", contracts.size());
            
            sendJsonResponse(response, ApiResponse.success(contracts, meta));
            
        } catch (Exception e) {
            LogUtil.error("搜索契约失败", e);
            sendJsonResponse(response, ApiResponse.error("搜索契约失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取契约统计信息
     */
    private void getContractStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层获取统计信息
            List<Contract> contracts = contractService.getAllContracts();
            
            int total = contracts.size();
            int active = (int) contracts.stream().filter(c -> "active".equals(c.getStatus())).count();
            int expired = (int) contracts.stream().filter(c -> "expired".equals(c.getStatus())).count();
            int cancelled = (int) contracts.stream().filter(c -> "cancelled".equals(c.getStatus())).count();
            int pending = (int) contracts.stream().filter(c -> "pending".equals(c.getStatus())).count();
            
            // 保险费统计
            double totalPremium = contracts.stream()
                .filter(c -> "active".equals(c.getStatus()))
                .mapToDouble(Contract::getPremiumAmount)
                .sum();
            
            // 按支付方式统计
            long bankTransfer = contracts.stream().filter(c -> "bank_transfer".equals(c.getPaymentMethod())).count();
            long creditCard = contracts.stream().filter(c -> "credit_card".equals(c.getPaymentMethod())).count();
            long otherPayment = total - bankTransfer - creditCard;
            
            // 构建统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", total);
            statistics.put("active", active);
            statistics.put("expired", expired);
            statistics.put("cancelled", cancelled);
            statistics.put("pending", pending);
            statistics.put("totalPremium", totalPremium);
            statistics.put("bankTransfer", bankTransfer);
            statistics.put("creditCard", creditCard);
            statistics.put("otherPayment", otherPayment);
            
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            LogUtil.error("获取契约统计失败", e);
            sendJsonResponse(response, ApiResponse.error("获取契约统计失败"));
        }
    }
    
    /**
     * 获取客户相关契约
     */
    private void getContractsByCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 这里需要实现根据客户ID获取契约的方法
            // 暂时返回空列表
            List<Contract> contracts = contractService.getAllContracts();
            contracts.removeIf(contract -> contract.getCustomerId() != customerId);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("customerId", customerId);
            meta.put("count", contracts.size());
            
            sendJsonResponse(response, ApiResponse.success(contracts, meta));
            
        } catch (Exception e) {
            LogUtil.error("获取客户契约失败: customerId=" + customerId, e);
            sendJsonResponse(response, ApiResponse.error("获取客户契约失败"));
        }
    }
    
    /**
     * 计算保险费
     */
    private void calculatePremium(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "productId", "age", "coverageAmount")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取参数
            int productId = getIntParameter(request, "productId", 0);
            int age = getIntParameter(request, "age", 0);
            double coverageAmount = getDoubleParameter(request, "coverageAmount", 0.0);
            String gender = getParameter(request, "gender", "male");
            
            // 验证参数范围
            if (age <= 0 || age > 100) {
                sendJsonResponse(response, ApiResponse.error("年龄必须在1-100之间"));
                return;
            }
            
            if (coverageAmount <= 0) {
                sendJsonResponse(response, ApiResponse.error("保险金额必须大于0"));
                return;
            }
            
            // 简单的保险费计算逻辑
            double premium = calculatePremiumSimple(productId, age, coverageAmount, gender);
            
            Map<String, Object> result = new HashMap<>();
            result.put("productId", productId);
            result.put("age", age);
            result.put("coverageAmount", coverageAmount);
            result.put("gender", gender);
            result.put("calculatedPremium", premium);
            result.put("currency", "JPY");
            
            sendJsonResponse(response, ApiResponse.success(result));
            
        } catch (Exception e) {
            LogUtil.error("保险费计算失败", e);
            sendJsonResponse(response, ApiResponse.error("保险费计算失败: " + e.getMessage()));
        }
    }
    
    /**
     * 简单的保险费计算逻辑
     */
    private double calculatePremiumSimple(int productId, int age, double coverageAmount, String gender) {
        // 基础费率
        double baseRate = 0.001; // 0.1%
        
        // 年龄系数
        double ageFactor = 1.0;
        if (age < 20) ageFactor = 0.8;
        else if (age < 30) ageFactor = 0.9;
        else if (age < 40) ageFactor = 1.0;
        else if (age < 50) ageFactor = 1.2;
        else if (age < 60) ageFactor = 1.5;
        else ageFactor = 2.0;
        
        // 性别系数
        double genderFactor = "female".equals(gender) ? 0.9 : 1.0;
        
        // 产品系数
        double productFactor = getProductFactor(productId);
        
        // 计算保险费
        return coverageAmount * baseRate * ageFactor * genderFactor * productFactor;
    }
    
    /**
     * 获取产品系数
     */
    private double getProductFactor(int productId) {
        // 根据产品ID返回不同系数
        switch (productId) {
            case 1: return 1.0;  // 基本保险
            case 2: return 1.2;  // 高级保险
            case 3: return 0.8;  // 轻量保险
            case 4: return 1.5;  // 全面保险
            default: return 1.0;
        }
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
    
    /**
     * 获取双精度参数
     */
    private double getDoubleParameter(HttpServletRequest request, String name, double defaultValue) {
        try {
            return Double.parseDouble(request.getParameter(name));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    @Override
    protected boolean hasPermission(HttpServletRequest request) {
        com.insurance.model.User user = getCurrentUser(request);
        return user != null && (user.isAdmin() || user.isSales() || user.isReviewer());
    }
}