# 空间管理模块（成员B：陈梦涵）

## 模块说明

本模块负责高校自习室的空间层级管理，包括校区、楼栋、楼层、自习室、座位的 CRUD 操作，以及 Excel 批量导入自习室和座位网格排布功能。

## 包结构

```
modules/space/
├── controller/
│   ├── CampusController.java         # 校区管理控制器
│   ├── BuildingController.java       # 楼栋管理控制器
│   ├── FloorController.java          # 楼层管理控制器
│   ├── RoomController.java           # 自习室管理（含批量导入）
│   └── SeatController.java           # 座位管理（含批量生成、状态查询）
├── dto/
│   ├── CampusRequest.java            # 校区请求
│   ├── BuildingRequest.java          # 楼栋请求
│   ├── FloorRequest.java             # 楼层请求
│   ├── RoomRequest.java              # 自习室请求
│   ├── RoomQueryRequest.java         # 自习室分页查询
│   ├── SeatGenerateRequest.java      # 批量生成座位请求
│   ├── SeatTagsUpdateRequest.java    # 批量更新标签请求
│   └── RoomSeatStatusVO.java         # 座位实时状态响应
├── entity/
│   ├── Campus.java                   # 校区实体
│   ├── Building.java                 # 楼栋实体
│   ├── Floor.java                    # 楼层实体
│   ├── StudyRoom.java                # 自习室实体
│   └── Seat.java                     # 座位实体
├── mapper/
│   ├── CampusMapper.java             # 校区 Mapper
│   ├── BuildingMapper.java           # 楼栋 Mapper
│   ├── FloorMapper.java              # 楼层 Mapper
│   ├── StudyRoomMapper.java          # 自习室 Mapper（含自定义分页查询）
│   └── SeatMapper.java              # 座位 Mapper
└── service/
    ├── CampusService.java            # 校区业务逻辑
    ├── BuildingService.java          # 楼栋业务逻辑
    ├── FloorService.java             # 楼层业务逻辑（含楼层数同步）
    ├── StudyRoomService.java         # 自习室业务逻辑（含Excel导入）
    └── SeatService.java             # 座位业务逻辑（含批量生成、状态查询）
```

## API 接口清单

### 一、校区管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 校区列表 | GET | `/api/campuses` | 获取所有校区 | 公开 |
| 校区详情 | GET | `/api/campuses/{id}` | 获取校区详情 | 公开 |
| 新增校区 | POST | `/api/campuses` | 新增校区 | SUPER_ADMIN |
| 更新校区 | PUT | `/api/campuses/{id}` | 更新校区 | SUPER_ADMIN |
| 删除校区 | DELETE | `/api/campuses/{id}` | 删除校区 | SUPER_ADMIN |

### 二、楼栋管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 楼栋列表 | GET | `/api/buildings?campusId=` | 获取楼栋列表 | 公开 |
| 楼栋详情 | GET | `/api/buildings/{id}` | 获取楼栋详情 | 公开 |
| 新增楼栋 | POST | `/api/buildings` | 新增楼栋 | SUPER_ADMIN |
| 更新楼栋 | PUT | `/api/buildings/{id}` | 更新楼栋 | SUPER_ADMIN |
| 删除楼栋 | DELETE | `/api/buildings/{id}` | 删除楼栋 | SUPER_ADMIN |

### 三、楼层管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 楼层列表 | GET | `/api/floors?buildingId=` | 获取楼层列表（buildingId必填） | 公开 |
| 楼层详情 | GET | `/api/floors/{id}` | 获取楼层详情 | 公开 |
| 新增楼层 | POST | `/api/floors` | 新增楼层 | SUPER_ADMIN |
| 更新楼层 | PUT | `/api/floors/{id}` | 更新楼层 | SUPER_ADMIN |
| 删除楼层 | DELETE | `/api/floors/{id}` | 删除楼层 | SUPER_ADMIN |

### 四、自习室管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 自习室列表 | GET | `/api/rooms` | 分页查询自习室 | 公开 |
| 自习室详情 | GET | `/api/rooms/{id}` | 获取自习室详情 | 公开 |
| 新增自习室 | POST | `/api/rooms` | 新增自习室 | ADMIN |
| 更新自习室 | PUT | `/api/rooms/{id}` | 更新自习室 | ADMIN |
| 删除自习室 | DELETE | `/api/rooms/{id}` | 删除自习室 | SUPER_ADMIN |
| 修改状态 | PATCH | `/api/rooms/{id}/status` | 修改开放状态 | ADMIN |
| 批量导入 | POST | `/api/rooms/import` | Excel批量导入 | ADMIN |

### 五、座位管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 座位列表 | GET | `/api/seats?roomId=` | 获取自习室座位列表 | 公开 |
| 座位详情 | GET | `/api/seats/{id}` | 获取座位详情 | 公开 |
| 更新座位 | PUT | `/api/seats/{id}` | 更新座位 | ADMIN |
| 删除座位 | DELETE | `/api/seats/{id}` | 删除座位 | ADMIN |
| 批量生成座位 | POST | `/api/rooms/{roomId}/seats/generate` | 按行列批量生成 | ADMIN |
| 批量更新标签 | PATCH | `/api/rooms/{roomId}/seats/tags` | 批量更新标签 | ADMIN |
| 座位实时状态 | GET | `/api/rooms/{roomId}/seats/status` | 实时状态查询 | 公开 |

## 业务规则

- **级联删除保护**：删除前检查下级是否存在数据，有则拒绝删除
  - 校区下有楼栋 → 禁止删除
  - 楼栋下有楼层 → 禁止删除
  - 楼层下有自习室 → 禁止删除
  - 自习室下有座位 → 禁止删除
- **楼层数同步**：新增/删除/移动楼层时自动更新楼栋的 `floor_count`
- **座位数同步**：生成/删除座位时自动更新自习室的 `total_seats`
- **座位编号规则**：行字母(1→A, 2→B, …, 26→Z, 27→AA) + "-" + 列号(01, 02…)
- **座位标签**：WINDOW-靠窗, POWER-有电源, ACCESSIBLE-无障碍
- **座位状态**：AVAILABLE(空闲), RESERVED(已预约), OCCUPIED(已占用), TEMPORARY_LEAVE(暂离), UNAVAILABLE(不可用)

## 依赖说明

本模块使用以下数据库表（均在 `docs/sql/schema.sql` 中定义）：
- `campus` - 校区表
- `building` - 楼栋表
- `floor` - 楼层表
- `study_room` - 自习室表
- `seat` - 座位表

座位实时状态查询需要关联 `reservation` 表（由成员C管理）。

## Excel 批量导入模板

| 校区名称 | 楼栋名称 | 楼层号 | 自习室名称 | 类型 | 开放时间 | 关闭时间 | 描述 |
|---------|---------|--------|-----------|------|---------|---------|------|
| 主校区 | 图书馆 | 1 | 图书馆101自习室 | LIBRARY | 08:00:00 | 22:00:00 | 安静自习区 |
| 主校区 | 图书馆 | 2 | 图书馆201自习室 | LIBRARY | 08:00:00 | 22:30:00 | 考研专区 |

**说明**：
- 如果校区/楼栋/楼层不存在，系统会自动创建
- 类型可选值：LIBRARY(图书馆), TEACHING(教学楼), READING(阅览室)
- 支持 .xlsx 和 .xls 格式
