package com.insurance.dao;

import com.insurance.model.DocumentRequest;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 资料请求数据访问对象
 * 负责处理资料请求相关的数据库操作
 */
public class DocumentRequestDAO {
    
    /**
     * 根据ID获取资料请求
     * @param id 资料请求ID
     * @return DocumentRequest对象，如果未找到返回null
     */
    public DocumentRequest getDocumentRequestById(int id) {
        // 定义SQL查询语句，通过ID查询资料请求，并关联顾客、商品和销售人员信息
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将id值赋给第一个占位符(?)
            pstmt.setInt(1, id);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 将结果集映射为DocumentRequest对象并返回
                return mapResultSetToDocumentRequest(rs);
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果未找到匹配的记录，返回null
        return null;
    }
    
    /**
     * 根据请求编号获取资料请求
     * @param requestNumber 请求编号
     * @return DocumentRequest对象，如果未找到返回null
     */
    public DocumentRequest getDocumentRequestByNumber(String requestNumber) {
        // 定义SQL查询语句，通过请求编号查询资料请求，并关联顾客、商品和销售人员信息
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.request_number = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将requestNumber值赋给第一个占位符(?)
            pstmt.setString(1, requestNumber);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 将结果集映射为DocumentRequest对象并返回
                return mapResultSetToDocumentRequest(rs);
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果未找到匹配的记录，返回null
        return null;
    }
    
    /**
     * 获取所有资料请求
     * @return 包含所有资料请求的列表
     */
    public List<DocumentRequest> getAllDocumentRequests() {
        // 创建一个空的资料请求列表用于存储查询结果
        List<DocumentRequest> requests = new ArrayList<>();
        // 定义SQL查询语句，查询所有资料请求，并关联顾客、商品和销售人员信息，按创建时间降序排列
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "ORDER BY dr.created_at DESC";
        
        // 使用try-with-resources自动管理数据库连接、语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为DocumentRequest对象并添加到列表中
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回资料请求列表
        return requests;
    }
    
    /**
     * 根据顾客ID获取资料请求
     * @param customerId 顾客ID
     * @return 包含指定顾客所有资料请求的列表
     */
    public List<DocumentRequest> getRequestsByCustomerId(int customerId) {
        // 创建一个空的资料请求列表用于存储查询结果
        List<DocumentRequest> requests = new ArrayList<>();
        // 定义SQL查询语句，根据顾客ID查询资料请求，并关联顾客、商品和销售人员信息，按创建时间降序排列
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.customer_id = ? " +
                   "ORDER BY dr.created_at DESC";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将customerId值赋给第一个占位符(?)
            pstmt.setInt(1, customerId);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为DocumentRequest对象并添加到列表中
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回资料请求列表
        return requests;
    }
    
    /**
     * 根据状态获取资料请求
     * @param status 请求状态
     * @return 包含指定状态所有资料请求的列表
     */
    public List<DocumentRequest> getRequestsByStatus(String status) {
        // 创建一个空的资料请求列表用于存储查询结果
        List<DocumentRequest> requests = new ArrayList<>();
        // 定义SQL查询语句，根据请求状态查询资料请求，并关联顾客、商品和销售人员信息，按创建时间降序排列
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.request_status = ? " +
                   "ORDER BY dr.created_at DESC";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将status值赋给第一个占位符(?)
            pstmt.setString(1, status);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为DocumentRequest对象并添加到列表中
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回资料请求列表
        return requests;
    }
    
    /**
     * 添加资料请求
     * @param request 要添加的资料请求对象
     * @return 添加成功返回true，失败返回false
     */
    public boolean addDocumentRequest(DocumentRequest request) {
        // 定义SQL插入语句，包含所有资料请求字段的占位符
        String sql = "INSERT INTO document_requests (request_number, customer_id, product_id, request_type, " +
                   "request_status, requested_documents, shipping_address, shipping_method, " +
                   "contact_preference, notes, follow_up_date, sales_person_id) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 按顺序设置所有字段的值
            pstmt.setString(1, request.getRequestNumber());      // 请求编号
            pstmt.setInt(2, request.getCustomerId());            // 顾客ID
            pstmt.setInt(3, request.getProductId());             // 商品ID
            pstmt.setString(4, request.getRequestType());        // 请求类型
            pstmt.setString(5, request.getRequestStatus());      // 请求状态
            pstmt.setString(6, request.getRequestedDocuments()); // 请求的文件
            pstmt.setString(7, request.getShippingAddress());    // 邮寄地址
            pstmt.setString(8, request.getShippingMethod());     // 邮寄方式
            pstmt.setString(9, request.getContactPreference());  // 联系偏好
            pstmt.setString(10, request.getNotes());             // 备注
            // 处理跟进日期，如果为null则设置为NULL
            if (request.getFollowUpDate() != null) {
                pstmt.setDate(11, new java.sql.Date(request.getFollowUpDate().getTime()));
            } else {
                pstmt.setNull(11, Types.DATE);
            }
            // 处理销售人员ID，如果大于0则设置，否则设置为NULL
            if (request.getSalesPersonId() > 0) {
                pstmt.setInt(12, request.getSalesPersonId());
            } else {
                pstmt.setNull(12, Types.INTEGER);
            }
            
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
     * 更新资料请求
     * @param request 包含更新信息的资料请求对象
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateDocumentRequest(DocumentRequest request) {
        // 定义SQL更新语句，更新资料请求字段，同时更新时间戳
        String sql = "UPDATE document_requests SET request_status = ?, requested_documents = ?, " +
                   "shipping_address = ?, shipping_method = ?, contact_preference = ?, " +
                   "notes = ?, follow_up_date = ?, sales_person_id = ?, updated_at = CURRENT_TIMESTAMP " +
                   "WHERE id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 按顺序设置所有要更新的字段值
            pstmt.setString(1, request.getRequestStatus());      // 请求状态
            pstmt.setString(2, request.getRequestedDocuments()); // 请求的文件
            pstmt.setString(3, request.getShippingAddress());    // 邮寄地址
            pstmt.setString(4, request.getShippingMethod());     // 邮寄方式
            pstmt.setString(5, request.getContactPreference());  // 联系偏好
            pstmt.setString(6, request.getNotes());              // 备注
            // 处理跟进日期，如果为null则设置为NULL
            if (request.getFollowUpDate() != null) {
                pstmt.setDate(7, new java.sql.Date(request.getFollowUpDate().getTime()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            // 处理销售人员ID，如果大于0则设置，否则设置为NULL
            if (request.getSalesPersonId() > 0) {
                pstmt.setInt(8, request.getSalesPersonId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            pstmt.setInt(9, request.getId());                    // 资料请求ID（WHERE条件）
            
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
     * 更新请求状态
     * @param id 资料请求ID
     * @param status 新的状态
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateRequestStatus(int id, String status) {
        // 定义SQL更新语句，只更新请求状态和时间戳
        String sql = "UPDATE document_requests SET request_status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置要更新的状态值
            pstmt.setString(1, status);
            // 设置要更新的资料请求ID
            pstmt.setInt(2, id);
            
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
     * 删除资料请求（物理删除）
     * @param id 要删除的资料请求ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteDocumentRequest(int id) {
        // 定义SQL删除语句
        String sql = "DELETE FROM document_requests WHERE id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置要删除的资料请求ID
            pstmt.setInt(1, id);
            // 执行删除操作并获取受影响的行数
            int rowsAffected = pstmt.executeUpdate();
            // 如果至少有一行被影响，说明删除成功
            return rowsAffected > 0;
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果出现异常或没有行被影响，返回false
        return false;
    }
    
    /**
     * 搜索资料请求（在多个字段中模糊搜索）
     * @param keyword 搜索关键字
     * @return 匹配的资料请求列表
     */
    public List<DocumentRequest> searchDocumentRequests(String keyword) {
        // 创建一个空的资料请求列表用于存储搜索结果
        List<DocumentRequest> requests = new ArrayList<>();
        // 定义SQL查询语句，在多个字段中进行模糊搜索
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.request_number LIKE ? OR c.first_name LIKE ? OR c.last_name LIKE ? " +
                   "OR p.product_name LIKE ? OR dr.request_status LIKE ? " +
                   "ORDER BY dr.created_at DESC";
        
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
                // 将每行记录映射为DocumentRequest对象并添加到列表中
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回搜索结果列表
        return requests;
    }
    
    /**
     * 生成新的请求编号
     * @return 新的请求编号
     */
    public String generateRequestNumber() {
        // 定义SQL查询语句，获取当前最大请求编号并加1
        String sql = "SELECT MAX(CAST(SUBSTRING(request_number, 4) AS UNSIGNED)) as max_num " +
                   "FROM document_requests " +
                   "WHERE request_number LIKE 'REQ%'";
        
        // 使用try-with-resources自动管理数据库连接、语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 获取最大编号并加1，格式化为4位数字
                int maxNum = rs.getInt("max_num");
                return String.format("REQ%04d", maxNum + 1);
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果没有找到现有编号，返回初始编号
        return "REQ0001";
    }
    
    /**
     * 获取统计信息
     * @return 包含各状态请求数量的映射
     */
    public Map<String, Integer> getRequestStatistics() {
        // 创建一个空的映射用于存储统计信息
        Map<String, Integer> stats = new HashMap<>();
        // 定义SQL查询语句，按请求状态分组统计数量
        String sql = "SELECT request_status, COUNT(*) as count " +
                   "FROM document_requests " +
                   "GROUP BY request_status";
        
        // 使用try-with-resources自动管理数据库连接、语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将状态和数量添加到映射中
                stats.put(rs.getString("request_status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回统计信息映射
        return stats;
    }
    
    /**
     * 将ResultSet映射到DocumentRequest对象
     * @param rs 数据库查询结果集
     * @return 映射后的DocumentRequest对象
     * @throws SQLException SQL异常
     */
    private DocumentRequest mapResultSetToDocumentRequest(ResultSet rs) throws SQLException {
        // 创建一个新的DocumentRequest对象
        DocumentRequest request = new DocumentRequest();
        // 逐个设置DocumentRequest对象的属性，从结果集中获取对应字段的值
        request.setId(rs.getInt("id"));                           // ID
        request.setRequestNumber(rs.getString("request_number")); // 请求编号
        request.setCustomerId(rs.getInt("customer_id"));          // 顾客ID
        request.setProductId(rs.getInt("product_id"));            // 商品ID
        request.setRequestType(rs.getString("request_type"));     // 请求类型
        request.setRequestStatus(rs.getString("request_status")); // 请求状态
        request.setRequestedDocuments(rs.getString("requested_documents")); // 请求的文件
        request.setShippingAddress(rs.getString("shipping_address")); // 邮寄地址
        request.setShippingMethod(rs.getString("shipping_method")); // 邮寄方式
        request.setContactPreference(rs.getString("contact_preference")); // 联系偏好
        request.setNotes(rs.getString("notes"));                  // 备注
        request.setFollowUpDate(rs.getDate("follow_up_date"));    // 跟进日期
        request.setSalesPersonId(rs.getInt("sales_person_id"));   // 销售人员ID
        request.setCreatedAt(rs.getTimestamp("created_at"));      // 创建时间
        request.setUpdatedAt(rs.getTimestamp("updated_at"));      // 更新时间
        
        // 关联信息
        // 如果有顾客姓名信息，则设置顾客姓名
        if (rs.getString("first_name") != null && rs.getString("last_name") != null) {
            request.setCustomerName(rs.getString("last_name") + " " + rs.getString("first_name"));
        }
        // 设置商品名称
        request.setProductName(rs.getString("product_name"));
        // 设置销售人员姓名
        request.setSalesPersonName(rs.getString("sales_person_name"));
        
        // 返回映射完成的DocumentRequest对象
        return request;
    }
}