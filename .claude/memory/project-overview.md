---
name: project-overview
description: 项目整体概览 — 高校自习室智能预约系统
metadata:
  type: project
---

# 高校自习室智能预约系统

## 基本信息
- **路径**: `E:\study\java_project\study-room-booking`
- **Git分支**: `cai` (主分支: `main`)
- **团队**: 6人（后端4人 + 前端2人）
  - 蔡俊晨 (成员A): 用户与权限模块，项目负责人
  - 陈梦涵 (成员B): 空间管理模块
  - 郭学威 (成员C): 预约核心 + 数据报表 + 仪表盘
  - 邓祺然 (成员D): 座位管控 + 报表
  - 余尚欣 (成员E): 学生端前端
  - 黄钰涵 (成员F): 管理端前端

## 技术栈
- **后端**: Spring Boot 3.2.5, Java 21, MyBatis-Plus 3.5.5, Maven
- **认证**: JWT (jjwt 0.12.5)，自定义 JwtInterceptor + RoleInterceptor（无 Spring Security）
- **API文档**: Knife4j 4.4.0 / OpenAPI 3
- **数据库**: MySQL 8.0+ (生产) / H2 (测试)
- **工具库**: Hutool 5.8.27 (BCrypt密码加密), Apache POI 5.2.5 (Excel)
- **前端**: 原生 HTML/JS/CSS + Tailwind CSS v4 CDN + Lucide Icons
- **端口**: 8081, Context Path: `/api`

## 关键文件
- `pom.xml` — Maven构建配置
- `README.md` — 完整项目文档（843行，API规格、数据库设计、对接指南）
- `docs/测试指南.md` — 测试指南 v1.1.0 (1255行, ~370个测试用例)
- `docs/sql/schema.sql` — MySQL完整DDL + 种子数据
- `frontend/` — 前端代码（39个文件）
- `mysql/` — 本地MySQL 9.7.1服务器（被.gitignore忽略）
- `query` — 内容"MySQL"，占位文件
