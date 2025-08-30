package com.insurance.controller;

import com.insurance.model.SystemLog;
import com.insurance.service.SystemLogService;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 系统日志管理控制器
 */
@WebServlet("/admin/logs")
public class SystemLogServlet extends HttpServlet {
    
    private SystemLogService logService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.logService = new SystemLogService();
    }
    
    /**
     * GET请求处理 - 显示日志列表
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("view".equals(action)) {
            // 查看单个日志详情
            viewLogDetail(request, response);
        } else if ("search".equals(action)) {
            // 搜索日志
            searchLogs(request, response);
        } else if ("statistics".equals(action)) {
            // 显示统计信息
            showStatistics(request, response);
        } else {
            // 默认显示日志列表
            showLogList(request, response);
        }
    }
    
    /**
     * POST请求处理 - 清理日志
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("cleanup".equals(action)) {
            // 清理旧日志
            cleanupLogs(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なアクションです");
        }
    }
    
    /**
     * 显示日志列表
     */
    private void showLogList(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<SystemLog> logs = logService.getAllLogs();
        request.setAttribute("logs", logs);
        request.setAttribute("totalCount", logService.getTotalLogCount());
        
        request.getRequestDispatcher("/WEB-INF/views/admin/logs.jsp").forward(request, response);
    }
    
    /**
     * 查看日志详情
     */
    private void viewLogDetail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ログIDが指定されていません");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            // 这里简化实现，实际应该通过DAO获取单个日志
            List<SystemLog> allLogs = logService.getAllLogs();
            SystemLog log = allLogs.stream()
                    .filter(l -> l.getId() == id)
                    .findFirst()
                    .orElse(null);
            
            if (log == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "指定されたログが見つかりません");
                return;
            }
            
            request.setAttribute("log", log);
            request.getRequestDispatcher("/WEB-INF/views/admin/log-detail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なログIDです");
        }
    }
    
    /**
     * 搜索日志
     */
    private void searchLogs(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String level = request.getParameter("level");
        String module = request.getParameter("module");
        String keyword = request.getParameter("keyword");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        Date startDate = null;
        Date endDate = null;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = dateFormat.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = dateFormat.parse(endDateStr);
            }
        } catch (ParseException e) {
            request.setAttribute("error", "日付形式が無効です");
        }
        
        List<SystemLog> logs = logService.searchLogs(level, module, keyword, startDate, endDate);
        
        request.setAttribute("logs", logs);
        request.setAttribute("searchLevel", level);
        request.setAttribute("searchModule", module);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("searchStartDate", startDateStr);
        request.setAttribute("searchEndDate", endDateStr);
        request.setAttribute("isSearch", true);
        
        request.getRequestDispatcher("/WEB-INF/views/admin/logs.jsp").forward(request, response);
    }
    
    /**
     * 显示统计信息
     */
    private void showStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Object[]> levelStats = logService.getLogStatistics();
        List<Object[]> moduleStats = logService.getModuleStatistics();
        
        request.setAttribute("levelStats", levelStats);
        request.setAttribute("moduleStats", moduleStats);
        request.setAttribute("totalLogs", logService.getTotalLogCount());
        
        request.getRequestDispatcher("/WEB-INF/views/admin/log-statistics.jsp").forward(request, response);
    }
    
    /**
     * 清理日志
     */
    private void cleanupLogs(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String daysStr = request.getParameter("days");
        int daysToKeep = 30; // 默认保留30天
        
        if (daysStr != null && !daysStr.isEmpty()) {
            try {
                daysToKeep = Integer.parseInt(daysStr);
            } catch (NumberFormatException e) {
                request.setAttribute("error", "無効な日数です");
            }
        }
        
        int deletedCount = logService.cleanupOldLogs(daysToKeep);
        
        request.setAttribute("message", deletedCount + "件の古いログを削除しました");
        request.setAttribute("messageType", "success");
        
        // 返回日志列表
        showLogList(request, response);
    }
}