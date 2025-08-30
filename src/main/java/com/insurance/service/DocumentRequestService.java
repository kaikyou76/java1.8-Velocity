package com.insurance.service;

import com.insurance.dao.DocumentRequestDAO;
import com.insurance.model.DocumentRequest;
import java.util.List;
import java.util.Map;

/**
 * 资料请求业务逻辑服务类
 */
public class DocumentRequestService {
    
    private DocumentRequestDAO documentRequestDAO;
    
    public DocumentRequestService() {
        this.documentRequestDAO = new DocumentRequestDAO();
    }
    
    /**
     * 获取资料请求信息
     */
    public DocumentRequest getDocumentRequestById(int id) {
        return documentRequestDAO.getDocumentRequestById(id);
    }
    
    public DocumentRequest getDocumentRequestByNumber(String requestNumber) {
        return documentRequestDAO.getDocumentRequestByNumber(requestNumber);
    }
    
    /**
     * 获取所有资料请求列表
     */
    public List<DocumentRequest> getAllDocumentRequests() {
        return documentRequestDAO.getAllDocumentRequests();
    }
    
    /**
     * 根据顾客ID获取资料请求
     */
    public List<DocumentRequest> getRequestsByCustomerId(int customerId) {
        return documentRequestDAO.getRequestsByCustomerId(customerId);
    }
    
    /**
     * 根据状态获取资料请求
     */
    public List<DocumentRequest> getRequestsByStatus(String status) {
        return documentRequestDAO.getRequestsByStatus(status);
    }
    
    /**
     * 添加资料请求
     */
    public boolean addDocumentRequest(DocumentRequest request) {
        // 业务逻辑验证
        String validationError = validateDocumentRequest(request);
        if (validationError != null) {
            return false;
        }
        
        // 生成请求编号
        if (request.getRequestNumber() == null || request.getRequestNumber().isEmpty()) {
            String requestNumber = documentRequestDAO.generateRequestNumber();
            request.setRequestNumber(requestNumber);
        }
        
        // 设置默认状态
        if (request.getRequestStatus() == null || request.getRequestStatus().isEmpty()) {
            request.setRequestStatus("受付");
        }
        
        return documentRequestDAO.addDocumentRequest(request);
    }
    
    /**
     * 更新资料请求
     */
    public boolean updateDocumentRequest(DocumentRequest request) {
        if (request == null || request.getId() <= 0) {
            return false;
        }
        
        // 检查请求是否存在
        DocumentRequest existingRequest = documentRequestDAO.getDocumentRequestById(request.getId());
        if (existingRequest == null) {
            return false;
        }
        
        // 业务逻辑验证
        String validationError = validateDocumentRequest(request);
        if (validationError != null) {
            return false;
        }
        
        return documentRequestDAO.updateDocumentRequest(request);
    }
    
    /**
     * 更新请求状态
     */
    public boolean updateRequestStatus(int id, String status) {
        if (id <= 0 || status == null || status.isEmpty()) {
            return false;
        }
        
        // 验证状态值
        if (!isValidStatus(status)) {
            return false;
        }
        
        // 检查请求是否存在
        DocumentRequest existingRequest = documentRequestDAO.getDocumentRequestById(id);
        if (existingRequest == null) {
            return false;
        }
        
        return documentRequestDAO.updateRequestStatus(id, status);
    }
    
    /**
     * 删除资料请求
     */
    public boolean deleteDocumentRequest(int id) {
        if (id <= 0) {
            return false;
        }
        
        // 检查请求是否存在
        DocumentRequest existingRequest = documentRequestDAO.getDocumentRequestById(id);
        if (existingRequest == null) {
            return false;
        }
        
        return documentRequestDAO.deleteDocumentRequest(id);
    }
    
    /**
     * 搜索资料请求
     */
    public List<DocumentRequest> searchDocumentRequests(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return documentRequestDAO.getAllDocumentRequests();
        }
        
        return documentRequestDAO.searchDocumentRequests(keyword.trim());
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Integer> getRequestStatistics() {
        return documentRequestDAO.getRequestStatistics();
    }
    
    /**
     * 生成新的请求编号
     */
    public String generateRequestNumber() {
        return documentRequestDAO.generateRequestNumber();
    }
    
    /**
     * 验证资料请求信息
     */
    public String validateDocumentRequest(DocumentRequest request) {
        if (request == null) {
            return "リクエスト情報がありません";
        }
        
        if (request.getCustomerId() <= 0) {
            return "顧客IDが無効です";
        }
        
        if (request.getRequestType() == null || request.getRequestType().isEmpty()) {
            return "リクエストタイプを選択してください";
        }
        
        if (!request.isValidRequestType()) {
            return "無効なリクエストタイプです";
        }
        
        if (request.getRequestStatus() != null && !request.isValidStatus()) {
            return "無効なステータスです";
        }
        
        if (request.getShippingMethod() != null && !request.isValidShippingMethod()) {
            return "無効な配送方法です";
        }
        
        if (request.getContactPreference() != null && !request.isValidContactPreference()) {
            return "無効な連絡方法です";
        }
        
        // 仮申込・本申込の場合は商品ID必須
        if (("仮申込".equals(request.getRequestType()) || "本申込".equals(request.getRequestType())) && 
            request.getProductId() <= 0) {
            return "商品を選択してください";
        }
        
        // 資料請求の場合は配送先住所必須
        if ("資料請求".equals(request.getRequestType()) && 
            (request.getShippingAddress() == null || request.getShippingAddress().isEmpty())) {
            return "配送先住所を入力してください";
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证状态值
     */
    private boolean isValidStatus(String status) {
        return "受付".equals(status) || 
               "処理中".equals(status) || 
               "完了".equals(status) || 
               "取消".equals(status);
    }
    
    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName(String status) {
        switch (status) {
            case "受付": return "受付済み";
            case "処理中": return "処理中";
            case "完了": return "完了";
            case "取消": return "取消";
            default: return status;
        }
    }
    
    /**
     * 获取请求类型显示名称
     */
    public String getRequestTypeDisplayName(String requestType) {
        switch (requestType) {
            case "資料請求": return "資料請求";
            case "仮申込": return "仮申込";
            case "本申込": return "本申込";
            case "変更申請": return "変更申請";
            default: return requestType;
        }
    }
    
    /**
     * 获取配送方法显示名称
     */
    public String getShippingMethodDisplayName(String shippingMethod) {
        switch (shippingMethod) {
            case "郵便": return "郵便";
            case "宅配便": return "宅配便";
            case "メール": return "メール";
            case "ダウンロード": return "ダウンロード";
            default: return shippingMethod;
        }
    }
    
    /**
     * 获取联系偏好显示名称
     */
    public String getContactPreferenceDisplayName(String contactPreference) {
        switch (contactPreference) {
            case "電話": return "電話";
            case "メール": return "メール";
            case "郵便": return "郵便";
            case "なし": return "連絡不要";
            default: return contactPreference;
        }
    }
}