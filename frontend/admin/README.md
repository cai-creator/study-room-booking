# 管理端（admin/）

高校自习室智能预约系统 - 管理端页面集

## 目录结构

```
admin/
├── pages/                         # 页面文件
│   ├── space-management.html      # 空间管理（校区/楼栋/楼层/自习室/座位）
│   ├── user-management.html       # 用户管理（CRUD/角色/状态/密码重置）
│   ├── blacklist.html             # 黑名单管理 + 爽约记录（双Tab）
│   ├── reports.html               # 数据报表（SVG图表、KPI）
│   └── dashboard.html             # 大屏看板（实时概览、楼栋排行、热力图、座位详情）
├── partials/
│   └── navbar.html                # 管理端导航栏（复用 + Fallback）
└── README.md                      # 本文档
```

## 快速开始

### 启动静态服务器

由于使用了根目录引用与 `fetch()` 加载导航栏，建议使用本地静态服务器：

```bash
# 在 frontend 根目录下
npx serve .
# 或使用 VS Code Live Server 插件
```

### 访问入口

- 登录页：`/public/login.html?mock=true`
- Mock 管理员账号：`admin / admin123`（或 `superadmin / super123`）
- 登录成功后自动跳转至：`/admin/pages/space-management.html`

### 直接访问页面

```
/admin/pages/space-management.html   # 空间管理
/admin/pages/user-management.html    # 用户管理
/admin/pages/blacklist.html          # 黑名单管理
/admin/pages/reports.html            # 数据报表
/admin/pages/dashboard.html          # 大屏看板
```

> 若通过 `file://` 协议直接打开，`fetch()` 会被 CORS 拦截；此时页面会自动使用内嵌的 `NAVBAR_HTML` 字符串降级渲染导航栏，并执行 `execNavbarScript()` 保证功能完整。

## 页面功能

### 1. 空间管理 space-management.html

四级树形层级管理 + 座位网格可视化。

| 层级 | 说明 | 操作 |
|------|------|------|
| 校区 Campus | 顶级空间节点 | 新增/编辑/删除 |
| 楼栋 Building | 隶属于校区 | 新增/编辑/删除，校区级快捷添加 |
| 楼层 Floor | 隶属于楼栋 | 新增/编辑/删除，楼栋级快捷添加 |
| 自习室 Room | 隶属于楼层 | 新增/编辑/删除，楼层级快捷添加 |
| 座位 Seat | 隶属于自习室 | 自动按行×列生成，可视化展示 |

**核心 UI**：
- 左侧：空间层级树（带缩进、图标、行内操作按钮、激活态高亮）
- 右侧：详情面板（KPI 卡片 / 自习室列表表格 / 座位网格图）
- 模态框：统一 CRUD 表单，校区→楼栋→楼层 级联联动
- 座位网格：按行（字母标识）× 列（数字编号）展示，5 种状态用颜色区分
  - `空闲` 绿 · `已预约` 黄 · `使用中` 红 · `暂离` 灰 · `不可用` 银灰
  - 顶部带讲台/投影屏视觉标识

### 2. 用户管理 user-management.html

完整的用户生命周期管理。

**功能清单**：
- 🔍 筛选：关键字（学号/姓名）、角色（学生/管理员/超级管理员）、状态（启用/停用）
- 📊 顶部 KPI 卡片：用户总数、当前页活跃、学生数、管理员数
- 📋 数据表格：用户头像、账号、角色徽章、联系方式、状态点、创建时间
- 🔀 状态切换：一键启用/停用
- 🔑 重置密码：独立模态框（双输入校验一致性，最小 6 位）
- ➕ 新增用户：姓名、账号、初始密码、角色、手机、邮箱
- ✏️ 编辑用户：账号字段只读，其余可改
- 🗑️ 删除用户：二次确认防误操作
- 📄 分页：数字页码 + 上下翻页，支持省略号压缩

### 3. 黑名单管理 blacklist.html

双 Tab 独立数据视图。

**Tab 1：黑名单记录**
- 搜索：学号/姓名 + 状态（生效中/已解除）
- 4 个 KPI：当前页总数、生效中、已解除、7 天内到期
- 列信息：用户信息、加入原因、封禁时段、操作员、状态点
- 操作：
  - 生效中 → `解除` 按钮（二次确认，调用 `SeatAPI.removeFromBlacklist`）
  - 已解除 → `重新加入` 按钮（自动填充账号到新增表单）
- 新增黑名单模态框：学号账号、原因下拉、开始/结束日期（默认 7 天）

**Tab 2：爽约记录**
- 搜索：学号/姓名/自习室名
- 列：用户、自习室+座位号、预约时段、爽约原因标签、记录日期
- Tab 顶栏徽标实时显示各 Tab 记录数

### 4. 数据报表 reports.html

全部使用**纯 SVG 手绘图表**（无 ECharts / Chart.js 等第三方库）。

**顶部工具栏**：
- 时间范围下拉（7/30/90 天）
- 刷新按钮、导出按钮（调用 `ReportAPI.exportReport`）

**4 个 KPI 卡片**：预约总量（千分位）、平均使用率、爽约率、签到转化率。

**图表模块**：
| 图表 | 类型 | 数据维度 |
|------|------|----------|
| 日均使用率趋势 | 折线 + 渐变面积 | 最近 7 天，0–100% 纵轴 |
| 热门时段 TOP 5 | 奖牌排行 + 进度条 | 按平均使用率排序 |
| 时段使用分布 | 柱状图 | 6:00–22:00 每小时，0–100% 纵轴 |
| 爽约率走势 | 折线 + 渐变面积 | 最近 6 个月，0–5% 纵轴（红色系）|
| 预约签到转化率 | 折线 + 渐变面积 | 最近 6 个月，70–100% 纵轴（绿色系）|

所有 SVG 数据点支持 `<title>` 悬停提示，网格线/坐标/标签纯 SVG 绘制，自适应容器宽度。

### 5. 大屏看板 dashboard.html

**对接后端模块**：空间管理 + 预约核心（`DashboardAPI`：校区概览 / 楼栋概览 / 自习室详情）。

实时展示全校区自习室座位使用状态，支持逐层下钻。

**顶部全局 KPI**：
- 覆盖校区数 / 座位总量 / 当前空闲 / 整体使用率

**模块一：校区使用概览**
- 卡片列表展示各校区：名称、自习室数、总座位
- 使用率进度条 + 颜色编码（<30%绿、<60%蓝、<85%黄、≥85%红）
- 已用/空闲/总座三位对比；**点击卡片 → 下钻到该校区楼栋热力图**

**模块二：楼栋使用排行**
- 🥇🥈🥉 奖牌榜 + 数字排名，按使用率倒序
- 每条含：所属校区徽标、使用率进度条、空闲座/总座、自习室数
- **点击楼栋 → 下钻到该楼栋自习室卡片**

**模块三：自习室实时热力图**
- 面包屑导航：全部校区 → [校区]楼栋视角 → [楼栋]自习室视角
- 三种视图随下钻切换：
  1. 全部校区：按楼栋分组的自习室网格（4色热力背景）
  2. 校区楼栋视角：同上，仅过滤该校区楼栋
  3. 楼栋自习室视角：自习室独立卡片，使用率进度条 + 空闲/使用/总数

**模块四：座位详情面板**
- 点击任意自习室卡片 → 触发 `DashboardAPI.getRoomDetail(roomId)`
- 讲台视觉标识 + A/B/C 行号 + 01/02 列号座位网格
- 5 种状态颜色（与空间管理页保持一致：空闲绿 / 预约黄 / 使用红 / 暂离灰 / 不可用银灰）
- 含标签（WINDOW等）展示，`<title>` 悬停提示

**刷新与交互**：右上角「实时更新中」脉冲指示器 + 手动刷新按钮。

## 设计规范与工程约定

所有页面严格遵循公共规范（见 `/public/README.md`）：

1. **头部资源顺序**：`colors_and_type.css` → Tailwind CDN → Lucide CDN → Tailwind 桥接 `<style type="text/tailwindcss">`
2. **CSS 变量**：统一使用 `/colors_and_type.css` 中的设计令牌（`--brand-primary`、`--color-bg`、`--state-error` 等），**禁止内联复制 `:root`**
3. **动态路径**：脚本/API 路径以 `../../public/` 相对根目录引用；导航栏通过 `BasePath.admin + '/partials/navbar.html'` 动态拼接，兼容目录改名或移动
4. **导航栏 Fallback**：
   - 优先 `fetch(BasePath.admin + '/partials/navbar.html')`
   - 失败时注入页面内嵌的 `NAVBAR_HTML` 字符串
   - 两种方式均执行 `execNavbarScript()` 完成：用户信息填充、当前页高亮、下拉菜单、汉堡菜单、退出登录
5. **localStorage 键名**：优先读 `AppConfig.storageKeys.user / token`，降级兼容硬编码 key
6. **Mock 分支**：API 层（`SpaceAPI` / `UserAPI` / `SeatAPI` / `ReportAPI`）通过 `AppConfig.useMock` 自动切换 Mock 数据，无需后端即可完整体验
7. **UI 组件库**：Tailwind 原子类 + CSS 变量 + Lucide Icons（每次注入 HTML 后需调用 `lucide.createIcons()` 渲染）
8. **交互反馈**：成功/失败/确认通过 `Utils.showToast` 与 `Utils.confirm` 统一输出

## 依赖模块

```
公共脚本（按引入顺序）
├── public/js/config.js        # AppConfig + BasePath（自动推导前端根目录）
├── public/js/utils.js         # Toast / 日期 / 状态映射 / Token 等
├── public/js/request.js       # fetch 封装（Token 注入 + 401 跳转）
├── public/mock/*.js           # Mock 模拟数据（useMock=true 时生效）
│   ├── space.js / user.js / seat.js / report.js
│   ├── dashboard.js           # 大屏看板 Mock（校区/楼栋/座位详情）
│   └── reservation.js / auth.js
└── public/api/*.js            # 业务 API（auth / space / user / seat / report / dashboard / reservation）
```

## 风格一致性

四个页面保持统一视觉语言：
- 圆角：`--radius-sm/md/lg`，卡片统一 `rounded-xl`
- 阴影：卡片使用 `--shadow-sm`，模态框 `--shadow-lg`
- 间距：12 列栅格（`lg:grid-cols-12`），`gap-4/gap-5/gap-6`
- 主色调：`--brand-primary (#0E7C6B)`，强调按钮为实底+白字
- 次级操作：`--color-bg-tertiary` 背景 + 深灰文字
- 状态色：成功绿、警告橙、错误红、信息蓝
- 表格：灰白斑马 hover、`--color-bg-secondary` 表头、细边分隔
- 图标：左侧图标 + 文字垂直居中的间距统一为 `gap-1.5 ~ gap-2`
