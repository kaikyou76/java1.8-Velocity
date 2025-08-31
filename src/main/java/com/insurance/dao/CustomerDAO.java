package com.insurance.dao;

import com.insurance.model.Customer;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 顾客数据访问对象
 * 负责处理顾客相关的数据库操作
 */
public class CustomerDAO {
    
    /**
     * 根据ID获取顾客信息
     * @param id 顾客ID
     * @return Customer对象，如果未找到返回null
     */
    public Customer getCustomerById(int id) {
        // 定义SQL查询语句，根据ID查询未被删除的顾客
        String sql = "SELECT * FROM customers WHERE id = ? AND deleted_flag = 0";
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将id值赋给第一个占位符(?)
            pstmt.setInt(1, id);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 将结果集映射为Customer对象并返回
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果未找到匹配的记录，返回null
        return null;
    }
    
    /**
     * 根据顾客编号获取顾客信息
     * @param customerCode 顾客编号
     * @return Customer对象，如果未找到返回null
     */
    public Customer getCustomerByCode(String customerCode) {
        // 定义SQL查询语句，根据顾客编号查询未被删除的顾客
        String sql = "SELECT * FROM customers WHERE customer_code = ? AND deleted_flag = 0";
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将customerCode值赋给第一个占位符(?)
            pstmt.setString(1, customerCode);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 将结果集映射为Customer对象并返回
                return mapResultSetToCustomer(rs);
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果未找到匹配的记录，返回null
        return null;
    }
    
    /**
     * 获取所有顾客列表
     * @return 包含所有未删除顾客的列表
     */
    public List<Customer> getAllCustomers() {
        // 创建一个空的顾客列表用于存储查询结果
        List<Customer> customers = new ArrayList<>();
        // 定义SQL查询语句，查询所有未被删除的顾客，按创建时间降序排列
        String sql = "SELECT * FROM customers WHERE deleted_flag = 0 ORDER BY created_at DESC";
        
        // 使用try-with-resources自动管理数据库连接、语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为Customer对象并添加到列表中
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回顾客列表
        return customers;
    }
    
    /**
     * 添加新顾客
     * @param customer 要添加的顾客对象
     * @return 添加成功返回true，失败返回false
     */
    public boolean addCustomer(Customer customer) {
        // 定义SQL插入语句，包含所有顾客字段的占位符
        String sql = "INSERT INTO customers (customer_code, first_name, last_name, first_name_kana, last_name_kana, " +
                    "gender, birth_date, age, postal_code, prefecture, city, address_line1, address_line2, " +
                    "phone_number, email, occupation, annual_income, family_composition) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 按顺序设置所有字段的值
            pstmt.setString(1, customer.getCustomerCode());      // 顾客编号
            pstmt.setString(2, customer.getFirstName());         // 名
            pstmt.setString(3, customer.getLastName());          // 姓
            pstmt.setString(4, customer.getFirstNameKana());     // 名(カナ)
            pstmt.setString(5, customer.getLastNameKana());      // 姓(カナ)
            pstmt.setString(6, customer.getGender());            // 性别
            pstmt.setDate(7, new java.sql.Date(customer.getBirthDate().getTime())); // 出生日期
            pstmt.setInt(8, customer.getAge());                  // 年龄
            pstmt.setString(9, customer.getPostalCode());        // 邮政编码
            pstmt.setString(10, customer.getPrefecture());       // 都道府県
            pstmt.setString(11, customer.getCity());             // 市区町村
            pstmt.setString(12, customer.getAddressLine1());     // 住所1
            pstmt.setString(13, customer.getAddressLine2());     // 住所2
            pstmt.setString(14, customer.getPhoneNumber());      // 电话号码
            pstmt.setString(15, customer.getEmail());            // 邮箱
            pstmt.setString(16, customer.getOccupation());       // 职业
            pstmt.setDouble(17, customer.getAnnualIncome());     // 年收入
            pstmt.setString(18, customer.getFamilyComposition()); // 家庭构成
            
            // 执行插入操作并获取受影响的行数
            int rowsAffected = pstmt.executeUpdate();
            // 如果至少有一行被影响，说明插入成功
            return rowsAffected > 0;
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果出现异常或没有行被影响，返回false
        return false;
    }
    
    /**
     * 更新顾客信息
     * @param customer 包含更新信息的顾客对象
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateCustomer(Customer customer) {
        // 定义SQL更新语句，更新所有顾客字段，同时更新时间戳
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, first_name_kana = ?, last_name_kana = ?, " +
                    "gender = ?, birth_date = ?, age = ?, postal_code = ?, prefecture = ?, city = ?, " +
                    "address_line1 = ?, address_line2 = ?, phone_number = ?, email = ?, occupation = ?, " +
                    "annual_income = ?, family_composition = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND deleted_flag = 0";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 按顺序设置所有要更新的字段值
            pstmt.setString(1, customer.getFirstName());         // 名
            pstmt.setString(2, customer.getLastName());          // 姓
            pstmt.setString(3, customer.getFirstNameKana());     // 名(カナ)
            pstmt.setString(4, customer.getLastNameKana());      // 姓(カナ)
            pstmt.setString(5, customer.getGender());            // 性别
            pstmt.setDate(6, new java.sql.Date(customer.getBirthDate().getTime())); // 出生日期
            pstmt.setInt(7, customer.getAge());                  // 年龄
            pstmt.setString(8, customer.getPostalCode());        // 邮政编码
            pstmt.setString(9, customer.getPrefecture());        // 都道府県
            pstmt.setString(10, customer.getCity());             // 市区町村
            pstmt.setString(11, customer.getAddressLine1());     // 住所1
            pstmt.setString(12, customer.getAddressLine2());     // 住所2
            pstmt.setString(13, customer.getPhoneNumber());      // 电话号码
            pstmt.setString(14, customer.getEmail());            // 邮箱
            pstmt.setString(15, customer.getOccupation());       // 职业
            pstmt.setDouble(16, customer.getAnnualIncome());     // 年收入
            pstmt.setString(17, customer.getFamilyComposition()); // 家庭构成
            pstmt.setInt(18, customer.getId());                  // 顾客ID（WHERE条件）
            
            // 执行更新操作并获取受影响的行数
            int rowsAffected = pstmt.executeUpdate();
            // 如果至少有一行被影响，说明更新成功
            return rowsAffected > 0;
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果出现异常或没有行被影响，返回false
        return false;
    }
    
    /**
     * 软删除顾客（将deleted_flag设置为1而不是真正删除记录）
     * @param id 要删除的顾客ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteCustomer(int id) {
        // 定义SQL更新语句，将deleted_flag设置为1并更新时间戳
        String sql = "UPDATE customers SET deleted_flag = 1, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置要删除的顾客ID
            pstmt.setInt(1, id);
            // 执行更新操作并获取受影响的行数
            int rowsAffected = pstmt.executeUpdate();
            // 如果至少有一行被影响，说明"删除"成功
            return rowsAffected > 0;
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果出现异常或没有行被影响，返回false
        return false;
    }
    
    /**
     * 搜索顾客（在多个字段中模糊搜索）
     * @param keyword 搜索关键字
     * @return 匹配的顾客列表
     */
    public List<Customer> searchCustomers(String keyword) {
        // 创建一个空的顾客列表用于存储搜索结果
        List<Customer> customers = new ArrayList<>();
        // 定义SQL查询语句，在多个字段中进行模糊搜索
        String sql = "SELECT * FROM customers WHERE deleted_flag = 0 AND " +
                    "(first_name LIKE ? OR last_name LIKE ? OR customer_code LIKE ? OR email LIKE ? OR phone_number LIKE ?) " +
                    "ORDER BY created_at DESC";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 构造搜索模式，在关键字前后加上%实现模糊匹配
            String searchPattern = "%" + keyword + "%";
            // 循环5次，因为SQL中有5个占位符，都设置为相同的搜索模式
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为Customer对象并添加到列表中
                customers.add(mapResultSetToCustomer(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回搜索结果列表
        return customers;
    }
    
    /**
     * 将ResultSet映射到Customer对象
     * @param rs 数据库查询结果集
     * @return 映射后的Customer对象
     * @throws SQLException SQL异常
     */
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        // 创建一个新的Customer对象
        Customer customer = new Customer();
        // 逐个设置Customer对象的属性，从结果集中获取对应字段的值
        customer.setId(rs.getInt("id"));                           // ID
        customer.setCustomerCode(rs.getString("customer_code"));   // 顾客编号
        customer.setFirstName(rs.getString("first_name"));         // 名
        customer.setLastName(rs.getString("last_name"));           // 姓
        customer.setFirstNameKana(rs.getString("first_name_kana")); // 名(カナ)
        customer.setLastNameKana(rs.getString("last_name_kana"));   // 姓(カナ)
        customer.setGender(rs.getString("gender"));                // 性别
        customer.setBirthDate(rs.getDate("birth_date"));           // 出生日期
        customer.setAge(rs.getInt("age"));                         // 年龄
        customer.setPostalCode(rs.getString("postal_code"));       // 邮政编码
        customer.setPrefecture(rs.getString("prefecture"));        // 都道府県
        customer.setCity(rs.getString("city"));                    // 市区町村
        customer.setAddressLine1(rs.getString("address_line1"));   // 住所1
        customer.setAddressLine2(rs.getString("address_line2"));   // 住所2
        customer.setPhoneNumber(rs.getString("phone_number"));     // 电话号码
        customer.setEmail(rs.getString("email"));                  // 邮箱
        customer.setOccupation(rs.getString("occupation"));        // 职业
        customer.setAnnualIncome(rs.getDouble("annual_income"));   // 年收入
        customer.setFamilyComposition(rs.getString("family_composition")); // 家庭构成
        customer.setCreatedAt(rs.getTimestamp("created_at"));      // 创建时间
        customer.setUpdatedAt(rs.getTimestamp("updated_at"));      // 更新时间
        customer.setDeletedFlag(rs.getBoolean("deleted_flag"));    // 删除标志
        
        // 返回映射完成的Customer对象
        return customer;
    }
}