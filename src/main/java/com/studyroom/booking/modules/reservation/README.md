# 预约核心模块 — README

> 成员C：郭学威

---

## 一、模块概述

本模块实现预约流程的核心逻辑，包括预约创建（含规则校验）、取消、查询、可用时段计算，以及数据报表（使用率、时段分布、热门时段、导出）。

映射 `reservation` 表，**类名使用 `Booking` 而非 `Reservation`**，避免与座位管控模块（成员D）的 `Reservation` 实体产生 MyBatis-Plus 别名冲突。

---

## 二、文件结构

```
modules/reservation/
├── entity/
│   └── Booking.java                 # 预约实体，@TableName("reservation")
├── mapper/
│   └── BookingMapper.java           # MyBatis-Plus BaseMapper
├── dto/
│   ├── CreateBookingRequest.java    # 创建预约请求 DTO
│   ├── BookingVO.java               # 预约响应 VO（对齐前端 Mock 格式）
│   └── AvailableSlotVO.java         # 可用时段响应 VO
├── service/
│   ├── BookingService.java          # 预约核心服务（创建/取消/查询/可用时段）
│   └── ReportService.java           # 报表服务（使用率/时段分布/热门时段/导出）
└── controller/
    ├── BookingController.java       # 6 个预约接口
    └── ReportController.java        # 4 个报表接口
```

---

## 三、接口清单（10个）

### 预约核心（6个）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/reservations` | 创建预约 | STUDENT |
| POST | `/api/reservations/{id}/cancel` | 取消预约 | STUDENT/ADMIN |
| GET | `/api/reservations/my` | 我的预约（分页） | STUDENT |
| GET | `/api/reservations/{id}` | 预约详情 | STUDENT/ADMIN |
| GET | `/api/reservations` | 全部预约（管理端） | ADMIN/SUPER_ADMIN |
| GET | `/api/seats/{seatId}/available-slots?date=` | 座位可用时段 | 公开 |

### 数据报表（4个）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/reports/usage-rate` | 日均使用率统计 | ADMIN/SUPER_ADMIN |
| GET | `/api/reports/time-distribution` | 24小时时段分布 | ADMIN/SUPER_ADMIN |
| GET | `/api/reports/hot-periods` | 热门时段 TOP5 | ADMIN/SUPER_ADMIN |
| GET | `/api/reports/export` | 导出 Excel 报表 | ADMIN/SUPER_ADMIN |

---

## 四、预约创建校验链

创建预约时按以下顺序校验，任一步失败即返回错误：

1. 黑名单检查（调用 `BlacklistService.isUserBlacklisted`）
2. 座位存在且 `status = 1`
3. 自习室存在且 `status = 1`
4. `endTime > startTime`
5. 预约窗口：不超过 `now + 24h`，不早于 `now + 15min`
6. 时长不超过 8 小时
7. 在自习室开放时间内
8. 当日预约不超过 3 次
9. 时段不与已有预约冲突（高并发下有 TOCTOU 窗口，已加注释说明）

对应错误码：`ResultCode` 中 3001-3008 + 5001（黑名单）。

---

## 五、关键设计决策

| 决策 | 原因 |
|------|------|
| 实体类名 `Booking` | 避免与 seat 模块的 `Reservation` 产生 MyBatis 别名冲突 |
| 前端 `CHECKED_OUT` ↔ 数据库 `COMPLETED` | 在 `BookingVO` 转换时映射，对齐前端 Mock 数据 |
| `checkinCode` 动态生成不存库 | 格式 `QR + yyyyMMdd + 4位ID`，无对应数据库列 |
| 可用时段 1 小时粒度 | 从 30 分钟步进优化为 1 小时步进，减少输出冗余 |
| `convertToVOPage` 批量查询 | 5 次批量查询替代逐条 N+1 查询，大幅提升性能 |
| 分页 `pageSize` 上限 100 | 防止恶意大分页请求 |
| `@Version` 乐观锁 | 已在 `MybatisPlusConfig` 注册乐观锁插件 |

---

## 六、对已有代码的修改

| 文件 | 改动 | 原因 |
|------|------|------|
| `common/config/MybatisPlusConfig.java` | +`OptimisticLockerInnerInterceptor` | 启用乐观锁 |
| `common/interceptor/JwtInterceptor.java` | +`UserContext.setRequest()` + `afterCompletion` | 修复 ThreadLocal 未初始化和内存泄漏 |
| `modules/space/dto/RoomRequest.java` | +`import jakarta.validation.constraints.NotNull` | 编译错误修复 |
| `modules/seat/entity/Seat.java` → `SeatControl.java` | 重命名 | 避免与 space 模块的 `Seat` 实体 MyBatis 别名冲突 |
| `modules/seat/mapper/SeatMapper.java` → `SeatControlMapper.java` | 重命名 | 避免与 space 模块的 `SeatMapper` Bean 名称冲突 |
| `modules/seat/service/CheckinService.java` | 更新 import | 适配上述重命名 |
| `modules/seat/service/NoShowRecordService.java` | 更新 import | 适配上述重命名 |
| `pom.xml` | POI 依赖移到 `<dependencies>` 内 | 修复 Maven 解析错误 |
| `frontend/public/js/config.js` | `useMock` 逻辑 + `casEnabled` | 修复 Mock 永远开启和 CAS 误开启 |
| `resources/schema-h2.sql` | +测试座位数据 | H2 开发环境可用 |
| `resources/application.yml` | profile: dev | 标准开发模式 |
