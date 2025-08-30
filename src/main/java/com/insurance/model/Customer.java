package com.insurance.model;

import java.util.Date;

/**
 * 顾客信息模型类
 */
public class Customer {
    private int id;
    private String customerCode;
    private String firstName;
    private String lastName;
    private String firstNameKana;
    private String lastNameKana;
    private String gender;
    private Date birthDate;
    private int age;
    private String postalCode;
    private String prefecture;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String phoneNumber;
    private String email;
    private String occupation;
    private double annualIncome;
    private String familyComposition;
    private Date createdAt;
    private Date updatedAt;
    private boolean deletedFlag;
    
    // 构造方法
    public Customer() {}
    
    public Customer(String customerCode, String firstName, String lastName, String gender, Date birthDate) {
        this.customerCode = customerCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
    }
    
    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getFirstNameKana() { return firstNameKana; }
    public void setFirstNameKana(String firstNameKana) { this.firstNameKana = firstNameKana; }
    
    public String getLastNameKana() { return lastNameKana; }
    public void setLastNameKana(String lastNameKana) { this.lastNameKana = lastNameKana; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    
    public String getPrefecture() { return prefecture; }
    public void setPrefecture(String prefecture) { this.prefecture = prefecture; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    public double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(double annualIncome) { this.annualIncome = annualIncome; }
    
    public String getFamilyComposition() { return familyComposition; }
    public void setFamilyComposition(String familyComposition) { this.familyComposition = familyComposition; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isDeletedFlag() { return deletedFlag; }
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