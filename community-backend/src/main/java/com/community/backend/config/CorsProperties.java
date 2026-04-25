package com.community.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 跨域配置项映射（app.cors.*）。
 * 说明：
 * 1) 开发环境前后端分离时，通过 allowedOrigins 精确放行来源域名
 * 2) 生产环境建议改成实际域名，避免放行过宽
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * 允许跨域访问的来源列表（必须是完整 Origin）。
     */
    private List<String> allowedOrigins = List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173"
    );

    /**
     * 允许跨域请求的方法。
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

    /**
     * 允许跨域请求携带的请求头。
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * 返回给浏览器可见的响应头。
     */
    private List<String> exposedHeaders = List.of("Authorization");

    /**
     * 是否允许携带凭证（如 Cookie、Authorization Header）。
     */
    private Boolean allowCredentials = true;

    /**
     * 预检请求缓存时间（秒）。
     */
    private Long maxAge = 3600L;
}
