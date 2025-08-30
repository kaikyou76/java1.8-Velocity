package com.insurance.dao;

import com.insurance.model.PremiumRate;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 保险料率数据访问对象
 */
public class PremiumRateDAO {
    
    /**
     * 根据ID获取料率信息
     */
    public PremiumRate getPremiumRateById(int id) {
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE pr.id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPremiumRate(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取指定商品的所有料率
     */
    public List<PremiumRate> getRatesByProductId(int productId) {
        List<PremiumRate> rates = new ArrayList<>();
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE pr.product_id = ? " +
                   "ORDER BY pr.gender, pr.entry_age, pr.insurance_period";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                rates.add(mapResultSetToPremiumRate(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rates;
    }
    
    /**
     * 根据条件获取有效料率
     */
    public PremiumRate getRateByConditions(int productId, String gender, int entryAge, int insurancePeriod) {
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE pr.product_id = ? " +
                   "AND pr.gender = ? " +
                   "AND pr.entry_age = ? " +
                   "AND pr.insurance_period = ? " +
                   "AND pr.valid_from <= CURDATE() " +
                   "AND (pr.valid_to IS NULL OR pr.valid_to >= CURDATE()) " +
                   "ORDER BY pr.valid_from DESC " +
                   "LIMIT 1";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            pstmt.setString(2, gender);
            pstmt.setInt(3, entryAge);
            pstmt.setInt(4, insurancePeriod);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToPremiumRate(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 获取所有有效料率
     */
    public List<PremiumRate> getAllValidRates() {
        List<PremiumRate> rates = new ArrayList<>();
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE pr.valid_from <= CURDATE() " +
                   "AND (pr.valid_to IS NULL OR pr.valid_to >= CURDATE()) " +
                   "ORDER BY ip.product_name, pr.gender, pr.entry_age, pr.insurance_period";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                rates.add(mapResultSetToPremiumRate(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rates;
    }
    
    /**
     * 添加新料率
     */
    public boolean addPremiumRate(PremiumRate rate) {
        String sql = "INSERT INTO premium_rates (product_id, gender, entry_age, insurance_period, " +
                   "base_rate, loading_rate, valid_from, valid_to) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rate.getProductId());
            pstmt.setString(2, rate.getGender());
            pstmt.setInt(3, rate.getEntryAge());
            pstmt.setInt(4, rate.getInsurancePeriod());
            pstmt.setDouble(5, rate.getBaseRate());
            pstmt.setDouble(6, rate.getLoadingRate());
            pstmt.setDate(7, new java.sql.Date(rate.getValidFrom().getTime()));
            
            if (rate.getValidTo() != null) {
                pstmt.setDate(8, new java.sql.Date(rate.getValidTo().getTime()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 更新料率信息
     */
    public boolean updatePremiumRate(PremiumRate rate) {
        String sql = "UPDATE premium_rates SET product_id = ?, gender = ?, entry_age = ?, " +
                   "insurance_period = ?, base_rate = ?, loading_rate = ?, " +
                   "valid_from = ?, valid_to = ?, updated_at = CURRENT_TIMESTAMP " +
                   "WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rate.getProductId());
            pstmt.setString(2, rate.getGender());
            pstmt.setInt(3, rate.getEntryAge());
            pstmt.setInt(4, rate.getInsurancePeriod());
            pstmt.setDouble(5, rate.getBaseRate());
            pstmt.setDouble(6, rate.getLoadingRate());
            pstmt.setDate(7, new java.sql.Date(rate.getValidFrom().getTime()));
            
            if (rate.getValidTo() != null) {
                pstmt.setDate(8, new java.sql.Date(rate.getValidTo().getTime()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            
            pstmt.setInt(9, rate.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 删除料率
     */
    public boolean deletePremiumRate(int id) {
        String sql = "DELETE FROM premium_rates WHERE id = ?";
        
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
     * 搜索料率
     */
    public List<PremiumRate> searchRates(String keyword) {
        List<PremiumRate> rates = new ArrayList<>();
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE ip.product_name LIKE ? OR ip.product_code LIKE ? " +
                   "ORDER BY ip.product_name, pr.valid_from DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                rates.add(mapResultSetToPremiumRate(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rates;
    }
    
    /**
     * 获取指定商品的有效年龄范围
     */
    public int[] getValidAgeRange(int productId) {
        String sql = "SELECT MIN(entry_age) as min_age, MAX(entry_age) as max_age " +
                   "FROM premium_rates " +
                   "WHERE product_id = ? " +
                   "AND valid_from <= CURDATE() " +
                   "AND (valid_to IS NULL OR valid_to >= CURDATE())";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int minAge = rs.getInt("min_age");
                int maxAge = rs.getInt("max_age");
                return new int[]{minAge, maxAge};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0};
    }
    
    /**
     * 获取指定商品的有效保险期间范围
     */
    public int[] getValidPeriodRange(int productId) {
        String sql = "SELECT MIN(insurance_period) as min_period, MAX(insurance_period) as max_period " +
                   "FROM premium_rates " +
                   "WHERE product_id = ? " +
                   "AND valid_from <= CURDATE() " +
                   "AND (valid_to IS NULL OR valid_to >= CURDATE())";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int minPeriod = rs.getInt("min_period");
                int maxPeriod = rs.getInt("max_period");
                return new int[]{minPeriod, maxPeriod};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0};
    }
    
    /**
     * 将ResultSet映射到PremiumRate对象
     */
    private PremiumRate mapResultSetToPremiumRate(ResultSet rs) throws SQLException {
        PremiumRate rate = new PremiumRate();
        rate.setId(rs.getInt("id"));
        rate.setProductId(rs.getInt("product_id"));
        rate.setGender(rs.getString("gender"));
        rate.setEntryAge(rs.getInt("entry_age"));
        rate.setInsurancePeriod(rs.getInt("insurance_period"));
        rate.setBaseRate(rs.getDouble("base_rate"));
        rate.setLoadingRate(rs.getDouble("loading_rate"));
        rate.setValidFrom(rs.getDate("valid_from"));
        rate.setValidTo(rs.getDate("valid_to"));
        rate.setCreatedAt(rs.getTimestamp("created_at"));
        rate.setUpdatedAt(rs.getTimestamp("updated_at"));
        rate.setProductName(rs.getString("product_name"));
        rate.setProductCode(rs.getString("product_code"));
        
        return rate;
    }
}