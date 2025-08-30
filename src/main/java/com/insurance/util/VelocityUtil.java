package com.insurance.util;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Velocity模板工具类
 */
public class VelocityUtil {
    
    private static VelocityEngine velocityEngine;
    
    /**
     * 初始化Velocity引擎
     */
    public static void initVelocityEngine(ServletContext servletContext) {
        if (velocityEngine == null) {
            velocityEngine = new VelocityEngine();
            
            Properties props = new Properties();
            
            // 设置Velocity配置
            props.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
            props.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
            props.setProperty(Velocity.RESOURCE_LOADER, "file");
            props.setProperty("file.resource.loader.class", 
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            props.setProperty("file.resource.loader.path", 
                servletContext.getRealPath("/WEB-INF/templates"));
            props.setProperty("file.resource.loader.cache", "true");
            props.setProperty("file.resource.loader.modificationCheckInterval", "2");
            
            // 宏配置
            props.setProperty("velocimacro.library", "/templates/macros.vm");
            props.setProperty("velocimacro.permissions.allow.inline", "true");
            props.setProperty("velocimacro.permissions.allow.inline.to.replace.global", "true");
            props.setProperty("velocimacro.context.localscope", "true");
            
            // 日志配置
            props.setProperty("runtime.log", servletContext.getRealPath("/WEB-INF/logs/velocity.log"));
            props.setProperty("runtime.log.logsystem.class", 
                "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
            
            try {
                velocityEngine.init(props);
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize Velocity engine", e);
            }
        }
    }
    
    /**
     * 渲染模板
     */
    public static String renderTemplate(String templateName, VelocityContext context) {
        try {
            Template template = velocityEngine.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }
    
    /**
     * 创建Velocity上下文并设置常用变量
     */
    public static VelocityContext createContext(HttpServletRequest request, HttpServletResponse response) {
        VelocityContext context = new VelocityContext();
        
        // 设置常用变量
        context.put("contextPath", request.getContextPath());
        context.put("request", request);
        context.put("response", response);
        context.put("session", request.getSession());
        
        // 设置工具类
        context.put("date", new org.apache.velocity.tools.generic.DateTool());
        context.put("math", new org.apache.velocity.tools.generic.MathTool());
        context.put("number", new org.apache.velocity.tools.generic.NumberTool());
        
        return context;
    }
    
    /**
     * 渲染模板并输出到响应
     */
    public static void renderTemplateToResponse(String templateName, VelocityContext context, 
                                               HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String content = renderTemplate(templateName, context);
        response.getWriter().write(content);
    }
    
    /**
     * 获取Velocity引擎实例
     */
    public static VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }
}