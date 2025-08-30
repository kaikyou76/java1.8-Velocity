package com.insurance.controller;

import com.insurance.service.PremiumCalculatorService;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.Map;

/**
 * 保险料计算Servlet
 */
@WebServlet("/premium")
public class PremiumCalculatorServlet extends HttpServlet {
    
    private PremiumCalculatorService premiumService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        premiumService = new PremiumCalculatorService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "simulate";
        }
        
        try {
            switch (action) {
                case "simulate":
                    showSimulationForm(request, response);
                    break;
                case "calculate":
                    calculatePremium(request, response);
                    break;
                case "rates":
                    showRateTable(request, response);
                    break;
                default:
                    showSimulationForm(request, response);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect("premium?action=simulate");
            return;
        }
        
        try {
            switch (action) {
                case "calculate":
                    calculatePremium(request, response);
                    break;
                case "batch":
                    batchCalculatePremium(request, response);
                    break;
                default:
                    response.sendRedirect("premium?action=simulate");
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    /**
     * 显示保费模拟表单
     */
    private void showSimulationForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置默认值
        request.setAttribute("defaultProductId", 1);
        request.setAttribute("defaultGender", "M");
        request.setAttribute("defaultEntryAge", 30);
        request.setAttribute("defaultInsurancePeriod", 20);
        request.setAttribute("defaultInsuredAmount", 3000000.0);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/simulate.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 计算保费
     */
    private void calculatePremium(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取参数
            int productId = Integer.parseInt(request.getParameter("productId"));
            String gender = request.getParameter("gender");
            int entryAge = Integer.parseInt(request.getParameter("entryAge"));
            int insurancePeriod = Integer.parseInt(request.getParameter("insurancePeriod"));
            double insuredAmount = Double.parseDouble(request.getParameter("insuredAmount"));
            
            // 计算保费
            Map<String, Object> result = premiumService.calculatePremium(
                productId, gender, entryAge, insurancePeriod, insuredAmount
            );
            
            if (result.containsKey("error")) {
                request.setAttribute("errorMessage", result.get("error"));
                // 保留输入值以便重新输入
                request.setAttribute("productId", productId);
                request.setAttribute("gender", gender);
                request.setAttribute("entryAge", entryAge);
                request.setAttribute("insurancePeriod", insurancePeriod);
                request.setAttribute("insuredAmount", insuredAmount);
                
                showSimulationForm(request, response);
                return;
            }
            
            // 设置结果
            request.setAttribute("calculationResult", result);
            request.setAttribute("productId", productId);
            request.setAttribute("gender", gender);
            request.setAttribute("entryAge", entryAge);
            request.setAttribute("insurancePeriod", insurancePeriod);
            request.setAttribute("insuredAmount", insuredAmount);
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/result.jsp");
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "入力値が無効です。数値を正しく入力してください。");
            showSimulationForm(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "保険料計算中にエラーが発生しました: " + e.getMessage());
            showSimulationForm(request, response);
        }
    }
    
    /**
     * 批量计算保费
     */
    private void batchCalculatePremium(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            String gender = request.getParameter("gender");
            double insuredAmount = Double.parseDouble(request.getParameter("insuredAmount"));
            
            // 解析年龄范围
            String ageRange = request.getParameter("ageRange");
            int[] ages = parseRange(ageRange);
            
            // 解析期间范围
            String periodRange = request.getParameter("periodRange");
            int[] periods = parseRange(periodRange);
            
            if (ages == null || periods == null) {
                request.setAttribute("errorMessage", "年齢または保険期間の範囲指定が無効です");
                showSimulationForm(request, response);
                return;
            }
            
            // 批量计算
            Map<String, Object> result = premiumService.batchCalculatePremium(
                productId, gender, ages, periods, insuredAmount
            );
            
            if (result.containsKey("error")) {
                request.setAttribute("errorMessage", result.get("error"));
                showSimulationForm(request, response);
                return;
            }
            
            request.setAttribute("batchResult", result);
            request.setAttribute("productId", productId);
            request.setAttribute("gender", gender);
            request.setAttribute("insuredAmount", insuredAmount);
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/batch_result.jsp");
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "入力値が無効です。数値を正しく入力してください。");
            showSimulationForm(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "バッチ計算中にエラーが発生しました: " + e.getMessage());
            showSimulationForm(request, response);
        }
    }
    
    /**
     * 显示料率表
     */
    private void showRateTable(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int productId = Integer.parseInt(request.getParameter("productId"));
            
            Map<String, Object> result = premiumService.getPremiumRateTable(productId);
            
            if (result.containsKey("error")) {
                request.setAttribute("errorMessage", result.get("error"));
                showSimulationForm(request, response);
                return;
            }
            
            request.setAttribute("rateTableResult", result);
            request.setAttribute("productId", productId);
            
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/rate_table.jsp");
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "商品IDが無効です");
            showSimulationForm(request, response);
        } catch (Exception e) {
            request.setAttribute("errorMessage", "料率表取得中にエラーが発生しました: " + e.getMessage());
            showSimulationForm(request, response);
        }
    }
    
    /**
     * 解析范围字符串（如 "25-35"）
     */
    private int[] parseRange(String rangeStr) {
        if (rangeStr == null || rangeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            String[] parts = rangeStr.split("-");
            if (parts.length != 2) {
                return null;
            }
            
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            
            if (start > end) {
                return null;
            }
            
            int length = end - start + 1;
            int[] result = new int[length];
            for (int i = 0; i < length; i++) {
                result[i] = start + i;
            }
            
            return result;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}