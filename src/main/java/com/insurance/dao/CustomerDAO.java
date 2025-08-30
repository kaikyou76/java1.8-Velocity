package com.insurance.dao;

import com.insurance.model.Customer;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 顾客数据访问对象
 */
public class CustomerDAO {
    
    /**
     * 根据ID获取顾客信息
     */
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id = ? AND deleted_flag = 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据顾客编号获取顾客信息
     */
    public Customer getCustomerByCode(String customerCode) {
        String sql = "SELECT * FROM customers WHERE customer_code = ? AND deleted_flag = 0";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customerCode);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取所有顾客列表
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE deleted_flag = 0 ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    /**
     * 添加新顾客
     */
    public boolean addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (customer_code, first_name, last_name, first_name_kana, last_name_kana, " +
                    "gender, birth_date, age, postal_code, prefecture, city, address_line1, address_line2, " +
                    "phone_number, email, occupation, annual_income, family_composition) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getCustomerCode());
            pstmt.setString(2, customer.getFirstName());
            pstmt.setString(3, customer.getLastName());
            pstmt.setString(4, customer.getFirstNameKana());
            pstmt.setString(5, customer.getLastNameKana());
            pstmt.setString(6, customer.getGender());
            pstmt.setDate(7, new java.sql.Date(customer.getBirthDate().getTime()));
            pstmt.setInt(8, customer.getAge());
            pstmt.setString(9, customer.getPostalCode());
            pstmt.setString(10, customer.getPrefecture());
            pstmt.setString(11, customer.getCity());
            pstmt.setString(12, customer.getAddressLine1());
            pstmt.setString(13, customer.getAddressLine2());
            pstmt.setString(14, customer.getPhoneNumber());
            pstmt.setString(15, customer.getEmail());
            pstmt.setString(16, customer.getOccupation());
            pstmt.setDouble(17, customer.getAnnualIncome());
            pstmt.setString(18, customer.getFamilyComposition());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 更新顾客信息
     */
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, first_name_kana = ?, last_name_kana = ?, " +
                    "gender = ?, birth_date = ?, age = ?, postal_code = ?, prefecture = ?, city = ?, " +
                    "address_line1 = ?, address_line2 = ?, phone_number = ?, email = ?, occupation = ?, " +
                    "annual_income = ?, family_composition = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND deleted_flag = 0";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getFirstNameKana());
            pstmt.setString(4, customer.getLastNameKana());
            pstmt.setString(5, customer.getGender());
            pstmt.setDate(6, new java.sql.Date(customer.getBirthDate().getTime()));
            pstmt.setInt(7, customer.getAge());
            pstmt.setString(8, customer.getPostalCode());
            pstmt.setString(9, customer.getPrefecture());
            pstmt.setString(10, customer.getCity());
            pstmt.setString(11, customer.getAddressLine1());
            pstmt.setString(12, customer.getAddressLine2());
            pstmt.setString(13, customer.getPhoneNumber());
            pstmt.setString(14, customer.getEmail());
            pstmt.setString(15, customer.getOccupation());
            pstmt.setDouble(16, customer.getAnnualIncome());
            pstmt.setString(17, customer.getFamilyComposition());
            pstmt.setInt(18, customer.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 软删除顾客
     */
    public boolean deleteCustomer(int id) {
        String sql = "UPDATE customers SET deleted_flag = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 搜索顾客
     */
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE deleted_flag = 0 AND " +
                    "(first_name LIKE ? OR last_name LIKE ? OR customer_code LIKE ? OR email LIKE ? OR phone_number LIKE ?) " +
                    "ORDER BY created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    /**
     * 将ResultSet映射到Customer对象
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getInt("id"));
        customer.setCustomerCode(rs.getString("customer_code"));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setFirstNameKana(rs.getString("first_name_kana"));
        customer.setLastNameKana(rs.getString("last_name_kana"));
        customer.setGender(rs.getString("gender"));
        customer.setBirthDate(rs.getDate("birth_date"));
        customer.setAge(rs.getInt("age"));
        customer.setPostalCode(rs.getString("postal_code"));
        customer.setPrefecture(rs.getString("prefecture"));
        customer.setCity(rs.getString("city"));
        customer.setAddressLine1(rs.getString("address_line1"));
        customer.setAddressLine2(rs.getString("address_line2"));
        customer.setPhoneNumber(rs.getString("phone_number"));
        customer.setEmail(rs.getString("email"));
        customer.setOccupation(rs.getString("occupation"));
        customer.setAnnualIncome(rs.getDouble("annual_income"));
        customer.setFamilyComposition(rs.getString("family_composition"));
        customer.setCreatedAt(rs.getTimestamp("created_at"));
        customer.setUpdatedAt(rs.getTimestamp("updated_at"));
        customer.setDeletedFlag(rs.getBoolean("deleted_flag"));
        
        return customer;
    }
}