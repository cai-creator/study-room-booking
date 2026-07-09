---
name: frontend-architecture
description: 前端架构 — 原生 MPA 无框架
metadata:
  type: project
---

# 前端架构

## 技术选型
- **无框架**: 原生 HTML/JS/CSS，无 Vue/React，无 Node 构建步骤
- **CSS**: Tailwind CSS v4.3.1 (CDN 浏览器版本 `@tailwindcss/browser`)
- **图标**: Lucide Icons v1.8 (CDN)
- **架构**: 多页应用 (MPA)，通过 `<a href>` 和 `window.location.href` 导航
- **运行方式**: 静态 HTTP 服务器 (`npx serve .` 或 `python -m http.server`)

## 目录结构
```
frontend/
├── colors_and_type.css          # 品牌设计系统（主色 #0E7C6B）
├── public/                      # 共享基础设施
│   ├── login.html               # 登录/注册
│   ├── js/config.js             # 全局配置（API URL, Mock开关, 存储键）
│   ├── js/request.js            # HTTP封装（JWT注入, 401拦截）
│   ├── js/utils.js              # 工具函数（日期, Toast, 状态映射）
│   ├── api/                     # 7个API服务模块
│   └── mock/                    # 7个Mock数据模块
├── student/                     # 学生端（5页面 + 导航栏）
│   ├── pages/home.html          # 首页
│   ├── pages/room-selection.html # 自习室浏览
│   ├── pages/seat-selection.html # 座位选择
│   ├── pages/my-reservations.html # 我的预约
│   ├── pages/dashboard.html     # 实时看板
│   └── partials/navbar.html
└── admin/                       # 管理端（5页面 + 导航栏）
    ├── pages/space-management.html # 空间管理
    ├── pages/user-management.html  # 用户管理
    ├── pages/blacklist.html        # 黑名单
    ├── pages/reports.html          # 数据报表（纯SVG图表）
    ├── pages/dashboard.html        # 大屏看板
    └── partials/navbar.html
```

## 关键设计
- **Mock模式**: 默认启用，URL参数 `?mock=false` 切换到真实API
- **JWT认证**: localStorage 存储，Authorization Bearer 头注入
- **401处理**: 自动清除token并重定向到登录页
- **API Base URL**: `http://localhost:8081/api`
- **测试账号** (Mock): `2024001001/password123`(学生), `admin/admin123`(管理员), `superadmin/super123`(超级管理员)
- **30秒轮询**: 仪表盘页面自动刷新

## 前端无自动化测试
- 无 Cypress/Playwright/Selenium 配置
- 测试指南提到建议但未实现
