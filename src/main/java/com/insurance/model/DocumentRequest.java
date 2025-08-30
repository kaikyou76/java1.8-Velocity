package com.insurance.model;

import java.util.Date;

/**
 * 资料请求模型类
 */
public class DocumentRequest {
    private int id;
    private String requestNumber;
    private int customerId;
    private int productId;
    private String requestType;
    private String requestStatus;
    private String requestedDocuments;
    private String shippingAddress;
    private String shippingMethod;
    private String contactPreference;
    private String notes;
    private Date followUpDate;
    private int salesPersonId;
    private Date createdAt;
    private Date updatedAt;
    
    // 关联信息
    private String customerName;
    private String productName;
    private String salesPersonName;
    
    // 构造方法
    public DocumentRequest() {}
    
    public DocumentRequest(String requestNumber, int customerId, String requestType) {
        this.requestNumber = requestNumber;
        this.customerId = customerId;
        this.requestType = requestType;
        this.requestStatus = "受付";
    }
    
    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
    
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }
    
    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    
    public String getRequestedDocuments() { return requestedDocuments; }
    public void setRequestedDocuments(String requestedDocuments) { this.requestedDocuments = requestedDocuments; }
    
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    public String getShippingMethod() { return shippingMethod; }
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    
    public String getContactPreference() { return contactPreference; }
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Date getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(Date followUpDate) { this.followUpDate = followUpDate; }
    
    public int getSalesPersonId() { return salesPersonId; }
    public void setSalesPersonId(int salesPersonId) { this.salesPersonId = salesPersonId; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getSalesPersonName() { return salesPersonName; }
    public void setSalesPersonName(String salesPersonName) { this.salesPersonName = salesPersonName; }
    
    /**
     * 请求类型验证
     */
    public boolean isValidRequestType() {
        return "資料請求".equals(requestType) || 
               "仮申込".equals(requestType) || 
               "本申込".equals(requestType) || 
               "変更申請".equals(requestType);
    }
    
    /**
     * 状态验证
     */
    public boolean isValidStatus() {
        return "受付".equals(requestStatus) || 
               "処理中".equals(requestStatus) || 
               "完了".equals(requestStatus) || 
               "取消".equals(requestStatus);
    }
    
    /**
     * 配送方法验证
     */
    public boolean isValidShippingMethod() {
        return "郵便".equals(shippingMethod) || 
               "宅配便".equals(shippingMethod) || 
               "メール".equals(shippingMethod) || 
               "ダウンロード".equals(shippingMethod);
    }
    
    /**
     * 联系偏好验证
     */
    public boolean isValidContactPreference() {
        return "電話".equals(contactPreference) || 
               "メール".equals(contactPreference) || 
               "郵便".equals(contactPreference) || 
               "なし".equals(contactPreference);
    }
    
    @Override
    public String toString() {
        return "DocumentRequest{" +
                "id=" + id +
                ", requestNumber='" + requestNumber + '\'' +
                ", customerId=" + customerId +
                ", productId=" + productId +
                ", requestType='" + requestType + '\'' +
                ", requestStatus='" + requestStatus + '\'' +
                ", customerName='" + customerName + '\'' +
                ", productName='" + productName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}