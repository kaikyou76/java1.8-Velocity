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
 */
@WebServlet("/api/premium/*")
public class PremiumApiServlet extends ApiBaseServlet {
    
    private PremiumCalculatorService premiumService;
    
    @Override
    public void init() {
        this.premiumService = new PremiumCalculatorService();
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
                    getPremiumRates(request, response);
                } else if ("POST".equals(method)) {
                    calculatePremium(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/calculate")) {
                // 计算接口
                if ("POST".equals(method)) {
                    calculatePremium(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/batch-calculate")) {
                // 批量计算接口
                if ("POST".equals(method)) {
                    batchCalculatePremium(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.matches("^/\\d+$")) {
                // 料率ID路径处理
                int rateId = Integer.parseInt(pathInfo.substring(1));
                if ("GET".equals(method)) {
                    getPremiumRate(request, response, rateId);
                } else if ("PUT".equals(method)) {
                    updatePremiumRate(request, response, rateId);
                } else if ("DELETE".equals(method)) {
                    deletePremiumRate(request, response, rateId);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/rates")) {
                // 料率列表
                if ("GET".equals(method)) {
                    getPremiumRates(request, response);
                } else if ("POST".equals(method)) {
                    createPremiumRate(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/statistics")) {
                // 统计接口
                if ("GET".equals(method)) {
                    getPremiumStatistics(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else if (pathInfo.equals("/products")) {
                // 产品列表
                if ("GET".equals(method)) {
                    getProducts(request, response);
                } else {
                    sendJsonResponse(response, ApiResponse.error("不支持的HTTP方法"));
                }
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
        } catch (Exception e) {
            LogUtil.error("料率API处理异常: " + pathInfo, e);
            sendJsonResponse(response, ApiResponse.internalError());
        }
    }
    
    /**
     * 获取料率列表
     */
    private void getPremiumRates(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取查询参数
            int productId = getIntParameter(request, "productId", 0);
            int page = getIntParameter(request, "page", 1);
            int size = getIntParameter(request, "size", 20);
            String activeOnly = getParameter(request, "activeOnly", "false");
            
            // 获取料率列表
            List<PremiumRate> rates = premiumService.getPremiumRates();
            
            // 产品过滤
            if (productId > 0) {
                rates.removeIf(rate -> rate.getProductId() != productId);
            }
            
            // 活跃料率过滤
            if ("true".equals(activeOnly)) {
                rates.removeIf(rate -> !rate.isActive());
            }
            
            // 分页处理
            int total = rates.size();
            int from = (page - 1) * size;
            int to = Math.min(from + size, total);
            
            if (from >= total) {
                rates.clear();
            } else {
                rates = rates.subList(from, to);
            }
            
            // 构建响应元数据
            Map<String, Object> meta = new HashMap<>();
            meta.put("page", page);
            meta.put("size", size);
            meta.put("total", total);
            meta.put("pages", (int) Math.ceil((double) total / size));
            meta.put("productId", productId > 0 ? productId : "all");
            meta.put("activeOnly", activeOnly);
            
            sendJsonResponse(response, ApiResponse.success(rates, meta));
            
        } catch (Exception e) {
            LogUtil.error("获取料率列表失败", e);
            sendJsonResponse(response, ApiResponse.error("获取料率列表失败"));
        }
    }
    
    /**
     * 获取单个料率详情
     */
    private void getPremiumRate(HttpServletRequest request, HttpServletResponse response, int rateId) 
            throws ServletException, IOException {
        
        try {
            PremiumRate rate = premiumService.getPremiumRateById(rateId);
            
            if (rate != null) {
                sendJsonResponse(response, ApiResponse.success(rate));
            } else {
                sendJsonResponse(response, ApiResponse.notFound());
            }
            
        } catch (Exception e) {
            LogUtil.error("获取料率详情失败: ID=" + rateId, e);
            sendJsonResponse(response, ApiResponse.error("获取料率详情失败"));
        }
    }
    
    /**
     * 创建新料率
     */
    private void createPremiumRate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 验证必需参数
            if (!validateRequiredParameters(request, "productId", "minAge", "maxAge", "baseRate")) {
                sendJsonResponse(response, ApiResponse.error("缺少必需参数"));
                return;
            }
            
            // 获取请求参数
            int productId = getIntParameter(request, "productId", 0);
            int minAge = getIntParameter(request, "minAge", 0);
            int maxAge = getIntParameter(request, "maxAge", 0);
            double baseRate = getDoubleParameter(request, "baseRate", 0.0);
            String gender = getParameter(request, "gender", "all");
            String description = getParameter(request, "description", "");
            String effectiveDate = getParameter(request, "effectiveDate", "");
            String expiryDate = getParameter(request, "expiryDate", "");
            boolean active = getBooleanParameter(request, "active", true);
            
            // 验证参数
            if (minAge <= 0 || maxAge <= 0 || minAge > maxAge) {
                sendJsonResponse(response, ApiResponse.error("年龄范围不正确"));
                return;
            }
            
            if (baseRate <= 0) {
                sendJsonResponse(response, ApiResponse.error("基础料率必须大于0"));
                return;
            }
            
            // 创建料率对象
            PremiumRate rate = new PremiumRate();
            rate.setProductId(productId);
            rate.setMinAge(minAge);
            rate.setMaxAge(maxAge);
            rate.setBaseRate(baseRate);
            rate.setGender(gender);
            rate.setDescription(description);
            rate.setEffectiveDate(effectiveDate);
            rate.setExpiryDate(expiryDate);
            rate.setActive(active);
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 创建料率
            boolean success = premiumService.createPremiumRate(rate, createdBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.created(rate));
            } else {
                sendJsonResponse(response, ApiResponse.error("料率创建失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("创建料率失败", e);
            sendJsonResponse(response, ApiResponse.error("料率创建失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新料率
     */
    private void updatePremiumRate(HttpServletRequest request, HttpServletResponse response, int rateId) 
            throws ServletException, IOException {
        
        try {
            // 检查料率是否存在
            PremiumRate existingRate = premiumService.getPremiumRateById(rateId);
            if (existingRate == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取请求参数
            int productId = getIntParameter(request, "productId", existingRate.getProductId());
            int minAge = getIntParameter(request, "minAge", existingRate.getMinAge());
            int maxAge = getIntParameter(request, "maxAge", existingRate.getMaxAge());
            double baseRate = getDoubleParameter(request, "baseRate", existingRate.getBaseRate());
            String gender = getParameter(request, "gender", existingRate.getGender());
            String description = getParameter(request, "description", existingRate.getDescription());
            String effectiveDate = getParameter(request, "effectiveDate", existingRate.getEffectiveDate());
            String expiryDate = getParameter(request, "expiryDate", existingRate.getExpiryDate());
            boolean active = getBooleanParameter(request, "active", existingRate.isActive());
            
            // 验证参数
            if (minAge <= 0 || maxAge <= 0 || minAge > maxAge) {
                sendJsonResponse(response, ApiResponse.error("年龄范围不正确"));
                return;
            }
            
            if (baseRate <= 0) {
                sendJsonResponse(response, ApiResponse.error("基础料率必须大于0"));
                return;
            }
            
            // 更新料率对象
            PremiumRate rate = new PremiumRate();
            rate.setId(rateId);
            rate.setProductId(productId);
            rate.setMinAge(minAge);
            rate.setMaxAge(maxAge);
            rate.setBaseRate(baseRate);
            rate.setGender(gender);
            rate.setDescription(description);
            rate.setEffectiveDate(effectiveDate);
            rate.setExpiryDate(expiryDate);
            rate.setActive(active);
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 更新料率
            boolean success = premiumService.updatePremiumRate(rate, updatedBy);
            
            if (success) {
                PremiumRate updatedRate = premiumService.getPremiumRateById(rateId);
                sendJsonResponse(response, ApiResponse.success(updatedRate));
            } else {
                sendJsonResponse(response, ApiResponse.error("料率更新失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("更新料率失败: ID=" + rateId, e);
            sendJsonResponse(response, ApiResponse.error("料率更新失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除料率
     */
    private void deletePremiumRate(HttpServletRequest request, HttpServletResponse response, int rateId) 
            throws ServletException, IOException {
        
        try {
            // 检查料率是否存在
            PremiumRate rate = premiumService.getPremiumRateById(rateId);
            if (rate == null) {
                sendJsonResponse(response, ApiResponse.notFound());
                return;
            }
            
            // 获取当前用户
            com.insurance.model.User currentUser = getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "api";
            
            // 删除料率
            boolean success = premiumService.deletePremiumRate(rateId, deletedBy);
            
            if (success) {
                sendJsonResponse(response, ApiResponse.success("料率删除成功"));
            } else {
                sendJsonResponse(response, ApiResponse.error("料率删除失败"));
            }
            
        } catch (Exception e) {
            LogUtil.error("删除料率失败: ID=" + rateId, e);
            sendJsonResponse(response, ApiResponse.error("料率删除失败: " + e.getMessage()));
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
            
            // 获取请求参数
            int productId = getIntParameter(request, "productId", 0);
            int age = getIntParameter(request, "age", 0);
            double coverageAmount = getDoubleParameter(request, "coverageAmount", 0.0);
            String gender = getParameter(request, "gender", "all");
            String paymentMethod = getParameter(request, "paymentMethod", "annual");
            
            // 验证参数
            if (age <= 0 || age > 120) {
                sendJsonResponse(response, ApiResponse.error("年龄必须在1-120之间"));
                return;
            }
            
            if (coverageAmount <= 0) {
                sendJsonResponse(response, ApiResponse.error("保险金额必须大于0"));
                return;
            }
            
            // 计算保险费
            Map<String, Object> result = premiumService.calculatePremium(
                productId, age, coverageAmount, gender, paymentMethod);
            
            sendJsonResponse(response, ApiResponse.success(result));
            
        } catch (Exception e) {
            LogUtil.error("保险费计算失败", e);
            sendJsonResponse(response, ApiResponse.error("保险费计算失败: " + e.getMessage()));
        }
    }
    
    /**
     * 批量计算保险费
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
            result1.put("productId", 1);
            result1.put("age", 30);
            result1.put("coverageAmount", 1000000.0);
            result1.put("gender", "male");
            result1.put("paymentMethod", "annual");
            result1.put("calculatedPremium", 12000.0);
            results.add(result1);
            
            Map<String, Object> result2 = new HashMap<>();
            result2.put("productId", 2);
            result2.put("age", 25);
            result2.put("coverageAmount", 2000000.0);
            result2.put("gender", "female");
            result2.put("paymentMethod", "monthly");
            result2.put("calculatedPremium", 2500.0);
            results.add(result2);
            
            Map<String, Object> meta = new HashMap<>();
            meta.put("count", results.size());
            meta.put("processed", results.size());
            meta.put("errors", 0);
            
            sendJsonResponse(response, ApiResponse.success(results, meta));
            
        } catch (Exception e) {
            LogUtil.error("批量保险费计算失败", e);
            sendJsonResponse(response, ApiResponse.error("批量保险费计算失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取料率统计信息
     */
    private void getPremiumStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取所有料率
            List<PremiumRate> rates = premiumService.getPremiumRates();
            
            // 基本统计
            int total = rates.size();
            int active = (int) rates.stream().filter(PremiumRate::isActive).count();
            int inactive = total - active;
            
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
                .orElse(0.0);
            
            double minRate = rates.stream()
                .mapToDouble(PremiumRate::getBaseRate)
                .min()
                .orElse(0.0);
            
            double maxRate = rates.stream()
                .mapToDouble(PremiumRate::getBaseRate)
                .max()
                .orElse(0.0);
            
            // 构建统计数据
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("total", total);
            statistics.put("active", active);
            statistics.put("inactive", inactive);
            statistics.put("productDistribution", productCount);
            statistics.put("genderDistribution", genderCount);
            statistics.put("averageRate", avgRate);
            statistics.put("minRate", minRate);
            statistics.put("maxRate", maxRate);
            
            sendJsonResponse(response, ApiResponse.success(statistics));
            
        } catch (Exception e) {
            LogUtil.error("获取料率统计失败", e);
            sendJsonResponse(response, ApiResponse.error("获取料率统计失败"));
        }
    }
    
    /**
     * 获取产品列表
     */
    private void getProducts(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取产品列表（示例数据）
            List<Map<String, Object>> products = new java.util.ArrayList<>();
            
            Map<String, Object> product1 = new HashMap<>();
            product1.put("id", 1);
            product1.put("name", "基本生命保険");
            product1.put("description", "基本的な保障を提供する生命保険商品");
            product1.put("minCoverage", 100000.0);
            product1.put("maxCoverage", 10000000.0);
            product1.put("active", true);
            products.add(product1);
            
            Map<String, Object> product2 = new HashMap<>();
            product2.put("id", 2);
            product2.put("name", "医療保険");
            product2.put("description", "医療費や入院費をカバーする医療保険商品");
            product2.put("minCoverage", 500000.0);
            product2.put("maxCoverage", 5000000.0);
            product2.put("active", true);
            products.add(product2);
            
            Map<String, Object> product3 = new HashMap<>();
            product3.put("id", 3);
            product3.put("name", "がん保険");
            product3.put("description", "がん診断・治療に特化した保険商品");
            product3.put("minCoverage", 1000000.0);
            product3.put("maxCoverage", 8000000.0);
            product3.put("active", true);
            products.add(product3);
            
            sendJsonResponse(response, ApiResponse.success(products));
            
        } catch (Exception e) {
            LogUtil.error("获取产品列表失败", e);
            sendJsonResponse(response, ApiResponse.error("获取产品列表失败"));
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
        return user != null && (user.isAdmin() || user.isSales());
    }
}