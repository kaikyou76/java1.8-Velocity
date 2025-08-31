package com.insurance.controller;

import com.insurance.model.Customer;
import com.insurance.service.CustomerService;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 顾客管理Servlet
 * 用于处理顾客相关的HTTP请求，包括增删改查等操作
 */
@WebServlet("/customer")
public class CustomerServlet extends HttpServlet {
    
    // 顾客服务对象，用于处理业务逻辑
    private CustomerService customerService;
    
    /**
     * 初始化Servlet
     * 在Servlet实例创建后调用，用于初始化必要的资源
     * @throws ServletException 当初始化过程中发生错误时抛出
     */
    @Override
    public void init() throws ServletException {
        // 调用父类的初始化方法
        super.init();
        // 创建顾客服务实例
        customerService = new CustomerService();
    }
    
    /**
     * 处理HTTP GET请求
     * 根据请求参数中的action值执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的action值，用于确定要执行的操作
        String action = request.getParameter("action");
        
        // 如果action参数为空，则默认设置为"list"
        if (action == null) {
            action = "list";
        }
        
        try {
            // 根据action值执行相应的操作
            switch (action) {
                case "list":
                    // 显示顾客列表
                    listCustomers(request, response);
                    break;
                case "view":
                    // 查看顾客详情
                    viewCustomer(request, response);
                    break;
                case "edit":
                    // 显示编辑表单
                    showEditForm(request, response);
                    break;
                case "delete":
                    // 删除顾客
                    deleteCustomer(request, response);
                    break;
                case "search":
                    // 搜索顾客
                    searchCustomers(request, response);
                    break;
                default:
                    // 默认显示顾客列表
                    listCustomers(request, response);
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 处理HTTP POST请求
     * 根据请求参数中的action值执行相应的操作
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的action值，用于确定要执行的操作
        String action = request.getParameter("action");
        
        // 如果action参数为空，则重定向到顾客列表页面
        if (action == null) {
            response.sendRedirect("customer?action=list");
            return;
        }
        
        try {
            // 根据action值执行相应的操作
            switch (action) {
                case "add":
                    // 添加顾客
                    addCustomer(request, response);
                    break;
                case "update":
                    // 更新顾客
                    updateCustomer(request, response);
                    break;
                default:
                    // 默认重定向到顾客列表页面
                    response.sendRedirect("customer?action=list");
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 显示顾客列表
     * 获取所有顾客信息并转发到列表页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void listCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 调用服务层方法获取所有顾客列表
        List<Customer> customers = customerService.getAllCustomers();
        // 将顾客列表设置为请求属性，供JSP页面使用
        request.setAttribute("customers", customers);
        
        // 获取请求转发器，指向顾客列表页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/list.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 查看顾客详情
     * 根据顾客ID获取顾客详细信息并转发到详情页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void viewCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的顾客ID
        String idStr = request.getParameter("id");
        // 如果ID为空，则重定向到顾客列表页面
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("customer?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 调用服务层方法根据ID获取顾客信息
            Customer customer = customerService.getCustomerById(id);
            
            // 如果顾客不存在，则设置错误信息并重定向到列表页面
            if (customer == null) {
                request.setAttribute("errorMessage", "顾客不存在");
                response.sendRedirect("customer?action=list");
                return;
            }
            
            // 将顾客信息设置为请求属性，供JSP页面使用
            request.setAttribute("customer", customer);
            // 获取请求转发器，指向顾客详情页面
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/view.jsp");
            // 转发请求到JSP页面
            dispatcher.forward(request, response);
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则设置错误信息并重定向到列表页面
            request.setAttribute("errorMessage", "无效的顾客ID");
            response.sendRedirect("customer?action=list");
        }
    }
    
    /**
     * 显示编辑表单
     * 根据顾客ID获取顾客信息并转发到编辑表单页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的顾客ID
        String idStr = request.getParameter("id");
        
        // 如果ID不为空，则尝试获取顾客信息
        if (idStr != null && !idStr.isEmpty()) {
            try {
                // 将字符串ID转换为整数
                int id = Integer.parseInt(idStr);
                // 调用服务层方法根据ID获取顾客信息
                Customer customer = customerService.getCustomerById(id);
                
                // 如果顾客存在，则将顾客信息设置为请求属性
                if (customer != null) {
                    request.setAttribute("customer", customer);
                    // 设置编辑标志，用于JSP页面判断是新增还是编辑
                    request.setAttribute("isEdit", true);
                }
            } catch (NumberFormatException e) {
                // 如果ID格式不正确，则忽略错误，显示空表单
            }
        }
        
        // 获取请求转发器，指向顾客表单页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/form.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 添加顾客
     * 从请求参数中获取顾客信息并添加到数据库
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void addCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 从请求参数中创建顾客对象
        Customer customer = createCustomerFromRequest(request);
        // 验证顾客信息
        String validationError = customerService.validateCustomer(customer);
        
        // 如果验证失败，则设置错误信息并转发到表单页面
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            request.setAttribute("customer", customer);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/form.jsp");
            dispatcher.forward(request, response);
            return;
        }
        
        // 调用服务层方法添加顾客
        boolean success = customerService.addCustomer(customer);
        
        // 根据添加结果进行相应处理
        if (success) {
            // 添加成功，重定向到列表页面并显示成功消息
            response.sendRedirect("customer?action=list&message=顾客添加成功");
        } else {
            // 添加失败，设置错误信息并转发到表单页面
            request.setAttribute("errorMessage", "添加顾客失败，请检查顾客编号是否重复");
            request.setAttribute("customer", customer);
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/form.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    /**
     * 更新顾客
     * 根据顾客ID和请求参数更新顾客信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void updateCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的顾客ID
        String idStr = request.getParameter("id");
        // 如果ID为空，则重定向到顾客列表页面
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("customer?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 从请求参数中创建顾客对象
            Customer customer = createCustomerFromRequest(request);
            // 设置顾客ID
            customer.setId(id);
            
            // 验证顾客信息
            String validationError = customerService.validateCustomer(customer);
            
            // 如果验证失败，则设置错误信息并转发到表单页面
            if (validationError != null) {
                request.setAttribute("errorMessage", validationError);
                request.setAttribute("customer", customer);
                request.setAttribute("isEdit", true);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/form.jsp");
                dispatcher.forward(request, response);
                return;
            }
            
            // 调用服务层方法更新顾客
            boolean success = customerService.updateCustomer(customer);
            
            // 根据更新结果进行相应处理
            if (success) {
                // 更新成功，重定向到列表页面并显示成功消息
                response.sendRedirect("customer?action=list&message=顾客更新成功");
            } else {
                // 更新失败，设置错误信息并转发到表单页面
                request.setAttribute("errorMessage", "更新顾客失败");
                request.setAttribute("customer", customer);
                request.setAttribute("isEdit", true);
                RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/form.jsp");
                dispatcher.forward(request, response);
            }
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则设置错误信息并重定向到列表页面
            request.setAttribute("errorMessage", "无效的顾客ID");
            response.sendRedirect("customer?action=list");
        }
    }
    
    /**
     * 删除顾客
     * 根据顾客ID删除顾客信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的顾客ID
        String idStr = request.getParameter("id");
        // 如果ID为空，则重定向到顾客列表页面
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect("customer?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 调用服务层方法删除顾客
            boolean success = customerService.deleteCustomer(id);
            
            // 根据删除结果进行相应处理
            if (success) {
                // 删除成功，重定向到列表页面并显示成功消息
                response.sendRedirect("customer?action=list&message=顾客删除成功");
            } else {
                // 删除失败，重定向到列表页面并显示错误消息
                response.sendRedirect("customer?action=list&error=删除顾客失败");
            }
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则重定向到列表页面并显示错误消息
            response.sendRedirect("customer?action=list&error=无效的顾客ID");
        }
    }
    
    /**
     * 搜索顾客
     * 根据关键字搜索顾客信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void searchCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 获取请求参数中的搜索关键字
        String keyword = request.getParameter("keyword");
        // 调用服务层方法搜索顾客
        List<Customer> customers = customerService.searchCustomers(keyword);
        
        // 将搜索结果和关键字设置为请求属性，供JSP页面使用
        request.setAttribute("customers", customers);
        request.setAttribute("searchKeyword", keyword);
        
        // 获取请求转发器，指向顾客列表页面
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/customer/list.jsp");
        // 转发请求到JSP页面
        dispatcher.forward(request, response);
    }
    
    /**
     * 从请求参数创建Customer对象
     * 提取请求中的参数并封装成Customer对象
     * @param request HTTP请求对象
     * @return Customer对象
     */
    private Customer createCustomerFromRequest(HttpServletRequest request) {
        // 创建新的顾客对象
        Customer customer = new Customer();
        
        // 设置顾客编号
        customer.setCustomerCode(request.getParameter("customerCode"));
        // 设置名
        customer.setFirstName(request.getParameter("firstName"));
        // 设置姓
        customer.setLastName(request.getParameter("lastName"));
        // 设置名（片假名）
        customer.setFirstNameKana(request.getParameter("firstNameKana"));
        // 设置姓（片假名）
        customer.setLastNameKana(request.getParameter("lastNameKana"));
        // 设置性别
        customer.setGender(request.getParameter("gender"));
        
        // 处理出生日期
        String birthDateStr = request.getParameter("birthDate");
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                // 创建日期格式化对象
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                // 解析日期字符串
                Date birthDate = sdf.parse(birthDateStr);
                // 设置出生日期
                customer.setBirthDate(birthDate);
            } catch (ParseException e) {
                // 日期格式错误，将在验证时处理
            }
        }
        
        // 设置邮政编码
        customer.setPostalCode(request.getParameter("postalCode"));
        // 设置都道府县
        customer.setPrefecture(request.getParameter("prefecture"));
        // 设置市区町村
        customer.setCity(request.getParameter("city"));
        // 设置地址1
        customer.setAddressLine1(request.getParameter("addressLine1"));
        // 设置地址2
        customer.setAddressLine2(request.getParameter("addressLine2"));
        // 设置电话号码
        customer.setPhoneNumber(request.getParameter("phoneNumber"));
        // 设置邮箱
        customer.setEmail(request.getParameter("email"));
        // 设置职业
        customer.setOccupation(request.getParameter("occupation"));
        
        // 处理年收入
        String annualIncomeStr = request.getParameter("annualIncome");
        if (annualIncomeStr != null && !annualIncomeStr.isEmpty()) {
            try {
                // 将字符串转换为double类型
                double annualIncome = Double.parseDouble(annualIncomeStr);
                // 设置年收入
                customer.setAnnualIncome(annualIncome);
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        // 设置家庭构成
        customer.setFamilyComposition(request.getParameter("familyComposition"));
        
        // 返回创建的顾客对象
        return customer;
    }
}