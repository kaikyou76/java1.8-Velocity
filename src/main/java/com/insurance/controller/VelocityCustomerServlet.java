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
 */
@WebServlet("/velocity/customer")
public class VelocityCustomerServlet extends HttpServlet {
    
    private CustomerService customerService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        customerService = new CustomerService();
        
        // 初始化Velocity引擎
        VelocityUtil.initVelocityEngine(getServletContext());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list";
        }
        
        try {
            switch (action) {
                case "list":
                    listCustomers(request, response);
                    break;
                case "view":
                    viewCustomer(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteCustomer(request, response);
                    break;
                case "search":
                    searchCustomers(request, response);
                    break;
                default:
                    listCustomers(request, response);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
            return;
        }
        
        try {
            switch (action) {
                case "add":
                    addCustomer(request, response);
                    break;
                case "update":
                    updateCustomer(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    /**
     * 显示顾客列表（使用Velocity模板）
     */
    private void listCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Customer> customers = customerService.getAllCustomers();
        
        VelocityContext context = VelocityUtil.createContext(request, response);
        context.put("customers", customers);
        context.put("pageTitle", "顧客一覧");
        
        // 设置消息
        String message = request.getParameter("message");
        if (message != null) {
            context.put("successMessage", message);
        }
        
        String error = request.getParameter("error");
        if (error != null) {
            context.put("errorMessage", error);
        }
        
        VelocityUtil.renderTemplateToResponse("customer/list.vm", context, response);
    }
    
    /**
     * 查看顾客详情
     */
    private void viewCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            Customer customer = customerService.getCustomerById(id);
            
            if (customer == null) {
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=顾客不存在");
                return;
            }
            
            VelocityContext context = VelocityUtil.createContext(request, response);
            context.put("customer", customer);
            context.put("pageTitle", "顧客詳細");
            
            VelocityUtil.renderTemplateToResponse("customer/view.vm", context, response);
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=无效的顾客ID");
        }
    }
    
    /**
     * 显示编辑表单
     */
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        VelocityContext context = VelocityUtil.createContext(request, response);
        context.put("pageTitle", "顧客登録・編集");
        
        String idStr = request.getParameter("id");
        
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int id = Integer.parseInt(idStr);
                Customer customer = customerService.getCustomerById(id);
                
                if (customer != null) {
                    context.put("customer", customer);
                    context.put("isEdit", true);
                }
            } catch (NumberFormatException e) {
                // 忽略错误，显示空表单
            }
        }
        
        // 设置错误消息（如果有）
        String errorMessage = (String) request.getAttribute("errorMessage");
        if (errorMessage != null) {
            context.put("errorMessage", errorMessage);
        }
        
        VelocityUtil.renderTemplateToResponse("customer/form.vm", context, response);
    }
    
    /**
     * 添加顾客
     */
    private void addCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        Customer customer = createCustomerFromRequest(request);
        String validationError = customerService.validateCustomer(customer);
        
        if (validationError != null) {
            request.setAttribute("errorMessage", validationError);
            showEditForm(request, response);
            return;
        }
        
        boolean success = customerService.addCustomer(customer);
        
        if (success) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&message=顾客添加成功");
        } else {
            request.setAttribute("errorMessage", "添加顾客失败，请检查顾客编号是否重复");
            showEditForm(request, response);
        }
    }
    
    /**
     * 更新顾客
     */
    private void updateCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            Customer customer = createCustomerFromRequest(request);
            customer.setId(id);
            
            String validationError = customerService.validateCustomer(customer);
            
            if (validationError != null) {
                request.setAttribute("errorMessage", validationError);
                showEditForm(request, response);
                return;
            }
            
            boolean success = customerService.updateCustomer(customer);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&message=顾客更新成功");
            } else {
                request.setAttribute("errorMessage", "更新顾客失败");
                showEditForm(request, response);
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=无效的顾客ID");
        }
    }
    
    /**
     * 删除顾客
     */
    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list");
            return;
        }
        
        try {
            int id = Integer.parseInt(idStr);
            boolean success = customerService.deleteCustomer(id);
            
            if (success) {
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&message=顾客删除成功");
            } else {
                response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=删除顾客失败");
            }
            
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/velocity/customer?action=list&error=无效的顾客ID");
        }
    }
    
    /**
     * 搜索顾客
     */
    private void searchCustomers(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("keyword");
        List<Customer> customers = customerService.searchCustomers(keyword);
        
        VelocityContext context = VelocityUtil.createContext(request, response);
        context.put("customers", customers);
        context.put("keyword", keyword);
        context.put("pageTitle", "顧客検索結果");
        
        VelocityUtil.renderTemplateToResponse("customer/list.vm", context, response);
    }
    
    /**
     * 从请求参数创建Customer对象
     */
    private Customer createCustomerFromRequest(HttpServletRequest request) {
        Customer customer = new Customer();
        
        customer.setCustomerCode(request.getParameter("customerCode"));
        customer.setFirstName(request.getParameter("firstName"));
        customer.setLastName(request.getParameter("lastName"));
        customer.setFirstNameKana(request.getParameter("firstNameKana"));
        customer.setLastNameKana(request.getParameter("lastNameKana"));
        customer.setGender(request.getParameter("gender"));
        
        // 处理出生日期
        String birthDateStr = request.getParameter("birthDate");
        if (birthDateStr != null && !birthDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date birthDate = sdf.parse(birthDateStr);
                customer.setBirthDate(birthDate);
            } catch (ParseException e) {
                // 日期格式错误，将在验证时处理
            }
        }
        
        customer.setPostalCode(request.getParameter("postalCode"));
        customer.setPrefecture(request.getParameter("prefecture"));
        customer.setCity(request.getParameter("city"));
        customer.setAddressLine1(request.getParameter("addressLine1"));
        customer.setAddressLine2(request.getParameter("addressLine2"));
        customer.setPhoneNumber(request.getParameter("phoneNumber"));
        customer.setEmail(request.getParameter("email"));
        customer.setOccupation(request.getParameter("occupation"));
        
        // 处理年收入
        String annualIncomeStr = request.getParameter("annualIncome");
        if (annualIncomeStr != null && !annualIncomeStr.isEmpty()) {
            try {
                double annualIncome = Double.parseDouble(annualIncomeStr);
                customer.setAnnualIncome(annualIncome);
            } catch (NumberFormatException e) {
                // 数字格式错误
            }
        }
        
        customer.setFamilyComposition(request.getParameter("familyComposition"));
        
        return customer;
    }
}