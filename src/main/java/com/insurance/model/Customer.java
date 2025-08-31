package com.insurance.model;

import java.util.Date;

/**
 * 顾客信息模型类
 */
public class Customer {
    // 顾客ID，主键
    private int id;
    // 顾客代码，唯一标识一个顾客
    private String customerCode;
    // 名字
    private String firstName;
    // 姓氏
    private String lastName;
    // 名字的片假名
    private String firstNameKana;
    // 姓氏的片假名
    private String lastNameKana;
    // 性别
    private String gender;
    // 出生日期
    private Date birthDate;
    // 年龄
    private int age;
    // 邮政编码
    private String postalCode;
    // 省/州
    private String prefecture;
    // 城市
    private String city;
    // 地址第一行
    private String addressLine1;
    // 地址第二行
    private String addressLine2;
    // 电话号码
    private String phoneNumber;
    // 电子邮件
    private String email;
    // 职业
    private String occupation;
    // 年收入
    private double annualIncome;
    // 家庭构成
    private String familyComposition;
    // 创建时间
    private Date createdAt;
    // 更新时间
    private Date updatedAt;
    // 删除标志，true表示已删除
    private boolean deletedFlag;
    
    // 无参构造方法
    public Customer() {}
    
    // 有参构造方法，用于创建新的顾客对象
    // @param customerCode 顾客代码
    // @param firstName 名字
    // @param lastName 姓氏
    // @param gender 性别
    // @param birthDate 出生日期
    public Customer(String customerCode, String firstName, String lastName, String gender, Date birthDate) {
        this.customerCode = customerCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
    }
    
    // Getter和Setter方法
    // 获取顾客ID
    public int getId() { return id; }
    // 设置顾客ID
    public void setId(int id) { this.id = id; }
    
    // 获取顾客代码
    public String getCustomerCode() { return customerCode; }
    // 设置顾客代码
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    
    // 获取名字
    public String getFirstName() { return firstName; }
    // 设置名字
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    // 获取姓氏
    public String getLastName() { return lastName; }
    // 设置姓氏
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    // 获取名字的片假名
    public String getFirstNameKana() { return firstNameKana; }
    // 设置名字的片假名
    public void setFirstNameKana(String firstNameKana) { this.firstNameKana = firstNameKana; }
    
    // 获取姓氏的片假名
    public String getLastNameKana() { return lastNameKana; }
    // 设置姓氏的片假名
    public void setLastNameKana(String lastNameKana) { this.lastNameKana = lastNameKana; }
    
    // 获取性别
    public String getGender() { return gender; }
    // 设置性别
    public void setGender(String gender) { this.gender = gender; }
    
    // 获取出生日期
    public Date getBirthDate() { return birthDate; }
    // 设置出生日期
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    
    // 获取年龄
    public int getAge() { return age; }
    // 设置年龄
    public void setAge(int age) { this.age = age; }
    
    // 获取邮政编码
    public String getPostalCode() { return postalCode; }
    // 设置邮政编码
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    // 获取省/州
    public String getPrefecture() { return prefecture; }
    // 设置省/州
    public void setPrefecture(String prefecture) { this.prefecture = prefecture; }
    
    // 获取城市
    public String getCity() { return city; }
    // 设置城市
    public void setCity(String city) { this.city = city; }
    
    // 获取地址第一行
    public String getAddressLine1() { return addressLine1; }
    // 设置地址第一行
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    
    // 获取地址第二行
    public String getAddressLine2() { return addressLine2; }
    // 设置地址第二行
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    
    // 获取电话号码
    public String getPhoneNumber() { return phoneNumber; }
    // 设置电话号码
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    // 获取电子邮件
    public String getEmail() { return email; }
    // 设置电子邮件
    public void setEmail(String email) { this.email = email; }
    
    // 获取职业
    public String getOccupation() { return occupation; }
    // 设置职业
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    // 获取年收入
    public double getAnnualIncome() { return annualIncome; }
    // 设置年收入
    public void setAnnualIncome(double annualIncome) { this.annualIncome = annualIncome; }
    
    // 获取家庭构成
    public String getFamilyComposition() { return familyComposition; }
    // 设置家庭构成
    public void setFamilyComposition(String familyComposition) { this.familyComposition = familyComposition; }
    
    // 获取创建时间
    public Date getCreatedAt() { return createdAt; }
    // 设置创建时间
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    // 获取更新时间
    public Date getUpdatedAt() { return updatedAt; }
    // 设置更新时间
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    // 获取删除标志
    public boolean isDeletedFlag() { return deletedFlag; }
    // 设置删除标志
    public void setDeletedFlag(boolean deletedFlag) { this.deletedFlag = deletedFlag; }
    
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", customerCode='" + customerCode + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}