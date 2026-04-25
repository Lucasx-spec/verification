# Verification 数字签名与在线验签平台

Verification 是一个基于 Spring Boot 与 Vue 3 的数字签名、验签链接和审计日志管理平台。系统提供用户注册登录、密钥对管理、签名记录创建、文件在线验签、验签链接管理、审计日志查询以及日志完整性校验等功能。

## 功能特性

- 用户注册、登录与当前用户信息获取
- JWT 鉴权与基于 Spring Security 的接口保护
- 用户 SM2 密钥对维护
- SM3 文件摘要与签名记录管理
- 验签链接创建、查看、禁用与访问次数控制
- 公开验签页面，支持上传文件进行在线验签
- 用户侧签名记录、验签记录、审计日志查询
- 管理侧签名记录、验签记录、审计日志与系统参数管理
- 审计日志完整性校验
- 前后端分离架构，前端使用 Vue 3、Vite、Element Plus

## 技术栈

### 后端

- Java 17
- Spring Boot 4.0.5
- Spring Web
- Spring Security
- Spring Data JPA
- MySQL
- Bouncy Castle
- JJWT
- Lombok
- Maven

### 前端

- Vue 3
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- sm-crypto

## 项目结构

```text
verification/
├── src/                         # 后端源码
│   ├── main/
│   │   ├── java/com/example/verification/
│   │   │   ├── common/          # 通用响应、异常码、业务异常
│   │   │   ├── configuration/   # 安全与密码配置
│   │   │   ├── controller/      # REST 接口控制器
│   │   │   ├── crypto/          # SM2/SM3 加密验签服务
│   │   │   ├── exception/       # 全局异常处理
│   │   │   ├── model/           # DTO、Entity、Enum、VO
│   │   │   ├── repository/      # JPA Repository
│   │   │   ├── security/        # JWT 与认证相关类
│   │   │   ├── service/         # 业务接口与实现
│   │   │   └── VerificationApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                    # 后端测试
├── verification-ui/             # 前端项目
│   ├── public/
│   ├── src/
│   │   ├── api/                 # API 请求封装
│   │   ├── layouts/             # 页面布局
│   │   ├── router/              # 路由配置
│   │   ├── stores/              # Pinia 状态管理
│   │   ├── utils/               # 加密、密钥、会话工具
│   │   └── views/               # 页面视图
│   ├── package.json
│   └── vite.config.js
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## 环境要求

- JDK 17+
- Maven 3.9+，也可以直接使用项目内置的 Maven Wrapper
- Node.js 20+，建议使用 LTS 版本
- npm
- MySQL 8.0+

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd verification
```

### 2. 初始化数据库

登录 MySQL 后创建数据库：

```sql
CREATE DATABASE verification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

项目当前使用 JPA 自动更新表结构：

```properties
spring.jpa.hibernate.ddl-auto=update
```

首次启动后会根据实体类自动创建或更新数据表。

### 3. 配置后端

后端配置文件位于：

```text
src/main/resources/application.properties
```

默认配置示例：

```properties
server.port=8080
server.servlet.context-path=/api

spring.datasource.url=jdbc:mysql://localhost:3306/verification?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root

verification.jwt.secret=change-this-jwt-secret-change-this-jwt-secret
verification.jwt.expire-hours=24
verification.security.password-pepper=change-this-pepper
```

本地运行前请根据实际环境修改 MySQL 用户名、密码，以及 JWT Secret、密码 Pepper 等敏感配置。

### 4. 启动后端

Windows：

```bash
mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
./mvnw spring-boot:run
```

后端服务默认地址：

```text
http://localhost:8080/api
```

### 5. 启动前端

```bash
cd verification-ui
npm install
npm run dev
```

前端开发服务默认地址：

```text
http://localhost:5173
```

前端 Vite 已配置 `/api` 代理到 `http://localhost:8080`，开发环境下可直接访问后端接口。

## 常用命令

### 后端

```bash
# 启动后端
mvnw.cmd spring-boot:run

# 运行测试
mvnw.cmd test

# 打包
mvnw.cmd clean package
```

macOS / Linux 请将 `mvnw.cmd` 替换为 `./mvnw`。

### 前端

```bash
cd verification-ui

# 安装依赖
npm install

# 启动开发服务
npm run dev

# 构建生产包
npm run build

# 本地预览生产构建
npm run preview
```

## 核心接口

后端统一上下文路径为 `/api`。

### 认证与用户

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/auth/me` | 获取当前用户信息 |
| PUT | `/api/auth/keypair` | 更新当前用户密钥对 |

### 签名记录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/sign/records` | 创建签名记录 |
| GET | `/api/sign/records` | 查询当前用户签名记录 |
| GET | `/api/sign/records/{signNo}` | 查询签名记录详情 |
| GET | `/api/sign/admin/records` | 管理员查询全部签名记录 |

### 验签

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/verify/links` | 创建验签链接 |
| GET | `/api/verify/links` | 查询当前用户验签链接 |
| GET | `/api/verify/links/{verifyToken}` | 查询验签链接详情 |
| PUT | `/api/verify/links/{verifyToken}/disable` | 禁用验签链接 |
| POST | `/api/verify/files` | 上传文件并执行验签 |
| GET | `/api/verify/records` | 查询当前用户验签记录 |
| GET | `/api/verify/admin/records` | 管理员查询全部验签记录 |

### 审计与系统设置

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/audit/logs` | 查询当前用户审计日志 |
| GET | `/api/audit/admin/logs` | 管理员查询全部审计日志 |
| GET | `/api/audit/integrity` | 审计日志完整性校验 |
| GET | `/api/system/settings` | 获取系统参数 |
| PUT | `/api/system/settings` | 更新系统参数 |

## 页面模块

前端主要页面包括：

- 登录与注册
- 用户仪表盘
- 签名记录管理
- 验签链接管理
- 验签记录管理
- 公开验签页面
- 个人资料与密钥管理
- 审计日志页面
- 管理员仪表盘
- 管理员系统设置
- 管理员审计完整性校验

## 安全配置建议

生产环境部署前建议完成以下配置：

- 修改 `verification.jwt.secret`，使用足够长且随机的密钥
- 修改 `verification.security.password-pepper`
- 使用独立数据库账号，避免使用 MySQL `root` 账号
- 将敏感配置迁移到环境变量、配置中心或部署平台密钥管理中
- 关闭或调整 `spring.jpa.show-sql`
- 根据生产环境需要调整文件上传大小限制
- 配置 HTTPS 与反向代理

## 生产构建

### 后端打包

```bash
mvnw.cmd clean package
```

打包产物通常位于：

```text
target/verification-0.0.1-SNAPSHOT.jar
```

运行方式：

```bash
java -jar target/verification-0.0.1-SNAPSHOT.jar
```

### 前端构建

```bash
cd verification-ui
npm run build
```

构建产物位于：

```text
verification-ui/dist
```

可将该目录部署到 Nginx、静态资源服务器，或由后端/网关统一转发。

## 许可证

如需开源或对外发布，请根据项目实际情况补充许可证信息。
