package com.insurance.dao;

import com.insurance.model.PremiumRate;
import com.insurance.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 保险料率数据访问对象
 * 负责处理保险料率相关的数据库操作
 */
public class PremiumRateDAO {
    
    /**
     * 根据ID获取料率信息
     * @param id 料率ID
     * @return PremiumRate对象，如果未找到返回null
     */
    public PremiumRate getPremiumRateById(int id) {
        // 定义SQL查询语句，通过ID查询料率信息，并关联保险商品信息
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE pr.id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将id值赋给第一个占位符(?)
            pstmt.setInt(1, id);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 将结果集映射为PremiumRate对象并返回
                return mapResultSetToPremiumRate(rs);
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果未找到匹配的记录，返回null
        return null;
    }
    
    /**
     * 获取指定商品的所有料率
     * @param productId 商品ID
     * @return 包含指定商品所有料率的列表
     */
    public List<PremiumRate> getRatesByProductId(int productId) {
        // 创建一个空的料率列表用于存储查询结果
        List<PremiumRate> rates = new ArrayList<>();
        // 定义SQL查询语句，根据商品ID查询所有料率，并关联保险商品信息，按性别、加入年龄、保险期间排序
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE pr.product_id = ? " +
                   "ORDER BY pr.gender, pr.entry_age, pr.insurance_period";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将productId值赋给第一个占位符(?)
            pstmt.setInt(1, productId);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为PremiumRate对象并添加到列表中
                rates.add(mapResultSetToPremiumRate(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回料率列表
        return rates;
    }
    
    /**
     * 根据条件获取有效料率
     * @param productId 商品ID
     * @param gender 性别
     * @param entryAge 加入年龄
     * @param insurancePeriod 保险期间
     * @return 符合条件的有效料率，如果未找到返回null
     */
    public PremiumRate getRateByConditions(int productId, String gender, int entryAge, int insurancePeriod) {
        // 定义SQL查询语句，根据多个条件查询有效料率，并关联保险商品信息
        // 有效料率是指当前日期在有效开始日期和有效结束日期之间的料率
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
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 按顺序设置所有查询条件
            pstmt.setInt(1, productId);        // 商品ID
            pstmt.setString(2, gender);        // 性别
            pstmt.setInt(3, entryAge);         // 加入年龄
            pstmt.setInt(4, insurancePeriod);  // 保险期间
            
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 将结果集映射为PremiumRate对象并返回
                return mapResultSetToPremiumRate(rs);
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果未找到匹配的记录，返回null
        return null;
    }
    
    /**
     * 获取所有有效料率
     * @return 包含所有有效料率的列表
     */
    public List<PremiumRate> getAllValidRates() {
        // 创建一个空的料率列表用于存储查询结果
        List<PremiumRate> rates = new ArrayList<>();
        // 定义SQL查询语句，查询所有有效料率，并关联保险商品信息
        // 有效料率是指当前日期在有效开始日期和有效结束日期之间的料率
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE pr.valid_from <= CURDATE() " +
                   "AND (pr.valid_to IS NULL OR pr.valid_to >= CURDATE()) " +
                   "ORDER BY ip.product_name, pr.gender, pr.entry_age, pr.insurance_period";
        
        // 使用try-with-resources自动管理数据库连接、语句和结果集
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为PremiumRate对象并添加到列表中
                rates.add(mapResultSetToPremiumRate(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回料率列表
        return rates;
    }
    
    /**
     * 添加新料率
     * @param rate 要添加的料率对象
     * @return 添加成功返回true，失败返回false
     */
    public boolean addPremiumRate(PremiumRate rate) {
        // 定义SQL插入语句，包含所有料率字段的占位符
        String sql = "INSERT INTO premium_rates (product_id, gender, entry_age, insurance_period, " +
                   "base_rate, loading_rate, valid_from, valid_to) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 按顺序设置所有字段的值
            pstmt.setInt(1, rate.getProductId());              // 商品ID
            pstmt.setString(2, rate.getGender());              // 性别
            pstmt.setInt(3, rate.getEntryAge());               // 加入年龄
            pstmt.setInt(4, rate.getInsurancePeriod());        // 保险期间
            pstmt.setDouble(5, rate.getBaseRate());            // 基本料率
            pstmt.setDouble(6, rate.getLoadingRate());         // 付加料率
            pstmt.setDate(7, new java.sql.Date(rate.getValidFrom().getTime())); // 适用开始日
            // 处理适用结束日，如果为null则设置为NULL
            if (rate.getValidTo() != null) {
                pstmt.setDate(8, new java.sql.Date(rate.getValidTo().getTime()));
            } else {
                pstmt.setNull(8, Types.DATE);
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
     * 更新料率信息
     * @param rate 包含更新信息的料率对象
     * @return 更新成功返回true，失败返回false
     */
    public boolean updatePremiumRate(PremiumRate rate) {
        // 定义SQL更新语句，更新料率字段，同时更新时间戳
        String sql = "UPDATE premium_rates SET product_id = ?, gender = ?, entry_age = ?, " +
                   "insurance_period = ?, base_rate = ?, loading_rate = ?, " +
                   "valid_from = ?, valid_to = ?, updated_at = CURRENT_TIMESTAMP " +
                   "WHERE id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 按顺序设置所有要更新的字段值
            pstmt.setInt(1, rate.getProductId());              // 商品ID
            pstmt.setString(2, rate.getGender());              // 性别
            pstmt.setInt(3, rate.getEntryAge());               // 加入年龄
            pstmt.setInt(4, rate.getInsurancePeriod());        // 保险期间
            pstmt.setDouble(5, rate.getBaseRate());            // 基本料率
            pstmt.setDouble(6, rate.getLoadingRate());         // 付加料率
            pstmt.setDate(7, new java.sql.Date(rate.getValidFrom().getTime())); // 适用开始日
            // 处理适用结束日，如果为null则设置为NULL
            if (rate.getValidTo() != null) {
                pstmt.setDate(8, new java.sql.Date(rate.getValidTo().getTime()));
            } else {
                pstmt.setNull(8, Types.DATE);
            }
            pstmt.setInt(9, rate.getId());                     // 料率ID（WHERE条件）
            
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
     * 删除料率
     * @param id 要删除的料率ID
     * @return 删除成功返回true，失败返回false
     */
    public boolean deletePremiumRate(int id) {
        // 定义SQL删除语句
        String sql = "DELETE FROM premium_rates WHERE id = ?";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置要删除的料率ID
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
     * 搜索料率（在商品名称和商品代码中模糊搜索）
     * @param keyword 搜索关键字
     * @return 匹配的料率列表
     */
    public List<PremiumRate> searchRates(String keyword) {
        // 创建一个空的料率列表用于存储搜索结果
        List<PremiumRate> rates = new ArrayList<>();
        // 定义SQL查询语句，在商品名称和商品代码中进行模糊搜索
        String sql = "SELECT pr.*, ip.product_name, ip.product_code " +
                   "FROM premium_rates pr " +
                   "LEFT JOIN insurance_products ip ON pr.product_id = ip.id " +
                   "WHERE ip.product_name LIKE ? OR ip.product_code LIKE ? " +
                   "ORDER BY ip.product_name, pr.valid_from DESC";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 构造搜索模式，在关键字前后加上%实现模糊匹配
            String searchPattern = "%" + keyword + "%";
            // 设置两个占位符都为相同的搜索模式
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            // 遍历结果集中的每一行记录
            while (rs.next()) {
                // 将每行记录映射为PremiumRate对象并添加到列表中
                rates.add(mapResultSetToPremiumRate(rs));
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 返回搜索结果列表
        return rates;
    }
    
    /**
     * 获取指定商品的有效年龄范围
     * @param productId 商品ID
     * @return 包含最小年龄和最大年龄的数组
     */
    public int[] getValidAgeRange(int productId) {
        // 定义SQL查询语句，获取指定商品的有效年龄范围
        String sql = "SELECT MIN(entry_age) as min_age, MAX(entry_age) as max_age " +
                   "FROM premium_rates " +
                   "WHERE product_id = ? " +
                   "AND valid_from <= CURDATE() " +
                   "AND (valid_to IS NULL OR valid_to >= CURDATE())";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将productId值赋给第一个占位符(?)
            pstmt.setInt(1, productId);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 获取最小年龄和最大年龄
                int minAge = rs.getInt("min_age");
                int maxAge = rs.getInt("max_age");
                // 返回包含最小年龄和最大年龄的数组
                return new int[]{minAge, maxAge};
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果没有找到数据，返回默认值
        return new int[]{0, 0};
    }
    
    /**
     * 获取指定商品的有效保险期间范围
     * @param productId 商品ID
     * @return 包含最短保险期间和最长保险期间的数组
     */
    public int[] getValidPeriodRange(int productId) {
        // 定义SQL查询语句，获取指定商品的有效保险期间范围
        String sql = "SELECT MIN(insurance_period) as min_period, MAX(insurance_period) as max_period " +
                   "FROM premium_rates " +
                   "WHERE product_id = ? " +
                   "AND valid_from <= CURDATE() " +
                   "AND (valid_to IS NULL OR valid_to >= CURDATE())";
        
        // 使用try-with-resources自动管理数据库连接和预处理语句
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置查询参数，将productId值赋给第一个占位符(?)
            pstmt.setInt(1, productId);
            // 执行查询并获取结果集
            ResultSet rs = pstmt.executeQuery();
            
            // 如果结果集中有数据
            if (rs.next()) {
                // 获取最短保险期间和最长保险期间
                int minPeriod = rs.getInt("min_period");
                int maxPeriod = rs.getInt("max_period");
                // 返回包含最短保险期间和最长保险期间的数组
                return new int[]{minPeriod, maxPeriod};
            }
        } catch (SQLException e) {
            // 捕获并打印SQL异常
            e.printStackTrace();
        }
        // 如果没有找到数据，返回默认值
        return new int[]{0, 0};
    }
    
    /**
     * 将ResultSet映射到PremiumRate对象
     * @param rs 数据库查询结果集
     * @return 映射后的PremiumRate对象
     * @throws SQLException SQL异常
     */
    private PremiumRate mapResultSetToPremiumRate(ResultSet rs) throws SQLException {
        // 创建一个新的PremiumRate对象
        PremiumRate rate = new PremiumRate();
        // 逐个设置PremiumRate对象的属性，从结果集中获取对应字段的值
        rate.setId(rs.getInt("id"));                           // ID
        rate.setProductId(rs.getInt("product_id"));            // 商品ID
        rate.setGender(rs.getString("gender"));                // 性别
        rate.setEntryAge(rs.getInt("entry_age"));              // 加入年龄
        rate.setInsurancePeriod(rs.getInt("insurance_period")); // 保险期间
        rate.setBaseRate(rs.getDouble("base_rate"));           // 基本料率
        rate.setLoadingRate(rs.getDouble("loading_rate"));     // 付加料率
        rate.setValidFrom(rs.getDate("valid_from"));           // 适用开始日
        rate.setValidTo(rs.getDate("valid_to"));               // 适用结束日
        rate.setCreatedAt(rs.getTimestamp("created_at"));      // 创建时间
        rate.setUpdatedAt(rs.getTimestamp("updated_at"));      // 更新时间
        rate.setProductName(rs.getString("product_name"));     // 商品名称
        rate.setProductCode(rs.getString("product_code"));     // 商品代码
        
        // 返回映射完成的PremiumRate对象
        return rate;
    }
}