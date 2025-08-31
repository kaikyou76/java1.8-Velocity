package com.insurance.controller;

import com.insurance.model.Customer;
import com.insurance.service.CustomerService;
import com.insurance.util.VelocityUtil;
import org.apache.velocity.VelocityContext;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 使用Velocity模板的顾客管理Servlet
 * 用于处理顾客管理相关的HTTP请求，并使用Velocity模板引擎生成响应页面
 */
@WebServlet("/velocity/customer")
public class VelocityCustomerServlet extends HttpServlet {
    
    // 顾客服务对象，用于处理顾客相关业务逻辑
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
        
        // 初始化Velocity引擎
        // 初始化Velocity模板引擎，传入Servlet上下文
        VelocityUtil.initVelocityEngine(getServletContext());
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
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
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
                    response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
                    break;
            }
        } catch (Exception e) {
            // 捕获异常并重新抛出为ServletException
            throw new ServletException(e);
        }
    }
    
    /**
     * 显示顾客列表（使用Velocity模板）
     * 获取所有顾客信息并使用Velocity模板渲染列表页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void listCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 调用服务层方法获取所有顾客列表
        List<Customer> customers = customerService.getAllCustomers();
        
        // 创建Velocity上下文对象
        VelocityContext context = VelocityUtil.createContext(request, response);
        // 将顾客列表设置到上下文中，供模板使用
        context.put("customers", customers);
        // 设置页面标题
        context.put("pageTitle", "顧客一覧");
        
        // 设置消息
        // 获取请求参数中的成功消息
        String message = request.getParameter("message");
        if (message != null) {
            // 将成功消息设置到上下文中，供模板使用
            context.put("successMessage", message);
        }
        
        // 获取请求参数中的错误消息
        String error = request.getParameter("error");
        if (error != null) {
            // 将错误消息设置到上下文中，供模板使用
            context.put("errorMessage", error);
        }
        
        // 使用Velocity模板渲染响应
        VelocityUtil.renderTemplateToResponse("customer/list.vm", context, response);
    }
    
    /**
     * 查看顾客详情
     * 根据顾客ID获取顾客详细信息并使用Velocity模板渲染详情页面
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
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
            return;
        }
        
        try {
            // 将字符串ID转换为整数
            int id = Integer.parseInt(idStr);
            // 调用服务层方法根据ID获取顾客信息
            Customer customer = customerService.getCustomerById(id);
            
            // 如果顾客不存在，则重定向到列表页面并显示错误消息
            if (customer == null) {
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=顾客不存在");
                return;
            }
            
            // 创建Velocity上下文对象
            VelocityContext context = VelocityUtil.createContext(request, response);
            // 将顾客信息设置到上下文中，供模板使用
            context.put("customer", customer);
            // 设置页面标题
            context.put("pageTitle", "顧客詳細");
            
            // 使用Velocity模板渲染响应
            VelocityUtil.renderTemplateToResponse("customer/view.vm", context, response);
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则重定向到列表页面并显示错误消息
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=无效的顾客ID");
        }
    }
    
    /**
     * 显示编辑表单
     * 使用Velocity模板渲染顾客编辑表单页面
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @throws ServletException 当处理过程中发生错误时抛出
     * @throws IOException 当IO操作发生错误时抛出
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 创建Velocity上下文对象
        VelocityContext context = VelocityUtil.createContext(request, response);
        // 设置页面标题
        context.put("pageTitle", "顧客登録・編集");
        
        // 获取请求参数中的顾客ID
        String idStr = request.getParameter("id");
        
        // 如果ID不为空，则尝试获取顾客信息
        if (idStr != null && !idStr.isEmpty()) {
            try {
                // 将字符串ID转换为整数
                int id = Integer.parseInt(idStr);
                // 调用服务层方法根据ID获取顾客信息
                Customer customer = customerService.getCustomerById(id);
                
                // 如果顾客存在，则将顾客信息设置到上下文中
                if (customer != null) {
                    context.put("customer", customer);
                    // 设置编辑标志，用于模板判断是新增还是编辑
                    context.put("isEdit", true);
                }
            } catch (NumberFormatException e) {
                // 如果ID格式不正确，则忽略错误，显示空表单
            }
        }
        
        // 设置错误消息（如果有）
        // 获取请求属性中的错误消息
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null) {
            // 将错误消息设置到上下文中，供模板使用
            context.put("errorMessage", errorMessage);
        }
        
        // 使用Velocity模板渲染响应
        VelocityUtil.renderTemplateToResponse("customer/form.vm", context, response);
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
        
        // 如果验证失败，则设置错误信息并显示编辑表单
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            showEditForm(request, response);
            return;
        }
        
        // 调用服务层方法添加顾客
        boolean success = customerService.addCustomer(customer);
        
        // 根据添加结果进行相应处理
        if (success) {
            // 添加成功，重定向到列表页面并显示成功消息
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&message=顾客添加成功");
        } else {
            // 添加失败，设置错误信息并显示编辑表单
            request.setAttribute("errorMessage", "添加顾客失败，请检查顾客编号是否重复");
            showEditForm(request, response);
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
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
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
            
            // 如果验证失败，则设置错误信息并显示编辑表单
            if (validationError != null) {
                request.setAttribute("errorMessage", validationError);
                showEditForm(request, response);
                return;
            }
            
            // 调用服务层方法更新顾客
            boolean success = customerService.updateCustomer(customer);
            
            // 根据更新结果进行相应处理
            if (success) {
                // 更新成功，重定向到列表页面并显示成功消息
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&message=顾客更新成功");
            } else {
                // 更新失败，设置错误信息并显示编辑表单
                request.setAttribute("errorMessage", "更新顾客失败");
                showEditForm(request, response);
            }
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则重定向到列表页面并显示错误消息
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=无效的顾客ID");
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
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
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
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&message=顾客删除成功");
            } else {
                // 删除失败，重定向到列表页面并显示错误消息
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=删除顾客失败");
            }
            
        } catch (NumberFormatException e) {
            // 如果ID格式不正确，则重定向到列表页面并显示错误消息
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=无效的顾客ID");
        }
    }
    
    /**
     * 搜索顾客
     * 根据关键字搜索顾客信息并使用Velocity模板渲染列表页面
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
        
        // 创建Velocity上下文对象
        VelocityContext context = VelocityUtil.createContext(request, response);
        // 将搜索结果设置到上下文中，供模板使用
        context.put("customers", customers);
        // 将搜索关键字设置到上下文中，供模板使用
        context.put("keyword", keyword);
        // 设置页面标题
        context.put("pageTitle", "顧客検索結果");
        
        // 使用Velocity模板渲染响应
        VelocityUtil.renderTemplateToResponse("customer/list.vm", context, response);
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