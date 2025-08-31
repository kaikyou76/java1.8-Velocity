package com.insurance.service;

import com.insurance.dao.CustomerDAO;
import com.insurance.model.Customer;
import java.util.List;

/**
 * 顾客业务逻辑服务类
 */
public class CustomerService {
    
    // 顾客数据访问对象，用于与数据库交互
    private CustomerDAO customerDAO;
    
    // 构造方法，初始化顾客数据访问对象
    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }
    
    /**
     * 获取顾客信息
     * 根据顾客ID获取顾客详细信息
     * @param id 顾客ID
     * @return Customer 顾客对象
     */
    public Customer getCustomerById(int id) {
        // 调用DAO层方法根据ID获取顾客信息
        return customerDAO.getCustomerById(id);
    }
    
    /**
     * 根据顾客编号获取顾客信息
     * @param customerCode 顾客编号
     * @return Customer 顾客对象
     */
    public Customer getCustomerByCode(String customerCode) {
        // 调用DAO层方法根据顾客编号获取顾客信息
        return customerDAO.getCustomerByCode(customerCode);
    }
    
    /**
     * 获取所有顾客列表
     * @return List<Customer> 顾客列表
     */
    public List<Customer> getAllCustomers() {
        // 调用DAO层方法获取所有顾客列表
        return customerDAO.getAllCustomers();
    }
    
    /**
     * 添加新顾客
     * 验证顾客信息并添加到数据库
     * @param customer 顾客对象
     * @return boolean 添加成功返回true，否则返回false
     */
    public boolean addCustomer(Customer customer) {
        // 业务逻辑验证
        // 检查顾客对象是否为空，顾客编号是否为空
        if (customer == null || customer.getCustomerCode() == null || customer.getCustomerCode().trim().isEmpty()) {
            return false;
        }
        
        // 检查顾客名字是否为空
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            return false;
        }
        
        // 检查顾客姓氏是否为空
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            return false;
        }
        
        // 检查顾客出生日期是否为空
        if (customer.getBirthDate() == null) {
            return false;
        }
        
        // 检查顾客编号是否已存在
        Customer existingCustomer = customerDAO.getCustomerByCode(customer.getCustomerCode());
        if (existingCustomer != null) {
            // 如果顾客编号已存在，返回false
            return false;
        }
        
        // 调用DAO层方法添加顾客
        return customerDAO.addCustomer(customer);
    }
    
    /**
     * 更新顾客信息
     * 验证顾客信息并更新到数据库
     * @param customer 顾客对象
     * @return boolean 更新成功返回true，否则返回false
     */
    public boolean updateCustomer(Customer customer) {
        // 检查顾客对象是否为空，顾客ID是否有效
        if (customer == null || customer.getId() <= 0) {
            return false;
        }
        
        // 检查顾客是否存在
        Customer existingCustomer = customerDAO.getCustomerById(customer.getId());
        if (existingCustomer == null) {
            // 如果顾客不存在，返回false
            return false;
        }
        
        // 调用DAO层方法更新顾客信息
        return customerDAO.updateCustomer(customer);
    }
    
    /**
     * 删除顾客
     * 根据顾客ID删除顾客信息
     * @param id 顾客ID
     * @return boolean 删除成功返回true，否则返回false
     */
    public boolean deleteCustomer(int id) {
        // 检查顾客ID是否有效
        if (id <= 0) {
            return false;
        }
        
        // 检查顾客是否存在
        Customer existingCustomer = customerDAO.getCustomerById(id);
        if (existingCustomer == null) {
            // 如果顾客不存在，返回false
            return false;
        }
        
        // 调用DAO层方法删除顾客
        return customerDAO.deleteCustomer(id);
    }
    
    /**
     * 搜索顾客
     * 根据关键字搜索顾客信息
     * @param keyword 搜索关键字
     * @return List<Customer> 符合条件的顾客列表
     */
    public List<Customer> searchCustomers(String keyword) {
        // 如果关键字为空，返回所有顾客列表
        if (keyword == null || keyword.trim().isEmpty()) {
            return customerDAO.getAllCustomers();
        }
        
        // 调用DAO层方法根据关键字搜索顾客
        return customerDAO.searchCustomers(keyword.trim());
    }
    
    /**
     * 验证顾客信息
     * 验证顾客对象的各个字段是否符合要求
     * @param customer 顾客对象
     * @return String 验证结果，null表示验证通过，其他值表示错误信息
     */
    public String validateCustomer(Customer customer) {
        // 验证顾客编号是否为空
        if (customer.getCustomerCode() == null || customer.getCustomerCode().trim().isEmpty()) {
            return "顾客编号不能为空";
        }
        
        // 验证名字是否为空
        if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
            return "名不能为空";
        }
        
        // 验证姓氏是否为空
        if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
            return "姓不能为空";
        }
        
        // 验证性别是否为M或F
        if (customer.getGender() == null || (!customer.getGender().equals("M") && !customer.getGender().equals("F"))) {
            return "性别必须为M或F";
        }
        
        // 验证出生日期是否为空
        if (customer.getBirthDate() == null) {
            return "出生日期不能为空";
        }
        
        // 验证邮箱格式是否正确
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (!isValidEmail(customer.getEmail())) {
                return "邮箱格式不正确";
            }
        }
        
        // 验证电话号码格式是否正确
        if (customer.getPhoneNumber() != null && !customer.getPhoneNumber().trim().isEmpty()) {
            if (!isValidPhoneNumber(customer.getPhoneNumber())) {
                return "电话号码格式不正确";
            }
        }
        
        // 所有验证通过，返回null
        return null; // 验证通过
    }
    
    /**
     * 验证邮箱格式
     * 使用正则表达式验证邮箱格式是否正确
     * @param email 邮箱地址
     * @return boolean 邮箱格式正确返回true，否则返回false
     */
    private boolean isValidEmail(String email) {
        // 使用正则表达式验证邮箱格式
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * 验证电话号码格式
     * 使用正则表达式验证电话号码格式是否正确
     * @param phoneNumber 电话号码
     * @return boolean 电话号码格式正确返回true，否则返回false
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        // 使用正则表达式验证电话号码格式，允许数字、连字符、括号和空格
        return phoneNumber.matches("^[0-9\\-\\(\\)\\s]+$");
    }
}