package com.insurance.controller;

import com.insurance.model.DocumentRequest;
import com.insurance.service.DocumentRequestService;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 资料请求管理Servlet
 */
@WebServlet("/document")
public class DocumentRequestServlet extends HttpServlet {
    
    private DocumentRequestService documentRequestService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        documentRequestService = new DocumentRequestService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list";
        }
        
        try {
            switch (action) {
                case "list":
                    listDocumentRequests(request, response);
                    break;
                case "view":
                    viewDocumentRequest(request, response);
                    break;
                case "request":
                    showRequestForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteDocumentRequest(request, response);
                    break;
                case "search":
                    searchDocumentRequests(request, response);
                    break;
                case "stats":
                    showStatistics(request, response);
                    break;
                default:
                    listDocumentRequests(request, response);
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
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            switch (action) {
                case "add":
                    addDocumentRequest(request, response);
                    break;
                case "update":
                    updateDocumentRequest(request, response);
                    break;
                case "updateStatus":
                    updateRequestStatus(request, response);
                    break;
                default:
                    response.sendRedirect("document?action=list");
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    /**
     * 显示资料请求列表
     */
    private void listDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
        request.setAttribute("requests", requests);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/list.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 查看资料请求详情
     */
    private void viewDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            DocumentRequest requestObj = documentRequestService.getDocumentRequestById(id);
            
            if (requestObj == null) {
                request.setAttribute("errorMessage", "資料請求が見つかりません");
                response.sendRedirect("document?action=list");
                return;
            }
            
            request.setAttribute("documentRequest", requestObj);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/view.jsp");
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "無効なIDです");
            response.sendRedirect("document?action=list");
        }
    }
    
    /**
     * 显示请求表单
     */
    private void showRequestForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 生成新的请求编号
        String requestNumber = documentRequestService.generateRequestNumber();
        request.setAttribute("requestNumber", requestNumber);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/request_form.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 显示编辑表单
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                DocumentRequest requestObj = documentRequestService.getDocumentRequestById(id);
                
                if (requestObj != null) {
                    request.setAttribute("documentRequest", requestObj);
                    request.setAttribute("isEdit", true);
                }
            } catch (NumberFormatException e) {
                // 忽略错误，显示空表单
            }
        }
        
        // 设置错误消息（如果有）
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/request_form.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 添加资料请求
     */
    private void addDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        DocumentRequest documentRequest = createDocumentRequestFromRequest(request);
        String validationError = documentRequestService.validateDocumentRequest(documentRequest);
        
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            request.setAttribute("documentRequest", documentRequest);
            showRequestForm(request, response);
            return;
        }
        
        boolean success = documentRequestService.addDocumentRequest(documentRequest);
        
        if (success) {
            response.sendRedirect("document?action=list&message=資料請求を登録しました");
        } else {
            request.setAttribute("errorMessage", "資料請求の登録に失敗しました");
            request.setAttribute("documentRequest", documentRequest);
            showRequestForm(request, response);
        }
    }
    
    /**
     * 更新资料请求
     */
    private void updateDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            DocumentRequest documentRequest = createDocumentRequestFromRequest(request);
            documentRequest.setId(id);
            
            String validationError = documentRequestService.validateDocumentRequest(documentRequest);
            
            if (validationError != null) {
                request.setAttribute("errorMessage", validationError);
                request.setAttribute("documentRequest", documentRequest);
                request.setAttribute("isEdit", true);
                showEditForm(request, response);
                return;
            }
            
            boolean success = documentRequestService.updateDocumentRequest(documentRequest);
            
            if (success) {
                response.sendRedirect("document?action=list&message=資料請求を更新しました");
            } else {
                request.setAttribute("errorMessage", "資料請求の更新に失敗しました");
                request.setAttribute("documentRequest", documentRequest);
                request.setAttribute("isEdit", true);
                showEditForm(request, response);
            }
            
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "無効なIDです");
            response.sendRedirect("document?action=list");
        }
    }
    
    /**
     * 更新请求状态
     */
    private void updateRequestStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        String status = request.getParameter("status");
        
        if (idStr == null || status == null) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            boolean success = documentRequestService.updateRequestStatus(id, status);
            
            if (success) {
                response.sendRedirect("document?action=list&message=ステータスを更新しました");
            } else {
                response.sendRedirect("document?action=list&error=ステータスの更新に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect("document?action=list&error=無効なIDです");
        }
    }
    
    /**
     * 删除资料请求
     */
    private void deleteDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            boolean success = documentRequestService.deleteDocumentRequest(id);
            
            if (success) {
                response.sendRedirect("document?action=list&message=資料請求を削除しました");
            } else {
                response.sendRedirect("document?action=list&error=資料請求の削除に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect("document?action=list&error=無効なIDです");
        }
    }
    
    /**
     * 搜索资料请求
     */
    private void searchDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        List<DocumentRequest> requests = documentRequestService.searchDocumentRequests(keyword);
        
        request.setAttribute("requests", requests);
        request.setAttribute("searchKeyword", keyword);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/list.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 显示统计信息
     */
    private void showStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        var stats = documentRequestService.getRequestStatistics();
        request.setAttribute("stats", stats);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/stats.jsp");
        dispatcher.forward(request, response);
    }
    
    /**
     * 从请求参数创建DocumentRequest对象
     */
    private DocumentRequest createDocumentRequestFromRequest(HttpServletRequest request) {
        DocumentRequest documentRequest = new DocumentRequest();
        
        documentRequest.setRequestNumber(request.getParameter("requestNumber"));
        
        String customerIdStr = request.getParameter("customerId");
        if (customerIdStr != null && !customerIdStr.isEmpty()) {
            try {
                documentRequest.setCustomerId(Integer.parseInt(customerIdStr));
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        String productIdStr = request.getParameter("productId");
        if (productIdStr != null && !productIdStr.isEmpty()) {
            try {
                documentRequest.setProductId(Integer.parseInt(productIdStr));
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        documentRequest.setRequestType(request.getParameter("requestType"));
        documentRequest.setRequestStatus(request.getParameter("requestStatus"));
        documentRequest.setRequestedDocuments(request.getParameter("requestedDocuments"));
        documentRequest.setShippingAddress(request.getParameter("shippingAddress"));
        documentRequest.setShippingMethod(request.getParameter("shippingMethod"));
        documentRequest.setContactPreference(request.getParameter("contactPreference"));
        documentRequest.setNotes(request.getParameter("notes"));
        
        // 处理跟进日期
        String followUpDateStr = request.getParameter("followUpDate");
        if (followUpDateStr != null && !followUpDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date followUpDate = sdf.parse(followUpDateStr);
                documentRequest.setFollowUpDate(followUpDate);
            } catch (ParseException e) {
                // 日期格式错误
            }
        }
        
        String salesPersonIdStr = request.getParameter("salesPersonId");
        if (salesPersonIdStr != null && !salesPersonIdStr.isEmpty()) {
            try {
                documentRequest.setSalesPersonId(Integer.parseInt(salesPersonIdStr));
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        return documentRequest;
    }
}