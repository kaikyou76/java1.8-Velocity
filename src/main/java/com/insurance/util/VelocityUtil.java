package com.insurance.util;

// 导入Velocity模板类，用于处理模板文件
import org.apache.velocity.Template;
// 导入Velocity上下文类，用于存储模板变量
import org.apache.velocity.VelocityContext;
// 导入Velocity常量类，包含各种配置常量
import org.apache.velocity.app.Velocity;
// 导入Velocity引擎类，用于模板处理的核心引擎
import org.apache.velocity.app.VelocityEngine;
// 导入Servlet上下文接口，用于获取Web应用上下文信息
import javax.servlet.ServletContext;
// 导入HTTP请求接口，用于处理HTTP请求
import javax.servlet.http.HttpServletRequest;
// 导入HTTP响应接口，用于处理HTTP响应
import javax.servlet.http.HttpServletResponse;
// 导入Java IO异常类，用于处理IO操作异常
import java.io.IOException;
// 导入Java字符串写入器类，用于将模板渲染结果写入字符串
import java.io.StringWriter;
// 导入Java属性类，用于设置Velocity引擎配置
import java.util.Properties;

/**
 * Velocity模板工具类
 * 提供Velocity模板引擎的初始化和模板渲染功能
 */
public class VelocityUtil {
    
    // Velocity引擎实例，用于处理模板渲染
    private static VelocityEngine velocityEngine;
    
    /**
     * 初始化Velocity引擎
     * 根据Servlet上下文配置并初始化Velocity引擎
     * @param servletContext Servlet上下文对象
     */
    public static void initVelocityEngine(ServletContext servletContext) {
        // 检查Velocity引擎是否已初始化，避免重复初始化
        if (velocityEngine == null) {
            // 创建新的Velocity引擎实例
            velocityEngine = new VelocityEngine();
            
            // 创建属性对象，用于设置Velocity引擎配置
            Properties props = new Properties();
            
            // 设置Velocity配置
            // 设置输入编码为UTF-8，确保正确处理中文字符
            props.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
            // 设置输出编码为UTF-8，确保正确输出中文字符
            props.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
            // 设置资源加载器为文件加载器
            props.setProperty(Velocity.RESOURCE_LOADER, "file");
            // 设置文件资源加载器的实现类
            props.setProperty("file.resource.loader.class", 
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            // 设置模板文件的路径，从Servlet上下文获取真实路径
            props.setProperty("file.resource.loader.path", 
                servletContext.getRealPath("/WEB-INF/templates"));
            // 启用模板缓存，提高性能
            props.setProperty("file.resource.loader.cache", "true");
            // 设置模板文件修改检查间隔为2秒
            props.setProperty("file.resource.loader.modificationCheckInterval", "2");
            
            // 宏配置
            // 设置宏库文件路径
            props.setProperty("velocimacro.library", "/templates/macros.vm");
            // 允许在模板中内联定义宏
            props.setProperty("velocimacro.permissions.allow.inline", "true");
            // 允许内联宏替换全局宏
            props.setProperty("velocimacro.permissions.allow.inline.to.replace.global", "true");
            // 设置宏的局部作用域
            props.setProperty("velocimacro.context.localscope", "true");
            
            // 日志配置
            // 设置Velocity运行日志文件路径
            props.setProperty("runtime.log", servletContext.getRealPath("/WEB-INF/logs/velocity.log"));
            // 设置日志系统实现类
            props.setProperty("runtime.log.logsystem.class", 
                "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            
            try {
                // 使用配置属性初始化Velocity引擎
                velocityEngine.init(props);
            } catch (Exception e) {
                // 如果初始化失败，抛出运行时异常
                throw new RuntimeException("Failed to initialize Velocity engine", e);
            }
        }
    }
    
    /**
     * 渲染模板
     * 根据模板名称和上下文渲染模板内容
     * @param templateName 模板文件名
     * @param context Velocity上下文对象
     * @return String 渲染后的模板内容
     */
    public static String renderTemplate(String templateName, VelocityContext context) {
        try {
            // 从Velocity引擎获取指定名称的模板
            Template template = velocityEngine.getTemplate(templateName);
            // 创建字符串写入器，用于接收渲染结果
            StringWriter writer = new StringWriter();
            // 将上下文数据合并到模板中，并将结果写入写入器
            template.merge(context, writer);
            // 返回渲染后的字符串内容
            return writer.toString();
        } catch (Exception e) {
            // 如果渲染失败，抛出运行时异常
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }
    
    /**
     * 创建Velocity上下文并设置常用变量
     * 创建Velocity上下文对象并设置Web应用中常用的变量
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @return VelocityContext Velocity上下文对象
     */
    public static VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {
        // 创建新的Velocity上下文对象
        VelocityContext context = new VelocityContext();
        
        // 设置常用变量
        // 设置应用上下文路径
        context.put("contextPath", request.getContextPath());
        // 将请求对象放入上下文
        context.put("request", request);
        // 将响应对象放入上下文
        context.put("response", response);
        // 将会话对象放入上下文
        context.put("session", request.getSession());
        
        // 设置工具类
        // 设置日期工具类，用于模板中的日期处理
        context.put("date", new org.apache.velocity.tools.generic.DateTool());
        // 设置数学工具类，用于模板中的数学计算
        context.put("math", new org.apache.velocity.tools.generic.MathTool());
        // 设置数字工具类，用于模板中的数字格式化
        context.put("number", new org.apache.velocity.tools.generic.NumberTool());
        
        // 返回配置好的上下文对象
        return context;
    }
    
    /**
     * 渲染模板并输出到响应
     * 渲染指定模板并将结果写入HTTP响应
     * @param templateName 模板文件名
     * @param context Velocity上下文对象
     * @param response HTTP响应对象
     * @throws IOException IO操作异常
     */
    public static void renderTemplateToResponse(String templateName, VelocityContext context, 
                                               HttpServletResponse response) throws IOException {
        // 设置响应内容类型为HTML，字符编码为UTF-8
        response.setContentType("text/html;charset=UTF-8");
        // 设置响应字符编码为UTF-8
        response.setCharacterEncoding("UTF-8");
        
        // 渲染模板获取内容
        String content = renderTemplate(templateName, context);
        // 将渲染后的内容写入响应输出流
        response.getWriter().write(content);
    }
    
    /**
     * 获取Velocity引擎实例
     * 返回已初始化的Velocity引擎实例
     * @return VelocityEngine Velocity引擎实例
     */
    public static VelocityEngine getVelocityEngine() {
        // 返回Velocity引擎实例
        return velocityEngine;
    }
}