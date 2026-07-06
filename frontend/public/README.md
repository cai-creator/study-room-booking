# 前端公共资源（public/）

高校自习室智能预约系统 - 前端公共基础设施

## 目录结构

```
public/
├── login.html            # 登录页面（CAS + 账号密码）
├── README.md             # 本文档
├── js/                   # 公共 JavaScript 工具
│   ├── config.js         # 环境配置（API地址、Mock开关、存储键名）
│   ├── request.js        # HTTP 请求封装（fetch + token注入 + 401拦截）
│   └── utils.js          # 通用工具（日期格式化、状态映射、Toast等）
├── api/                  # 后端 API 对接（按业务模块拆分）
│   ├── auth.js           # 认证模块（登录/登出/获取用户）
│   ├── space.js          # 空间管理（校区/楼栋/楼层/自习室/座位CRUD）
│   ├── reservation.js    # 预约核心（创建/取消/查询预约）
│   ├── seat.js           # 座位管控（签到/签退/暂离/黑名单/爽约）
│   ├── user.js           # 用户管理（用户CRUD）
│   ├── report.js         # 数据报表（使用率/时段分布/爽约率等）
│   └── dashboard.js      # 实时看板（校区/楼栋/自习室概览）
└── mock/                 # Mock 模拟数据（与 api/ 一一对应）
    ├── auth.js
    ├── space.js
    ├── reservation.js
    ├── seat.js
    ├── user.js
    ├── report.js
    └── dashboard.js
```

## 快速开始

### 启动开发服务器

由于使用了根相对路径（如 `/public/js/config.js`），需要本地静态服务器：

```bash
# 在 frontend 目录下
npx serve .

# 或使用 VS Code Live Server 插件打开任意页面
```

### Mock 模式

默认关闭 Mock。可通过以下方式开启：

1. **URL 参数**：`http://localhost:3000/public/login.html?mock=true`
2. **修改配置**：编辑 `public/js/config.js`，将 `useMock` 设为 `true`

Mock 模式下所有 API 调用返回本地模拟数据，无需后端服务。

### 测试账号（Mock 模式）

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 学生 | `2024001001` | `password123` |
| 管理员 | `admin` | `admin123` |
| 超级管理员 | `superadmin` | `super123` |

## 技术栈

- **Tailwind CSS v4**（CDN 浏览器版本）
- **Lucide Icons v1.8**（CDN）
- **原生 JavaScript**（无框架，全局命名空间模式）
- **CSS 变量**来自 `colors_and_type.css`（品牌设计系统）

## 设计规范

### 页面模板

所有页面遵循统一模板：

```html
<!DOCTYPE html>
<html lang="zh-CN" class="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面标题 - 智约自习</title>
    <!-- 1. 引用根目录品牌设计系统（不再内联） -->
    <link rel="stylesheet" href="../colors_and_type.css">
    <!-- 2. Tailwind CDN -->
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4.3.1/dist/index.global.js"></script>
    <!-- 3. Lucide 图标 -->
    <script src="https://unpkg.com/lucide@1.8.0/dist/umd/lucide.min.js"></script>
    <!-- 4. Tailwind 桥接配置 -->
    <style type="text/tailwindcss">
      @theme inline { --color-primary: var(--brand-primary); }
      @layer base { body { background: var(--color-bg); color: var(--color-text-primary); } }
    </style>
</head>
<body>
    <!-- 导航栏 -->
    <div id="navbar-placeholder"></div>
    <!-- 页面内容 -->
    <main>...</main>
    <!-- 脚本 -->
    <script src="/public/js/config.js"></script>
    <script src="/public/js/utils.js"></script>
    <script src="/public/js/request.js"></script>
    <!-- 按需引入 API 模块 -->
</body>
</html>
```

> **重要**：品牌设计系统变量统一存放在项目根目录 `colors_and_type.css`，所有 HTML 页面必须通过 `<link rel="stylesheet">` 引用，**禁止内联复制 CSS 变量**。
> - `public/login.html` 引用路径：`../colors_and_type.css`
> - `student/pages/*.html` 引用路径：`../../colors_and_type.css`
> - `admin/pages/*.html` 引用路径：`../../colors_and_type.css`

### 登录页（login.html）特殊说明

1. **CSS 引用**：登录页不再内联复制 `:root` 变量块，改为直接 `<link rel="stylesheet" href="../colors_and_type.css">` 引用根目录品牌设计系统。
2. **已登录不再自动跳转**：页面加载时若检测到 localStorage 中存在 token + user，**不再立即跳转**，而是在登录卡片顶部显示蓝色提示条，用户可手动选择：
   - **继续进入** → 按当前登录角色跳转到对应首页
   - **切换账号** → 清除 token/user，清空输入框，停留在登录页
3. 仅 CAS 回调（URL 含 `ticket` 参数）时仍保持自动登录处理流程。

### 导航栏使用

各页面通过 fetch 加载导航栏：

```js
fetch('/student/partials/navbar.html')
  .then(r => r.text())
  .then(html => {
    document.getElementById('navbar-placeholder').innerHTML = html;
    lucide.createIcons();
  });
```

### CSS 变量速查

| 用途 | 变量 |
|------|------|
| 主色 | `var(--brand-primary)` #0E7C6B |
| 背景 | `var(--color-bg)` #FFFFFF |
| 次要背景 | `var(--color-bg-secondary)` #F8FAFB |
| 文字 | `var(--color-text-primary)` #1E293B |
| 次要文字 | `var(--color-text-secondary)` #475569 |
| 边框 | `var(--color-border)` #E2E8F0 |
| 错误 | `var(--state-error)` #DC2626 |
| 成功 | `var(--state-success)` #16A34A |

## API 规范

- **Base URL**: `http://localhost:8081/api`
- **认证**: JWT Bearer Token（请求头 `Authorization: Bearer {token}`）
- **统一响应**: `{ code: 200, message: "操作成功", data: {...}, timestamp: 1719900000000 }`
- **分页响应**: `{ list: [], total: 100, pageNum: 1, pageSize: 20 }`

## 导航栏链接

### 学生端（5个）
首页 → 自习室选择 → 座位预约 → 我的预约 → 实时看板

### 管理端（4个）
空间管理 → 用户管理 → 黑名单管理 → 数据报表

## 添加新页面

1. 复制模板结构（参见上方"页面模板"）
2. 引入所需 API 模块和对应的 Mock 模块
3. 加载对应端的导航栏
4. 使用 Tailwind 类 + CSS 变量编写页面内容
5. 在导航栏 HTML 中添加对应的导航链接
