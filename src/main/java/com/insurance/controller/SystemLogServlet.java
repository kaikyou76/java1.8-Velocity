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
 * 用于处理系统日志相关的HTTP请求，包括日志列表显示、日志详情查看、日志搜索、统计信息显示和日志清理等功能
 */
@WebServlet("/admin/logs")
public class SystemLogServlet extends HttpServlet {
    
    // 系统日志服务对象，用于处理日志相关业务逻辑
    private SystemLogService logService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     * @throws ServletException 当初始化过程中发生错误时抛出
     */
    @Override
    public void init() throws ServletException {
        // 调用父类的初始化方法
        super.init();
        // 创建系统日志服务实例
        this.logService = new SystemLogService();
    }
    
    /**
     * GET请求处理 - 显示日志列表
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
        if ("cleanup".equals(action)) {
            // 清理旧日志
            cleanupLogs(request, response);
        } else {
            // 发送错误响应，表示无效操作
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なアクションです");
        }
    }
    
    /**
     * 显示日志列表
     * 获取所有日志信息并转发到日志列表页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showLogList(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 调用服务层方法获取所有日志列表
        List<SystemLog> logs = logService.getAllLogs();
        // 将日志列表设置为请求属性，供JSP页面使用
        request.setAttribute("logs", logs);
        // 将日志总数设置为请求属性，供JSP页面使用
        request.setAttribute("totalCount", logService.getTotalLogCount());
        
        // 获取请求转发器，指向日志列表页面
        request.getRequestDispatcher("/WEB-INF/views/admin/logs.jsp").forward(request, response);
    }
    
    /**
     * 查看日志详情
     * 根据日志ID获取日志详细信息并转发到日志详情页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void viewLogDetail(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的日志ID
        String idStr = request.getParameter("id");
        // 如果ID为空，则发送错误响应
        if (idStr == null || idStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ログIDが指定されていません");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 这里简化实现，实际应该通过DAO获取单个日志
            // 获取所有日志列表
            List<SystemLog> allLogs = logService.getAllLogs();
            // 从日志列表中查找指定ID的日志
            SystemLog log = allLogs.stream()
                    .filter(l -> l.getId() == id) // 过滤出ID匹配的日志
                    .findFirst() // 获取第一个匹配的日志
                    .orElse(null); // 如果没有找到则返回null
            
            // 如果日志不存在，则发送错误响应
            if (log == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "指定されたログが見つかりません");
                return;
            }
            
            // 将日志信息设置为请求属性，供JSP页面使用
            request.setAttribute("log", log);
            // 获取请求转发器，指向日志详情页面
            request.getRequestDispatcher("/WEB-INF/views/admin/log-detail.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则发送错误响应
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なログIDです");
        }
    }
    
    /**
     * 搜索日志
     * 根据搜索条件搜索日志信息并转发到日志列表页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void searchLogs(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取搜索条件参数
        String level = request.getParameter("level"); // 日志级别
        String module = request.getParameter("module"); // 模块名称
        String keyword = request.getParameter("keyword"); // 关键词
        String startDateStr = request.getParameter("startDate"); // 开始日期
        String endDateStr = request.getParameter("endDate"); // 结束日期
        
        // 初始化日期变量
        Date startDate = null;
        Date endDate = null;
        
        // 创建日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            // 解析开始日期
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = dateFormat.parse(startDateStr);
            }
            // 解析结束日期
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = dateFormat.parse(endDateStr);
            }
        } catch (ParseException e) {
            // 如果日期格式不正确，则设置错误信息
            request.setAttribute("error", "日付形式が無効です");
        }
        
        // 调用服务层方法搜索日志
        List<SystemLog> logs = logService.searchLogs(level, module, keyword, startDate, endDate);
        
        // 将搜索结果和搜索条件设置为请求属性，供JSP页面使用
        request.setAttribute("logs", logs);
        request.setAttribute("searchLevel", level);
        request.setAttribute("searchModule", module);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("searchStartDate", startDateStr);
        request.setAttribute("searchEndDate", endDateStr);
        request.setAttribute("isSearch", true);
        
        // 获取请求转发器，指向日志列表页面
        request.getRequestDispatcher("/WEB-INF/views/admin/logs.jsp").forward(request, response);
    }
    
    /**
     * 显示统计信息
     * 获取日志统计信息并转发到统计信息页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 调用服务层方法获取日志级别统计信息
        List<Object[]> levelStats = logService.getLogStatistics();
        // 调用服务层方法获取模块统计信息
        List<Object[]> moduleStats = logService.getModuleStatistics();
        
        // 将统计信息设置为请求属性，供JSP页面使用
        request.setAttribute("levelStats", levelStats);
        request.setAttribute("moduleStats", moduleStats);
        request.setAttribute("totalLogs", logService.getTotalLogCount());
        
        // 获取请求转发器，指向日志统计信息页面
        request.getRequestDispatcher("/WEB-INF/views/admin/log-statistics.jsp").forward(request, response);
    }
    
    /**
     * 清理日志
     * 根据保留天数清理旧日志并转发到日志列表页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void cleanupLogs(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的保留天数
        String daysStr = request.getParameter("days");
        int daysToKeep = 30; // 默认保留30天
        
        // 如果天数参数不为空，则尝试解析为整数
        if (daysStr != null && !daysStr.isEmpty()) {
            try {
                daysToKeep = Integer.parseInt(daysStr);
            } catch (NumberFormatException e) {
                // 如果天数格式不正确，则设置错误信息
                request.setAttribute("error", "無効な日数です");
            }
        }
        
        // 调用服务层方法清理旧日志
        int deletedCount = logService.cleanupOldLogs(daysToKeep);
        
        // 设置成功消息
        request.setAttribute("message", deletedCount + "件の古いログを削除しました");
        request.setAttribute("messageType", "success");
        
        // 返回日志列表
        showLogList(request, response);
    }
}