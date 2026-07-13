# 座位管控模块（成员D：邓祺然）

## 模块说明

本模块负责自习室座位的实时管控，包括签到/签退/暂离操作、黑名单管理、爽约记录追踪，以及相关的自动化定时任务。

## 包结构

```
modules/seat/
├── config/
│   └── SeatSchedulingConfig.java      # 定时任务配置
├── controller/
│   ├── CheckinController.java         # 签到/签退/暂离/返回控制器
│   ├── BlacklistController.java       # 黑名单管理控制器
│   ├── NoShowRecordController.java    # 爽约记录控制器
│   └── SeatReportController.java      # 报表接口（爽约率/转化率）
├── dto/
│   ├── CheckinRequest.java            # 签到请求
│   ├── CheckinVO.java                 # 签到/签退/暂离响应
│   ├── SeatActionRequest.java         # 签退/暂离/返回通用请求
│   ├── BlacklistRequest.java          # 黑名单操作请求
│   ├── BlacklistVO.java              # 黑名单响应
│   └── NoShowRecordVO.java           # 爽约记录响应
├── entity/
│   ├── Blacklist.java                # 黑名单实体
│   ├── NoShowRecord.java             # 爽约记录实体
│   ├── Reservation.java              # 预约记录实体（内部使用）
│   └── Seat.java                     # 座位实体（内部使用）
├── mapper/
│   ├── BlacklistMapper.java           # 黑名单 Mapper
│   ├── NoShowRecordMapper.java        # 爽约记录 Mapper
│   ├── ReservationMapper.java         # 预约记录 Mapper
│   └── SeatMapper.java               # 座位 Mapper
└── service/
    ├── CheckinService.java            # 签到/签退/暂离/返回业务逻辑
    ├── BlacklistService.java          # 黑名单管理业务逻辑
    ├── NoShowRecordService.java       # 爽约记录管理业务逻辑
    └── SeatScheduledService.java      # 定时任务业务逻辑
```

## API 接口清单

### 一、签到签退

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 签到 | POST | `/api/checkin` | 学生在预约时段内到达自习室后签到 | 学生 |
| 签退 | POST | `/api/checkout` | 学生离开自习室时签退 | 学生 |
| 暂离 | POST | `/api/temporary-leave` | 学生暂时离开座位 | 学生 |
| 返回座位 | POST | `/api/return-seat` | 学生暂离后返回座位 | 学生 |

### 二、黑名单管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 黑名单列表 | GET | `/api/blacklist` | 分页查询黑名单 | 管理员 |
| 黑名单详情 | GET | `/api/blacklist/{id}` | 获取黑名单详情 | 管理员 |
| 加入黑名单 | POST | `/api/blacklist` | 手动将用户加入黑名单 | 管理员 |
| 移出黑名单 | DELETE | `/api/blacklist/{id}` | 手动解除黑名单 | 管理员 |
| 我的黑名单状态 | GET | `/api/blacklist/my` | 查看自己的黑名单状态 | 学生 |

### 三、爽约记录

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 爽约记录列表 | GET | `/api/no-show-records` | 分页查询爽约记录 | 管理员 |
| 我的爽约记录 | GET | `/api/no-show-records/my` | 查看自己的爽约记录 | 学生 |

### 四、数据报表（D提供）

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 爽约率统计 | GET | `/api/reports/no-show-rate` | 时间段内爽约率统计 | 管理员 |
| 预约转化率 | GET | `/api/reports/conversion-rate` | 预约→签到→完成转化率 | 管理员 |

## 定时任务说明

| 任务 | 频率 | 说明 |
|------|------|------|
| 超时未签到处理 | 每60秒 | 超过签到宽限时间仍未签到的预约标记为爽约 |
| 暂离超时处理 | 每30秒 | 暂离超过保留时间未返回的标记为爽约 |
| 自动完成预约 | 每120秒 | 已过结束时间的活跃预约自动标记为已完成 |
| 黑名单自动检查 | 每300秒 | 统计7天内爽约次数，达标自动加入黑名单 |
| 黑名单自动解除 | 每300秒 | 到期黑名单自动解除 |

## 业务规则

- **签到时间**：
  - 提前预约（创建时间 < 时段开始时间）：签到窗口为开始时间 ~ 开始时间+50分钟
  - 开始后预约（创建时间 ≥ 时段开始时间）：签到窗口为创建时间 ~ 创建时间+50分钟
  - 超过签到窗口未签到则判定为爽约
- **多时段预约**：
  - 支持一次预约多个相邻时段，合并为单条预约记录（开始时间=首时段开始，结束时间=末时段结束）
  - 签到/签退/暂离等机制与单时段预约完全一致，无需特殊处理
- **暂离保留**：暂离后座位保留30分钟
- **黑名单阈值**：7天内累计爽约3次自动加入黑名单
- **黑名单时长**：默认7天

## 依赖说明

本模块使用以下数据库表：
- `reservation` - 预约记录表（核心依赖，由成员C管理）
- `blacklist` - 黑名单表
- `no_show_record` - 爽约记录表
- `seat` - 座位表（由成员B管理）
