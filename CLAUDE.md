# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 常用命令

```bash
# 启动后端（开发环境，需要本地 MySQL 运行）
mvn spring-boot:run

# 使用 H2 内存数据库启动（无需 MySQL，适合测试）
mvn spring-boot:run -Dspring-boot.run.profiles=h2

# 运行全部测试
mvn test

# 运行单个测试类
mvn test -Dtest=CoreBusinessIntegrationTest

# 运行测试类中的单个方法
mvn test -Dtest=CoreBusinessIntegrationTest#testLogin

# 使用 H2 profile 运行测试
mvn test -Dspring-boot.run.profiles=h2

# 编译并打包
mvn clean package -DskipTests

# 启动前端（在前端目录下）
cd frontend && npx serve .
# 或
cd frontend && python -m http.server 3000
```

端口 **8081**，所有 API 路径前缀 `/api`。
Knife4j 文档: `http://localhost:8081/api/doc.html`
前端首页: `http://localhost:3000/public/login.html`

## 架构要点

### 认证流程（非 Spring Security）

项目没有使用 Spring Security。认证通过两个 `HandlerInterceptor` 实现，在 `WebMvcConfig` 中按序注册：

1. **JwtInterceptor**（order=1）：校验 JWT Token。白名单包括 `/auth/login`、`/auth/register`、`/auth/logout` 和 Swagger 文档路径。空间查询类接口（`/campuses`、`/buildings`、`/floors`、`/rooms`、`/seats`）和仪表盘（`/dashboard/`）的 **GET 请求**免认证，但 POST/PUT/DELETE/PATCH 写操作仍需 Token。OPTIONS 请求（CORS 预检）直接放行。Token 支持三种请求头名：`Authorization`、`bearerAuth`、`bearerauth`（兼容 Knife4j 的双 Bearer 前缀问题）。
2. **RoleInterceptor**（order=2）：校验 `@RequireRole` 注解。从 `JwtInterceptor` 设置的 request attribute 中读取角色，与注解声明的角色数组比对。

用户上下文通过 `UserContext`（ThreadLocal）在请求生命周期内传递，服务层通过 `UserContext.getUserId()` / `getUsername()` / `getRole()` 获取当前用户。

### 三层角色

`STUDENT` → `ADMIN` → `SUPER_ADMIN`，权限逐级包含。Student 只能操作自己的预约；Admin 可管理空间和查看报表，但不能管理其他管理员；SuperAdmin 拥有全部权限包括用户管理。

### 模块结构

每个业务模块独立分包 `modules/{name}/`，内部统一采用 Controller → Service → Mapper 分层：

| 模块 | 包 | 关键 Service |
|------|-----|-------------|
| 用户与认证 | `modules/user/` | `UserService` — BCrypt 密码验证、JWT 签发、角色权限校验 |
| 空间管理 | `modules/space/` | `SeatService` — 座位网格批量生成（A-01, A-02...）、`StudyRoomService` — Excel 导入自动创建层级 |
| 预约核心 | `modules/reservation/` | `BookingService` — 完整规则链校验（黑名单→时间窗口→时长→冲突→日限额） |
| 座位管控 | `modules/seat/` | `CheckinService` — 签到/签退/暂离/返回；`SeatScheduledService` — 5个定时任务处理爽约和黑名单 |
| 仪表盘 | `modules/dashboard/` | `DashboardService` — 校园/楼栋/教室三级使用率聚合 |

**注意**：`reservation` 表在 `modules/reservation/` 中叫 `Booking`，在 `modules/seat/` 中叫 `Reservation`；`seat` 表在 `modules/space/` 中叫 `Seat`，在 `modules/seat/` 中叫 `SeatControl`。这是团队分工导致的有意重复映射，修改时需同时关注两边。

### 定时任务

`SeatScheduledService` 包含 5 个定时任务：
- 每 60s：将过期未签到的 RESERVED 预约标记为 NO_SHOW
- 每 30s：处理暂离超时
- 每 2min：自动完成已过期的预约
- 每 5min：自动检查爽约次数并加入黑名单
- 每 5min：自动释放过期的黑名单条目

### 数据层约定

- **逻辑删除**：`sys_user`、`campus`、`building`、`floor`、`study_room`、`seat`、`reservation`、`blacklist`、`no_show_record` 均使用 `deleted` 字段（`@TableLogic`，1=已删除）
- **乐观锁**：`reservation` 表使用 `version` 字段（`@Version`），由 MyBatis-Plus `OptimisticLockerInnerInterceptor` 自动处理
- **自动填充**：所有实体的 `createdAt`/`updatedAt` 由 `MybatisPlusConfig` 中的 `MetaObjectHandler` 自动填充
- **自定义查询**：仅 `StudyRoomMapper` 有自定义 XML（`StudyRoomMapper.xml`），支持多条件动态筛选 + JOIN 到 campus

### 前端架构

前端是**原生 HTML/JS/CSS 多页应用**（MPA），无框架，无构建工具。依赖通过 CDN 加载（Tailwind CSS v4 Browser、Lucide Icons）。所有页面共享 `frontend/public/` 下的基础设施：
- `js/config.js` — API 地址、Mock 开关、BasePath 自动检测
- `js/request.js` — fetch 封装，自动注入 JWT Bearer Token，401 自动重定向登录
- `js/utils.js` — Toast、日期格式化、状态映射
- `api/*.js` — 7 个 API 服务模块
- `mock/*.js` — 7 个 Mock 数据模块（`?mock=true` 启用，默认开启）

**重要：前端无构建流程**，修改 JS/CSS/HTML 后直接刷新浏览器即可生效。

## 配置与环境

- **dev**（默认）：连接本地 MySQL `root/root@localhost:3306/study_room_booking`
- **h2**：内存数据库，schema 自动从 `schema-h2.sql` 初始化，适合测试
- **prod**：数据库凭据和 JWT 密钥从环境变量读取（**注意**：`application-prod.yml` 中 JDBC URL 有 bug，`jdbc://` 应为 `jdbc:mysql://`）

JWT 过期时间 24h（86400000ms），密钥硬编码在 dev 配置中。
预约规则（`booking.rules.*`）：日限额 3 次、最长 8 小时、提前最多 24 小时预订、最少提前 -50 分钟（可临期预约）、提前签到宽限 50 分钟、签到截止 10 分钟、暂离时限 30 分钟、爽约阈值 3 次/7 天、黑名单时长 7 天。

## 开发规范

- 遵循阿里巴巴 Java 开发手册
- Conventional Commits：`feat:` / `fix:` / `docs:` / `style:` / `refactor:` / `test:` / `chore:`
- 分支命名：`feature/xxx` / `bugfix/xxx`
- 使用 Lombok（`@Data`、`@TableName` 等），避免手写 getter/setter
