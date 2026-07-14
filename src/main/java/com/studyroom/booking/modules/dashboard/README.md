# 实时看板模块 — README

> 成员C：郭学威

---

## 一、模块概述

本模块实现实时看板功能，提供校区、楼栋、自习室三级使用概览和座位实时状态查询。所有接口均为**公开接口**，无需登录即可访问。

数据实时从数据库计算：通过查询 `reservation` 表中当前活跃的预约（状态为 RESERVED/CHECKED_IN/TEMPORARY_LEAVE）来统计座位占用情况。

---

## 二、文件结构

```
modules/dashboard/
├── dto/
│   ├── CampusOverviewVO.java       # 校区使用概览响应 VO
│   ├── BuildingOverviewVO.java     # 楼栋使用概览响应 VO
│   ├── RoomOverviewVO.java         # 自习室使用概览响应 VO（批量）
│   └── RoomDetailVO.java           # 自习室使用详情响应 VO（含座位列表）
├── service/
│   └── DashboardService.java       # 实时看板业务逻辑
└── controller/
    └── DashboardController.java    # 4 个公开接口
```

---

## 三、接口清单（4个）

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 校区使用概览 | GET | `/api/dashboard/campus-overview` | 各校区自习室使用率概览 | 公开 |
| 楼栋使用概览 | GET | `/api/dashboard/building-overview` | 各楼栋自习室使用率，支持 `?campusId=` 过滤 | 公开 |
| 自习室使用概览 | GET | `/api/dashboard/room-overview` | 所有自习室使用率概览（批量） | 公开 |
| 自习室使用详情 | GET | `/api/dashboard/room-detail/{roomId}` | 单个自习室所有座位实时状态 | 公开 |

### 3.1 校区使用概览

**请求：** `GET /api/dashboard/campus-overview`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "campusId": 1,
      "campusName": "主校区",
      "totalRooms": 2,
      "totalSeats": 24,
      "availableSeats": 20,
      "usageRate": 16.67
    }
  ],
  "timestamp": 1719900000000
}
```

### 3.2 楼栋使用概览

**请求：** `GET /api/dashboard/building-overview?campusId=1`

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| campusId | Long | 否 | 校区ID，不传则返回全部楼栋 |

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "buildingId": 1,
      "buildingName": "图书馆",
      "campusId": 1,
      "campusName": "主校区",
      "totalRooms": 2,
      "totalSeats": 24,
      "availableSeats": 20,
      "usageRate": 16.67
    }
  ],
  "timestamp": 1719900000000
}
```

### 3.3 自习室使用详情

**请求：** `GET /api/dashboard/room-detail/1`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "roomId": 1,
    "roomName": "图书馆101自习室",
    "roomType": "LIBRARY",
    "floorName": "一楼",
    "buildingName": "图书馆",
    "campusName": "主校区",
    "openTime": "08:00",
    "closeTime": "22:00",
    "totalSeats": 24,
    "availableSeats": 20,
    "reservedSeats": 3,
    "occupiedSeats": 1,
    "usageRate": 16.67,
    "seats": [
      {
        "seatId": 1,
        "seatCode": "A-01",
        "rowNumber": 1,
        "colNumber": 1,
        "status": "AVAILABLE",
        "tags": ["WINDOW"]
      }
    ]
  },
  "timestamp": 1719900000000
}
```

**座位状态说明：**

| 状态值 | 含义 |
|--------|------|
| `AVAILABLE` | 空闲可用 |
| `RESERVED` | 已被预约 |
| `OCCUPIED` | 已签到使用中 |
| `TEMPORARY_LEAVE` | 使用者暂离 |
| `UNAVAILABLE` | 座位不可用（物理禁用） |

---

## 四、计算逻辑

### 使用率公式

```
usageRate = (totalSeats - availableSeats) / totalSeats × 100%
```

- **totalSeats**：自习室中所有 status=1（可用）的座位数，从 `seat` 表实时统计
- **availableSeats**：totalSeats 中未被当前活跃预约占用的座位数
- **活跃预约**：`reservation` 表中 status IN ('RESERVED', 'CHECKED_IN', 'TEMPORARY_LEAVE') 且 start_time ≤ now ≤ end_time

### 数据聚合层次

```
campus（校区）
  └── building（楼栋）→ 聚合楼栋下所有自习室
        └── floor（楼层）
              └── study_room（自习室）→ 聚合自习室内所有座位
                    └── seat（座位）→ 单个座位的实时预约状态
```

---

## 五、关键设计决策

| 决策 | 原因 |
|------|------|
| 接口全部公开 | 实时看板面向所有用户，不涉及敏感个人信息 |
| real-time 非缓存 | 每次请求实时查询数据库，保证看板数据即时准确 |
| 座位数从 seat 表统计 | 比 `study_room.total_seats` 字段更准确，反映实际数据 |
| 状态区分 RESERVED / OCCUPIED / TEMPORARY_LEAVE | 不同状态在热力图中用不同颜色区分，前端可精细化展示 |
| 排除 status=0 的座位 | 物理不可用的座位不计入统计，避免虚增总容量 |

---

## 六、对已有代码的修改

| 文件 | 改动 | 原因 |
|------|------|------|
| `common/config/WebMvcConfig.java` | +`"/dashboard/**"` 到排除列表 | 看板接口无需登录 |
| `pom.xml` | Lombok 1.18.36 → 1.18.46，+编译器 add-opens 参数 | Java 25 兼容 |

---

## 七、报表接口（模块四/五已实现）

实时看板模块专注于**公开的实时状态展示**。以下管理员报表接口由预约模块（成员C）和座位管控模块（成员D）提供：

| 接口 | 所属模块 | 说明 |
|------|----------|------|
| `GET /api/reports/usage-rate` | reservation/ReportController | 日均使用率统计（C） |
| `GET /api/reports/time-distribution` | reservation/ReportController | 24小时时段占用分布（C） |
| `GET /api/reports/hot-periods` | reservation/ReportController | 热门时段TOP5（C） |
| `GET /api/reports/export` | reservation/ReportController | 导出Excel报表（C） |
| `GET /api/reports/no-show-rate` | seat/SeatReportController | 爽约率统计（D） |
| `GET /api/reports/conversion-rate` | seat/SeatReportController | 预约转化率（D） |
