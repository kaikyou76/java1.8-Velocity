package com.insurance.controller;

import com.insurance.monitor.SystemMonitor;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * 系统监控控制器
 * 用于处理系统监控相关的HTTP请求，包括显示监控信息、获取系统状态、手动刷新监控数据和生成监控报告等功能
 */
@WebServlet("/admin/monitor")
public class SystemMonitorServlet extends HttpServlet {
    
    /**
     * GET请求处理 - 显示监控信息
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
        
        // 根据action值执行相应的操作
        if ("status".equals(action)) {
            // 获取系统状态
            getSystemStatus(request, response);
        } else {
            // 显示监控页面
            showMonitorPage(request, response);
        }
    }
    
    /**
     * POST请求处理 - 执行监控操作
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
        
        // 根据action值执行相应的操作
        if ("refresh".equals(action)) {
            // 手动刷新监控数据
            refreshMonitoring(request, response);
        } else if ("report".equals(action)) {
            // 生成监控报告
            generateReport(request, response);
        } else {
            // 发送错误响应，表示无效操作
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なアクションです");
        }
    }
    
    /**
     * 显示监控页面
     * 设置监控页面所需的属性并转发到监控页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showMonitorPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 这里可以添加实时监控数据的获取
        // 设置监控激活状态为true
        request.setAttribute("monitorActive", true);
        
        // 获取请求转发器，指向监控页面
        request.getRequestDispatcher("/WEB-INF/views/admin/monitor.jsp").forward(request, response);
    }
    
    /**
     * 获取系统状态
     * 获取系统状态报告并以纯文本格式返回
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void getSystemStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用系统监控类获取系统状态报告
            String statusReport = SystemMonitor.getSystemStatusReport();
            
            // 设置响应内容类型为纯文本，字符集为UTF-8
            response.setContentType("text/plain;charset=UTF-8");
            // 将状态报告写入响应输出流
            response.getWriter().write(statusReport);
            
        } catch (Exception e) {
            // 如果获取系统状态过程中发生异常，则发送错误响应
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "システムステータスの取得中にエラーが発生しました: " + e.getMessage());
        }
    }
    
    /**
     * 手动刷新监控数据
     * 执行手动系统检查并刷新监控数据
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void refreshMonitoring(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用系统监控类执行手动系统检查
            SystemMonitor.manualSystemCheck();
            
            // 设置成功消息和消息类型
            request.setAttribute("message", "モニタリングデータを手動更新しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            // 如果刷新监控数据过程中发生异常，则设置错误消息和消息类型
            request.setAttribute("message", "モニタリング更新中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        // 显示监控页面
        showMonitorPage(request, response);
    }
    
    /**
     * 生成监控报告
     * 生成系统状态报告并以文件下载形式返回
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void generateReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 调用系统监控类获取系统状态报告
            String report = SystemMonitor.getSystemStatusReport();
            
            // 设置下载头
            // 设置响应内容类型为纯文本，字符集为UTF-8
            response.setContentType("text/plain;charset=UTF-8");
            // 设置响应头，指定文件下载名称
            response.setHeader("Content-Disposition", "attachment; filename=system_report.txt");
            
            // 将报告写入响应输出流
            response.getWriter().write(report);
            
        } catch (Exception e) {
            // 如果生成报告过程中发生异常，则设置错误消息和消息类型，并显示监控页面
            request.setAttribute("message", "レポート生成中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
            showMonitorPage(request, response);
        }
    }
}