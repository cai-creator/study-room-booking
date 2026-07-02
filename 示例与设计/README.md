# 示例与设计

本目录存放项目分析文档与前端页面设计参考，供团队成员开发时参考使用。

## 目录说明

```
示例与设计/
├── study-room-prd.html          # 项目需求分析文档（PRD）
├── _shared/                     # PRD文档依赖的共享资源
│   ├── fonts/                   # 字体文件
│   └── js/                      # echarts、mermaid 等JS库
├── assets/                      # PRD文档图表脚本
│   └── charts.js
├── pages/                       # 前端页面设计原型
│   ├── home.html                # 首页
│   ├── room-selection.html      # 自习室筛选页
│   ├── seat-selection.html      # 座位选座页
│   ├── my-reservations.html     # 我的预约页
│   ├── dashboard.html           # 实时看板页
│   ├── reports.html             # 数据报表页
│   ├── space-management.html    # 空间管理后台
│   ├── user-management.html     # 用户管理页
│   └── blacklist.html           # 黑名单管理页
├── partials/                    # 页面公共组件
│   └── project-shell.html       # 页面外壳（导航栏等）
└── colors_and_type.css          # 品牌色彩与字体规范
```

## 使用说明

### 项目需求分析文档（PRD）

打开 `study-room-prd.html` 即可查看完整的项目需求分析文档，包含：
- 项目概述与背景痛点
- 用户画像与核心场景
- 功能需求（自习室管理、学生预约、座位管控、实时看板、数据报表）
- 非功能需求（性能、可用性、安全、可维护性）
- 数据指标与追踪

### 前端页面设计原型

`pages/` 目录下的 HTML 文件为前端页面设计原型，可直接在浏览器中打开预览。前端开发成员（E/F）可参考以下页面设计：

- **学生端页面**（成员E参考）：
  - `home.html` - 首页
  - `room-selection.html` - 自习室筛选页
  - `seat-selection.html` - 座位选座页
  - `my-reservations.html` - 我的预约页
  - `dashboard.html` - 实时看板页

- **管理端页面**（成员F参考）：
  - `space-management.html` - 空间管理后台
  - `user-management.html` - 用户管理页
  - `blacklist.html` - 黑名单管理页
  - `reports.html` - 数据报表页

### 品牌设计规范

`colors_and_type.css` 定义了系统的品牌色彩体系和字体规范，前端开发时请遵循该设计规范。
