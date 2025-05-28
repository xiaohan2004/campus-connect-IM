package com.campus.im.config;

import com.campus.im.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有路径的跨域请求
//                .allowedOrigins("*") // 允许所有来源
                .allowedOriginPatterns("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 允许的请求方法
                .allowedHeaders("*") // 允许的请求头
                .allowCredentials(true) // 是否允许带认证信息
                .maxAge(3600); // 预检请求的缓存时间
    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的存储目录映射为静态资源路径
        String uploadPath = System.getProperty("user.dir") + File.separator + "upload" + File.separator;
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath);
    }

    /**
     * 登录拦截器
     */
    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/login", "/api/register", "/api/forgetPassword", "/api/sendVerificationCode", "/upload/**", "/api/websocket/**");
    }
} 