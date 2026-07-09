---
name: architecture-notes
description: 架构关键发现 — 已识别的问题和注意事项
metadata:
  type: project
---

# 架构关键发现

## 已修复 (2026-07-09)

### ✅ production环境数据库URL bug
`application-prod.yml` 中 `jdbc://` → `jdbc:mysql://`，修复于 `cai` 分支。

### ✅ @EnableScheduling 冗余
已从 `SeatSchedulingConfig` 中移除，统一由 `StudyRoomBookingApplication` 声明。

### ✅ application-prod.yml 缺少 booking.rules
已补充 8 个业务规则配置参数，与 dev 环境保持一致。

### ✅ README 项目结构错误
`modules/report/` 修正为 `modules/dashboard/`（README.md + frontend/总体项目.md）。

## 已知问题（尚未修复）

### 1. 实体重复映射
`Booking` 和 `Reservation` 都映射到 `reservation` 表；`Seat` 和 `SeatControl` 都映射到 `seat` 表。
这是有意的设计选择——为避免 MyBatis 类型别名冲突，不同模块（成员C和成员D）各自持有共享表的只读视图。
**Why:** 团队分工导致，目前不会造成运行时问题。
**How to apply:** 如需合并，可统一使用一个实体类，但需要协调成员C和成员D的代码。

### 2. TOCTOU 竞态条件
`BookingService.createBooking()` 使用 SELECT-then-INSERT 模式做冲突检测。
代码注释已承认："高并发场景可能需要分布式锁或数据库唯一约束"。
**Why:** 并发预约可能导致同一座位被重复预约。
**How to apply:** 可考虑添加数据库唯一约束或使用 Redis 分布式锁。

### 3. H2种子数据密码哈希
`schema-h2.sql` 中的 BCrypt 哈希可能与 Hutool 生成的哈希不兼容。
**Why:** `PasswordGeneratorTest` 使用 Hutool 的 BCrypt 实现生成密码，不同 BCrypt 实现的 salt 格式可能有差异。
**How to apply:** 统一使用 Hutool BCrypt 生成测试密码哈希。

### 4. 测试覆盖率低
- 仅3个测试文件（1个工具测试 + 1个拦截器测试 + 1个集成测试）
- 测试指南记载 ~370 个测试用例，但实际自动化测试仅覆盖 ~26 个场景
- 前端无任何自动化测试
**Why:** 大量手动测试用例未转化为自动化测试。
**How to apply:** 逐步将测试指南中的测试用例转化为 JUnit/MockMvc 测试。

## 架构模式
- **三层角色**: STUDENT → ADMIN → SUPER_ADMIN（层级权限）
- **REST API**: 统一 `Result<T>` 响应格式，42个 ResultCode 枚举值
- **前端 MPA**: 多页应用，非 SPA，通过 `<a href>` 导航
- **Mock优先开发**: 前端所有 API 模块都有并行 Mock 实现，URL 参数 `?mock=true` 切换
- **逻辑删除**: 所有核心表使用 `deleted` 字段 + `@TableLogic`
- **乐观锁**: `reservation` 表使用 `version` 字段
