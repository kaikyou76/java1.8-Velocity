package com.insurance.model;

import java.util.Date;

/**
 * 保险料率模型类
 */
public class PremiumRate {
    private int id;
    private int productId;
    private String gender;
    private int entryAge;
    private int insurancePeriod;
    private double baseRate;
    private double loadingRate;
    private Date validFrom;
    private Date validTo;
    private Date createdAt;
    private Date updatedAt;
    
    // 关联的商品信息
    private String productName;
    private String productCode;
    
    // 构造方法
    public PremiumRate() {}
    
    public PremiumRate(int productId, String gender, int entryAge, int insurancePeriod, 
                      double baseRate, double loadingRate, Date validFrom) {
        this.productId = productId;
        this.gender = gender;
        this.entryAge = entryAge;
        this.insurancePeriod = insurancePeriod;
        this.baseRate = baseRate;
        this.loadingRate = loadingRate;
        this.validFrom = validFrom;
    }
    
    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public int getEntryAge() { return entryAge; }
    public void setEntryAge(int entryAge) { this.entryAge = entryAge; }
    
    public int getInsurancePeriod() { return insurancePeriod; }
    public void setInsurancePeriod(int insurancePeriod) { this.insurancePeriod = insurancePeriod; }
    
    public double getBaseRate() { return baseRate; }
    public void setBaseRate(double baseRate) { this.baseRate = baseRate; }
    
    public double getLoadingRate() { return loadingRate; }
    public void setLoadingRate(double loadingRate) { this.loadingRate = loadingRate; }
    
    public Date getValidFrom() { return validFrom; }
    public void setValidFrom(Date validFrom) { this.validFrom = validFrom; }
    
    public Date getValidTo() { return validTo; }
    public void setValidTo(Date validTo) { this.validTo = validTo; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    /**
     * 计算总料率
     */
    public double getTotalRate() {
        return baseRate + loadingRate;
    }
    
    /**
     * 检查料率是否有效
     */
    public boolean isValid() {
        Date now = new Date();
        return validFrom.before(now) && (validTo == null || validTo.after(now));
    }
    
    @Override
    public String toString() {
        return "PremiumRate{" +
                "id=" + id +
                ", productId=" + productId +
                ", gender='" + gender + '\'' +
                ", entryAge=" + entryAge +
                ", insurancePeriod=" + insurancePeriod +
                ", baseRate=" + baseRate +
                ", loadingRate=" + loadingRate +
                ", totalRate=" + getTotalRate() +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                '}';
    }
}