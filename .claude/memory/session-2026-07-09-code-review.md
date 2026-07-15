---
name: session-2026-07-09-code-review
description: 三轮代码审查+修复，30项优化，security+perf+dedup
metadata:
  type: project
---

# 2026-07-09 代码审查与优化 —— 三轮修复全记录

## 背景

对 study-room-booking 项目进行全面代码审查，发现 40 项问题。分三轮完成了 30 项修复，涵盖后端安全/性能/日志/错误码、前端安全/去重。

**分支**: `cai` → `main`，所有修改已合并推送。

## 第一轮：紧急修复 (5项)

Commit: `eb73df7`

| 问题 | 文件 | 修改 |
|------|------|------|
| CORS allowCredentials+wildcard 冲突 | `WebMvcConfig.java` | `*` → 4个具体 localhost 地址 |
| Reservation 缺 @Version | `Reservation.java` | 添加 `@Version` 注解 |
| StudyRoomService.updateStatus 数据丢失 | `StudyRoomService.java` | `updateById` → `LambdaUpdateWrapper.set()` |
| JwtInterceptor println 泄露 token | `JwtInterceptor.java` | 10处 `System.out.println` → `log.debug()` |
| Token 重复解析 3次/请求 | `JwtInterceptor.java` | `parseToken()`一次 → 从 `Claims` 直接提取 |

## 第二轮：后端全面质量优化 (11项)

Commit: `def68af`

| 问题 | 文件 | 修改 |
|------|------|------|
| NoShowRecordService N+1 查询 | `NoShowRecordService.java` | 41次SQL→3次(批量 `selectBatchIds`) |
| 3个Service缺分页上限 | `UserService`/`BlacklistService`/`NoShowRecordService` | `Math.min(pageSize, 100)` |
| Knife4j/Swagger 生产未关闭 | `application-prod.yml` | `knife4j.enable: false` + `springdoc` |
| JwtUtils 吞异常无日志 | `JwtUtils.java` | `catch` 加 `log.warn` |
| DashboardService O(n³) | `DashboardService.java` | 预分组 `Map<floorId, List<Room>>` |
| DashboardService 重复查询 | `DashboardService.java` | 两个方法合并为 `getActiveBookings()` |
| H2 scope→test | `pom.xml` | `runtime` → `test` |
| SeatReportController 逻辑在 Controller | 新 `SeatReportService.java` | 抽离业务逻辑到 Service |
| 4个Service无日志 | `UserService`/`Building`/`Campus`/`Floor` | `@Slf4j` + 关键操作日志 |
| RoleInterceptor 拒绝无审计 | `RoleInterceptor.java` | `log.warn` 记录拒绝详情 |
| 全部魔法错误码 → ResultCode | 8个文件 | 新增8个枚举 + 全量替换 |

**新增 ResultCode**:
- `CAMPUS_NOT_FOUND(6001)`, `BUILDING_NOT_FOUND(6002)`, `FLOOR_NOT_FOUND(6003)`
- `CAMPUS_HAS_BUILDINGS(6101)`, `BUILDING_HAS_FLOORS(6102)`, `FLOOR_HAS_ROOMS(6103)`, `ROOM_HAS_SEATS(6104)`
- `FILE_IMPORT_FAILED(7001)`

## 第三轮：前端安全+去重 (4项)

Commit: `e4717b4`

| 问题 | 文件 | 修改 |
|------|------|------|
| 登录密码在URL参数中 | `auth.js` + `AuthController.java` | Query params → JSON body; `@RequestParam` → `@RequestBody @Valid` |
| 5个admin页面导航栏重复 | `admin/pages/*.html` ×5 | 删除 `NAVBAR_HTML`(~1200字×5) + `execNavbarScript`(~55行×5) |
| admin/student-navbar.js 95%重复 | 新 `navbar.js` | 合并为统一的 `initNavbarUI()`，向后兼容别名 |
| student页面迁移 | `student/pages/*.html` ×5 | `student-navbar.js` → `navbar.js` |

## 仍待修复 (优先级排序)

### 🟡 小改动、快收益
1. 前端模态框缺 loading 态（防重复提交）
2. `report.js` 导出绕过 `request.js` 封装
3. Mock 数据结构与真实 API 不一致
4. `SeatSchedulingConfig` 空类
5. 缺 Spring Actuator 健康检查
6. `application-dev.yml` 明文凭据环境变量化

### 🔵 长期架构
7. Booking/Reservation + Seat/SeatControl 双实体映射统一
8. 空间层级数据 `@Cacheable` 缓存
9. CI/CD (GitHub Actions)
10. API 版本化 (`/api/v1/`)
11. `Utils.confirm` 自定义模态框
12. 页面 auth guard

## 注意事项

- **前端用 HTTP 服务器访问**: `cd frontend && python -m http.server 3000`，然后 `http://localhost:3000/public/login.html`。`file://` 协议不支持新的 CORS 配置。
- **测试时间依赖**: `CoreBusinessIntegrationTest` 中的 booking 测试使用 `LocalDateTime.now().plusHours(X)`，在 20:00 后运行会超出自习室 08:00-22:00 开放时间而失败。这不是 bug，是测试设计问题。
- **API 契约未变**: 所有 Controller 对外接口签名和行为完全兼容，前端无需额外适配（除 `auth.js` 登录方式同步修改）。
- **Maven 路径**: `C:/Users/Administrator/.m2/wrapper/dists/apache-maven-3.8.5-bin/5i5jha092a3i37g0paqnfr15e0/apache-maven-3.8.5/bin/mvn`
