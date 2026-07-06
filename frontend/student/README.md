# 学生端前端 - 智约自习

高校自习室智能预约系统 - 学生端页面，负责学生用户的所有交互界面。

## 目录结构

```
student/
├── pages/              # 业务页面
│   ├── home.html       # 首页 - 欢迎、快捷入口、校区概览、今日预约、推荐自习室
│   ├── room-selection.html   # 自习室筛选 - 多级筛选、列表展示、分页
│   ├── seat-selection.html   # 座位选座 - 座位网格、状态图例、时段选择、预约
│   ├── my-reservations.html  # 我的预约 - 状态筛选、预约列表、签到/签退/暂离操作
│   └── dashboard.html        # 实时看板 - 校区概览、楼栋使用率、座位实况、自动刷新
│
└── partials/
    └── navbar.html     # 学生端导航栏（通过 fetch 动态加载到各页面）
```

## 页面功能说明

| 页面 | 文件 | 核心功能 | 对接后端 |
|---|---|---|---|
| 首页 | `home.html` | 欢迎横幅、4个快捷入口、校区概览、今日预约预览、推荐自习室 | 空间管理 + 看板 |
| 自习室筛选 | `room-selection.html` | 校区/楼栋/楼层/类型筛选、关键词搜索、仅看有空座、卡片列表、分页 | 空间管理 |
| 座位选座 | `seat-selection.html` | 座位网格可视化（按行列）、5种状态颜色、选中座位信息、可用时段选择、确认预约 | 预约核心 |
| 我的预约 | `my-reservations.html` | 状态筛选标签、预约卡片列表、取消/签到/签退/暂离/返回操作、分页 | 预约核心 + 座位管控 |
| 实时看板 | `dashboard.html` | 校区概览卡片、楼栋使用率进度条、自习室座位实况、30秒自动刷新 | 空间管理 + 预约核心 |

## 技术栈

- **Tailwind CSS v4** - 通过 CDN 引入 (`@tailwindcss/browser`)
- **Lucide Icons** - 图标库 (`unpkg.com/lucide`)
- **原生 JavaScript** - 无框架依赖，IIFE 模式
- **CSS 变量** - 统一使用 `colors_and_type.css` 定义的品牌色彩和字体规范

## 公共资源引用

所有页面统一引用 `../public/` 下的公共基础设施：

```
../../colors_and_type.css          # 品牌色彩与字体规范
../../public/js/config.js          # 环境配置（API地址、Mock开关等）
../../public/js/utils.js           # 工具函数（日期格式化、状态映射、Toast等）
../../public/js/request.js         # HTTP 请求封装（JWT注入、401拦截、超时处理）
../../public/api/*.js              # API 接口定义（7个模块）
../../public/mock/*.js             # Mock 数据（7个模块，与API一一对应）
```

## 导航栏

通过 `fetch('../partials/navbar.html')` 动态加载到各页面的 `#navbar-placeholder` 中：

- 包含 Logo（可点击返回首页）、桌面端导航链接、移动端汉堡菜单
- 自动高亮当前页面
- 显示当前登录用户信息（头像、姓名、角色）
- 支持下拉菜单（退出登录）

**注意**：导航栏链接使用相对路径（如 `home.html`），确保在本地服务器和部署环境下都能正常工作。

## Mock 数据模式

开发阶段默认启用 Mock 数据，无需启动后端即可测试全部页面：

- **测试账号**：
  - 学生：`2024001001` / `password123`
  - 管理员：`admin` / `admin123`
  - 超级管理员：`superadmin` / `super123`

- **切换真实 API**：修改 `public/js/config.js` 第16行，将 `|| true` 改为 `|| false`
  ```javascript
  useMock: params.get('mock') === 'true' || false
  ```

## 启动方式

在 `frontend/` 根目录下启动本地服务器：

```bash
npx serve .
# 或
python -m http.server 3000
```

然后访问：`http://localhost:3000/public/login.html`

> **注意**：必须使用 HTTP 服务器（不能双击 HTML 文件用 file:// 协议打开），否则根相对路径和资源加载会失败。

## 开发规范

1. **样式统一**：所有页面使用 `colors_and_type.css` 中的 CSS 变量，不硬编码颜色值
2. **图标统一**：使用 Lucide Icons，通过 `data-lucide="图标名"` 声明，页面加载后调用 `lucide.createIcons()`
3. **路径规范**：页面内链接使用相对路径（如 `home.html`），公共资源使用 `../../public/` 相对路径
4. **错误处理**：所有 API 调用使用 `.catch()` 捕获错误，配合 `Utils.showToast()` 显示提示
5. **XSS 防护**：动态插入内容时使用 `Utils.escapeHtml()` 转义用户输入

## 与后端对接

| 前端页面 | 对接后端模块 | 核心接口 | 后端负责人 |
|---|---|---|---|
| 首页 | 空间管理 + 看板 | 校区概览、使用率统计 | 成员B |
| 自习室筛选 | 空间管理 | 自习室列表查询、筛选过滤 | 成员B |
| 座位选座 | 预约核心 | 座位实时状态、创建预约、可用时段 | 成员C |
| 我的预约 | 预约核心 + 座位管控 | 预约列表、取消、签到/签退/暂离 | 成员C + 成员D |
| 实时看板 | 空间管理 + 预约核心 | 概览、座位实时状态 | 成员B + 成员C |

后端接口文档（Knife4j）：`http://localhost:8081/api/doc.html`
