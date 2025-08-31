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
 * 用于处理保险契约相关的API请求，包括契约的增删改查、搜索、统计和保险费计算等功能
 */
@WebServlet("/api/contracts/*")
public class ContractApiServlet extends ApiBaseServlet {
    
    // 契约服务对象，用于处理契约相关业务逻辑
    private ContractService contractService;
    // 日期格式化对象，用于解析和格式化日期字符串
    private SimpleDateFormat dateFormat;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     */
    @Override
    public void init() {
        // 创建契约服务实例
        this.contractService = new ContractService();
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
                    // 获取契约列表
                    getContracts(request, response);
                } else if ("POST".equals(method)) {
                    // 创建新契约
                    createContract(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // ID路径处理（如/123）
                // 从路径中提取契约ID
                int contractId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    // 获取单个契约详情
                    getContract(request, response, contractId);
                } else if ("PUT".equals(method)) {
                    // 更新契约信息
                    updateContract(request, response, contractId);
                } else if ("DELETE".equals(method)) {
                    // 删除契约
                    deleteContract(request, response, contractId);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/search")) {
                // 搜索接口
                if ("GET".equals(method)) {
                    // 搜索契约
                    searchContracts(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    // 获取契约统计信息
                    getContractStatistics(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/customer/\\d+$")) {
                // 客户相关契约（如/customer/123）
                // 从路径中提取客户ID
                int customerId = Integer.parseInt(pathInfo.substring(10));
                if ("GET".equals(method)) {
                    // 获取客户相关契约
                    getContractsByCustomer(request, response, customerId);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/premium")) {
                // 保险费计算接口
                if ("POST".equals(method)) {
                    // 计算保险费
                    calculatePremium(request, response);
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
            LogUtil.error("契约API处理异常: " + pathInfo, e);
            // 返回内部错误响应
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取契约列表
     * 获取所有契约信息，支持分页、状态过滤和排序
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getContracts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int page = getIntParameter(request, "page", 1); // 页码，默认为1
            int size = getIntParameter(request, "size", 20); // 每页大小，默认为20
            String status = getParameter(request, "status", ""); // 状态过滤条件
            String sortBy = getParameter(request, "sortBy", "id"); // 排序字段，默认为id
            String sortOrder = getParameter(request, "sortOrder", "desc"); // 排序顺序，默认为降序
            
            // 调用服务层获取所有契约
            List<Contract> contracts = contractService.getAllContracts();
            
            // 状态过滤
            if (!status.isEmpty()) {
                // 移除不匹配状态的契约
                contracts.removeIf(contract -> !status.equals(contract.getStatus()));
            }
            
            // 简单分页处理
            int total = contracts.size(); // 总记录数
            int from = (page - 1) * size; // 起始索引
            int to = Math.min(from + size, total); // 结束索引
            
            // 如果起始索引超出总记录数，则清空列表
            if (from >= total) {
                contracts.clear();
            } else {
                // 截取当前页的数据
                contracts = contracts.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page); // 当前页码
            meta.put("size", size); // 每页大小
            meta.put("total", total); // 总记录数
            meta.put("pages", (int) Math.ceil((double) total / size)); // 总页数
            meta.put("status", status); // 状态过滤条件
            meta.put("sortBy", sortBy); // 排序字段
            meta.put("sortOrder", sortOrder); // 排序顺序
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(contracts, meta));
            
        } catch (Exception e) {
            // 记录获取契约列表失败日志
            LogUtil.error("获取契约列表失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取契约列表失败"));
        }
    }
    
    /**
     * 获取单个契约详情
     * 根据契约ID获取契约详细信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param contractId 契约ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getContract(HttpServletRequest request, HttpServletResponse response, int contractId) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层根据ID获取契约
            Contract contract = contractService.getContractById(contractId);
            
            // 如果契约存在，发送成功响应；否则发送未找到响应
            if (contract != null) {
                sendJsonResponse(response, ApiResponse.success(contract));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            // 记录获取契约详情失败日志
            LogUtil.error("获取契约详情失败: ID=" + contractId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取契约详情失败"));
        }
    }
    
    /**
     * 创建新契约
     * 根据请求参数创建新的契约
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void createContract(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "customerId", "productId", "contractNumber", "startDate", "endDate")) {
                // 缺少必需参数，发送错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            int customerId = getIntParameter(request, "customerId", 0); // 客户ID
            int productId = getIntParameter(request, "productId", 0); // 产品ID
            String contractNumber = request.getParameter("contractNumber"); // 契约编号
            String startDateStr = request.getParameter("startDate"); // 开始日期字符串
            String endDateStr = request.getParameter("endDate"); // 结束日期字符串
            double premiumAmount = getDoubleParameter(request, "premiumAmount", 0.0); // 保险费金额
            String paymentMethod = getParameter(request, "paymentMethod", "bank_transfer"); // 支付方式
            String status = getParameter(request, "status", "active"); // 状态
            String notes = getParameter(request, "notes", ""); // 备注
            
            // 验证日期格式
            java.util.Date startDate = parseDate(startDateStr); // 解析开始日期
            java.util.Date endDate = parseDate(endDateStr); // 解析结束日期
            if (startDate == null || endDate == null) {
                // 日期格式不正确，发送错误响应
                sendJsonResponse(response, ApiResponse.error("日期格式不正确，请使用yyyy-MM-dd格式"));
                return;
            }
            
            // 创建契约对象
            Contract contract = new Contract();
            contract.setCustomerId(customerId); // 设置客户ID
            contract.setProductId(productId); // 设置产品ID
            contract.setContractNumber(contractNumber); // 设置契约编号
            contract.setStartDate(new java.sql.Date(startDate.getTime())); // 设置开始日期
            contract.setEndDate(new java.sql.Date(endDate.getTime())); // 设置结束日期
            contract.setPremiumAmount(premiumAmount); // 设置保险费金额
            contract.setPaymentMethod(paymentMethod); // 设置支付方式
            contract.setStatus(status); // 设置状态
            contract.setNotes(notes); // 设置备注
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api"; // 创建者
            
            // 创建契约
            boolean success = contractService.createContract(contract, createdBy);
            
            // 根据创建结果发送相应响应
            if (success) {
                sendJsonResponse(response, ApiResponse.created(contract));
            } else {
                sendJsonResponse(response, ApiResponse.error("契约创建失败"));
            }
            
        } catch (Exception e) {
            // 记录创建契约失败日志
            LogUtil.error("创建契约失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("契约创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新契约信息
     * 根据契约ID和请求参数更新契约信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param contractId 契约ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updateContract(HttpServletRequest request, HttpServletResponse response, int contractId) 
            throws ServletException, IOException {
        
        try {
            // 检查契约是否存在
            Contract existingContract = contractService.getContractById(contractId);
            if (existingContract == null) {
                // 契约不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            int customerId = getIntParameter(request, "customerId", existingContract.getCustomerId()); // 客户ID
            int productId = getIntParameter(request, "productId", existingContract.getProductId()); // 产品ID
            String contractNumber = getParameter(request, "contractNumber", existingContract.getContractNumber()); // 契约编号
            String startDateStr = getParameter(request, "startDate", ""); // 开始日期字符串
            String endDateStr = getParameter(request, "endDate", ""); // 结束日期字符串
            double premiumAmount = getDoubleParameter(request, "premiumAmount", existingContract.getPremiumAmount()); // 保险费金额
            String paymentMethod = getParameter(request, "paymentMethod", existingContract.getPaymentMethod()); // 支付方式
            String status = getParameter(request, "status", existingContract.getStatus()); // 状态
            String notes = getParameter(request, "notes", existingContract.getNotes()); // 备注
            
            // 更新契约对象
            Contract contract = new Contract();
            contract.setId(contractId); // 设置契约ID
            contract.setCustomerId(customerId); // 设置客户ID
            contract.setProductId(productId); // 设置产品ID
            contract.setContractNumber(contractNumber); // 设置契约编号
            
            // 处理日期更新
            if (!startDateStr.isEmpty()) {
                // 如果提供了开始日期，则解析并设置
                java.util.Date startDate = parseDate(startDateStr);
                if (startDate == null) {
                    // 开始日期格式不正确，发送错误响应
                    sendJsonResponse(response, ApiResponse.error("开始日期格式不正确"));
                    return;
                }
                contract.setStartDate(new java.sql.Date(startDate.getTime()));
            } else {
                // 如果未提供开始日期，则使用原有日期
                contract.setStartDate(existingContract.getStartDate());
            }
            
            if (!endDateStr.isEmpty()) {
                // 如果提供了结束日期，则解析并设置
                java.util.Date endDate = parseDate(endDateStr);
                if (endDate == null) {
                    // 结束日期格式不正确，发送错误响应
                    sendJsonResponse(response, ApiResponse.error("结束日期格式不正确"));
                    return;
                }
                contract.setEndDate(new java.sql.Date(endDate.getTime()));
            } else {
                // 如果未提供结束日期，则使用原有日期
                contract.setEndDate(existingContract.getEndDate());
            }
            
            contract.setPremiumAmount(premiumAmount); // 设置保险费金额
            contract.setPaymentMethod(paymentMethod); // 设置支付方式
            contract.setStatus(status); // 设置状态
            contract.setNotes(notes); // 设置备注
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api"; // 更新者
            
            // 更新契约
            boolean success = contractService.updateContract(contract, updatedBy);
            
            // 根据更新结果发送相应响应
            if (success) {
                // 更新成功，重新获取契约信息并发送成功响应
                Contract updatedContract = contractService.getContractById(contractId);
                sendJsonResponse(response, ApiResponse.success(updatedContract));
            } else {
                // 更新失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("契约更新失败"));
            }
            
        } catch (Exception e) {
            // 记录更新契约失败日志
            LogUtil.error("更新契约失败: ID=" + contractId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("契约更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除契约
     * 根据契约ID删除契约
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param contractId 契约ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void deleteContract(HttpServletRequest request, HttpServletResponse response, int contractId) 
            throws ServletException, IOException {
        
        try {
            // 检查契约是否存在
            Contract contract = contractService.getContractById(contractId);
            if (contract == null) {
                // 契约不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api"; // 删除者
            
            // 删除契约
            boolean success = contractService.deleteContract(contractId, deletedBy);
            
            // 根据删除结果发送相应响应
            if (success) {
                // 删除成功，发送成功响应
                sendJsonResponse(response, ApiResponse.success("契约删除成功"));
            } else {
                // 删除失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("契约删除失败"));
            }
            
        } catch (Exception e) {
            // 记录删除契约失败日志
            LogUtil.error("删除契约失败: ID=" + contractId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("契约删除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 搜索契约
     * 根据关键词搜索契约
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void searchContracts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取搜索关键词
            String keyword = getParameter(request, "keyword", "");
            if (keyword.trim().isEmpty()) {
                // 搜索关键词为空，发送错误响应
                sendJsonResponse(response, ApiResponse.error("搜索关键词不能为空"));
                return;
            }
            
            // 调用服务层搜索契约
            List<Contract> contracts = contractService.searchContracts(keyword);
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("keyword", keyword); // 搜索关键词
            meta.put("count", contracts.size()); // 搜索结果数量
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(contracts, meta));
            
        } catch (Exception e) {
            // 记录搜索契约失败日志
            LogUtil.error("搜索契约失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("搜索契约失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取契约统计信息
     * 获取契约相关的统计信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getContractStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层获取所有契约
            List<Contract> contracts = contractService.getAllContracts();
            
            // 计算各种状态的契约数量
            int total = contracts.size(); // 总契约数
            int active = (int) contracts.stream().filter(c -> "active".equals(c.getStatus())).count(); // 活跃契约数
            int expired = (int) contracts.stream().filter(c -> "expired".equals(c.getStatus())).count(); // 过期契约数
            int cancelled = (int) contracts.stream().filter(c -> "cancelled".equals(c.getStatus())).count(); // 取消契约数
            int pending = (int) contracts.stream().filter(c -> "pending".equals(c.getStatus())).count(); // 待处理契约数
            
            // 保险费统计
            double totalPremium = contracts.stream()
                .filter(c -> "active".equals(c.getStatus())) // 只统计活跃契约
                .mapToDouble(Contract::getPremiumAmount) // 获取保险费金额
                .sum(); // 计算总和
            
            // 按支付方式统计
            long bankTransfer = contracts.stream().filter(c -> "bank_transfer".equals(c.getPaymentMethod())).count(); // 银行转账契约数
            long creditCard = contracts.stream().filter(c -> "credit_card".equals(c.getPaymentMethod())).count(); // 信用卡契约数
            long otherPayment = total - bankTransfer - creditCard; // 其他支付方式契约数
            
            // 构建统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", total); // 总契约数
            statistics.put("active", active); // 活跃契约数
            statistics.put("expired", expired); // 过期契约数
            statistics.put("cancelled", cancelled); // 取消契约数
            statistics.put("pending", pending); // 待处理契约数
            statistics.put("totalPremium", totalPremium); // 总保险费
            statistics.put("bankTransfer", bankTransfer); // 银行转账契约数
            statistics.put("creditCard", creditCard); // 信用卡契约数
            statistics.put("otherPayment", otherPayment); // 其他支付方式契约数
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            // 记录获取契约统计失败日志
            LogUtil.error("获取契约统计失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取契约统计失败"));
        }
    }
    
    /**
     * 获取客户相关契约
     * 根据客户ID获取该客户的所有契约
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param customerId 客户ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getContractsByCustomer(HttpServletRequest request, HttpServletResponse response, int customerId) 
            throws ServletException, IOException {
        
        try {
            // 这里需要实现根据客户ID获取契约的方法
            // 暂时返回空列表
            // 调用服务层获取所有契约
            List<Contract> contracts = contractService.getAllContracts();
            // 移除不属于指定客户的契约
            contracts.removeIf(contract -> contract.getCustomerId() != customerId);
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("customerId", customerId); // 客户ID
            meta.put("count", contracts.size()); // 契约数量
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(contracts, meta));
            
        } catch (Exception e) {
            // 记录获取客户契约失败日志
            LogUtil.error("获取客户契约失败: customerId=" + customerId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取客户契约失败"));
        }
    }
    
    /**
     * 计算保险费
     * 根据请求参数计算保险费
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void calculatePremium(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "productId", "age", "coverageAmount")) {
                // 缺少必需参数，发送错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取参数
            int productId = getIntParameter(request, "productId", 0); // 产品ID
            int age = getIntParameter(request, "age", 0); // 年龄
            double coverageAmount = getDoubleParameter(request, "coverageAmount", 0.0); // 保险金额
            String gender = getParameter(request, "gender", "male"); // 性别
            
            // 验证参数范围
            if (age <= 0 || age > 100) {
                // 年龄不在有效范围内，发送错误响应
                sendJsonResponse(response, ApiResponse.error("年龄必须在1-100之间"));
                return;
            }
            
            if (coverageAmount <= 0) {
                // 保险金额必须大于0，发送错误响应
                sendJsonResponse(response, ApiResponse.error("保险金额必须大于0"));
                return;
            }
            
            // 简单的保险费计算逻辑
            double premium = calculatePremiumSimple(productId, age, coverageAmount, gender);
            
            // 构建计算结果
            Map<String, Object> result = new HashMap<>();
            result.put("productId", productId); // 产品ID
            result.put("age", age); // 年龄
            result.put("coverageAmount", coverageAmount); // 保险金额
            result.put("gender", gender); // 性别
            result.put("calculatedPremium", premium); // 计算出的保险费
            result.put("currency", "JPY"); // 货币单位
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(result));
            
        } catch (Exception e) {
            // 记录保险费计算失败日志
            LogUtil.error("保险费计算失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("保险费计算失败: " + e.getMessage()));
        }
    }
    
    /**
     * 简单的保险费计算逻辑
     * 根据产品ID、年龄、保险金额和性别计算保险费
     * @param productId 产品ID
     * @param age 年龄
     * @param coverageAmount 保险金额
     * @param gender 性别
     * @return 计算出的保险费
     */
    private double calculatePremiumSimple(int productId, int age, double coverageAmount, String gender) {
        // 基础费率
        double baseRate = 0.001; // 0.1%
        
        // 年龄系数
        double ageFactor = 1.0;
        if (age < 20) ageFactor = 0.8; // 20岁以下系数为0.8
        else if (age < 30) ageFactor = 0.9; // 20-29岁系数为0.9
        else if (age < 40) ageFactor = 1.0; // 30-39岁系数为1.0
        else if (age < 50) ageFactor = 1.2; // 40-49岁系数为1.2
        else if (age < 60) ageFactor = 1.5; // 50-59岁系数为1.5
        else ageFactor = 2.0; // 60岁以上系数为2.0
        
        // 性别系数
        double genderFactor = "female".equals(gender) ? 0.9 : 1.0; // 女性系数为0.9，男性系数为1.0
        
        // 产品系数
        double productFactor = getProductFactor(productId); // 根据产品ID获取产品系数
        
        // 计算保险费
        return coverageAmount * baseRate * ageFactor * genderFactor * productFactor;
    }
    
    /**
     * 获取产品系数
     * 根据产品ID返回相应的产品系数
     * @param productId 产品ID
     * @return 产品系数
     */
    private double getProductFactor(int productId) {
        // 根据产品ID返回不同系数
        switch (productId) {
            case 1: return 1.0;  // 基本保险
            case 2: return 1.2;  // 高级保险
            case 3: return 0.8;  // 轻量保险
            case 4: return 1.5;  // 全面保险
            default: return 1.0; // 默认系数
        }
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
     * 获取双精度参数
     * 获取请求参数并转换为double类型
     * @param request HTTP请求对象
     * @param name 参数名称
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    private double getDoubleParameter(HttpServletRequest request, String name, double defaultValue) {
        try {
            // 尝试将参数值转换为double类型
            return Double.parseDouble(request.getParameter(name));
        } catch (NumberFormatException e) {
            // 转换失败返回默认值
            return defaultValue;
        }
    }
    
    /**
     * 检查是否需要权限
     * 契约API需要权限验证
     * @return 总是返回true，表示需要权限验证
     */
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    /**
     * 检查用户是否有权限访问
     * 只有管理员、销售人员和审核人员才能访问契约API
     * @param request HTTP请求对象
     * @return 如果用户有权限返回true，否则返回false
     */
    @Override
    protected boolean hasPermission(HttpServletRequest request) {
        // 获取当前用户
        com.insurance.model.User user = getCurrentUser(request);
        // 检查用户是否为管理员、销售人员或审核人员
        return user != null && (user.isAdmin() || user.isSales() || user.isReviewer());
    }
}