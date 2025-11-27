# CORS 和后端连接配置指南

## 当前问题

前端在 `localhost:5173` 尝试访问后端的 `localhost:8123`，出现 CORS 跨域错误：

```
Access to XMLHttpRequest at 'http://localhost:8123/user/login' from origin 'http://localhost:5173'
has been blocked by CORS policy
```

---

## 解决方案

### 方案 1：使用 Vite 代理（开发环境 - 已配置）✅

**优点：** 快速、无需修改后端  
**适用：** 开发环境

#### 工作原理

```
前端请求 → Vite 代理 → 后端
http://localhost:5173/api/user/login
         ↓ (转发)
http://localhost:8123/user/login
```

#### 已自动配置

- ✅ `vite.config.ts` - 添加了代理配置
- ✅ `.env.development` - 开发环境使用 `/api` 代理
- ✅ `src/utils/request.ts` - 自动切换 API 地址

#### 使用方法

```bash
# 重启开发服务器
npm run dev

# 访问应用，登录会自动通过代理转发到后端
```

---

### 方案 2：配置后端 CORS（生产环境 - 推荐）✅

**优点：** 标准化、生产就绪  
**适用：** 生产环境

#### Java Spring Boot 后端配置

在后端项目中添加 CORS 配置：

**方案 A：注解配置（简单）**

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")  // 允许前端地址
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

**方案 B：注解方式（灵活）**

在 Controller 方法上添加：

```java
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:5173")
@PostMapping("/user/login")
public Response login(@RequestBody LoginRequest request) {
    // 实现登录逻辑
}
```

**方案 C：过滤器配置（全局）**

```java
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsFilterConfig {
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        registrationBean.setFilter(new CorsFilter(source));
        registrationBean.setOrder(0);

        return registrationBean;
    }
}
```

#### 生产环境配置

修改 `.env` 文件：

```bash
# 生产环境 - 直接调用后端（后端已配置 CORS）
VITE_API_URL=http://your-backend-domain:8123
```

---

## 环境切换

### 开发环境（使用代理）

```bash
npm run dev
# 使用 .env.development 配置
# API 地址：/api（本地代理，转发到 localhost:8123）
```

### 生产构建

```bash
npm run build
# 使用 .env 配置
# API 地址：http://localhost:8123（或配置的后端地址）
```

---

## 测试步骤

### 1. 确保后端正在运行

```bash
# 后端应该在 8123 端口运行
http://localhost:8123/user/login
```

### 2. 启动开发服务器

```bash
npm run dev
# 访问 http://localhost:5173
```

### 3. 测试登录

```
用户名: admin
密码: admin123
```

### 4. 验证代理工作

打开浏览器控制台 (F12)，查看网络请求：

- 请求 URL 应该是：`http://localhost:5173/api/user/login`
- 但实际转发到：`http://localhost:8123/user/login`

---

## 调试技巧

### 查看 Vite 代理日志

在 `vite.config.ts` 中添加日志：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8123',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
      logLevel: 'debug',  // 显示调试信息
    },
  },
}
```

### 检查响应状态

浏览器开发者工具 → Network 标签 → 查看请求和响应

### 常见问题

**问题 1：代理不工作**

- 解决：确保后端运行在 8123，重启开发服务器

**问题 2：生产构建后 CORS 错误**

- 解决：配置后端 CORS，或使用生产级反向代理 (Nginx)

**问题 3：Cookie 丢失**

- 解决：确保 `allowCredentials: true`

---

## 后端 CORS 配置最佳实践

### 安全配置（生产环境）

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 仅允许特定域名（避免 *)
                .allowedOrigins(
                    "https://yourdomain.com",
                    "https://www.yourdomain.com"
                )
                // 仅允许需要的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 仅允许需要的请求头
                .allowedHeaders("Content-Type", "Authorization")
                // 暴露特定响应头
                .exposedHeaders("Authorization")
                // 允许证书（Cookies）
                .allowCredentials(true)
                // 预检请求缓存时间
                .maxAge(3600);
    }
}
```

### 环境变量配置

```properties
# application-dev.properties
cors.allowed-origins=http://localhost:5173
cors.allowed-methods=*
cors.allow-credentials=true
cors.max-age=3600

# application-prod.properties
cors.allowed-origins=https://yourdomain.com
cors.allowed-methods=GET,POST,PUT,DELETE
cors.allow-credentials=true
cors.max-age=3600
```

---

## 流程图

### 开发环境（使用代理）

```
┌─────────────────────┐
│  前端应用           │
│ localhost:5173      │
└──────────┬──────────┘
           │
        请求 /api/user/login
           │
           ▼
┌─────────────────────┐
│  Vite 代理服务器    │
│ localhost:5173      │
└──────────┬──────────┘
           │
        转发请求（移除 /api 前缀）
           │
           ▼
┌─────────────────────┐
│  后端服务           │
│ localhost:8123      │
│ /user/login         │
└─────────────────────┘
```

### 生产环境（直接连接）

```
┌─────────────────────┐
│  前端应用           │
│ yourdomain.com      │
└──────────┬──────────┘
           │
        请求 /user/login
           │
           ▼
┌─────────────────────┐
│  后端服务           │
│ backend.yourdomain  │
│ （已配置 CORS）     │
└─────────────────────┘
```

---

## 当前配置总结

| 环境 | API 地址              | 方式      | 文件             |
| ---- | --------------------- | --------- | ---------------- |
| 开发 | /api                  | Vite 代理 | .env.development |
| 生产 | http://localhost:8123 | 直接连接  | .env             |

### 配置文件位置

- `vite.config.ts` - 代理配置
- `.env` - 生产环境
- `.env.development` - 开发环境
- `src/utils/request.ts` - API 客户端

---

**需要帮助？**

- 检查后端是否运行在 8123
- 查看浏览器 Network 标签
- 查看 Vite 控制台日志
