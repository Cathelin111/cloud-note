package com.cloudnote.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 让后端顺带托管前端构建产物（classpath:/static/）。
 *
 * <p>这样把 frontend-web 的 dist 拷进 static 后再打包，就得到一个“单 jar 就能跑”的可执行程序：
 * <pre>java -jar cloudnote-backend-0.1.0-SNAPSHOT.jar</pre>
 * 浏览器访问 http://localhost:8081 即是完整应用（页面 + 接口），部署演示都很省事。
 *
 * <p>注意：application.yml 里的 {@code spring.web.resources.add-mappings=false} 关掉的是
 * Spring Boot 自动配置的静态资源映射；这里只手动放行前端真正需要的几个路径，
 * 因此 {@code /api/**} 的行为（含未匹配接口的 JSON 404）完全不受影响。
 *
 * <p>开发时仍然可以前后端分离：前端跑 5173（Vite），后端跑 8081，互不干扰。
 */
@Configuration
public class WebSpaConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 注意: 带通配符的映射, 通配部分才是相对 location 的路径,
        // 所以 /assets/** 必须指向 static/assets/ 这一层
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
        registry.addResourceHandler("/index.html")
                .addResourceLocations("classpath:/static/");
    }
}
