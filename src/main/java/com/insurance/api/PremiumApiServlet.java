package com.insurance.api;

import com.insurance.model.PremiumRate;
import com.insurance.service.PremiumCalculatorService;
import com.insurance.util.LogUtil;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保险料率计算API
 * 提供保险费计算和料率管理接口
 * 用于处理保险费率相关的API请求，包括费率的增删改查、保险费计算、批量计算和统计等功能
 */
@WebServlet("/api/premium/*")
public class PremiumApiServlet extends ApiBaseServlet {
    
    // 保险费计算服务对象，用于处理保险费计算和费率管理相关业务逻辑
    private PremiumCalculatorService premiumService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     */
    @Override
    public void init() {
        // 创建保险费计算服务实例
        this.premiumService = new PremiumCalculatorService();
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
                    // 获取料率列表
                    getPremiumRates(request, response);
                } else if ("POST".equals(method)) {
                    // 计算保险费
                    calculatePremium(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/calculate")) {
                // 计算接口
                if ("POST".equals(method)) {
                    // 计算保险费
                    calculatePremium(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/batch-calculate")) {
                // 批量计算接口
                if ("POST".equals(method)) {
                    // 批量计算保险费
                    batchCalculatePremium(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // 料率ID路径处理（如/123）
                // 从路径中提取料率ID
                int rateId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    // 获取单个料率详情
                    getPremiumRate(request, response, rateId);
                } else if ("PUT".equals(method)) {
                    // 更新料率
                    updatePremiumRate(request, response, rateId);
                } else if ("DELETE".equals(method)) {
                    // 删除料率
                    deletePremiumRate(request, response, rateId);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/rates")) {
                // 料率列表
                if ("GET".equals(method)) {
                    // 获取料率列表
                    getPremiumRates(request, response);
                } else if ("POST".equals(method)) {
                    // 创建新料率
                    createPremiumRate(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    // 获取料率统计信息
                    getPremiumStatistics(request, response);
                } else {
                    // 不支持的HTTP方法
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/products")) {
                // 产品列表
                if ("GET".equals(method)) {
                    // 获取产品列表
                    getProducts(request, response);
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
            LogUtil.error("料率API处理异常: " + pathInfo, e);
            // 返回内部错误响应
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取料率列表
     * 获取所有料率信息，支持产品过滤、活跃状态过滤和分页
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getPremiumRates(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int productId = getIntParameter(request, "productId", 0); // 产品ID过滤条件
            int page = getIntParameter(request, "page", 1); // 页码，默认为1
            int size = getIntParameter(request, "size", 20); // 每页大小，默认为20
            String activeOnly = getParameter(request, "activeOnly", "false"); // 仅活跃料率过滤条件
            
            // 获取料率列表
            List<PremiumRate> rates = premiumService.getPremiumRates();
            
            // 产品过滤
            if (productId > 0) {
                // 移除不匹配产品ID的料率
                rates.removeIf(rate -> rate.getProductId() != productId);
            }
            
            // 活跃料率过滤
            if ("true".equals(activeOnly)) {
                // 移除非活跃的料率
                rates.removeIf(rate -> !rate.isActive());
            }
            
            // 分页处理
            int total = rates.size(); // 总记录数
            int from = (page - 1) * size; // 起始索引
            int to = Math.min(from + size, total); // 结束索引
            
            // 如果起始索引超出总记录数，则清空列表
            if (from >= total) {
                rates.clear();
            } else {
                // 截取当前页的数据
                rates = rates.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page); // 当前页码
            meta.put("size", size); // 每页大小
            meta.put("total", total); // 总记录数
            meta.put("pages", (int) Math.ceil((double) total / size)); // 总页数
            meta.put("productId", productId > 0 ? productId : "all"); // 产品ID过滤条件
            meta.put("activeOnly", activeOnly); // 仅活跃料率过滤条件
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(rates, meta));
            
        } catch (Exception e) {
            // 记录获取料率列表失败日志
            LogUtil.error("获取料率列表失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取料率列表失败"));
        }
    }
    
    /**
     * 获取单个料率详情
     * 根据料率ID获取料率详细信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param rateId 料率ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getPremiumRate(HttpServletRequest request, HttpServletResponse response, int rateId) 
            throws ServletException, IOException {
        
        try {
            // 调用服务层根据ID获取料率
            PremiumRate rate = premiumService.getPremiumRateById(rateId);
            
            // 如果料率存在，发送成功响应；否则发送未找到响应
            if (rate != null) {
                sendJsonResponse(response, ApiResponse.success(rate));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            // 记录获取料率详情失败日志
            LogUtil.error("获取料率详情失败: ID=" + rateId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取料率详情失败"));
        }
    }
    
    /**
     * 创建新料率
     * 根据请求参数创建新的料率
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void createPremiumRate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "productId", "minAge", "maxAge", "baseRate")) {
                // 缺少必需参数，发送错误响应
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            int productId = getIntParameter(request, "productId", 0); // 产品ID
            int minAge = getIntParameter(request, "minAge", 0); // 最小年龄
            int maxAge = getIntParameter(request, "maxAge", 0); // 最大年龄
            double baseRate = getDoubleParameter(request, "baseRate", 0.0); // 基础费率
            String gender = getParameter(request, "gender", "all"); // 性别
            String description = getParameter(request, "description", ""); // 描述
            String effectiveDate = getParameter(request, "effectiveDate", ""); // 生效日期
            String expiryDate = getParameter(request, "expiryDate", ""); // 到期日期
            boolean active = getBooleanParameter(request, "active", true); // 是否活跃
            
            // 验证参数
            if (minAge <= 0 || maxAge <= 0 || minAge > maxAge) {
                // 年龄范围不正确，发送错误响应
                sendJsonResponse(response, ApiResponse.error("年龄范围不正确"));
                return;
            }
            
            if (baseRate <= 0) {
                // 基础料率必须大于0，发送错误响应
                sendJsonResponse(response, ApiResponse.error("基础料率必须大于0"));
                return;
            }
            
            // 创建料率对象
            PremiumRate rate = new PremiumRate();
            rate.setProductId(productId); // 设置产品ID
            rate.setMinAge(minAge); // 设置最小年龄
            rate.setMaxAge(maxAge); // 设置最大年龄
            rate.setBaseRate(baseRate); // 设置基础费率
            rate.setGender(gender); // 设置性别
            rate.setDescription(description); // 设置描述
            rate.setEffectiveDate(effectiveDate); // 设置生效日期
            rate.setExpiryDate(expiryDate); // 设置到期日期
            rate.setActive(active); // 设置是否活跃
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api"; // 创建者
            
            // 创建料率
            boolean success = premiumService.createPremiumRate(rate, createdBy);
            
            // 根据创建结果发送相应响应
            if (success) {
                sendJsonResponse(response, ApiResponse.created(rate));
            } else {
                sendJsonResponse(response, ApiResponse.error("料率创建失败"));
            }
            
        } catch (Exception e) {
            // 记录创建料率失败日志
            LogUtil.error("创建料率失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("料率创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新料率
     * 根据料率ID和请求参数更新料率信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param rateId 料率ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void updatePremiumRate(HttpServletRequest request, HttpServletResponse response, int rateId) 
            throws ServletException, IOException {
        
        try {
            // 检查料率是否存在
            PremiumRate existingRate = premiumService.getPremiumRateById(rateId);
            if (existingRate == null) {
                // 料率不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            int productId = getIntParameter(request, "productId", existingRate.getProductId()); // 产品ID
            int minAge = getIntParameter(request, "minAge", existingRate.getMinAge()); // 最小年龄
            int maxAge = getIntParameter(request, "maxAge", existingRate.getMaxAge()); // 最大年龄
            double baseRate = getDoubleParameter(request, "baseRate", existingRate.getBaseRate()); // 基础费率
            String gender = getParameter(request, "gender", existingRate.getGender()); // 性别
            String description = getParameter(request, "description", existingRate.getDescription()); // 描述
            String effectiveDate = getParameter(request, "effectiveDate", existingRate.getEffectiveDate()); // 生效日期
            String expiryDate = getParameter(request, "expiryDate", existingRate.getExpiryDate()); // 到期日期
            boolean active = getBooleanParameter(request, "active", existingRate.isActive()); // 是否活跃
            
            // 验证参数
            if (minAge <= 0 || maxAge <= 0 || minAge > maxAge) {
                // 年龄范围不正确，发送错误响应
                sendJsonResponse(response, ApiResponse.error("年龄范围不正确"));
                return;
            }
            
            if (baseRate <= 0) {
                // 基础料率必须大于0，发送错误响应
                sendJsonResponse(response, ApiResponse.error("基础料率必须大于0"));
                return;
            }
            
            // 更新料率对象
            PremiumRate rate = new PremiumRate();
            rate.setId(rateId); // 设置料率ID
            rate.setProductId(productId); // 设置产品ID
            rate.setMinAge(minAge); // 设置最小年龄
            rate.setMaxAge(maxAge); // 设置最大年龄
            rate.setBaseRate(baseRate); // 设置基础费率
            rate.setGender(gender); // 设置性别
            rate.setDescription(description); // 设置描述
            rate.setEffectiveDate(effectiveDate); // 设置生效日期
            rate.setExpiryDate(expiryDate); // 设置到期日期
            rate.setActive(active); // 设置是否活跃
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api"; // 更新者
            
            // 更新料率
            boolean success = premiumService.updatePremiumRate(rate, updatedBy);
            
            // 根据更新结果发送相应响应
            if (success) {
                // 更新成功，重新获取料率信息并发送成功响应
                PremiumRate updatedRate = premiumService.getPremiumRateById(rateId);
                sendJsonResponse(response, ApiResponse.success(updatedRate));
            } else {
                // 更新失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("料率更新失败"));
            }
            
        } catch (Exception e) {
            // 记录更新料率失败日志
            LogUtil.error("更新料率失败: ID=" + rateId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("料率更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除料率
     * 根据料率ID删除料率
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param rateId 料率ID
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void deletePremiumRate(HttpServletRequest request, HttpServletResponse response, int rateId) 
            throws ServletException, IOException {
        
        try {
            // 检查料率是否存在
            PremiumRate rate = premiumService.getPremiumRateById(rateId);
            if (rate == null) {
                // 料率不存在，发送未找到响应
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api"; // 删除者
            
            // 删除料率
            boolean success = premiumService.deletePremiumRate(rateId, deletedBy);
            
            // 根据删除结果发送相应响应
            if (success) {
                // 删除成功，发送成功响应
                sendJsonResponse(response, ApiResponse.success("料率删除成功"));
            } else {
                // 删除失败，发送错误响应
                sendJsonResponse(response, ApiResponse.error("料率删除失败"));
            }
            
        } catch (Exception e) {
            // 记录删除料率失败日志
            LogUtil.error("删除料率失败: ID=" + rateId, e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("料率删除失败: " + e.getMessage()));
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
            
            // 获取请求参数
            int productId = getIntParameter(request, "productId", 0); // 产品ID
            int age = getIntParameter(request, "age", 0); // 年龄
            double coverageAmount = getDoubleParameter(request, "coverageAmount", 0.0); // 保险金额
            String gender = getParameter(request, "gender", "all"); // 性别
            String paymentMethod = getParameter(request, "paymentMethod", "annual"); // 缴费方式
            
            // 验证参数
            if (age <= 0 || age > 120) {
                // 年龄必须在1-120之间，发送错误响应
                sendJsonResponse(response, ApiResponse.error("年龄必须在1-120之间"));
                return;
            }
            
            if (coverageAmount <= 0) {
                // 保险金额必须大于0，发送错误响应
                sendJsonResponse(response, ApiResponse.error("保险金额必须大于0"));
                return;
            }
            
            // 计算保险费
            Map<String, Object> result = premiumService.calculatePremium(
                productId, age, coverageAmount, gender, paymentMethod);
            
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
     * 批量计算保险费
     * 批量计算多个保险费
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void batchCalculatePremium(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取请求体（JSON格式）
            String requestBody = getRequestBody(request);
            
            // 这里简化处理，实际项目中应该解析JSON
            // 暂时返回示例结果
            List<Map<String, Object>> results = new java.util.ArrayList<>();
            
            // 添加示例计算结果
            Map<String, Object> result1 = new HashMap<>();
            result1.put("productId", 1); // 产品ID
            result1.put("age", 30); // 年龄
            result1.put("coverageAmount", 1000000.0); // 保险金额
            result1.put("gender", "male"); // 性别
            result1.put("paymentMethod", "annual"); // 缴费方式
            result1.put("calculatedPremium", 12000.0); // 计算出的保险费
            results.add(result1);
            
            Map<String, Object> result2 = new HashMap<>();
            result2.put("productId", 2); // 产品ID
            result2.put("age", 25); // 年龄
            result2.put("coverageAmount", 2000000.0); // 保险金额
            result2.put("gender", "female"); // 性别
            result2.put("paymentMethod", "monthly"); // 缴费方式
            result2.put("calculatedPremium", 2500.0); // 计算出的保险费
            results.add(result2);
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("count", results.size()); // 结果数量
            meta.put("processed", results.size()); // 处理数量
            meta.put("errors", 0); // 错误数量
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(results, meta));
            
        } catch (Exception e) {
            // 记录批量保险费计算失败日志
            LogUtil.error("批量保险费计算失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("批量保险费计算失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取料率统计信息
     * 获取料率相关的统计信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getPremiumStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取所有料率
            List<PremiumRate> rates = premiumService.getPremiumRates();
            
            // 基本统计
            int total = rates.size(); // 总料率数
            int active = (int) rates.stream().filter(PremiumRate::isActive).count(); // 活跃料率数
            int inactive = total - active; // 非活跃料率数
            
            // 按产品统计
            Map<Integer, Long> productCount = rates.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    PremiumRate::getProductId, 
                    java.util.stream.Collectors.counting()
                ));
            
            // 按性别统计
            Map<String, Long> genderCount = rates.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    PremiumRate::getGender, 
                    java.util.stream.Collectors.counting()
                ));
            
            // 料率范围统计
            double avgRate = rates.stream()
                .mapToDouble(PremiumRate::getBaseRate)
                .average()
                .orElse(0.0); // 平均费率
            
            double minRate = rates.stream()
                .mapToDouble(PremiumRate::getBaseRate)
                .min()
                .orElse(0.0); // 最小费率
            
            double maxRate = rates.stream()
                .mapToDouble(PremiumRate::getBaseRate)
                .max()
                .orElse(0.0); // 最大费率
            
            // 构建统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", total); // 总料率数
            statistics.put("active", active); // 活跃料率数
            statistics.put("inactive", inactive); // 非活跃料率数
            statistics.put("productDistribution", productCount); // 产品分布
            statistics.put("genderDistribution", genderCount); // 性别分布
            statistics.put("averageRate", avgRate); // 平均费率
            statistics.put("minRate", minRate); // 最小费率
            statistics.put("maxRate", maxRate); // 最大费率
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            // 记录获取料率统计失败日志
            LogUtil.error("获取料率统计失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取料率统计失败"));
        }
    }
    
    /**
     * 获取产品列表
     * 获取所有可用的产品信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void getProducts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取产品列表（示例数据）
            List<Map<String, Object>> products = new java.util.ArrayList<>();
            
            // 添加基本生命保険产品
            Map<String, Object> product1 = new HashMap<>();
            product1.put("id", 1); // 产品ID
            product1.put("name", "基本生命保険"); // 产品名称
            product1.put("description", "基本的な保障を提供する生命保険商品"); // 产品描述
            product1.put("minCoverage", 100000.0); // 最小保险金额
            product1.put("maxCoverage", 10000000.0); // 最大保险金额
            product1.put("active", true); // 是否活跃
            products.add(product1);
            
            // 添加医療保険产品
            Map<String, Object> product2 = new HashMap<>();
            product2.put("id", 2); // 产品ID
            product2.put("name", "医療保険"); // 产品名称
            product2.put("description", "医療費や入院費をカバーする医療保険商品"); // 产品描述
            product2.put("minCoverage", 500000.0); // 最小保险金额
            product2.put("maxCoverage", 5000000.0); // 最大保险金额
            product2.put("active", true); // 是否活跃
            products.add(product2);
            
            // 添加がん保険产品
            Map<String, Object> product3 = new HashMap<>();
            product3.put("id", 3); // 产品ID
            product3.put("name", "がん保険"); // 产品名称
            product3.put("description", "がん診断・治療に特化した保険商品"); // 产品描述
            product3.put("minCoverage", 1000000.0); // 最小保险金额
            product3.put("maxCoverage", 8000000.0); // 最大保险金额
            product3.put("active", true); // 是否活跃
            products.add(product3);
            
            // 发送成功响应
            sendJsonResponse(response, ApiResponse.success(products));
            
        } catch (Exception e) {
            // 记录获取产品列表失败日志
            LogUtil.error("获取产品列表失败", e);
            // 发送错误响应
            sendJsonResponse(response, ApiResponse.error("获取产品列表失败"));
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
     * 料率API需要权限验证
     * @return 总是返回true，表示需要权限验证
     */
    @Override
    protected boolean requiresPermission() {
        return true;
    }
    
    /**
     * 检查用户是否有权限访问
     * 只有管理员和销售人员才能访问料率API
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