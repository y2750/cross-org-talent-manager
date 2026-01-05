# 跨组织人才管理系统

## 📖 项目简介

跨组织人才管理系统是一个面向中小微企业的智能人才评估平台，旨在解决企业招聘中的背景调查难、成本高、不深入等问题。系统通过建立员工职业数字档案，为新雇主提供客观、全面的员工评估依据，实现跨组织的人才信息共享与评估。

### 核心价值

- **建立职业数字档案**：为员工建立完整的职业履历和评价档案
- **智能 AI 评估**：基于历史数据和 AI 技术，提供多维度人才对比分析
- **跨组织数据隔离**：确保不同组织间的数据安全隔离和隐私保护
- **降低背调成本**：通过数字化方式降低企业背景调查成本

## 🛠️ 技术栈

### 后端技术

- **框架**：Spring Boot 3.5.7
- **语言**：Java 17
- **ORM**：MyBatis-Flex 1.11.0
- **数据库**：MySQL 8.0
- **缓存与限流**：
  - Redis（缓存与 Session 存储）
  - Redisson 3.50.0（分布式限流）
- **AI 服务**：
  - LangChain4j 1.1.0
  - OpenAI API（异步任务处理）
- **对象存储**：腾讯云 COS
- **API 文档**：Knife4j 4.4.0
- **工具库**：Hutool 5.8.38
- **其他**：
  - Apache POI 5.2.5（Excel 处理）
  - Jakarta Mail 2.0.1（邮件发送）

### 前端技术

- **框架**：Vue 3.5.22 + TypeScript 5.9.0
- **UI 组件库**：Ant Design Vue 4.2.6
- **状态管理**：Pinia 3.0.3
- **路由**：Vue Router 4.6.3
- **HTTP 客户端**：Axios 1.13.2
- **图表库**：ECharts 5.6.0
- **构建工具**：Vite 7.1.11

## ✨ 核心功能

### 1. 用户与组织管理

- 用户注册、登录、权限管理
- 企业注册与审核
- 部门管理
- 角色权限控制

### 2. 员工档案管理

- 员工基本信息管理
- 职业履历记录
- 奖惩记录管理
- 档案访问权限控制

### 3. 评价系统

- 多维度评价体系
- 评价任务管理
- 评价标签管理
- 历史评价记录查询

### 4. AI 智能分析

- 基于历史数据的 AI 人才对比分析
- 异步任务处理与状态管理
- 智能上下文增强

### 5. 人才市场

- 人才信息浏览
- 人才对比功能
- 人才收藏
- 访问记录追踪

### 6. 权限与访问控制

- 跨组织数据隔离
- 动态权限查询构建
- 档案访问请求与审批
- 联系信息访问控制

### 7. 投诉与通知

- 投诉管理
- 系统通知
- 邮件通知

### 8. 限流系统

- 任务级限流
- 用户级限流
- 分布式限流保护

## 📁 项目结构

```
cross-org-talent-manager/
├── src/                          # 后端源码
│   ├── main/
│   │   ├── java/com/crossorgtalentmanager/
│   │   │   ├── ai/               # AI服务相关
│   │   │   ├── annotation/       # 自定义注解
│   │   │   ├── aop/              # AOP切面
│   │   │   ├── common/           # 通用类
│   │   │   ├── config/           # 配置类
│   │   │   ├── constant/         # 常量定义
│   │   │   ├── controller/       # 控制器
│   │   │   ├── exception/        # 异常处理
│   │   │   ├── manager/          # 管理器（如COS）
│   │   │   ├── mapper/           # MyBatis Mapper
│   │   │   ├── model/            # 数据模型
│   │   │   │   ├── dto/          # 数据传输对象
│   │   │   │   ├── entity/       # 实体类
│   │   │   │   ├── enums/        # 枚举类
│   │   │   │   └── vo/           # 视图对象
│   │   │   ├── ratelimiter/      # 限流相关
│   │   │   ├── schedule/         # 定时任务
│   │   │   ├── service/          # 业务服务
│   │   │   └── utils/            # 工具类
│   │   └── resources/
│   │       ├── application.yml   # 应用配置
│   │       ├── mapper/           # MyBatis XML映射文件
│   │       └── prompt/           # AI提示词
│   └── test/                     # 测试代码
├── vue-project/                  # 前端项目
│   ├── src/
│   │   ├── api/                  # API接口定义
│   │   ├── components/           # 组件
│   │   ├── composables/          # 组合式函数
│   │   ├── constants/            # 常量
│   │   ├── layouts/              # 布局
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # Pinia状态管理
│   │   ├── utils/                # 工具函数
│   │   └── views/                # 页面视图
│   └── package.json
├── database/                      # 数据库脚本
├── pom.xml                        # Maven配置
└── README.md                      # 项目说明文档
```

## 🔧 环境要求

### 后端环境

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 前端环境

- Node.js 20.19.0+ 或 22.12.0+
- npm 或 yarn

### 其他服务

- 腾讯云 COS（对象存储）
- OpenAI API（AI 服务）

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd cross-org-talent-manager
```

### 2. 数据库初始化

1. 创建数据库：

```sql
CREATE DATABASE cross_org_talent_manager CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行数据库脚本：
   - `cross_org_talent_manager.sql` - 完整数据库结构

### 3. 后端配置

1. 修改 `src/main/resources/application.yml` 或 `application-local.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cross_org_talent_manager
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
```

2. 配置腾讯云 COS（在 `CosClientConfig.java` 中配置）：

   - SecretId
   - SecretKey
   - Region
   - Bucket

3. 配置 OpenAI API（在 `application.yml` 中配置）：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: your_openai_api_key
      base-url: https://api.openai.com/v1
      model-name: gpt-4
```

### 4. 启动后端

```bash
# 使用 Maven 启动
mvn spring-boot:run

# 或使用 Maven Wrapper
./mvnw spring-boot:run  # Linux/Mac
mvnw.cmd spring-boot:run  # Windows
```

后端服务默认运行在：`http://localhost:8123/api`

### 5. 前端配置

1. 进入前端目录：

```bash
cd vue-project
```

2. 安装依赖：

```bash
npm install
# 或
yarn install
```

3. 配置 API 地址（在 `src/api/index.ts` 或环境变量中）：

```typescript
const baseURL = "http://localhost:8123/api";
```

### 6. 启动前端

```bash
npm run dev
# 或
yarn dev
```

前端服务默认运行在：`http://localhost:5173`

## 📚 API 文档

启动后端服务后，访问 Knife4j API 文档：

```
http://localhost:8123/api/doc.html
```

## 🔐 核心特性说明

### 1. 异步 AI 任务处理

系统采用专用线程池处理 AI 分析任务，避免阻塞 HTTP 请求：

- **专用线程池**：核心线程数 10，最大线程数 50
- **任务状态缓存**：使用 ConcurrentHashMap 存储任务状态
- **状态轮询**：前端通过轮询接口获取任务执行状态
- **结果持久化**：任务完成后将结果保存到数据库

### 2. 两层分布式限流系统

- **任务级限流**：控制 AI 分析任务的并发数量
- **用户级限流**：限制单个用户的请求频率
- **分布式限流**：基于 Redisson 实现分布式限流，支持多实例部署

### 3. 跨组织数据隔离

- **动态权限查询**：根据用户角色动态构建 SQL 查询条件
- **数据隔离**：确保不同组织间的数据完全隔离
- **访问控制**：通过访问请求机制控制跨组织数据访问

### 4. 历史对比记录智能复用

- **对比记录缓存**：相同对比请求复用历史结果
- **智能上下文增强**：基于历史数据增强 AI 分析上下文

## 🧪 开发指南

### 代码生成

项目集成了 MyBatis-Flex 代码生成器，可以快速生成实体类、Mapper、Service 等：

```java
// 运行 MyBatisCodeGenerator.java
```

### 添加新功能模块

1. 创建实体类（Entity）
2. 创建 Mapper 接口和 XML
3. 创建 Service 接口和实现类
4. 创建 Controller
5. 创建 DTO 和 VO 类
6. 配置路由（前端）

### 数据库迁移

数据库升级脚本统一放在项目根目录，命名规范：

- `database_*.sql` - 数据库结构变更
- `*_migration.sql` - 数据迁移脚本

## 📝 配置说明

### 应用配置

主要配置文件：

- `application.yml` - 主配置文件
- `application-local.yml` - 本地开发环境配置

### 重要配置项

- **服务器端口**：`server.port=8123`
- **上下文路径**：`server.servlet.context-path=/api`
- **Redis TTL**：`spring.data.redis.ttl=3600`（缓存过期时间）

## 🚢 部署说明

### 后端部署

1. 打包项目：

```bash
mvn clean package
```

2. 运行 JAR 包：

```bash
java -jar target/cross-org-talent-manager-0.0.1-SNAPSHOT.jar
```

3. 或使用 Docker（需要 Dockerfile）

### 前端部署

1. 构建生产版本：

```bash
cd vue-project
npm run build
```

2. 将 `dist` 目录部署到 Nginx 或其他 Web 服务器

3. 配置 Nginx 反向代理（示例）：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8123;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件

## 🙏 致谢

感谢以下开源项目和技术社区的支持：

- Spring Boot
- Vue.js
- MyBatis-Flex
- Ant Design Vue
- LangChain4j
- Redisson

---

**注意**：本项目仍在持续开发中，部分功能可能尚未完善。使用前请仔细阅读文档并做好数据备份。







