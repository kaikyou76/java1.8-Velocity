package com.insurance.controller;

import com.insurance.batch.ContractStatusBatch;
import com.insurance.batch.PremiumUpdateBatch;
import com.insurance.batch.ReportGenerationBatch;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * バッチ処理管理コントローラー
 * バッチ処理の手動実行と状態確認を提供
 * 批量处理管理控制器
 * 提供批量处理的手动执行和状态检查功能
 */
@WebServlet("/admin/batch")
public class BatchControllerServlet extends HttpServlet {
    
    /**
     * GETリクエスト処理 - バッチ状態表示
     * GET请求处理 - 显示批处理状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的操作类型
        String action = request.getParameter("action");
        
        // 根据操作类型执行相应处理
        if ("status".equals(action)) {
            // バッチ状態表示
            // 显示批处理状态
            showBatchStatus(request, response);
        } else {
            // デフォルトはバッチ管理ページ表示
            // 默认显示批处理管理页面
            request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
        }
    }
    
    /**
     * POSTリクエスト処理 - バッチ手動実行
     * POST请求处理 - 手动执行批处理
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的操作类型
        String action = request.getParameter("action");
        
        // 根据操作类型执行相应的批处理
        switch (action) {
            case "premium_update":
                // 执行保险费更新批处理
                executePremiumUpdateBatch(request, response);
                break;
            case "contract_status":
                // 执行合同状态更新批处理
                executeContractStatusBatch(request, response);
                break;
            case "report_generation":
                // 执行报告生成批处理
                executeReportGenerationBatch(request, response);
                break;
            case "all_batches":
                // 执行所有批处理
                executeAllBatches(request, response);
                break;
            default:
                // 发送错误响应，表示无效操作
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なアクションです");
        }
    }
    
    /**
     * バッチ状態表示
     * 显示批处理状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void showBatchStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // シンプルなステータス情報（実際の実装ではより詳細な情報を提供）
        // 简单的状态信息（实际实现中会提供更详细的信息）
        request.setAttribute("batchStatus", "すべてのバッチ処理が正常に実行中です");
        request.setAttribute("lastExecution", new java.util.Date());
        
        // 设置响应内容类型为JSON
        response.setContentType("application/json");
        // 向客户端写入JSON格式的状态信息
        response.getWriter().write(
            "{\"status\":\"running\",\"lastExecution\":\"" + new java.util.Date() + "\"}"
        );
    }
    
    /**
     * 保険料更新バッチを手動実行
     * 手动执行保险费更新批处理
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void executePremiumUpdateBatch(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 使用try-catch处理可能的异常
        try {
            // 调用保险费更新批处理的手动执行方法
            PremiumUpdateBatch.manualExecute();
            
            // 设置成功消息和消息类型
            request.setAttribute("message", "保険料更新バッチを手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            // 捕获异常并设置错误消息和消息类型
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        // 转发请求到批处理管理页面
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
    
    /**
     * 契約ステータス更新バッチを手動実行
     * 手动执行合同状态更新批处理
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void executeContractStatusBatch(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 使用try-catch处理可能的异常
        try {
            // 调用合同状态更新批处理的手动执行方法
            ContractStatusBatch.manualExecute();
            
            // 设置成功消息和消息类型
            request.setAttribute("message", "契約ステータス更新バッチを手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            // 捕获异常并设置错误消息和消息类型
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        // 转发请求到批处理管理页面
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
    
    /**
     * レポート生成バッチを手動実行
     * 手动执行报告生成批处理
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void executeReportGenerationBatch(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 使用try-catch处理可能的异常
        try {
            // 调用报告生成批处理的手动执行方法
            ReportGenerationBatch.manualExecute();
            
            // 设置成功消息和消息类型
            request.setAttribute("message", "レポート生成バッチを手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            // 捕获异常并设置错误消息和消息类型
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        // 转发请求到批处理管理页面
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
    
    /**
     * すべてのバッチを手動実行
     * 手动执行所有批处理
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    private void executeAllBatches(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 使用try-catch处理可能的异常
        try {
            // 依次执行所有批处理的手动执行方法
            PremiumUpdateBatch.manualExecute();
            ContractStatusBatch.manualExecute();
            ReportGenerationBatch.manualExecute();
            
            // 设置成功消息和消息类型
            request.setAttribute("message", "すべてのバッチ処理を手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            // 捕获异常并设置错误消息和消息类型
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        // 转发请求到批处理管理页面
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
}