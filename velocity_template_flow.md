# Velocity模板页面完整流程分析

本文档详细描述了从用户请求到Velocity模板页面展示的完整流程，包括涉及的类文件和方法。

## 1. 流程概述

```
用户请求 → 控制器 → 服务层 → DAO层 → 数据库 → DAO层 → 服务层 → 控制器 → Velocity模板 → 用户展示
```

## 2. 详细流程分析

### 2.1 用户请求阶段

用户访问URL：`/velocity/customer?action=list`

### 2.2 控制层 (Controller)

**文件**: `src/main/java/com/insurance/controller/VelocityCustomerServlet.java`

**方法**:
- `doGet()` - 处理GET请求
- `listCustomers()` - 显示顾客列表

**关键代码**:
```java
@WebServlet("/velocity/customer")
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    String action = request.getParameter("action");
    if (action == null) {
        action = "list";
    }
    switch (action) {
        case "list":
            listCustomers(request, response);
            break;
        // 其他case...
    }
}

private void listCustomers(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    List<Customer> customers = customerService.getAllCustomers();
    VelocityContext context = VelocityUtil.createContext(request, response);
    context.put("customers", customers);
    context.put("pageTitle", "顧客一覧");
    VelocityUtil.renderTemplateToResponse("customer/list.vm", context, response);
}
```

### 2.3 服务层 (Service)

**文件**: `src/main/java/com/insurance/service/CustomerService.java`

**方法**:
- `getAllCustomers()` - 获取所有顾客列表

**关键代码**:
```java
public List<Customer> getAllCustomers() {
    return customerDAO.getAllCustomers();
}
```

### 2.4 数据访问层 (DAO)

**文件**: `src/main/java/com/insurance/dao/CustomerDAO.java`

**方法**:
- `getAllCustomers()` - 从数据库获取所有顾客
- `mapResultSetToCustomer()` - 将结果集映射到Customer对象

**关键代码**:
```java
public List<Customer> getAllCustomers() {
    List<Customer> customers = new ArrayList<>();
    String sql = "SELECT * FROM customers WHERE deleted_flag = 0 ORDER BY created_at DESC";
    
    try (Connection conn = DatabaseUtil.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            customers.add(mapResultSetToCustomer(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return customers;
}

private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
    Customer customer = new Customer();
    customer.setId(rs.getInt("id"));
    customer.setCustomerCode(rs.getString("customer_code"));
    customer.setFirstName(rs.getString("first_name"));
    customer.setLastName(rs.getString("last_name"));
    // ... 其他字段映射
    return customer;
}
```

### 2.5 模型层 (Model)

**文件**: `src/main/java/com/insurance/model/Customer.java`

**作用**: 映射数据库中的顾客信息表数据

**关键字段**:
- id
- customerCode
- firstName
- lastName
- gender
- birthDate
- email
- phoneNumber
- 等...

### 2.6 数据库层

**表名**: `customers`

**关键字段**:
- id (主键)
- customer_code (顾客编号)
- first_name (名)
- last_name (姓)
- gender (性别)
- birth_date (出生日期)
- email (邮箱)
- phone_number (电话号码)
- created_at (创建时间)
- deleted_flag (删除标志)

### 2.7 模板渲染工具

**文件**: `src/main/java/com/insurance/util/VelocityUtil.java`

**方法**:
- `createContext()` - 创建Velocity上下文
- `renderTemplateToResponse()` - 渲染模板并输出到响应

**关键代码**:
```java
public static VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {
    VelocityContext context = new VelocityContext();
    context.put("contextPath", request.getContextPath());
    context.put("request", request);
    context.put("response", response);
    return context;
}

public static void renderTemplateToResponse(String templateName, VelocityContext context, 
                                           HttpServletResponse response) throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    
    String content = renderTemplate(templateName, context);
    response.getWriter().write(content);
}
```

### 2.8 Velocity模板页面

**文件**: `src/main/webapp/WEB-INF/templates/customer/list.vm`

**关键代码**:
```html
#parse("/templates/layout.vm")
#set($pageTitle = "顧客一覧")

<div class="table-container">
    <table class="data-table">
        <thead>
            <tr>
                <th>顧客コード</th>
                <th>氏名</th>
                <th>性別</th>
                <th>年齢</th>
                <th>電話番号</th>
                <th>メール</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            #if($customers && !$customers.isEmpty())
                #foreach($customer in $customers)
                    <tr>
                        <td>$!{customer.customerCode}</td>
                        <td>$!{customer.lastName} $!{customer.firstName}</td>
                        <td>#if($customer.gender == 'M')男#else女#end</td>
                        <td>$!{customer.age}歳</td>
                        <td>$!{customer.phoneNumber}</td>
                        <td>$!{customer.email}</td>
                        <td class="actions">
                            <a href="$!{contextPath}/customer?action=view&id=$!{customer.id}" class="btn btn-info btn-sm">詳細</a>
                            <a href="$!{contextPath}/customer?action=edit&id=$!{customer.id}" class="btn btn-warning btn-sm">編集</a>
                        </td>
                    </tr>
                #end
            #else
                <tr>
                    <td colspan="8" class="text-center">顧客データがありません</td>
                </tr>
            #end
        </tbody>
    </table>
</div>
```

### 2.9 布局模板

**文件**: `src/main/webapp/WEB-INF/templates/layout.vm`

**作用**: 提供统一的页面布局结构

## 3. 流程总结

1. **用户请求**: 用户访问`/velocity/customer?action=list`
2. **控制器处理**: `VelocityCustomerServlet`接收请求，调用`listCustomers()`方法
3. **服务层调用**: `CustomerService.getAllCustomers()`获取顾客列表
4. **数据访问**: `CustomerDAO.getAllCustomers()`查询数据库并映射为Customer对象列表
5. **数据返回**: 数据逐层返回到控制器
6. **模板渲染**: 控制器使用`VelocityUtil`将数据放入上下文并渲染`list.vm`模板
7. **页面展示**: 渲染后的HTML页面返回给用户浏览器显示

这个流程展示了典型的MVC架构在Velocity模板系统中的应用，数据从数据库通过DAO、Service、Controller层层传递，最终通过Velocity模板渲染展示给用户。