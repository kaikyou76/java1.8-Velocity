package com.insurance.dao;

import com.insurance.model.DocumentRequest;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 资料请求数据访问对象
 */
public class DocumentRequestDAO {
    
    /**
     * 根据ID获取资料请求
     */
    public DocumentRequest getDocumentRequestById(int id) {
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDocumentRequest(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据请求编号获取资料请求
     */
    public DocumentRequest getDocumentRequestByNumber(String requestNumber) {
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.request_number = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, requestNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToDocumentRequest(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取所有资料请求
     */
    public List<DocumentRequest> getAllDocumentRequests() {
        List<DocumentRequest> requests = new ArrayList<>();
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "ORDER BY dr.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    /**
     * 根据顾客ID获取资料请求
     */
    public List<DocumentRequest> getRequestsByCustomerId(int customerId) {
        List<DocumentRequest> requests = new ArrayList<>();
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.customer_id = ? " +
                   "ORDER BY dr.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    /**
     * 根据状态获取资料请求
     */
    public List<DocumentRequest> getRequestsByStatus(String status) {
        List<DocumentRequest> requests = new ArrayList<>();
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.request_status = ? " +
                   "ORDER BY dr.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    /**
     * 添加资料请求
     */
    public boolean addDocumentRequest(DocumentRequest request) {
        String sql = "INSERT INTO document_requests (request_number, customer_id, product_id, request_type, " +
                   "request_status, requested_documents, shipping_address, shipping_method, " +
                   "contact_preference, notes, follow_up_date, sales_person_id) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, request.getRequestNumber());
            pstmt.setInt(2, request.getCustomerId());
            pstmt.setInt(3, request.getProductId());
            pstmt.setString(4, request.getRequestType());
            pstmt.setString(5, request.getRequestStatus());
            pstmt.setString(6, request.getRequestedDocuments());
            pstmt.setString(7, request.getShippingAddress());
            pstmt.setString(8, request.getShippingMethod());
            pstmt.setString(9, request.getContactPreference());
            pstmt.setString(10, request.getNotes());
            
            if (request.getFollowUpDate() != null) {
                pstmt.setDate(11, new java.sql.Date(request.getFollowUpDate().getTime()));
            } else {
                pstmt.setNull(11, Types.DATE);
            }
            
            if (request.getSalesPersonId() > 0) {
                pstmt.setInt(12, request.getSalesPersonId());
            } else {
                pstmt.setNull(12, Types.INTEGER);
            }
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 更新资料请求
     */
    public boolean updateDocumentRequest(DocumentRequest request) {
        String sql = "UPDATE document_requests SET request_status = ?, requested_documents = ?, " +
                   "shipping_address = ?, shipping_method = ?, contact_preference = ?, " +
                   "notes = ?, follow_up_date = ?, sales_person_id = ?, updated_at = CURRENT_TIMESTAMP " +
                   "WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, request.getRequestStatus());
            pstmt.setString(2, request.getRequestedDocuments());
            pstmt.setString(3, request.getShippingAddress());
            pstmt.setString(4, request.getShippingMethod());
            pstmt.setString(5, request.getContactPreference());
            pstmt.setString(6, request.getNotes());
            
            if (request.getFollowUpDate() != null) {
                pstmt.setDate(7, new java.sql.Date(request.getFollowUpDate().getTime()));
            } else {
                pstmt.setNull(7, Types.DATE);
            }
            
            if (request.getSalesPersonId() > 0) {
                pstmt.setInt(8, request.getSalesPersonId());
            } else {
                pstmt.setNull(8, Types.INTEGER);
            }
            
            pstmt.setInt(9, request.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 更新请求状态
     */
    public boolean updateRequestStatus(int id, String status) {
        String sql = "UPDATE document_requests SET request_status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 删除资料请求
     */
    public boolean deleteDocumentRequest(int id) {
        String sql = "DELETE FROM document_requests WHERE id = ?";
        
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
     * 搜索资料请求
     */
    public List<DocumentRequest> searchDocumentRequests(String keyword) {
        List<DocumentRequest> requests = new ArrayList<>();
        String sql = "SELECT dr.*, c.first_name, c.last_name, p.product_name, u.full_name as sales_person_name " +
                   "FROM document_requests dr " +
                   "LEFT JOIN customers c ON dr.customer_id = c.id " +
                   "LEFT JOIN insurance_products p ON dr.product_id = p.id " +
                   "LEFT JOIN users u ON dr.sales_person_id = u.id " +
                   "WHERE dr.request_number LIKE ? OR c.first_name LIKE ? OR c.last_name LIKE ? " +
                   "OR p.product_name LIKE ? OR dr.request_status LIKE ? " +
                   "ORDER BY dr.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                requests.add(mapResultSetToDocumentRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
    
    /**
     * 生成新的请求编号
     */
    public String generateRequestNumber() {
        String sql = "SELECT MAX(CAST(SUBSTRING(request_number, 4) AS UNSIGNED)) as max_num " +
                   "FROM document_requests " +
                   "WHERE request_number LIKE 'REQ%'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                int maxNum = rs.getInt("max_num");
                return String.format("REQ%04d", maxNum + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "REQ0001";
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Integer> getRequestStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT request_status, COUNT(*) as count " +
                   "FROM document_requests " +
                   "GROUP BY request_status";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("request_status"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
    
    /**
     * 将ResultSet映射到DocumentRequest对象
     */
    private DocumentRequest mapResultSetToDocumentRequest(ResultSet rs) throws SQLException {
        DocumentRequest request = new DocumentRequest();
        request.setId(rs.getInt("id"));
        request.setRequestNumber(rs.getString("request_number"));
        request.setCustomerId(rs.getInt("customer_id"));
        request.setProductId(rs.getInt("product_id"));
        request.setRequestType(rs.getString("request_type"));
        request.setRequestStatus(rs.getString("request_status"));
        request.setRequestedDocuments(rs.getString("requested_documents"));
        request.setShippingAddress(rs.getString("shipping_address"));
        request.setShippingMethod(rs.getString("shipping_method"));
        request.setContactPreference(rs.getString("contact_preference"));
        request.setNotes(rs.getString("notes"));
        request.setFollowUpDate(rs.getDate("follow_up_date"));
        request.setSalesPersonId(rs.getInt("sales_person_id"));
        request.setCreatedAt(rs.getTimestamp("created_at"));
        request.setUpdatedAt(rs.getTimestamp("updated_at"));
        
        // 关联信息
        if (rs.getString("first_name") != null && rs.getString("last_name") != null) {
            request.setCustomerName(rs.getString("last_name") + " " + rs.getString("first_name"));
        }
        request.setProductName(rs.getString("product_name"));
        request.setSalesPersonName(rs.getString("sales_person_name"));
        
        return request;
    }
}