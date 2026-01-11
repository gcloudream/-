# Silemore (死了么)

一款基于生命周期的紧急联系人通知应用。

## 项目简介

Silemore 是一款创新的移动应用，用户可以设置紧急联系人，当用户在指定时间内未签到时，系统会自动通知紧急联系人。适用于独居老人、探险者、独自工作者等需要定期确认安全的场景。

## 技术栈

- **后端**: Spring Boot 3.x + MySQL + JPA
- **移动端**: Flutter / Android (Kotlin)
- **认证**: JWT Token

## 快速开始

### 1. 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.8+
- Node.js 18+ (可选，用于移动端构建)

### 2. 环境配置

复制环境变量模板并填写你的配置：

```bash
cp .env.example .env
# 编辑 .env 文件，填写真实的配置值
```

### 3. 后端配置

```bash
cd silemore-backend

# 复制配置文件模板
cp src/main/resources/application.yml.example src/main/resources/application.yml

# 编辑 application.yml，填写数据库、邮件等配置
```

### 4. 数据库准备

```sql
CREATE DATABASE silemore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 5. 运行后端

```bash
cd silemore-backend
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 6. 运行移动端 (Android)

```bash
cd silemore-app-legacy-android
./gradlew assembleDebug
```

## 配置说明

### 必需配置项

| 环境变量 | 说明 | 示例 |
|---------|------|------|
| `DB_HOST` | 数据库主机 | `localhost` |
| `DB_PORT` | 数据库端口 | `3306` |
| `DB_NAME` | 数据库名称 | `silemore` |
| `DB_USER` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | - |
| `MAIL_HOST` | SMTP服务器 | `smtp.163.com` |
| `MAIL_PORT` | SMTP端口 | `465` |
| `MAIL_USERNAME` | 邮箱地址 | - |
| `MAIL_PASSWORD` | 邮箱授权码 | - |
| `JWT_SECRET` | JWT密钥 (>=32字符) | - |

## 项目结构

```
sileme/
├── silemore-backend/          # Spring Boot 后端
│   └── src/main/
│       ├── java/com/silemore/ # Java 源码
│       └── resources/         # 配置文件
├── silemore-app-legacy-android/ # Android 客户端
├── docs/                      # 项目文档
│   ├── 需求文档.md
│   ├── 详细设计说明书.md
│   ├── API接口详细设计.md
│   └── 开发计划.md
└── README.md
```

## 文档

- [需求文档](docs/需求文档.md)
- [详细设计说明书](docs/详细设计说明书.md)
- [API接口详细设计](docs/API接口详细设计.md)
- [开发计划](docs/开发计划.md)

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License
