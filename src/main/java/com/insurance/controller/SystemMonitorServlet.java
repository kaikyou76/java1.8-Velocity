package com.insurance.controller;

import com.insurance.monitor.SystemMonitor;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * 系统监控控制器
 */
@WebServlet("/admin/monitor")
public class SystemMonitorServlet extends HttpServlet {
    
    /**
     * GET请求处理 - 显示监控信息
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
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
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("refresh".equals(action)) {
            // 手动刷新监控数据
            refreshMonitoring(request, response);
        } else if ("report".equals(action)) {
            // 生成监控报告
            generateReport(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なアクションです");
        }
    }
    
    /**
     * 显示监控页面
     */
    private void showMonitorPage(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 这里可以添加实时监控数据的获取
        request.setAttribute("monitorActive", true);
        
        request.getRequestDispatcher("/WEB-INF/views/admin/monitor.jsp").forward(request, response);
    }
    
    /**
     * 获取系统状态
     */
    private void getSystemStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String statusReport = SystemMonitor.getSystemStatusReport();
            
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(statusReport);
            
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "システムステータスの取得中にエラーが発生しました: " + e.getMessage());
        }
    }
    
    /**
     * 手动刷新监控数据
     */
    private void refreshMonitoring(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            SystemMonitor.manualSystemCheck();
            
            request.setAttribute("message", "モニタリングデータを手動更新しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            request.setAttribute("message", "モニタリング更新中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        showMonitorPage(request, response);
    }
    
    /**
     * 生成监控报告
     */
    private void generateReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String report = SystemMonitor.getSystemStatusReport();
            
            // 设置下载头
            response.setContentType("text/plain;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=system_report.txt");
            
            response.getWriter().write(report);
            
        } catch (Exception e) {
            request.setAttribute("message", "レポート生成中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
            showMonitorPage(request, response);
        }
    }
}