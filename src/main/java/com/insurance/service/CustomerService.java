package com.insurance.service;

import com.insurance.dao.CustomerDAO;
import com.insurance.model.Customer;
import java.util.List;

/**
 * 顾客业务逻辑服务类
 */
public class CustomerService {
    
    private CustomerDAO customerDAO;
    
    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }
    
    /**
     * 获取顾客信息
     */
    public Customer getCustomerById(int id) {
        return customerDAO.getCustomerById(id);
    }
    
    public Customer getCustomerByCode(String customerCode) {
        return customerDAO.getCustomerByCode(customerCode);
    }
    
    /**
     * 获取所有顾客列表
     */
    public List<Customer> getAllCustomers() {
        return customerDAO.getAllCustomers();
    }
    
    /**
     * 添加新顾客
     */
    public boolean addCustomer(Customer customer) {
        // 业务逻辑验证
        if (customer == null || customer.getCustomerCode() == null || customer.getCustomerCode().trim().isEmpty()) {
            return false;
        }
        
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            return false;
        }
        
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            return false;
        }
        
        if (customer.getBirthDate() == null) {
            return false;
        }
        
        // 检查顾客编号是否已存在
        Customer existingCustomer = customerDAO.getCustomerByCode(customer.getCustomerCode());
        if (existingCustomer != null) {
            return false;
        }
        
        return customerDAO.addCustomer(customer);
    }
    
    /**
     * 更新顾客信息
     */
    public boolean updateCustomer(Customer customer) {
        if (customer == null || customer.getId() <= 0) {
            return false;
        }
        
        // 检查顾客是否存在
        Customer existingCustomer = customerDAO.getCustomerById(customer.getId());
        if (existingCustomer == null) {
            return false;
        }
        
        return customerDAO.updateCustomer(customer);
    }
    
    /**
     * 删除顾客
     */
    public boolean deleteCustomer(int id) {
        if (id <= 0) {
            return false;
        }
        
        // 检查顾客是否存在
        Customer existingCustomer = customerDAO.getCustomerById(id);
        if (existingCustomer == null) {
            return false;
        }
        
        return customerDAO.deleteCustomer(id);
    }
    
    /**
     * 搜索顾客
     */
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return customerDAO.getAllCustomers();
        }
        
        return customerDAO.searchCustomers(keyword.trim());
    }
    
    /**
     * 验证顾客信息
     */
    public String validateCustomer(Customer customer) {
        if (customer.getCustomerCode() == null || customer.getCustomerCode().trim().isEmpty()) {
            return "顾客编号不能为空";
        }
        
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            return "名不能为空";
        }
        
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            return "姓不能为空";
        }
        
        if (customer.getGender() == null || (!customer.getGender().equals("M") && !customer.getGender().equals("F"))) {
            return "性别必须为M或F";
        }
        
        if (customer.getBirthDate() == null) {
            return "出生日期不能为空";
        }
        
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (!isValidEmail(customer.getEmail())) {
                return "邮箱格式不正确";
            }
        }
        
        if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().trim().isEmpty()) {
            if (!isValidPhoneNumber(customer.getPhoneNumber())) {
                return "电话号码格式不正确";
            }
        }
        
        return null; // 验证通过
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * 验证电话号码格式
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("^[0-9\\-\\(\\)\\s]+$");
    }
}