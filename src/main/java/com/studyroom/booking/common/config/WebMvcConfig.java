package com.studyroom.booking.common.config;

import com.studyroom.booking.common.interceptor.JwtInterceptor;
import com.studyroom.booking.common.interceptor.RoleInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final RoleInterceptor roleInterceptor;

    /**
     * 配置跨域
     * <p>注意：allowCredentials(true) 与 allowedOriginPatterns("*") 不兼容（违反 CORS 规范），
     * 浏览器会拒绝带 credentials 的通配符跨域请求。此处使用具体的前端地址。
     * 生产环境请根据实际部署域名修改。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000",
                        "http://localhost:8081",
                        "http://127.0.0.1:8081"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     * <p>注意：公共查询接口（如空间列表）不放行在 excludePathPatterns 中，
     * 而是统一进入 JwtInterceptor，由其按「GET 方法 + 路径前缀」的细粒度规则放行，
     * 这样可避免 POST/PUT/DELETE 等写操作因通配符匹配而绕过权限校验。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/logout",
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/error",
                        "/favicon.ico"
                )
                .order(1);

        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/logout",
                        "/doc.html",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/error",
                        "/favicon.ico"
                )
                .order(2);
    }
}