package com.insurance.model;

import java.util.Date;

/**
 * 资料请求模型类
 */
public class DocumentRequest {
    // 资料请求ID，主键
    private int id;
    // 请求编号，唯一标识一个请求
    private String requestNumber;
    // 顾客ID，关联顾客信息
    private int customerId;
    // 商品ID，关联商品信息
    private int productId;
    // 请求类型（资料请求、仮申込、本申込、变更申请等）
    private String requestType;
    // 请求状态（受付、处理中、完成、取消等）
    private String requestStatus;
    // 请求的资料列表
    private String requestedDocuments;
    // 配送地址
    private String shippingAddress;
    // 配送方法（邮便、宅配便、邮件、下载等）
    private String shippingMethod;
    // 联系偏好（电话、邮件、邮便、无等）
    private String contactPreference;
    // 备注信息
    private String notes;
    // 跟进日期
    private Date followUpDate;
    // 销售人员ID，关联销售人员信息
    private int salesPersonId;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    
    // 关联信息
    // 顾客姓名，用于显示
    private String customerName;
    // 商品名称，用于显示
    private String productName;
    // 销售人员姓名，用于显示
    private String salesPersonName;
    
    // 构造方法
    // 无参构造方法
    public DocumentRequest() {}
    
    // 有参构造方法，用于创建新的资料请求对象
    // @param requestNumber 请求编号
    // @param customerId 顾客ID
    // @param requestType 请求类型
    public DocumentRequest(String requestNumber, int customerId, String requestType) {
        this.requestNumber = requestNumber;
        this.customerId = customerId;
        this.requestType = requestType;
        this.requestStatus = "受付";
    }
    
    // Getter和Setter方法
    // 获取资料请求ID
    public int getId() { return id; }
    // 设置资料请求ID
    public void setId(int id) { this.id = id; }
    
    // 获取请求编号
    public String getRequestNumber() { return requestNumber; }
    // 设置请求编号
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }
    
    // 获取顾客ID
    public int getCustomerId() { return customerId; }
    // 设置顾客ID
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    
    // 获取商品ID
    public int getProductId() { return productId; }
    // 设置商品ID
    public void setProductId(int productId) { this.productId = productId; }
    
    // 获取请求类型
    public String getRequestType() { return requestType; }
    // 设置请求类型
    public void setRequestType(String requestType) { this.requestType = requestType; }
    
    // 获取请求状态
    public String getRequestStatus() { return requestStatus; }
    // 设置请求状态
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    
    // 获取请求的资料列表
    public String getRequestedDocuments() { return requestedDocuments; }
    // 设置请求的资料列表
    public void setRequestedDocuments(String requestedDocuments) { this.requestedDocuments = requestedDocuments; }
    
    // 获取配送地址
    public String getShippingAddress() { return shippingAddress; }
    // 设置配送地址
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    
    // 获取配送方法
    public String getShippingMethod() { return shippingMethod; }
    // 设置配送方法
    public void setShippingMethod(String shippingMethod) { this.shippingMethod = shippingMethod; }
    
    // 获取联系偏好
    public String getContactPreference() { return contactPreference; }
    // 设置联系偏好
    public void setContactPreference(String contactPreference) { this.contactPreference = contactPreference; }
    
    // 获取备注信息
    public String getNotes() { return notes; }
    // 设置备注信息
    public void setNotes(String notes) { this.notes = notes; }
    
    // 获取跟进日期
    public Date getFollowUpDate() { return followUpDate; }
    // 设置跟进日期
    public void setFollowUpDate(Date followUpDate) { this.followUpDate = followUpDate; }
    
    // 获取销售人员ID
    public int getSalesPersonId() { return salesPersonId; }
    // 设置销售人员ID
    public void setSalesPersonId(int salesPersonId) { this.salesPersonId = salesPersonId; }
    
    // 获取创建时间
    public Date getCreatedAt() { return createdAt; }
    // 设置创建时间
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    // 获取更新时间
    public Date getUpdatedAt() { return updatedAt; }
    // 设置更新时间
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    // 获取顾客姓名
    public String getCustomerName() { return customerName; }
    // 设置顾客姓名
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    // 获取商品名称
    public String getProductName() { return productName; }
    // 设置商品名称
    public void setProductName(String productName) { this.productName = productName; }
    
    // 获取销售人员姓名
    public String getSalesPersonName() { return salesPersonName; }
    // 设置销售人员姓名
    public void setSalesPersonName(String salesPersonName) { this.salesPersonName = salesPersonName; }
    
    /**
     * 请求类型验证
     * 验证请求类型是否为有效的值
     * @return 如果请求类型有效返回true，否则返回false
     */
    public boolean isValidRequestType() {
        return "資料請求".equals(requestType) || 
               "仮申込".equals(requestType) || 
               "本申込".equals(requestType) || 
               "変更申請".equals(requestType);
    }
    
    /**
     * 状态验证
     * 验证请求状态是否为有效的值
     * @return 如果请求状态有效返回true，否则返回false
     */
    public boolean isValidStatus() {
        return "受付".equals(requestStatus) || 
               "処理中".equals(requestStatus) || 
               "完了".equals(requestStatus) || 
               "取消".equals(requestStatus);
    }
    
    /**
     * 配送方法验证
     * 验证配送方法是否为有效的值
     * @return 如果配送方法有效返回true，否则返回false
     */
    public boolean isValidShippingMethod() {
        return "郵便".equals(shippingMethod) || 
               "宅配便".equals(shippingMethod) || 
               "メール".equals(shippingMethod) || 
               "ダウンロード".equals(shippingMethod);
    }
    
    /**
     * 联系偏好验证
     * 验证联系偏好是否为有效的值
     * @return 如果联系偏好有效返回true，否则返回false
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