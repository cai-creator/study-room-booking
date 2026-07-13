# 前端工程 - 智约自习

高校自习室智能预约系统 - 前端工程，包含学生端、管理端和公共资源三大部分。

## 目录结构

```
frontend/
├── colors_and_type.css         # 品牌色彩与字体规范（CSS 变量设计令牌）
├── README.md                   # 本文档
├── public/                     # 公共资源（登录页、JS工具、API封装、Mock数据）
│   ├── login.html              # 登录页面（学生端 / 管理端双 Tab）
│   ├── js/                     # 公共 JavaScript 基础设施
│   │   ├── config.js           # 环境配置（API地址、存储键名）
│   │   ├── request.js          # HTTP 请求封装（JWT注入 + 401拦截 + 错误码处理）
│   │   ├── utils.js            # 通用工具（日期格式化、状态映射、Toast、角色校验等）
│   │   ├── navbar.js           # 导航栏通用加载逻辑
│   │   ├── student-navbar.js   # 学生端导航栏 UI 初始化脚本
│   │   └── admin-navbar.js     # 管理端导航栏 UI 初始化脚本
│   ├── api/                    # 后端 API 对接（按业务模块拆分）
│   │   ├── auth.js             # 认证模块（注册/登录/登出/获取用户）
│   │   ├── space.js            # 空间管理（校区/楼栋/楼层/自习室/座位 CRUD）
│   │   ├── reservation.js      # 预约核心（创建/取消/查询预约）
│   │   ├── seat.js             # 座位管控（签到/签退/暂离/黑名单/爽约记录）
│   │   ├── user.js             # 用户管理（用户CRUD、角色、状态）
│   │   ├── report.js           # 数据报表（使用率/时段分布/爽约率/转化率等）
│   │   └── dashboard.js        # 实时看板（校区/楼栋/自习室概览、座位详情）
│   └── mock/                   # Mock 模拟数据（已禁用，保留供参考）
│       ├── auth.js
│       ├── space.js
│       ├── reservation.js
│       ├── seat.js
│       ├── user.js
│       ├── report.js
│       └── dashboard.js
├── student/                    # 学生端（4个业务页面）
│   ├── pages/
│   │   ├── home.html           # 首页 - 欢迎、快捷入口、校区概览、今日预约、推荐自习室
│   │   ├── seat-selection.html # 座位预约 - 自习室筛选、座位网格、状态图例、时段选择、预约提交
│   │   ├── my-reservations.html# 我的预约 - 状态筛选、操作（取消/签到/签退/暂离/返回）
│   │   └── dashboard.html      # 实时看板 - 校区概览、楼栋热力、座位实况
│   └── partials/
│       └── navbar.html         # 学生端导航栏模板
└── admin/                      # 管理端（5个业务页面）
    ├── pages/
    │   ├── space-management.html   # 空间管理 - 四级树（校区/楼栋/楼层/自习室/座位）
    │   ├── user-management.html    # 用户管理 - CRUD、角色、状态、密码重置
    │   ├── blacklist.html          # 黑名单管理 + 爽约记录（双 Tab）
    │   ├── reports.html            # 数据报表 - SVG手绘图表、KPI卡片
    │   └── dashboard.html          # 大屏看板 - 实时概览、楼栋排行、热力下钻
    └── partials/
        └── navbar.html             # 管理端导航栏模板
```

## 技术栈

| 技术 | 说明 | 引入方式 |
|------|------|---------|
| **Tailwind CSS v4** | 原子类 CSS 框架 | CDN (`@tailwindcss/browser`) |
| **Lucide Icons v1.8** | 图标库 | CDN (`unpkg.com/lucide`) |
| **原生 JavaScript** | 无框架，IIFE + 全局命名空间 | 内联 `<script>` |
| **CSS 变量设计系统** | 品牌色彩/字体/圆角/阴影令牌 | `colors_and_type.css` |

## 快速开始

### 1. 启动本地静态服务器

由于使用了根相对路径和 `fetch()` 动态加载导航栏，**不能直接用 `file://` 协议双击打开**，需启动本地静态服务器：

```bash
# 方式一：npx serve（推荐）
cd frontend
npx serve .

# 方式二：Python 内置服务器
cd frontend
python -m http.server 3000

# 方式三：VS Code Live Server 插件
# 右键任意 HTML → Open with Live Server
```

### 2. 访问入口

| 页面 | URL | 说明 |
|------|-----|------|
| 登录页 | `http://localhost:3000/public/login.html` | 学生端 / 管理端双 Tab，自动按角色跳转 |

### 3. 后端 API 配置

后端接口基地址在 [public/js/config.js](file:///d:/CODEGIT/study-room-booking/frontend/public/js/config.js) 中配置：

```javascript
apiBaseUrl: 'http://localhost:8081/api'
```

> **重要**：Spring Boot 已配置 `server.servlet.context-path=/api`，所有 Controller `@RequestMapping` **不要**重复加 `/api` 前缀。

### 4. 后端 API 文档（Knife4j）

启动后端后访问：`http://localhost:8081/api/doc.html`

---

## 学生端页面说明

学生端共 4 个页面，路径均在 `student/pages/` 下：

| 页面 | 文件 | 核心功能 | 对接模块 |
|------|------|---------|---------|
| 首页 | `home.html` | 欢迎横幅、快捷入口、校区概览卡片、今日预约预览、推荐自习室列表 | 空间管理 + 看板 |
| 座位预约 | `seat-selection.html` | 自习室筛选（校区/楼栋/楼层/类型/关键词）、卡片列表、座位网格可视化（5种状态颜色）、时段选择、确认预约 | 空间管理 + 预约核心 |
| 我的预约 | `my-reservations.html` | 状态筛选标签、预约卡片列表、取消/签到/签退/暂离/返回操作、分页 | 预约核心 + 座位管控 |
| 实时看板 | `dashboard.html` | 校区概览卡片、楼栋使用率进度条、自习室座位实况、自动刷新 | 空间管理 + 预约核心 |

**座位状态颜色图例**（全端统一）：
- 🟢 空闲（绿）· 🟡 已预约（黄）· 🔴 使用中（红）· ⚪ 暂离（灰）· ⬜ 不可用（银灰）

---

## 管理端页面说明

管理端共 5 个页面，路径均在 `admin/pages/` 下：

| 页面 | 文件 | 核心功能 | 对接模块 |
|------|------|---------|---------|
| 空间管理 | `space-management.html` | 四级树形层级（校区→楼栋→楼层→自习室→座位）、CRUD、座位网格可视化 | 空间管理 |
| 用户管理 | `user-management.html` | 用户列表筛选（关键词/角色/状态）、新增/编辑/删除、重置密码、启用/停用 | 用户与权限 |
| 黑名单管理 | `blacklist.html` | Tab1：黑名单记录（新增/解除/搜索）；Tab2：爽约记录浏览 | 座位管控 |
| 数据报表 | `reports.html` | 时间范围下拉（7/30/90天）、4 个 KPI 卡片、5 张纯 SVG 手绘图表 | 报表模块 |
| 大屏看板 | `dashboard.html` | 全局 KPI、校区卡片下钻、楼栋排行奖牌榜、自习室热力图、座位详情面板 | 看板模块 |

### 空间管理层级关系

```
校区 Campus
  └── 楼栋 Building
        └── 楼层 Floor
              └── 自习室 StudyRoom
                    └── 座位 Seat（按 行×列 自动生成网格）
```

### 数据报表 SVG 图表

全部使用纯 SVG 手绘（无 ECharts / Chart.js 第三方库）：

| 图表 | 类型 | 说明 |
|------|------|------|
| 日均使用率趋势 | 折线 + 渐变面积 | 最近 7 天，0–100% |
| 热门时段 TOP 5 | 奖牌排行 + 进度条 | 按使用率排序 |
| 时段使用分布 | 柱状图 | 6:00–22:00 每小时 |
| 爽约率走势 | 折线 + 渐变面积（红色系）| 最近 6 个月 |
| 预约签到转化率 | 折线 + 渐变面积（绿色系）| 最近 6 个月 |

---

## 公共资源与工程约定

### 1. 页面头部资源顺序（所有页面必须遵守）

```html
<head>
    <!-- 1. 品牌设计系统（禁止内联复制 :root 变量） -->
    <link rel="stylesheet" href="../../colors_and_type.css">
    <!-- 2. Tailwind CDN -->
    <script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4.3.1/dist/index.global.js"></script>
    <!-- 3. Lucide Icons -->
    <script src="https://unpkg.com/lucide@1.8.0/dist/umd/lucide.min.js"></script>
    <!-- 4. Tailwind 桥接配置（映射 CSS 变量） -->
    <style type="text/tailwindcss">
      @theme inline { --color-primary: var(--brand-primary); }
      @layer base { body { background: var(--color-bg); color: var(--color-text-primary); } }
    </style>
</head>
```

### 2. 页面脚本引入顺序

```html
<body>
    <div id="navbar-placeholder"></div>
    <main>...</main>

    <!-- 基础脚本（顺序不可变） -->
    <script src="../../public/js/config.js"></script>
    <script src="../../public/js/utils.js"></script>
    <script src="../../public/js/request.js"></script>
    <!-- 业务 API 模块（按需引入） -->
    <script src="../../public/api/space.js"></script>
    <script src="../../public/api/dashboard.js"></script>
    <!-- 页面内联脚本 -->
    <script> /* IIFE 业务逻辑 */ </script>
</body>
```

### 3. 导航栏加载机制

导航栏 HTML 模板存放在各端的 `partials/navbar.html`，通过 `fetch()` 动态插入：

```javascript
// 学生端示例
Utils.loadNavbar('student')
  .then(() => initStudentNavbarUI())
  .catch(() => execNavbarScript());
```

**关键注意点**：
- 动态插入的 HTML 中内联 `<script>` 不会被浏览器执行，因此导航栏交互逻辑（下拉、高亮、退出登录）抽离到 `student-navbar.js` / `admin-navbar.js`，插入后**必须显式调用初始化函数**。
- 若 `fetch()` 因 `file://` 协议失败，会降级使用页面内嵌的 `NAVBAR_HTML` 字符串，并执行 `execNavbarScript()` 保证功能完整。
- 插入完成后必须调用 `lucide.createIcons()` 重新渲染图标。

### 4. 角色校验与安全

| 校验场景 | 函数 | 调用位置 |
|---------|------|---------|
| 防止跨角色访问页面 | `Utils.requireRole([...])` | 每个业务页面 IIFE **第一行** |
| 已登录用户自动跳转首页 | `Utils.redirectIfLoggedIn()` | 登录页面加载时 |
| 登录 Tab 角色锁定 | `AuthAPI.login({ expectedRole })` | 学生Tab传`STUDENT`，管理Tab传`ADMIN`/`SUPER_ADMIN` |

角色校验失败时：清除 token → 错误信息写入 `sessionStorage` → 重定向登录页 → 登录页读取并显示红条提示。

### 5. CSS 变量速查

| 用途 | 变量 | 默认值 |
|------|------|--------|
| 主色调 | `--brand-primary` | `#0E7C6B`（墨绿）|
| 页面背景 | `--color-bg` | `#FFFFFF` |
| 次要背景 | `--color-bg-secondary` | `#F8FAFB` |
| 主要文字 | `--color-text-primary` | `#1E293B` |
| 次要文字 | `--color-text-secondary` | `#475569` |
| 边框 | `--color-border` | `#E2E8F0` |
| 成功态 | `--state-success` | `#16A34A` |
| 警告态 | `--state-warning` | `#D97706` |
| 错误态 | `--state-error` | `#DC2626` |
| 信息态 | `--state-info` | `#2563EB` |
| 圆角卡片 | `--radius-lg` / `rounded-xl` | — |
| 卡片阴影 | `--shadow-sm` | — |
| 模态框阴影 | `--shadow-lg` | — |

### 6. API 请求与响应约定

- **Base URL**：`http://localhost:8081/api`（`config.js` 可改）
- **认证方式**：请求头 `Authorization: Bearer {token}`（`request.js` 自动注入）
- **统一响应结构**：`{ code: 200, message: "成功", data: {...}, timestamp }`
- **成功码**：`code === 200`；`request.js` 自动解包返回 `data` 字段
- **401 未授权**：自动清除 token → 跳转登录页
- **分页响应字段**：前端使用兼容写法 `records || list`、`current || pageNum`、`size || pageSize` 以兼容不同返回格式

### 7. 参数传递约定

| 场景 | 传参方式 | 示例 |
|------|---------|------|
| GET 查询 | Query String | `SpaceAPI.getRooms({ floorId: 1, pageNum: 1 })` |
| POST/PUT 新建/更新 | JSON Body | `SpaceAPI.createRoom({ ... })` |
| PATCH 状态切换 | Query String | `UserAPI.updateStatus(userId, status)` |
| 登录/注册 | Query String（application.yml 已配置接收）| `AuthAPI.login({ username, password, expectedRole })` |
| 签到/签退/暂离/返回 | Query String（`roomId` + `seatCode`，**不用 reservationId**）| `SeatAPI.checkIn(roomId, seatCode)` |
| 报表日期范围 | Query String（`startDate` + `endDate`，格式 `yyyy-MM-dd`）| `ReportAPI.getUsageRate({ startDate, endDate })` |

---

## 后端 API 一览（前端已封装）

共 7 个 API 模块，文件在 `public/api/` 下：

### auth.js 认证模块
| 方法 | HTTP | 路径 | 说明 |
|------|------|------|------|
| `register` | POST | `/auth/register` | 用户注册（username/realName/password/email/phone）|
| `login` | POST | `/auth/login` | 登录（含 expectedRole 角色校验）|
| `logout` | POST | `/auth/logout` | 登出 |
| `getCurrentUser` | GET | `/auth/me` | 获取当前登录用户 |

### space.js 空间管理
`getCampuses` / `getBuildings` / `getFloors` / `getRooms` / `getRoomDetail` / `getSeats` /
`createCampus` / `updateCampus` / `deleteCampus` /
`createBuilding` / `updateBuilding` / `deleteBuilding` /
`createFloor` / `updateFloor` / `deleteFloor` /
`createRoom` / `updateRoom` / `deleteRoom` /
`createSeats` / `updateSeat` / `deleteSeat` / `getRoomSeatsStatus`

### reservation.js 预约核心
`createReservation` / `cancelReservation` / `getMyReservations` / `getReservationDetail` / `getAvailableTimes`

### seat.js 座位管控
`checkIn` / `checkOut` / `temporaryLeave` / `returnFromLeave` /
`getBlacklist` / `addToBlacklist` / `removeFromBlacklist` /
`getNoShowRecords`

### user.js 用户管理
`getUsers` / `createUser` / `updateUser` / `deleteUser` /
`updateStatus` / `resetPassword`

### report.js 数据报表
`getUsageRate` / `getHotPeriods` / `getTimeDistribution` /
`getNoShowRate` / `getConversionRate` / `exportReport`

### dashboard.js 实时看板
`getCampusOverview` / `getBuildingOverview` / `getRoomDetail` / `getRoomList`

---

## 注册功能说明

登录页内置注册 Tab，与后端 `/auth/register` 接口对齐：

| 字段 | 前端校验 | 必填 |
|------|---------|------|
| 学号/工号 username | 非空 | ✅ |
| 真实姓名 realName | 非空 | ✅ |
| 密码 password | 最小 6 位 | ✅ |
| 确认密码 confirmPassword | 与 password 一致 | ✅ |
| 邮箱 email | 邮箱格式（如填写） | ❌ |
| 手机号 phone | 手机号格式（如填写） | ❌ |

注册成功后：自动切换回登录 Tab → 自动填充 username → 弹出成功 Toast。

---

## 前端公共工具（Utils）

`public/js/utils.js` 提供：

| 函数 | 说明 |
|------|------|
| `showToast(msg, type)` | Toast 提示（success/error/warning/info）|
| `confirm(msg)` | Promise 风格确认弹窗 |
| `requireRole(roles)` | 页面角色校验（失败跳转登录）|
| `redirectIfLoggedIn()` | 登录页已登录自动跳转 |
| `formatDate` / `formatDateTime` / `formatTime` | 日期时间格式化（yyyy-MM-dd 等）|
| `parseDate` / `addDays` / `diffDays` | 日期运算 |
| `thousands(num)` | 千分位数字格式化 |
| `escapeHtml(str)` | XSS 转义 |
| `getStatusLabel` / `getStatusColor` | 枚举状态映射（预约状态、座位状态等）|
| `getStorage(key)` / `setStorage(k,v)` / `removeStorage(k)` | 带前缀的 localStorage 封装 |
| `loadNavbar(role)` | fetch 加载对应端导航栏模板 |

---

## 开发规范 Checklist

新增页面时请逐一核对：

1. [ ] 头部资源顺序正确，`colors_and_type.css` 通过 `<link>` 引入，不复制 `:root`
2. [ ] CSS 只使用变量名，不硬编码 `#0E7C6B` 等色值
3. [ ] 图标使用 `data-lucide="xxx"`，动态插入 HTML 后调用 `lucide.createIcons()`
4. [ ] 页面顶部第一行执行 `Utils.requireRole(['STUDENT'])` 或对应角色
5. [ ] 所有 `fetch()` / API 调用均有 `.catch()`，配合 `Utils.showToast` 给出提示
6. [ ] 不使用 `.catch(() => [])` 静默吞错（应至少 `console.error`）
7. [ ] 动态插入用户输入内容先经 `Utils.escapeHtml()` 转义
8. [ ] 分页字段用兼容写法 `records || list` / `current || pageNum` / `size || pageSize`
9. [ ] 导航栏插入后显式调用 `initStudentNavbarUI()` / `initAdminNavbarUI()`
10. [ ] 登录请求带上 `expectedRole` 参数
