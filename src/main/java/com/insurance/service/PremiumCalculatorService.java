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
    
    private PremiumRateDAO premiumRateDAO;
    
    public PremiumCalculatorService() {
        this.premiumRateDAO = new PremiumRateDAO();
    }
    
    /**
     * 计算保险料
     */
    public Map<String, Object> calculatePremium(int productId, String gender, int entryAge, 
                                               int insurancePeriod, double insuredAmount) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证输入参数
        String validationError = validateCalculationParameters(productId, gender, entryAge, 
                                                             insurancePeriod, insuredAmount);
        if (validationError != null) {
            result.put("error", validationError);
            return result;
        }
        
        // 获取料率
        PremiumRate rate = premiumRateDAO.getRateByConditions(productId, gender, entryAge, insurancePeriod);
        if (rate == null) {
            result.put("error", "指定条件の料率が見つかりません");
            return result;
        }
        
        // 计算保费
        double totalRate = rate.getTotalRate();
        double annualPremium = insuredAmount * totalRate;
        double monthlyPremium = annualPremium / 12;
        
        // 构建结果
        result.put("success", true);
        result.put("annualPremium", annualPremium);
        result.put("monthlyPremium", monthlyPremium);
        result.put("totalRate", totalRate);
        result.put("baseRate", rate.getBaseRate());
        result.put("loadingRate", rate.getLoadingRate());
        result.put("premiumRate", rate);
        result.put("insuredAmount", insuredAmount);
        result.put("calculationDate", new Date());
        
        return result;
    }
    
    /**
     * 使用存储过程计算保险料
     */
    public Map<String, Object> calculatePremiumUsingProcedure(int productId, String gender, int entryAge, 
                                                             int insurancePeriod, double insuredAmount) {
        Map<String, Object> result = new HashMap<>();
        
        // 验证输入参数
        String validationError = validateCalculationParameters(productId, gender, entryAge, 
                                                             insurancePeriod, insuredAmount);
        if (validationError != null) {
            result.put("error", validationError);
            return result;
        }
        
        // 调用存储过程
        String sql = "CALL calculate_premium(?, ?, ?, ?, ?, ?)";
        
        try (var conn = com.insurance.util.DatabaseUtil.getConnection();
             var pstmt = conn.prepareCall(sql)) {
            
            pstmt.setInt(1, productId);
            pstmt.setString(2, gender);
            pstmt.setInt(3, entryAge);
            pstmt.setInt(4, insurancePeriod);
            pstmt.setDouble(5, insuredAmount);
            
            // 注册输出参数
            pstmt.registerOutParameter(6, java.sql.Types.DECIMAL);
            pstmt.registerOutParameter(7, java.sql.Types.DECIMAL);
            
            pstmt.execute();
            
            double monthlyPremium = pstmt.getDouble(6);
            double annualPremium = pstmt.getDouble(7);
            
            result.put("success", true);
            result.put("annualPremium", annualPremium);
            result.put("monthlyPremium", monthlyPremium);
            result.put("insuredAmount", insuredAmount);
            result.put("calculationDate", new Date());
            
        } catch (Exception e) {
            result.put("error", "保険料計算中にエラーが発生しました: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证计算参数
     */
    private String validateCalculationParameters(int productId, String gender, int entryAge, 
                                                int insurancePeriod, double insuredAmount) {
        if (productId <= 0) {
            return "商品IDが無効です";
        }
        
        if (gender == null || (!gender.equals("M") && !gender.equals("F"))) {
            return "性別はMまたはFを指定してください";
        }
        
        if (entryAge < 0 || entryAge > 100) {
            return "加入年齢が無効です (0-100)";
        }
        
        if (insurancePeriod <= 0 || insurancePeriod > 50) {
            return "保険期間が無効です (1-50年)";
        }
        
        if (insuredAmount <= 0) {
            return "保険金額は0より大きい値を指定してください";
        }
        
        // 检查年龄和期间范围
        int[] validAgeRange = premiumRateDAO.getValidAgeRange(productId);
        if (entryAge < validAgeRange[0] || entryAge > validAgeRange[1]) {
            return "加入年齢が有効範囲外です (" + validAgeRange[0] + "-" + validAgeRange[1] + "歳)";
        }
        
        int[] validPeriodRange = premiumRateDAO.getValidPeriodRange(productId);
        if (insurancePeriod < validPeriodRange[0] || insurancePeriod > validPeriodRange[1]) {
            return "保険期間が有効範囲外です (" + validPeriodRange[0] + "-" + validPeriodRange[1] + "年)";
        }
        
        return null;
    }
    
    /**
     * 格式化保费金额
     */
    public String formatPremium(double premium) {
        return String.format("¥%,.0f", premium);
    }
    
    /**
     * 格式化料率
     */
    public String formatRate(double rate) {
        return String.format("%.4f%%", rate * 100);
    }
    
    /**
     * 获取商品的料率表
     */
    public Map<String, Object> getPremiumRateTable(int productId) {
        Map<String, Object> result = new HashMap<>();
        
        var rates = premiumRateDAO.getRatesByProductId(productId);
        if (rates.isEmpty()) {
            result.put("error", "指定商品の料率データが見つかりません");
            return result;
        }
        
        // 按性别和年龄分组
        Map<String, Map<Integer, Map<Integer, PremiumRate>>> rateTable = new HashMap<>();
        
        for (PremiumRate rate : rates) {
            String genderKey = rate.getGender();
            int age = rate.getEntryAge();
            int period = rate.getInsurancePeriod();
            
            rateTable
                .computeIfAbsent(genderKey, k -> new HashMap<>())
                .computeIfAbsent(age, k -> new HashMap<>())
                .put(period, rate);
        }
        
        result.put("success", true);
        result.put("rateTable", rateTable);
        result.put("productName", rates.get(0).getProductName());
        result.put("productCode", rates.get(0).getProductCode());
        result.put("validAgeRange", premiumRateDAO.getValidAgeRange(productId));
        result.put("validPeriodRange", premiumRateDAO.getValidPeriodRange(productId));
        
        return result;
    }
    
    /**
     * 批量计算保费
     */
    public Map<String, Object> batchCalculatePremium(int productId, String gender, 
                                                    int[] ages, int[] periods, double insuredAmount) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Map<Integer, Map<Integer, Object>>> calculationResults = new HashMap<>();
        
        for (int age : ages) {
            for (int period : periods) {
                var calculation = calculatePremium(productId, gender, age, period, insuredAmount);
                
                calculationResults
                    .computeIfAbsent(gender, k -> new HashMap<>())
                    .computeIfAbsent(age, k -> new HashMap<>())
                    .put(period, calculation);
            }
        }
        
        result.put("success", true);
        result.put("batchResults", calculationResults);
        result.put("insuredAmount", insuredAmount);
        
        return result;
    }
}