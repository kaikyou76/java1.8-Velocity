package com.insurance.service;

import com.insurance.dao.PremiumRateDAO;
import com.insurance.model.PremiumRate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 保险料计算服务类
 */
public class PremiumCalculatorService {
    
    // 保险料率数据访问对象，用于与数据库交互获取料率信息
    private PremiumRateDAO premiumRateDAO;
    
    // 构造方法，初始化保险料率数据访问对象
    public PremiumCalculatorService() {
        this.premiumRateDAO = new PremiumRateDAO();
    }
    
    /**
     * 计算保险料
     * 根据商品ID、性别、加入年龄、保险期间和保险金额计算保险料
     * @param productId 商品ID
     * @param gender 性别 (M/F)
     * @param entryAge 加入年龄
     * @param insurancePeriod 保险期间
     * @param insuredAmount 保险金额
     * @return Map<String, Object> 计算结果，包含年保费、月保费等信息
     */
    public Map<String, Object> calculatePremium(int productId, String gender, int entryAge, 
                                               int insurancePeriod, double insuredAmount) {
        // 创建结果映射
        Map<String, Object> result = new HashMap<>();
        
        // 验证输入参数
        // 验证计算参数是否符合要求
        String validationError = validateCalculationParameters(productId, gender, entryAge, 
                                                             insurancePeriod, insuredAmount);
        if (validationError != null) {
            // 如果验证失败，将错误信息放入结果并返回
            result.put("error", validationError);
            return result;
        }
        
        // 获取料率
        // 根据条件从数据库获取对应的料率信息
        PremiumRate rate = premiumRateDAO.getRateByConditions(productId, gender, entryAge, insurancePeriod);
        if (rate == null) {
            // 如果未找到对应的料率，将错误信息放入结果并返回
            result.put("error", "指定条件の料率が見つかりません");
            return result;
        }
        
        // 计算保费
        // 获取总料率并计算年保费和月保费
        double totalRate = rate.getTotalRate();
        double annualPremium = insuredAmount * totalRate;
        double monthlyPremium = annualPremium / 12;
        
        // 构建结果
        // 将计算结果放入结果映射
        result.put("success", true);
        result.put("annualPremium", annualPremium);
        result.put("monthlyPremium", monthlyPremium);
        result.put("totalRate", totalRate);
        result.put("baseRate", rate.getBaseRate());
        result.put("loadingRate", rate.getLoadingRate());
        result.put("premiumRate", rate);
        result.put("insuredAmount", insuredAmount);
        result.put("calculationDate", new Date());
        
        // 返回计算结果
        return result;
    }
    
    /**
     * 使用存储过程计算保险料
     * 通过调用数据库存储过程来计算保险料
     * @param productId 商品ID
     * @param gender 性别 (M/F)
     * @param entryAge 加入年龄
     * @param insurancePeriod 保险期间
     * @param insuredAmount 保险金额
     * @return Map<String, Object> 计算结果，包含年保费、月保费等信息
     */
    public Map<String, Object> calculatePremiumUsingProcedure(int productId, String gender, int entryAge, 
                                                             int insurancePeriod, double insuredAmount) {
        // 创建结果映射
        Map<String, Object> result = new HashMap<>();
        
        // 验证输入参数
        // 验证计算参数是否符合要求
        String validationError = validateCalculationParameters(productId, gender, entryAge, 
                                                             insurancePeriod, insuredAmount);
        if (validationError != null) {
            // 如果验证失败，将错误信息放入结果并返回
            result.put("error", validationError);
            return result;
        }
        
        // 调用存储过程
        // 定义调用存储过程的SQL语句
        String sql = "CALL calculate_premium(?, ?, ?, ?, ?, ?)";
        
        try (var conn = com.insurance.util.DatabaseUtil.getConnection();
             var pstmt = conn.prepareCall(sql)) {
            
            // 设置输入参数
            pstmt.setInt(1, productId);
            pstmt.setString(2, gender);
            pstmt.setInt(3, entryAge);
            pstmt.setInt(4, insurancePeriod);
            pstmt.setDouble(5, insuredAmount);
            
            // 注册输出参数
            // 注册存储过程的输出参数
            pstmt.registerOutParameter(6, java.sql.Types.DECIMAL);
            pstmt.registerOutParameter(7, java.sql.Types.DECIMAL);
            
            // 执行存储过程
            pstmt.execute();
            
            // 获取输出参数
            double monthlyPremium = pstmt.getDouble(6);
            double annualPremium = pstmt.getDouble(7);
            
            // 构建结果
            // 将计算结果放入结果映射
            result.put("success", true);
            result.put("annualPremium", annualPremium);
            result.put("monthlyPremium", monthlyPremium);
            result.put("insuredAmount", insuredAmount);
            result.put("calculationDate", new Date());
            
        } catch (Exception e) {
            // 如果发生异常，将错误信息放入结果
            result.put("error", "保険料計算中にエラーが発生しました: " + e.getMessage());
        }
        
        // 返回计算结果
        return result;
    }
    
    /**
     * 验证计算参数
     * 验证保险料计算所需的各个参数是否符合要求
     * @param productId 商品ID
     * @param gender 性别
     * @param entryAge 加入年龄
     * @param insurancePeriod 保险期间
     * @param insuredAmount 保险金额
     * @return String 验证结果，null表示验证通过，其他值表示错误信息
     */
    private String validateCalculationParameters(int productId, String gender, int entryAge, 
                                                int insurancePeriod, double insuredAmount) {
        // 验证商品ID是否有效
        if (productId <= 0) {
            return "商品IDが無効です";
        }
        
        // 验证性别是否为M或F
        if (gender == null || (!gender.equals("M") && !gender.equals("F"))) {
            return "性別はMまたはFを指定してください";
        }
        
        // 验证加入年龄是否在有效范围内(0-100)
        if (entryAge < 0 || entryAge > 100) {
            return "加入年齢が無効です (0-100)";
        }
        
        // 验证保险期间是否在有效范围内(1-50年)
        if (insurancePeriod <= 0 || insurancePeriod > 50) {
            return "保険期間が無効です (1-50年)";
        }
        
        // 验证保险金额是否大于0
        if (insuredAmount <= 0) {
            return "保険金額は0より大きい値を指定してください";
        }
        
        // 检查年龄和期间范围
        // 获取商品的有效年龄范围并验证
        int[] validAgeRange = premiumRateDAO.getValidAgeRange(productId);
        if (entryAge < validAgeRange[0] || entryAge > validAgeRange[1]) {
            return "加入年齢が有効範囲外です (" + validAgeRange[0] + "-" + validAgeRange[1] + "歳)";
        }
        
        // 获取商品的有效期间范围并验证
        int[] validPeriodRange = premiumRateDAO.getValidPeriodRange(productId);
        if (insurancePeriod < validPeriodRange[0] || insurancePeriod > validPeriodRange[1]) {
            return "保険期間が有効範囲外です (" + validPeriodRange[0] + "-" + validPeriodRange[1] + "年)";
        }
        
        // 所有验证通过，返回null
        return null;
    }
    
    /**
     * 格式化保费金额
     * 将保费金额格式化为带千位分隔符的日元格式
     * @param premium 保费金额
     * @return String 格式化后的保费金额
     */
    public String formatPremium(double premium) {
        // 使用String.format格式化金额，添加千位分隔符和日元符号
        return String.format("¥%,.0f", premium);
    }
    
    /**
     * 格式化料率
     * 将料率格式化为百分比形式
     * @param rate 料率
     * @return String 格式化后的料率
     */
    public String formatRate(double rate) {
        // 将料率转换为百分比形式并保留4位小数
        return String.format("%.4f%%", rate * 100);
    }
    
    /**
     * 获取商品的料率表
     * 根据商品ID获取该商品的所有料率信息并按性别、年龄、期间分组
     * @param productId 商品ID
     * @return Map<String, Object> 料率表信息
     */
    public Map<String, Object> getPremiumRateTable(int productId) {
        // 创建结果映射
        Map<String, Object> result = new HashMap<>();
        
        // 根据商品ID获取所有料率信息
        var rates = premiumRateDAO.getRatesByProductId(productId);
        // 如果没有找到料率数据，返回错误信息
        if (rates.isEmpty()) {
            result.put("error", "指定商品の料率データが見つかりません");
            return result;
        }
        
        // 按性别和年龄分组
        // 创建三层嵌套的映射结构：性别->年龄->期间->料率
        Map<String, Map<Integer, Map<Integer, PremiumRate>>> rateTable = new HashMap<>();
        
        // 遍历所有料率信息，按性别、年龄、期间进行分组
        for (PremiumRate rate : rates) {
            String genderKey = rate.getGender();
            int age = rate.getEntryAge();
            int period = rate.getInsurancePeriod();
            
            // 使用computeIfAbsent方法构建三层嵌套结构
            rateTable
                .computeIfAbsent(genderKey, k -> new HashMap<>())
                .computeIfAbsent(age, k -> new HashMap<>())
                .put(period, rate);
        }
        
        // 构建结果
        result.put("success", true);
        result.put("rateTable", rateTable);
        result.put("productName", rates.get(0).getProductName());
        result.put("productCode", rates.get(0).getProductCode());
        result.put("validAgeRange", premiumRateDAO.getValidAgeRange(productId));
        result.put("validPeriodRange", premiumRateDAO.getValidPeriodRange(productId));
        
        // 返回结果
        return result;
    }
    
    /**
     * 批量计算保费
     * 根据商品ID、性别、多个年龄和期间组合批量计算保费
     * @param productId 商品ID
     * @param gender 性别
     * @param ages 年龄数组
     * @param periods 期间数组
     * @param insuredAmount 保险金额
     * @return Map<String, Object> 批量计算结果
     */
    public Map<String, Object> batchCalculatePremium(int productId, String gender, 
                                                    int[] ages, int[] periods, double insuredAmount) {
        // 创建结果映射
        Map<String, Object> result = new HashMap<>();
        // 创建批量计算结果映射，结构为：性别->年龄->期间->计算结果
        Map<String, Map<Integer, Map<Integer, Object>>> calculationResults = new HashMap<>();
        
        // 遍历年龄和期间的组合，进行批量计算
        for (int age : ages) {
            for (int period : periods) {
                // 调用单次计算方法计算保费
                var calculation = calculatePremium(productId, gender, age, period, insuredAmount);
                
                // 使用computeIfAbsent方法构建三层嵌套结构存储计算结果
                calculationResults
                    .computeIfAbsent(gender, k -> new HashMap<>())
                    .computeIfAbsent(age, k -> new HashMap<>())
                    .put(period, calculation);
            }
        }
        
        // 构建结果
        result.put("success", true);
        result.put("batchResults", calculationResults);
        result.put("insuredAmount", insuredAmount);
        
        // 返回批量计算结果
        return result;
    }
}