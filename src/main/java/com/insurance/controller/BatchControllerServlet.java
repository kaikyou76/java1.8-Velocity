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
 */
@WebServlet("/admin/batch")
public class BatchControllerServlet extends HttpServlet {
    
    /**
     * GETリクエスト処理 - バッチ状態表示
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("status".equals(action)) {
            // バッチ状態表示
            showBatchStatus(request, response);
        } else {
            // デフォルトはバッチ管理ページ表示
            request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
        }
    }
    
    /**
     * POSTリクエスト処理 - バッチ手動実行
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        switch (action) {
            case "premium_update":
                executePremiumUpdateBatch(request, response);
                break;
            case "contract_status":
                executeContractStatusBatch(request, response);
                break;
            case "report_generation":
                executeReportGenerationBatch(request, response);
                break;
            case "all_batches":
                executeAllBatches(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なアクションです");
        }
    }
    
    /**
     * バッチ状態表示
     */
    private void showBatchStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // シンプルなステータス情報（実際の実装ではより詳細な情報を提供）
        request.setAttribute("batchStatus", "すべてのバッチ処理が正常に実行中です");
        request.setAttribute("lastExecution", new java.util.Date());
        
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"status\":\"running\",\"lastExecution\":\"" + new java.util.Date() + "\"}"
        );
    }
    
    /**
     * 保険料更新バッチを手動実行
     */
    private void executePremiumUpdateBatch(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            PremiumUpdateBatch.manualExecute();
            
            request.setAttribute("message", "保険料更新バッチを手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
    
    /**
     * 契約ステータス更新バッチを手動実行
     */
    private void executeContractStatusBatch(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            ContractStatusBatch.manualExecute();
            
            request.setAttribute("message", "契約ステータス更新バッチを手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
    
    /**
     * レポート生成バッチを手動実行
     */
    private void executeReportGenerationBatch(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            ReportGenerationBatch.manualExecute();
            
            request.setAttribute("message", "レポート生成バッチを手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
    
    /**
     * すべてのバッチを手動実行
     */
    private void executeAllBatches(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            PremiumUpdateBatch.manualExecute();
            ContractStatusBatch.manualExecute();
            ReportGenerationBatch.manualExecute();
            
            request.setAttribute("message", "すべてのバッチ処理を手動実行しました");
            request.setAttribute("messageType", "success");
            
        } catch (Exception e) {
            request.setAttribute("message", "バッチ実行中にエラーが発生しました: " + e.getMessage());
            request.setAttribute("messageType", "error");
        }
        
        request.getRequestDispatcher("/WEB-INF/views/admin/batch.jsp").forward(request, response);
    }
}