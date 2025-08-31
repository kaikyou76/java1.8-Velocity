package com.insurance.controller;

import com.insurance.service.PremiumCalculatorService;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.Map;

/**
 * 保险料计算Servlet
 * 用于处理保险费计算相关的HTTP请求，包括保费模拟、计算和料率表显示等功能
 */
@WebServlet("/premium")
public class PremiumCalculatorServlet extends HttpServlet {
    
    // 保险费计算服务对象，用于处理保险费计算业务逻辑
    private PremiumCalculatorService premiumService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     * @throws ServletException 当初始化过程中发生错误时抛出
     */
    @Override
    public void init() throws ServletException {
        // 调用父类的初始化方法
        super.init();
        // 创建保险费计算服务实例
        premiumService = new PremiumCalculatorService();
    }
    
    /**
     * 处理HTTP GET请求
     * 根据请求参数中的action值执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的action值，用于确定要执行的操作
        String action = request.getParameter("action");
        
        // 如果action参数为空，则默认设置为"simulate"
        if (action == null) {
            action = "simulate";
        }
        
        try {
            // 根据action值执行相应的操作
            switch (action) {
                case "simulate":
                    // 显示保费模拟表单
                    showSimulationForm(request, response);
                    break;
                case "calculate":
                    // 计算保费
                    calculatePremium(request, response);
                    break;
                case "rates":
                    // 显示料率表
                    showRateTable(request, response);
                    break;
                default:
                    // 默认显示保费模拟表单
                    showSimulationForm(request, response);
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 处理HTTP POST请求
     * 根据请求参数中的action值执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的action值，用于确定要执行的操作
        String action = request.getParameter("action");
        
        // 如果action参数为空，则重定向到保费模拟页面
        if (action == null) {
            response.sendRedirect("premium?action=simulate");
            return;
        }
        
        try {
            // 根据action值执行相应的操作
            switch (action) {
                case "calculate":
                    // 计算保费
                    calculatePremium(request, response);
                    break;
                case "batch":
                    // 批量计算保费
                    batchCalculatePremium(request, response);
                    break;
                default:
                    // 默认重定向到保费模拟页面
                    response.sendRedirect("premium?action=simulate");
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 显示保费模拟表单
     * 设置默认值并转发到保费模拟表单页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showSimulationForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 设置默认值
        request.setAttribute("defaultProductId", 1); // 默认产品ID
        request.setAttribute("defaultGender", "M"); // 默认性别
        request.setAttribute("defaultEntryAge", 30); // 默认加入年龄
        request.setAttribute("defaultInsurancePeriod", 20); // 默认保险期间
        request.setAttribute("defaultInsuredAmount", 3000000.0); // 默认保险金额
        
        // 获取请求转发器，指向保费模拟表单页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/simulate.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 计算保费
     * 根据请求参数计算保险费并显示结果
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void calculatePremium(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取参数
            int productId = Integer.parseInt(request.getParameter("productId")); // 产品ID
            String gender = request.getParameter("gender"); // 性别
            int entryAge = Integer.parseInt(request.getParameter("entryAge")); // 加入年龄
            int insurancePeriod = Integer.parseInt(request.getParameter("insurancePeriod")); // 保险期间
            double insuredAmount = Double.parseDouble(request.getParameter("insuredAmount")); // 保险金额
            
            // 计算保费
            Map<String, Object> result = premiumService.calculatePremium(
                productId, gender, entryAge, insurancePeriod, insuredAmount
            );
            
            // 如果计算结果包含错误信息，则设置错误信息并重新显示表单
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
            request.setAttribute("calculationResult", result); // 计算结果
            request.setAttribute("productId", productId); // 产品ID
            request.setAttribute("gender", gender); // 性别
            request.setAttribute("entryAge", entryAge); // 加入年龄
            request.setAttribute("insurancePeriod", insurancePeriod); // 保险期间
            request.setAttribute("insuredAmount", insuredAmount); // 保险金额
            
            // 获取请求转发器，指向保费计算结果页面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/result.jsp");
            // 转发请求到JSP页面
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            // 如果参数格式不正确，设置错误信息并重新显示表单
            request.setAttribute("errorMessage", "入力値が無効です。数値を正しく入力してください。");
            showSimulationForm(request, response);
        } catch (Exception e) {
            // 如果计算过程中发生其他错误，设置错误信息并重新显示表单
            request.setAttribute("errorMessage", "保険料計算中にエラーが発生しました: " + e.getMessage());
            showSimulationForm(request, response);
        }
    }
    
    /**
     * 批量计算保费
     * 根据请求参数批量计算保险费并显示结果
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void batchCalculatePremium(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取参数
            int productId = Integer.parseInt(request.getParameter("productId")); // 产品ID
            String gender = request.getParameter("gender"); // 性别
            double insuredAmount = Double.parseDouble(request.getParameter("insuredAmount")); // 保险金额
            
            // 解析年龄范围
            String ageRange = request.getParameter("ageRange"); // 年龄范围
            int[] ages = parseRange(ageRange); // 解析年龄范围字符串
            
            // 解析期间范围
            String periodRange = request.getParameter("periodRange"); // 期间范围
            int[] periods = parseRange(periodRange); // 解析期间范围字符串
            
            // 如果年龄范围或期间范围解析失败，则设置错误信息并重新显示表单
            if (ages == null || periods == null) {
                request.setAttribute("errorMessage", "年齢または保険期間の範囲指定が無効です");
                showSimulationForm(request, response);
                return;
            }
            
            // 批量计算
            Map<String, Object> result = premiumService.batchCalculatePremium(
                productId, gender, ages, periods, insuredAmount
            );
            
            // 如果计算结果包含错误信息，则设置错误信息并重新显示表单
            if (result.containsKey("error")) {
                request.setAttribute("errorMessage", result.get("error"));
                showSimulationForm(request, response);
                return;
            }
            
            // 设置批量计算结果
            request.setAttribute("batchResult", result); // 批量计算结果
            request.setAttribute("productId", productId); // 产品ID
            request.setAttribute("gender", gender); // 性别
            request.setAttribute("insuredAmount", insuredAmount); // 保险金额
            
            // 获取请求转发器，指向批量计算结果页面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/batch_result.jsp");
            // 转发请求到JSP页面
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            // 如果参数格式不正确，设置错误信息并重新显示表单
            request.setAttribute("errorMessage", "入力値が無効です。数値を正しく入力してください。");
            showSimulationForm(request, response);
        } catch (Exception e) {
            // 如果计算过程中发生其他错误，设置错误信息并重新显示表单
            request.setAttribute("errorMessage", "バッチ計算中にエラーが発生しました: " + e.getMessage());
            showSimulationForm(request, response);
        }
    }
    
    /**
     * 显示料率表
     * 根据产品ID获取并显示料率表
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showRateTable(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取产品ID参数
            int productId = Integer.parseInt(request.getParameter("productId"));
            
            // 获取料率表
            Map<String, Object> result = premiumService.getPremiumRateTable(productId);
            
            // 如果获取结果包含错误信息，则设置错误信息并重新显示表单
            if (result.containsKey("error")) {
                request.setAttribute("errorMessage", result.get("error"));
                showSimulationForm(request, response);
                return;
            }
            
            // 设置料率表结果
            request.setAttribute("rateTableResult", result); // 料率表结果
            request.setAttribute("productId", productId); // 产品ID
            
            // 获取请求转发器，指向料率表页面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/premium/rate_table.jsp");
            // 转发请求到JSP页面
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            // 如果产品ID格式不正确，设置错误信息并重新显示表单
            request.setAttribute("errorMessage", "商品IDが無効です");
            showSimulationForm(request, response);
        } catch (Exception e) {
            // 如果获取过程中发生其他错误，设置错误信息并重新显示表单
            request.setAttribute("errorMessage", "料率表取得中にエラーが発生しました: " + e.getMessage());
            showSimulationForm(request, response);
        }
    }
    
    /**
     * 解析范围字符串（如 "25-35"）
     * 将范围字符串解析为整数数组
     * @param rangeStr 范围字符串
     * @return 解析后的整数数组，如果解析失败则返回null
     */
    private int[] parseRange(String rangeStr) {
        // 如果范围字符串为空，则返回null
        if (rangeStr == null || rangeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 按"-"分割范围字符串
            String[] parts = rangeStr.split("-");
            // 如果分割后的部分不是两个，则返回null
            if (parts.length != 2) {
                return null;
            }
            
            // 解析起始值和结束值
            int start = Integer.parseInt(parts[0].trim());
            int end = Integer.parseInt(parts[1].trim());
            
            // 如果起始值大于结束值，则返回null
            if (start > end) {
                return null;
            }
            
            // 创建结果数组并填充数据
            int length = end - start + 1;
            int[] result = new int[length];
            for (int i = 0; i < length; i++) {
                result[i] = start + i;
            }
            
            // 返回解析结果
            return result;
        } catch (NumberFormatException e) {
            // 如果解析过程中发生NumberFormatException，则返回null
            return null;
        }
    }
}