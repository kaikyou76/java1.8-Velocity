package com.insurance.controller;

import com.insurance.model.User;
import com.insurance.security.AuthUtil;
import com.insurance.security.SecurityFilter;
import com.insurance.service.UserService;
import com.insurance.util.LogUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 用户管理控制器
 * 处理用户相关的管理操作
 */
@WebServlet("/admin/users/*")
public class UserManagementController extends HttpServlet {
    
    private UserService userService;
    
    @Override
    public void init() throws ServletException {
        this.userService = new UserService();
    }
    
    /**
     * 处理GET请求
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 检查管理员权限
        if (!AuthUtil.hasPermission(request, "ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return;
        }
        
        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 用户列表页面
            listUsers(request, response);
        } else if (pathInfo.equals("/create")) {
            // 创建用户页面
            createUserForm(request, response);
        } else if (pathInfo.equals("/edit")) {
            // 编辑用户页面
            editUserForm(request, response);
        } else if (pathInfo.equals("/view")) {
            // 查看用户详情
            viewUser(request, response);
        } else {
            // 404页面
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
    
    /**
     * 处理POST请求
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 检查管理员权限
        if (!AuthUtil.hasPermission(request, "ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return;
        }
        
        // 设置请求字符编码
        request.setCharacterEncoding("UTF-8");
        
        // 验证CSRF令牌
        String csrfToken = request.getParameter("csrfToken");
        String sessionToken = SecurityFilter.getCSRFToken(request);
        if (sessionToken == null || !sessionToken.equals(csrfToken)) {
            LogUtil.warn("CSRF令牌验证失败", "UserManagementController", AuthUtil.getRemoteAddr(request));
            request.setAttribute("error", "CSRF令牌验证失败");
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
            return;
        }
        
        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            createUser(request, response);
        } else if ("update".equals(action)) {
            updateUser(request, response);
        } else if ("delete".equals(action)) {
            deleteUser(request, response);
        } else if ("toggle-status".equals(action)) {
            toggleUserStatus(request, response);
        } else if ("reset-password".equals(action)) {
            resetPassword(request, response);
        } else if ("unlock-account".equals(action)) {
            unlockAccount(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * 用户列表页面
     */
    private void listUsers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取搜索关键词
            String keyword = request.getParameter("keyword");
            List<User> users;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword);
                request.setAttribute("keyword", keyword);
            } else {
                users = userService.getAllUsers();
            }
            
            // 获取统计信息
            UserService.UserStatistics stats = userService.getUserStatistics();
            request.setAttribute("users", users);
            request.setAttribute("stats", stats);
            
            // 生成CSRF令牌
            String csrfToken = SecurityFilter.generateCSRFToken(request);
            request.setAttribute("csrfToken", csrfToken);
            
            // 转发到用户列表页面
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
            
        } catch (Exception e) {
            LogUtil.error("获取用户列表失败", e);
            request.setAttribute("error", "获取用户列表失败");
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
        }
    }
    
    /**
     * 创建用户表单页面
     */
    private void createUserForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 生成CSRF令牌
        String csrfToken = SecurityFilter.generateCSRFToken(request);
        request.setAttribute("csrfToken", csrfToken);
        
        // 转发到创建用户页面
        request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
    }
    
    /**
     * 编辑用户表单页面
     */
    private void editUserForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            User user = userService.getUserById(id);
            
            if (user == null) {
                request.setAttribute("error", "用户不存在");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
                return;
            }
            
            // 生成CSRF令牌
            String csrfToken = SecurityFilter.generateCSRFToken(request);
            request.setAttribute("csrfToken", csrfToken);
            
            request.setAttribute("user", user);
            request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
            
        } catch (Exception e) {
            LogUtil.error("获取用户信息失败", e);
            request.setAttribute("error", "获取用户信息失败");
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 查看用户详情
     */
    private void viewUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            User user = userService.getUserById(id);
            
            if (user == null) {
                request.setAttribute("error", "用户不存在");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
                return;
            }
            
            request.setAttribute("user", user);
            request.getRequestDispatcher("/WEB-INF/views/admin/users/view.jsp").forward(request, response);
            
        } catch (Exception e) {
            LogUtil.error("获取用户详情失败", e);
            request.setAttribute("error", "获取用户详情失败");
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 创建用户
     */
    private void createUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取表单参数
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String fullName = request.getParameter("fullName");
            String role = request.getParameter("role");
            String department = request.getParameter("department");
            String phoneNumber = request.getParameter("phoneNumber");
            
            // 创建用户对象
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(role);
            user.setDepartment(department);
            user.setPhoneNumber(phoneNumber);
            user.setActive(true);
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 创建用户
            boolean success = userService.createUser(user, createdBy);
            
            if (success) {
                LogUtil.info("创建用户成功: " + username, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户创建成功");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
            } else {
                request.setAttribute("error", "用户创建失败");
                request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            LogUtil.error("创建用户失败", e);
            request.setAttribute("error", "用户创建失败: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
        }
    }
    
    /**
     * 更新用户
     */
    private void updateUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取表单参数
            int id = Integer.parseInt(request.getParameter("id"));
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String fullName = request.getParameter("fullName");
            String role = request.getParameter("role");
            String department = request.getParameter("department");
            String phoneNumber = request.getParameter("phoneNumber");
            
            // 获取用户对象
            User user = userService.getUserById(id);
            if (user == null) {
                request.setAttribute("error", "用户不存在");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
                return;
            }
            
            // 更新用户信息
            user.setUsername(username);
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(role);
            user.setDepartment(department);
            user.setPhoneNumber(phoneNumber);
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 更新用户
            boolean success = userService.updateUser(user, updatedBy);
            
            if (success) {
                LogUtil.info("更新用户成功: " + username, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户更新成功");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
            } else {
                request.setAttribute("error", "用户更新失败");
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            LogUtil.error("更新用户失败", e);
            request.setAttribute("error", "用户更新失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 删除用户
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 删除用户
            boolean success = userService.deleteUser(id, deletedBy);
            
            if (success) {
                LogUtil.info("删除用户成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户删除成功");
            } else {
                request.setAttribute("error", "用户删除失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            LogUtil.error("删除用户失败", e);
            request.setAttribute("error", "用户删除失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 切换用户状态
     */
    private void toggleUserStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 切换用户状态
            boolean success = userService.toggleUserStatus(id, updatedBy);
            
            if (success) {
                LogUtil.info("切换用户状态成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户状态更新成功");
            } else {
                request.setAttribute("error", "用户状态更新失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            LogUtil.error("切换用户状态失败", e);
            request.setAttribute("error", "用户状态更新失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 重置密码
     */
    private void resetPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String newPassword = request.getParameter("newPassword");
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 重置密码
            boolean success = userService.resetPassword(id, newPassword, updatedBy);
            
            if (success) {
                LogUtil.info("重置用户密码成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "密码重置成功");
            } else {
                request.setAttribute("error", "密码重置失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            LogUtil.error("重置密码失败", e);
            request.setAttribute("error", "密码重置失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 解锁账户
     */
    private void unlockAccount(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 解锁账户
            boolean success = userService.unlockUserAccount(id, updatedBy);
            
            if (success) {
                LogUtil.info("解锁用户账户成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "账户解锁成功");
            } else {
                request.setAttribute("error", "账户解锁失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            LogUtil.error("解锁账户失败", e);
            request.setAttribute("error", "账户解锁失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
}