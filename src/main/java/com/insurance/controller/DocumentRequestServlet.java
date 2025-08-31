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
 * 用于处理资料请求相关的HTTP请求，包括增删改查等操作
 */
@WebServlet("/document")
public class DocumentRequestServlet extends HttpServlet {
    
    // 资料请求服务对象，用于处理业务逻辑
    private DocumentRequestService documentRequestService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     * @throws ServletException 当初始化过程中发生错误时抛出
     */
    @Override
    public void init() throws ServletException {
        // 调用父类的初始化方法
        super.init();
        // 创建资料请求服务实例
        documentRequestService = new DocumentRequestService();
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
        
        // 如果action参数为空，则默认设置为"list"
        if (action == null) {
            action = "list";
        }
        
        try {
            // 根据action值执行相应的操作
            switch (action) {
                case "list":
                    // 显示资料请求列表
                    listDocumentRequests(request, response);
                    break;
                case "view":
                    // 查看资料请求详情
                    viewDocumentRequest(request, response);
                    break;
                case "request":
                    // 显示请求表单
                    showRequestForm(request, response);
                    break;
                case "edit":
                    // 显示编辑表单
                    showEditForm(request, response);
                    break;
                case "delete":
                    // 删除资料请求
                    deleteDocumentRequest(request, response);
                    break;
                case "search":
                    // 搜索资料请求
                    searchDocumentRequests(request, response);
                    break;
                case "stats":
                    // 显示统计信息
                    showStatistics(request, response);
                    break;
                default:
                    // 默认显示资料请求列表
                    listDocumentRequests(request, response);
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
        
        // 如果action参数为空，则重定向到资料请求列表页面
        if (action == null) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            // 根据action值执行相应的操作
            switch (action) {
                case "add":
                    // 添加资料请求
                    addDocumentRequest(request, response);
                    break;
                case "update":
                    // 更新资料请求
                    updateDocumentRequest(request, response);
                    break;
                case "updateStatus":
                    // 更新请求状态
                    updateRequestStatus(request, response);
                    break;
                default:
                    // 默认重定向到资料请求列表页面
                    response.sendRedirect("document?action=list");
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 显示资料请求列表
     * 获取所有资料请求信息并转发到列表页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void listDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 调用服务层方法获取所有资料请求列表
        List<DocumentRequest> requests = documentRequestService.getAllDocumentRequests();
        // 将资料请求列表设置为请求属性，供JSP页面使用
        request.setAttribute("requests", requests);
        
        // 获取请求转发器，指向资料请求列表页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/list.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 查看资料请求详情
     * 根据资料请求ID获取详细信息并转发到详情页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void viewDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的资料请求ID
        String idStr = request.getParameter("id");
        // 如果ID为空，则重定向到资料请求列表页面
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 调用服务层方法根据ID获取资料请求信息
            DocumentRequest requestObj = documentRequestService.getDocumentRequestById(id);
            
            // 如果资料请求不存在，则设置错误信息并重定向到列表页面
            if (requestObj == null) {
                request.setAttribute("errorMessage", "資料請求が見つかりません");
                response.sendRedirect("document?action=list");
                return;
            }
            
            // 将资料请求信息设置为请求属性，供JSP页面使用
            request.setAttribute("documentRequest", requestObj);
            // 获取请求转发器，指向资料请求详情页面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/view.jsp");
            // 转发请求到JSP页面
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则设置错误信息并重定向到列表页面
            request.setAttribute("errorMessage", "無効なIDです");
            response.sendRedirect("document?action=list");
        }
    }
    
    /**
     * 显示请求表单
     * 生成新的请求编号并转发到请求表单页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showRequestForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 生成新的请求编号
        String requestNumber = documentRequestService.generateRequestNumber();
        // 将请求编号设置为请求属性，供JSP页面使用
        request.setAttribute("requestNumber", requestNumber);
        
        // 获取请求转发器，指向资料请求表单页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/request_form.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 显示编辑表单
     * 根据资料请求ID获取信息并转发到编辑表单页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的资料请求ID
        String idStr = request.getParameter("id");
        
        // 如果ID不为空，则尝试获取资料请求信息
        if (idStr != null && !idStr.isEmpty()) {
            try {
                // 将字符串ID转换为整数
                int id = Integer.parseInt(idStr);
                // 调用服务层方法根据ID获取资料请求信息
                DocumentRequest requestObj = documentRequestService.getDocumentRequestById(id);
                
                // 如果资料请求存在，则将资料请求信息设置为请求属性
                if (requestObj != null) {
                    request.setAttribute("documentRequest", requestObj);
                    // 设置编辑标志，用于JSP页面判断是新增还是编辑
                    request.setAttribute("isEdit", true);
                }
            } catch (NumberFormatException e) {
                // 如果ID格式不正确，则忽略错误，显示空表单
            }
        }
        
        // 设置错误消息（如果有）
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }
        
        // 获取请求转发器，指向资料请求表单页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/request_form.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 添加资料请求
     * 从请求参数中获取资料请求信息并添加到数据库
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void addDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 从请求参数中创建资料请求对象
        DocumentRequest documentRequest = createDocumentRequestFromRequest(request);
        // 验证资料请求信息
        String validationError = documentRequestService.validateDocumentRequest(documentRequest);
        
        // 如果验证失败，则设置错误信息并显示请求表单
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            request.setAttribute("documentRequest", documentRequest);
            showRequestForm(request, response);
            return;
        }
        
        // 调用服务层方法添加资料请求
        boolean success = documentRequestService.addDocumentRequest(documentRequest);
        
        // 根据添加结果进行相应处理
        if (success) {
            // 添加成功，重定向到列表页面并显示成功消息
            response.sendRedirect("document?action=list&message=資料請求を登録しました");
        } else {
            // 添加失败，设置错误信息并显示请求表单
            request.setAttribute("errorMessage", "資料請求の登録に失敗しました");
            request.setAttribute("documentRequest", documentRequest);
            showRequestForm(request, response);
        }
    }
    
    /**
     * 更新资料请求
     * 根据资料请求ID和请求参数更新资料请求信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void updateDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的资料请求ID
        String idStr = request.getParameter("id");
        // 如果ID为空，则重定向到资料请求列表页面
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 从请求参数中创建资料请求对象
            DocumentRequest documentRequest = createDocumentRequestFromRequest(request);
            // 设置资料请求ID
            documentRequest.setId(id);
            
            // 验证资料请求信息
            String validationError = documentRequestService.validateDocumentRequest(documentRequest);
            
            // 如果验证失败，则设置错误信息并显示编辑表单
            if (validationError != null) {
                request.setAttribute("errorMessage", validationError);
                request.setAttribute("documentRequest", documentRequest);
                request.setAttribute("isEdit", true);
                showEditForm(request, response);
                return;
            }
            
            // 调用服务层方法更新资料请求
            boolean success = documentRequestService.updateDocumentRequest(documentRequest);
            
            // 根据更新结果进行相应处理
            if (success) {
                // 更新成功，重定向到列表页面并显示成功消息
                response.sendRedirect("document?action=list&message=資料請求を更新しました");
            } else {
                // 更新失败，设置错误信息并显示编辑表单
                request.setAttribute("errorMessage", "資料請求の更新に失敗しました");
                request.setAttribute("documentRequest", documentRequest);
                request.setAttribute("isEdit", true);
                showEditForm(request, response);
            }
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则设置错误信息并重定向到列表页面
            request.setAttribute("errorMessage", "無効なIDです");
            response.sendRedirect("document?action=list");
        }
    }
    
    /**
     * 更新请求状态
     * 根据资料请求ID和状态值更新请求状态
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void updateRequestStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的资料请求ID和状态值
        String idStr = request.getParameter("id");
        String status = request.getParameter("status");
        
        // 如果ID或状态值为空，则重定向到资料请求列表页面
        if (idStr == null || status == null) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 调用服务层方法更新请求状态
            boolean success = documentRequestService.updateRequestStatus(id, status);
            
            // 根据更新结果进行相应处理
            if (success) {
                // 更新成功，重定向到列表页面并显示成功消息
                response.sendRedirect("document?action=list&message=ステータスを更新しました");
            } else {
                // 更新失败，重定向到列表页面并显示错误消息
                response.sendRedirect("document?action=list&error=ステータスの更新に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则重定向到列表页面并显示错误消息
            response.sendRedirect("document?action=list&error=無効なIDです");
        }
    }
    
    /**
     * 删除资料请求
     * 根据资料请求ID删除资料请求信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void deleteDocumentRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的资料请求ID
        String idStr = request.getParameter("id");
        // 如果ID为空，则重定向到资料请求列表页面
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("document?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 调用服务层方法删除资料请求
            boolean success = documentRequestService.deleteDocumentRequest(id);
            
            // 根据删除结果进行相应处理
            if (success) {
                // 删除成功，重定向到列表页面并显示成功消息
                response.sendRedirect("document?action=list&message=資料請求を削除しました");
            } else {
                // 删除失败，重定向到列表页面并显示错误消息
                response.sendRedirect("document?action=list&error=資料請求の削除に失敗しました");
            }
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则重定向到列表页面并显示错误消息
            response.sendRedirect("document?action=list&error=無効なIDです");
        }
    }
    
    /**
     * 搜索资料请求
     * 根据关键字搜索资料请求信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void searchDocumentRequests(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的搜索关键字
        String keyword = request.getParameter("keyword");
        // 调用服务层方法搜索资料请求
        List<DocumentRequest> requests = documentRequestService.searchDocumentRequests(keyword);
        
        // 将搜索结果和关键字设置为请求属性，供JSP页面使用
        request.setAttribute("requests", requests);
        request.setAttribute("searchKeyword", keyword);
        
        // 获取请求转发器，指向资料请求列表页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/list.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 显示统计信息
     * 获取资料请求统计信息并转发到统计页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showStatistics(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 调用服务层方法获取资料请求统计信息
        var stats = documentRequestService.getRequestStatistics();
        // 将统计信息设置为请求属性，供JSP页面使用
        request.setAttribute("stats", stats);
        
        // 获取请求转发器，指向资料请求统计页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/document/stats.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 从请求参数创建DocumentRequest对象
     * 提取请求中的参数并封装成DocumentRequest对象
     * @param request HTTP请求对象
     * @return DocumentRequest对象
     */
    private DocumentRequest createDocumentRequestFromRequest(HttpServletRequest request) {
        // 创建新的资料请求对象
        DocumentRequest documentRequest = new DocumentRequest();
        
        // 设置请求编号
        documentRequest.setRequestNumber(request.getParameter("requestNumber"));
        
        // 设置客户ID
        String customerIdStr = request.getParameter("customerId");
        if (customerIdStr != null && !customerIdStr.isEmpty()) {
            try {
                documentRequest.setCustomerId(Integer.parseInt(customerIdStr));
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        // 设置产品ID
        String productIdStr = request.getParameter("productId");
        if (productIdStr != null && !productIdStr.isEmpty()) {
            try {
                documentRequest.setProductId(Integer.parseInt(productIdStr));
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        // 设置请求类型
        documentRequest.setRequestType(request.getParameter("requestType"));
        // 设置请求状态
        documentRequest.setRequestStatus(request.getParameter("requestStatus"));
        // 设置请求的文件
        documentRequest.setRequestedDocuments(request.getParameter("requestedDocuments"));
        // 设置邮寄地址
        documentRequest.setShippingAddress(request.getParameter("shippingAddress"));
        // 设置邮寄方式
        documentRequest.setShippingMethod(request.getParameter("shippingMethod"));
        // 设置联系方式偏好
        documentRequest.setContactPreference(request.getParameter("contactPreference"));
        // 设置备注
        documentRequest.setNotes(request.getParameter("notes"));
        
        // 处理跟进日期
        String followUpDateStr = request.getParameter("followUpDate");
        if (followUpDateStr != null && !followUpDateStr.isEmpty()) {
            try {
                // 创建日期格式化对象
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                // 解析日期字符串
                Date followUpDate = sdf.parse(followUpDateStr);
                // 设置跟进日期
                documentRequest.setFollowUpDate(followUpDate);
            } catch (ParseException e) {
                // 日期格式错误
            }
        }
        
        // 设置销售人员ID
        String salesPersonIdStr = request.getParameter("salesPersonId");
        if (salesPersonIdStr != null && !salesPersonIdStr.isEmpty()) {
            try {
                documentRequest.setSalesPersonId(Integer.parseInt(salesPersonIdStr));
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        // 返回创建的资料请求对象
        return documentRequest;
    }
}