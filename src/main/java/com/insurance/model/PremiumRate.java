package com.insurance.model;

import java.util.Date;

/**
 * 保险料率模型类
 */
public class PremiumRate {
    // 保险料率ID，主键
    private int id;
    // 商品ID，关联商品信息
    private int productId;
    // 性别（M/F）
    private String gender;
    // 加入年龄
    private int entryAge;
    // 保险期间（年）
    private int insurancePeriod;
    // 基本料率
    private double baseRate;
    // 附加料率
    private double loadingRate;
    // 有效开始日期
    private Date validFrom;
    // 有效结束日期
    private Date validTo;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    
    // 关联的商品信息
    // 商品名称，用于显示
    private String productName;
    // 商品代码，用于显示
    private String productCode;
    
    // 构造方法
    // 无参构造方法
    public PremiumRate() {}
    
    // 有参构造方法，用于创建新的保险料率对象
    // @param productId 商品ID
    // @param gender 性别
    // @param entryAge 加入年龄
    // @param insurancePeriod 保险期间
    // @param baseRate 基本料率
    // @param loadingRate 附加料率
    // @param validFrom 有效开始日期
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
    // 获取保险料率ID
    public int getId() { return id; }
    // 设置保险料率ID
    public void setId(int id) { this.id = id; }
    
    // 获取商品ID
    public int getProductId() { return productId; }
    // 设置商品ID
    public void setProductId(int productId) { this.productId = productId; }
    
    // 获取性别
    public String getGender() { return gender; }
    // 设置性别
    public void setGender(String gender) { this.gender = gender; }
    
    // 获取加入年龄
    public int getEntryAge() { return entryAge; }
    // 设置加入年龄
    public void setEntryAge(int entryAge) { this.entryAge = entryAge; }
    
    // 获取保险期间
    public int getInsurancePeriod() { return insurancePeriod; }
    // 设置保险期间
    public void setInsurancePeriod(int insurancePeriod) { this.insurancePeriod = insurancePeriod; }
    
    // 获取基本料率
    public double getBaseRate() { return baseRate; }
    // 设置基本料率
    public void setBaseRate(double baseRate) { this.baseRate = baseRate; }
    
    // 获取附加料率
    public double getLoadingRate() { return loadingRate; }
    // 设置附加料率
    public void setLoadingRate(double loadingRate) { this.loadingRate = loadingRate; }
    
    // 获取有效开始日期
    public Date getValidFrom() { return validFrom; }
    // 设置有效开始日期
    public void setValidFrom(Date validFrom) { this.validFrom = validFrom; }
    
    // 获取有效结束日期
    public Date getValidTo() { return validTo; }
    // 设置有效结束日期
    public void setValidTo(Date validTo) { this.validTo = validTo; }
    
    // 获取创建时间
    public Date getCreatedAt() { return createdAt; }
    // 设置创建时间
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    // 获取更新时间
    public Date getUpdatedAt() { return updatedAt; }
    // 设置更新时间
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    // 获取商品名称
    public String getProductName() { return productName; }
    // 设置商品名称
    public void setProductName(String productName) { this.productName = productName; }
    
    // 获取商品代码
    public String getProductCode() { return productCode; }
    // 设置商品代码
    public void setProductCode(String productCode) { this.productCode = productCode; }
    
    /**
     * 计算总料率
     * 总料率 = 基本料率 + 附加料率
     * @return 总料率
     */
    public double getTotalRate() {
        return baseRate + loadingRate;
    }
    
    /**
     * 检查料率是否有效
     * 料率有效条件：当前日期在有效期间内
     * @return 如果料率有效返回true，否则返回false
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