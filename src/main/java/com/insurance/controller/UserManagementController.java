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
 * 用于处理用户管理的HTTP请求，包括用户列表显示、用户创建、编辑、删除、状态切换、密码重置和账户解锁等功能
 */
@WebServlet("/admin/users/*")
public class UserManagementController extends HttpServlet {
    
    // 用户服务对象，用于处理用户相关业务逻辑
    private UserService userService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     * @throws ServletException 当初始化过程中发生错误时抛出
     */
    @Override
    public void init() throws ServletException {
        // 创建用户服务实例
        this.userService = new UserService();
    }
    
    /**
     * 处理GET请求
     * 根据请求路径执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 检查管理员权限
        // 验证当前用户是否具有管理员权限，如果没有则重定向到访问拒绝页面
        if (!AuthUtil.hasPermission(request, "ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return;
        }
        
        // 获取请求路径信息
        String pathInfo = request.getPathInfo();
        
        // 根据路径信息执行相应的操作
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
     * 根据请求参数中的action值执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 检查管理员权限
        // 验证当前用户是否具有管理员权限，如果没有则重定向到访问拒绝页面
        if (!AuthUtil.hasPermission(request, "ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/access-denied");
            return;
        }
        
        // 设置请求字符编码
        // 设置请求的字符编码为UTF-8，以正确处理中文字符
        request.setCharacterEncoding("UTF-8");
        
        // 验证CSRF令牌
        // 获取请求参数中的CSRF令牌
        String csrfToken = request.getParameter("csrfToken");
        // 获取会话中的CSRF令牌
        String sessionToken = SecurityFilter.getCSRFToken(request);
        // 如果令牌为空或不匹配，则记录警告日志并返回错误信息
        if (sessionToken == null || !sessionToken.equals(csrfToken)) {
            LogUtil.warn("CSRF令牌验证失败", "UserManagementController", AuthUtil.getRemoteAddr(request));
            request.setAttribute("error", "CSRF令牌验证失败");
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
            return;
        }
        
        // 获取请求参数中的操作类型
        String action = request.getParameter("action");
        
        // 根据操作类型执行相应的处理方法
        if ("create".equals(action)) {
            // 创建用户
            createUser(request, response);
        } else if ("update".equals(action)) {
            // 更新用户
            updateUser(request, response);
        } else if ("delete".equals(action)) {
            // 删除用户
            deleteUser(request, response);
        } else if ("toggle-status".equals(action)) {
            // 切换用户状态
            toggleUserStatus(request, response);
        } else if ("reset-password".equals(action)) {
            // 重置密码
            resetPassword(request, response);
        } else if ("unlock-account".equals(action)) {
            // 解锁账户
            unlockAccount(request, response);
        } else {
            // 发送错误响应，表示无效操作
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * 用户列表页面
     * 显示用户列表和统计信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void listUsers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取搜索关键词
            String keyword = request.getParameter("keyword");
            List<User> users;
            
            // 根据是否有搜索关键词来决定获取所有用户还是搜索用户
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword);
                request.setAttribute("keyword", keyword);
            } else {
                users = userService.getAllUsers();
            }
            
            // 获取统计信息
            UserService.UserStatistics stats = userService.getUserStatistics();
            // 将用户列表和统计信息设置为请求属性，供JSP页面使用
            request.setAttribute("users", users);
            request.setAttribute("stats", stats);
            
            // 生成CSRF令牌
            String csrfToken = SecurityFilter.generateCSRFToken(request);
            request.setAttribute("csrfToken", csrfToken);
            
            // 转发到用户列表页面
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
            
        } catch (Exception e) {
            // 记录错误日志并设置错误信息
            LogUtil.error("获取用户列表失败", e);
            request.setAttribute("error", "获取用户列表失败");
            request.getRequestDispatcher("/WEB-INF/views/admin/users/list.jsp").forward(request, response);
        }
    }
    
    /**
     * 创建用户表单页面
     * 显示创建用户表单
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
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
     * 显示编辑用户表单
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void editUserForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取请求参数中的用户ID
            int id = Integer.parseInt(request.getParameter("id"));
            // 根据ID获取用户信息
            User user = userService.getUserById(id);
            
            // 如果用户不存在，则设置错误信息并重定向到用户列表页面
            if (user == null) {
                request.setAttribute("error", "用户不存在");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
                return;
            }
            
            // 生成CSRF令牌
            String csrfToken = SecurityFilter.generateCSRFToken(request);
            request.setAttribute("csrfToken", csrfToken);
            
            // 将用户信息设置为请求属性，供JSP页面使用
            request.setAttribute("user", user);
            // 转发到编辑用户页面
            request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
            
        } catch (Exception e) {
            // 记录错误日志并设置错误信息，然后重定向到用户列表页面
            LogUtil.error("获取用户信息失败", e);
            request.setAttribute("error", "获取用户信息失败");
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 查看用户详情
     * 显示用户详细信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void viewUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取请求参数中的用户ID
            int id = Integer.parseInt(request.getParameter("id"));
            // 根据ID获取用户信息
            User user = userService.getUserById(id);
            
            // 如果用户不存在，则设置错误信息并重定向到用户列表页面
            if (user == null) {
                request.setAttribute("error", "用户不存在");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
                return;
            }
            
            // 将用户信息设置为请求属性，供JSP页面使用
            request.setAttribute("user", user);
            // 转发到查看用户详情页面
            request.getRequestDispatcher("/WEB-INF/views/admin/users/view.jsp").forward(request, response);
            
        } catch (Exception e) {
            // 记录错误日志并设置错误信息，然后重定向到用户列表页面
            LogUtil.error("获取用户详情失败", e);
            request.setAttribute("error", "获取用户详情失败");
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 创建用户
     * 处理创建用户表单提交
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void createUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取表单参数
            String username = request.getParameter("username"); // 用户名
            String password = request.getParameter("password"); // 密码
            String email = request.getParameter("email"); // 邮箱
            String fullName = request.getParameter("fullName"); // 全名
            String role = request.getParameter("role"); // 角色
            String department = request.getParameter("department"); // 部门
            String phoneNumber = request.getParameter("phoneNumber"); // 电话号码
            
            // 创建用户对象
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(role);
            user.setDepartment(department);
            user.setPhoneNumber(phoneNumber);
            user.setActive(true); // 默认激活状态
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String createdBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 创建用户
            boolean success = userService.createUser(user, createdBy);
            
            // 根据创建结果进行相应处理
            if (success) {
                // 记录创建用户成功的日志
                LogUtil.info("创建用户成功: " + username, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户创建成功");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
            } else {
                request.setAttribute("error", "用户创建失败");
                request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            // 记录创建用户失败的日志并设置错误信息
            LogUtil.error("创建用户失败", e);
            request.setAttribute("error", "用户创建失败: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/admin/users/create.jsp").forward(request, response);
        }
    }
    
    /**
     * 更新用户
     * 处理更新用户表单提交
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void updateUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取表单参数
            int id = Integer.parseInt(request.getParameter("id")); // 用户ID
            String username = request.getParameter("username"); // 用户名
            String password = request.getParameter("password"); // 密码
            String email = request.getParameter("email"); // 邮箱
            String fullName = request.getParameter("fullName"); // 全名
            String role = request.getParameter("role"); // 角色
            String department = request.getParameter("department"); // 部门
            String phoneNumber = request.getParameter("phoneNumber"); // 电话号码
            
            // 获取用户对象
            User user = userService.getUserById(id);
            if (user == null) {
                request.setAttribute("error", "用户不存在");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
                return;
            }
            
            // 更新用户信息
            user.setUsername(username);
            // 如果密码不为空，则更新密码
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
            
            // 根据更新结果进行相应处理
            if (success) {
                // 记录更新用户成功的日志
                LogUtil.info("更新用户成功: " + username, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户更新成功");
                response.sendRedirect(request.getContextPath() + "/admin/users/");
            } else {
                request.setAttribute("error", "用户更新失败");
                request.setAttribute("user", user);
                request.getRequestDispatcher("/WEB-INF/views/admin/users/edit.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            // 记录更新用户失败的日志并设置错误信息，然后重定向到用户列表页面
            LogUtil.error("更新用户失败", e);
            request.setAttribute("error", "用户更新失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 删除用户
     * 处理删除用户请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取请求参数中的用户ID
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String deletedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 删除用户
            boolean success = userService.deleteUser(id, deletedBy);
            
            // 根据删除结果进行相应处理
            if (success) {
                // 记录删除用户成功的日志
                LogUtil.info("删除用户成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户删除成功");
            } else {
                request.setAttribute("error", "用户删除失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            // 记录删除用户失败的日志并设置错误信息，然后重定向到用户列表页面
            LogUtil.error("删除用户失败", e);
            request.setAttribute("error", "用户删除失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 切换用户状态
     * 处理切换用户激活状态请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void toggleUserStatus(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取请求参数中的用户ID
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 切换用户状态
            boolean success = userService.toggleUserStatus(id, updatedBy);
            
            // 根据切换结果进行相应处理
            if (success) {
                // 记录切换用户状态成功的日志
                LogUtil.info("切换用户状态成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "用户状态更新成功");
            } else {
                request.setAttribute("error", "用户状态更新失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            // 记录切换用户状态失败的日志并设置错误信息，然后重定向到用户列表页面
            LogUtil.error("切换用户状态失败", e);
            request.setAttribute("error", "用户状态更新失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 重置密码
     * 处理重置用户密码请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void resetPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取请求参数中的用户ID和新密码
            int id = Integer.parseInt(request.getParameter("id"));
            String newPassword = request.getParameter("newPassword");
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 重置密码
            boolean success = userService.resetPassword(id, newPassword, updatedBy);
            
            // 根据重置结果进行相应处理
            if (success) {
                // 记录重置用户密码成功的日志
                LogUtil.info("重置用户密码成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "密码重置成功");
            } else {
                request.setAttribute("error", "密码重置失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            // 记录重置密码失败的日志并设置错误信息，然后重定向到用户列表页面
            LogUtil.error("重置密码失败", e);
            request.setAttribute("error", "密码重置失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
    
    /**
     * 解锁账户
     * 处理解锁用户账户请求
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void unlockAccount(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 获取请求参数中的用户ID
            int id = Integer.parseInt(request.getParameter("id"));
            
            // 获取当前登录用户
            User currentUser = AuthUtil.getCurrentUser(request);
            String updatedBy = currentUser != null ? currentUser.getUsername() : "system";
            
            // 解锁账户
            boolean success = userService.unlockUserAccount(id, updatedBy);
            
            // 根据解锁结果进行相应处理
            if (success) {
                // 记录解锁用户账户成功的日志
                LogUtil.info("解锁用户账户成功: ID=" + id, "UserManagementController", AuthUtil.getRemoteAddr(request));
                request.setAttribute("success", "账户解锁成功");
            } else {
                request.setAttribute("error", "账户解锁失败");
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/users/");
            
        } catch (Exception e) {
            // 记录解锁账户失败的日志并设置错误信息，然后重定向到用户列表页面
            LogUtil.error("解锁账户失败", e);
            request.setAttribute("error", "账户解锁失败: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users/");
        }
    }
}