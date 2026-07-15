---
name: next-steps
description: 后续待办事项 — 测试扩充、架构改进
metadata:
  type: project
---

# 后续待办事项

## 高优先级

### 1. 扩充自动化测试覆盖率（当前仅 ~15%）
- `docs/测试指南.md` 记载 ~370 个测试用例，实际自动化仅 ~55 个
- **后端**: 优先补齐预约模块（RES-001~045）、空间模块（SPACE-001~051）、签到模块（CHK-001~019）的集成测试
- 使用 `@SpringBootTest` + H2 profile + `@Transactional` 模式，参考现有 `CoreBusinessIntegrationTest`
- **前端**: 引入 Cypress 或 Playwright 做 E2E 测试（测试指南已有建议但未实现）

### 2. 修复 TOCTOU 竞态条件
- `BookingService.createBooking()` 的 SELECT-then-INSERT 冲突检测在高并发下有窗口
- 建议方案：添加数据库唯一约束（seat_id + start_time + status）或引入 Redis 分布式锁
- 代码已有注释承认此问题

## 中优先级

### 3. H2 测试密码哈希兼容性
- 统一使用 Hutool BCrypt 生成 `schema-h2.sql` 中的测试密码哈希
- 当前种子数据中的哈希由不同 BCrypt 实现生成，可能存在不兼容

### 4. 完善 `docs/测试指南.md`
- 当前版本 v1.1.0，可能需要根据修复后的代码更新
- 确保测试用例与最新 API 行为一致

## 低优先级

### 5. 统一实体映射
- `Booking`/`Reservation` 和 `Seat`/`SeatControl` 的双重映射是团队分工的历史遗留
- 如有机会重构，可合并为单一实体类

### 6. 前端构建工具化
- 当前原生 HTML/JS 无构建流程
- 可考虑引入 Vite 或简单打包工具，但需团队共识
