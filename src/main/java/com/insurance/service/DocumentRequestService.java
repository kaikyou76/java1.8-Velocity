package com.insurance.service;

import com.insurance.dao.DocumentRequestDAO;
import com.insurance.model.DocumentRequest;
import java.util.List;
import java.util.Map;

/**
 * 资料请求业务逻辑服务类
 */
public class DocumentRequestService {
    
    // 资料请求数据访问对象，用于与数据库交互
    private DocumentRequestDAO documentRequestDAO;
    
    // 构造方法，初始化资料请求数据访问对象
    public DocumentRequestService() {
        this.documentRequestDAO = new DocumentRequestDAO();
    }
    
    /**
     * 获取资料请求信息
     * 根据资料请求ID获取详细信息
     * @param id 资料请求ID
     * @return DocumentRequest 资料请求对象
     */
    public DocumentRequest getDocumentRequestById(int id) {
        // 调用DAO层方法根据ID获取资料请求信息
        return documentRequestDAO.getDocumentRequestById(id);
    }
    
    /**
     * 根据请求编号获取资料请求信息
     * @param requestNumber 请求编号
     * @return DocumentRequest 资料请求对象
     */
    public DocumentRequest getDocumentRequestByNumber(String requestNumber) {
        // 调用DAO层方法根据请求编号获取资料请求信息
        return documentRequestDAO.getDocumentRequestByNumber(requestNumber);
    }
    
    /**
     * 获取所有资料请求列表
     * @return List<DocumentRequest> 资料请求列表
     */
    public List<DocumentRequest> getAllDocumentRequests() {
        // 调用DAO层方法获取所有资料请求列表
        return documentRequestDAO.getAllDocumentRequests();
    }
    
    /**
     * 根据顾客ID获取资料请求
     * @param customerId 顾客ID
     * @return List<DocumentRequest> 资料请求列表
     */
    public List<DocumentRequest> getRequestsByCustomerId(int customerId) {
        // 调用DAO层方法根据顾客ID获取资料请求列表
        return documentRequestDAO.getRequestsByCustomerId(customerId);
    }
    
    /**
     * 根据状态获取资料请求
     * @param status 请求状态
     * @return List<DocumentRequest> 资料请求列表
     */
    public List<DocumentRequest> getRequestsByStatus(String status) {
        // 调用DAO层方法根据状态获取资料请求列表
        return documentRequestDAO.getRequestsByStatus(status);
    }
    
    /**
     * 添加资料请求
     * 验证资料请求信息并添加到数据库
     * @param request 资料请求对象
     * @return boolean 添加成功返回true，否则返回false
     */
    public boolean addDocumentRequest(DocumentRequest request) {
        // 业务逻辑验证
        // 验证资料请求信息是否符合要求
        String validationError = validateDocumentRequest(request);
        if (validationError != null) {
            // 如果验证失败，返回false
            return false;
        }
        
        // 生成请求编号
        // 如果请求编号为空，自动生成一个新的请求编号
        if (request.getRequestNumber() == null || request.getRequestNumber().isEmpty()) {
            String requestNumber = documentRequestDAO.generateRequestNumber();
            request.setRequestNumber(requestNumber);
        }
        
        // 设置默认状态
        // 如果请求状态为空，设置默认状态为"受付"
        if (request.getRequestStatus() == null || request.getRequestStatus().isEmpty()) {
            request.setRequestStatus("受付");
        }
        
        // 调用DAO层方法添加资料请求
        return documentRequestDAO.addDocumentRequest(request);
    }
    
    /**
     * 更新资料请求
     * 验证资料请求信息并更新到数据库
     * @param request 资料请求对象
     * @return boolean 更新成功返回true，否则返回false
     */
    public boolean updateDocumentRequest(DocumentRequest request) {
        // 检查请求对象是否为空，请求ID是否有效
        if (request == null || request.getId() <= 0) {
            return false;
        }
        
        // 检查请求是否存在
        DocumentRequest existingRequest = documentRequestDAO.getDocumentRequestById(request.getId());
        if (existingRequest == null) {
            // 如果请求不存在，返回false
            return false;
        }
        
        // 业务逻辑验证
        // 验证资料请求信息是否符合要求
        String validationError = validateDocumentRequest(request);
        if (validationError != null) {
            // 如果验证失败，返回false
            return false;
        }
        
        // 调用DAO层方法更新资料请求
        return documentRequestDAO.updateDocumentRequest(request);
    }
    
    /**
     * 更新请求状态
     * 更新资料请求的状态
     * @param id 资料请求ID
     * @param status 新的状态
     * @return boolean 更新成功返回true，否则返回false
     */
    public boolean updateRequestStatus(int id, String status) {
        // 检查请求ID和状态是否有效
        if (id <= 0 || status == null || status.isEmpty()) {
            return false;
        }
        
        // 验证状态值是否有效
        if (!isValidStatus(status)) {
            return false;
        }
        
        // 检查请求是否存在
        DocumentRequest existingRequest = documentRequestDAO.getDocumentRequestById(id);
        if (existingRequest == null) {
            // 如果请求不存在，返回false
            return false;
        }
        
        // 调用DAO层方法更新请求状态
        return documentRequestDAO.updateRequestStatus(id, status);
    }
    
    /**
     * 删除资料请求
     * 根据资料请求ID删除请求信息
     * @param id 资料请求ID
     * @return boolean 删除成功返回true，否则返回false
     */
    public boolean deleteDocumentRequest(int id) {
        // 检查请求ID是否有效
        if (id <= 0) {
            return false;
        }
        
        // 检查请求是否存在
        DocumentRequest existingRequest = documentRequestDAO.getDocumentRequestById(id);
        if (existingRequest == null) {
            // 如果请求不存在，返回false
            return false;
        }
        
        // 调用DAO层方法删除资料请求
        return documentRequestDAO.deleteDocumentRequest(id);
    }
    
    /**
     * 搜索资料请求
     * 根据关键字搜索资料请求信息
     * @param keyword 搜索关键字
     * @return List<DocumentRequest> 符合条件的资料请求列表
     */
    public List<DocumentRequest> searchDocumentRequests(String keyword) {
        // 如果关键字为空，返回所有资料请求列表
        if (keyword == null || keyword.trim().isEmpty()) {
            return documentRequestDAO.getAllDocumentRequests();
        }
        
        // 调用DAO层方法根据关键字搜索资料请求
        return documentRequestDAO.searchDocumentRequests(keyword.trim());
    }
    
    /**
     * 获取统计信息
     * 获取资料请求的统计信息
     * @return Map<String, Integer> 统计信息映射
     */
    public Map<String, Integer> getRequestStatistics() {
        // 调用DAO层方法获取统计信息
        return documentRequestDAO.getRequestStatistics();
    }
    
    /**
     * 生成新的请求编号
     * 生成唯一的资料请求编号
     * @return String 新的请求编号
     */
    public String generateRequestNumber() {
        // 调用DAO层方法生成新的请求编号
        return documentRequestDAO.generateRequestNumber();
    }
    
    /**
     * 验证资料请求信息
     * 验证资料请求对象的各个字段是否符合要求
     * @param request 资料请求对象
     * @return String 验证结果，null表示验证通过，其他值表示错误信息
     */
    public String validateDocumentRequest(DocumentRequest request) {
        // 验证请求对象是否为空
        if (request == null) {
            return "リクエスト情報がありません";
        }
        
        // 验证顾客ID是否有效
        if (request.getCustomerId() <= 0) {
            return "顧客IDが無効です";
        }
        
        // 验证请求类型是否为空
        if (request.getRequestType() == null || request.getRequestType().isEmpty()) {
            return "リクエストタイプを選択してください";
        }
        
        // 验证请求类型是否有效
        if (!request.isValidRequestType()) {
            return "無効なリクエストタイプです";
        }
        
        // 验证请求状态是否有效
        if (request.getRequestStatus() != null && !request.isValidStatus()) {
            return "無効なステータスです";
        }
        
        // 验证配送方法是否有效
        if (request.getShippingMethod() != null && !request.isValidShippingMethod()) {
            return "無効な配送方法です";
        }
        
        // 验证联系偏好是否有效
        if (request.getContactPreference() != null && !request.isValidContactPreference()) {
            return "無効な連絡方法です";
        }
        
        // 仮申込・本申込的場合は商品ID必須
        // 如果是仮申込或本申込，商品ID必须大于0
        if (("仮申込".equals(request.getRequestType()) || "本申込".equals(request.getRequestType())) && 
            request.getProductId() <= 0) {
            return "商品を選択してください";
        }
        
        // 資料請求的場合は配送先住所必須
        // 如果是资料请求，配送地址不能为空
        if ("資料請求".equals(request.getRequestType()) && 
            (request.getShippingAddress() == null || request.getShippingAddress().isEmpty())) {
            return "配送先住所を入力してください";
        }
        
        // 所有验证通过，返回null
        return null; // 验证通过
    }
    
    /**
     * 验证状态值
     * 验证给定的状态值是否为有效的状态
     * @param status 状态值
     * @return boolean 状态值有效返回true，否则返回false
     */
    private boolean isValidStatus(String status) {
        // 检查状态值是否为有效的状态
        return "受付".equals(status) || 
               "処理中".equals(status) || 
               "完了".equals(status) || 
               "取消".equals(status);
    }
    
    /**
     * 获取状态显示名称
     * 根据状态值获取对应的显示名称
     * @param status 状态值
     * @return String 状态显示名称
     */
    public String getStatusDisplayName(String status) {
        // 根据状态值返回对应的显示名称
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
     * 根据请求类型获取对应的显示名称
     * @param requestType 请求类型
     * @return String 请求类型显示名称
     */
    public String getRequestTypeDisplayName(String requestType) {
        // 根据请求类型返回对应的显示名称
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
     * 根据配送方法获取对应的显示名称
     * @param shippingMethod 配送方法
     * @return String 配送方法显示名称
     */
    public String getShippingMethodDisplayName(String shippingMethod) {
        // 根据配送方法返回对应的显示名称
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
     * 根据联系偏好获取对应的显示名称
     * @param contactPreference 联系偏好
     * @return String 联系偏好显示名称
     */
    public String getContactPreferenceDisplayName(String contactPreference) {
        // 根据联系偏好返回对应的显示名称
        switch (contactPreference) {
            case "電話": return "電話";
            case "メール": return "メール";
            case "郵便": return "郵便";
            case "なし": return "連絡不要";
            default: return contactPreference;
        }
    }
}