# 高校自习室智能预约系统

## 项目简介

本项目是一套覆盖全校区的高校自习室在线预约管理系统，实现座位资源的数字化管理和动态调配。学生可在线查看座位状态、自主选择座位、提前预约；系统自动处理超时未签到释放、爽约黑名单等座位管控逻辑；提供实时座位热力图和多维度数据报表，辅助管理决策。

## 技术栈

- **后端框架**: Spring Boot 3.2.x
- **ORM框架**: MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0+
- **认证方案**: JWT
- **API文档**: Knife4j / OpenAPI 3
- **工具库**: Hutool
- **构建工具**: Maven
- **JDK版本**: Java 21+

## 项目结构

```
study-room-booking/
├── src/
│   ├── main/
│   │   ├── java/com/studyroom/booking/
│   │   │   ├── StudyRoomBookingApplication.java   # 启动类
│   │   │   ├── common/                             # 公共组件
│   │   │   │   ├── Result.java                     # 统一响应格式
│   │   │   │   ├── ResultCode.java                 # 状态码枚举
│   │   │   │   ├── annotation/                     # 自定义注解
│   │   │   │   ├── config/                         # 配置类
│   │   │   │   └── exception/                      # 异常处理
│   │   │   ├── modules/                            # 业务模块
│   │   │   │   ├── user/                           # 用户与权限（成员A）
│   │   │   │   ├── space/                          # 空间管理（成员B）
│   │   │   │   ├── reservation/                    # 预约核心（成员C）
│   │   │   │   ├── seat/                           # 座位管控（成员D）
│   │   │   │   └── report/                         # 数据报表
│   │   │   └── utils/                              # 工具类
│   │   └── resources/
│   │       ├── mapper/                             # MyBatis Mapper XML
│   │       ├── application.yml                     # 主配置文件
│   │       ├── application-dev.yml                 # 开发环境配置
│   │       └── application-prod.yml                # 生产环境配置
│   └── test/                                       # 测试代码
├── frontend/                                        # 前端工程（成员E/F）
├── docs/
│   ├── sql/                                        # 数据库脚本
│   └── api/                                        # API文档
├── 示例与设计/                                      # 前端页面设计原型与PRD文档
└── pom.xml
```

### 模块负责人一览

| 目录 | 模块名 | 负责人 | 说明 |
|------|--------|--------|------|
| `modules/user/` | 用户与权限 | 蔡俊晨 | 用户认证、RBAC权限、账号管理 |
| `modules/space/` | 空间管理 | 陈梦涵 | 校区/楼栋/楼层/自习室/座位的CRUD、批量导入、座位排布 |
| `modules/reservation/` | 预约核心 | 郭学威 | 预约创建/取消/查询、规则引擎、并发控制、实时状态 |
| `modules/seat/` | 座位管控 | 邓祺然 | 签到/签退/暂离、定时任务、黑名单、消息通知 |
| `modules/report/` | 数据报表 | 郭学威 + 邓祺然 | C提供使用率/时段分布接口，D提供爽约率/转化率接口 |
| `common/` | 公共组件 | 蔡俊晨 | 统一响应、异常处理、配置类、自定义注解等公共基础设施 |
| `utils/` | 工具类 | 蔡俊晨（可由各成员补充） | 通用工具方法，各模块开发中遇到可往里补充 |

## 团队分工

| 姓名 | 角色 | 负责模块 | 技术侧重 |
|------|------|---------|---------|
| 蔡俊晨 | 项目负责人/后端 | 用户与权限、数据库设计、API规范制定、项目基础设施 | 后端 + 协调 |
| 陈梦涵 | 后端开发 | 空间管理（校区/楼栋/楼层/自习室/座位CRUD） | 后端 |
| 郭学威 | 后端开发 | 预约核心（预约流程、规则引擎、并发控制、报表统计） | 后端 |
| 邓祺然 | 后端开发 | 座位管控（签到/签退/暂离、定时任务、黑名单、报表统计） | 后端 |
| 虞上昕 | 前端开发 | 学生端全部页面（选座、预约、看板） | 前端 |
| 黄宇涵 | 前端开发 | 管理端后台、数据报表页、大屏看板 | 前端 |

---

## 前后端对接指南

### 1. 对接矩阵

#### 1.1 前端E（学生端）对接后端

| 前端页面（成员E） | 对接后端 | 核心接口 | 后端负责人 |
|-------------------|----------|---------|-----------|
| 首页 | 空间管理 + 看板 | 校区/楼栋列表、自习室概览、使用率统计 | 成员B |
| 自习室筛选页 | 空间管理 | 自习室列表查询、筛选过滤、座位数统计 | 成员B |
| 座位选座页 | 预约核心 | 座位实时状态查询、创建预约、取消预约、可用时段 | 成员C |
| 我的预约页 | 预约核心 + 座位管控 | 我的预约列表、取消预约、签到/签退/暂离 | 成员C + 成员D |
| 实时看板页 | 空间管理 + 预约核心 | 校区/楼栋概览、自习室座位实时状态 | 成员B + 成员C |

#### 1.2 前端F（管理端）对接后端

| 前端页面（成员F） | 对接后端 | 核心接口 | 后端负责人 |
|-------------------|----------|---------|-----------|
| 空间管理后台 | 空间管理 | 空间层级CRUD、座位排布配置、批量导入 | 成员B |
| 用户管理页 | 用户与权限 | 用户列表、角色管理、状态修改 | 成员A |
| 黑名单管理页 | 座位管控 | 黑名单列表查询、手动加入/移出 | 成员D |
| 数据报表页 | 预约核心 + 座位管控 | 使用率统计、时段分布、爽约率、转化率 | 成员C + 成员D |
| 大屏看板 | 空间管理 + 预约核心 | 实时概览、热力图数据 | 成员B + 成员C |

### 2. 对接流程

```
阶段一：Mock并行开发
    ↓
后端接口开发      前端页面开发（使用Mock数据）
    ↓                  ↓
阶段二：接口联调
    ↓
前后端约定接口字段 → 前端对接真实接口 → 发现问题反馈后端修复
    ↓
阶段三：集成测试
    ↓
完整业务流程联调（选座→预约→签到→暂离→签退）
```

### 3. Mock 数据约定

前端在等待后端接口期间，可使用以下Mock数据规范先行开发：

- Mock数据结构与最终API返回格式一致（统一响应格式：`code/message/data/timestamp`）
- 接口路径与正式API路径保持一致，仅域名/端口不同
- 建议使用 Mock.js 或本地JSON文件模拟数据
- 状态枚举值必须与后端一致（如座位状态：`AVAILABLE/RESERVED/OCCUPIED/TEMPORARY_LEAVE/UNAVAILABLE`）
- 分页参数和返回结构遵循统一规范（`list/total/pageNum/pageSize`）

### 4. 联调注意事项

#### 4.1 跨域配置
- 后端已配置CORS跨域支持（前端开发阶段无需配置代理）
- 前端请求base URL：`http://localhost:8081/api`
- 生产环境建议使用Nginx反向代理，统一域名下部署

#### 4.2 认证对接
- 登录接口成功后，将返回的 `token` 存储到 localStorage / sessionStorage
- 后续请求在 Header 中携带：`Authorization: Bearer {token}`
- 后端返回 `code: 401` 时，前端应跳转到登录页

#### 4.3 字段命名约定
| 位置 | 命名风格 | 示例 |
|------|---------|------|
| 数据库 | 下划线命名 | `user_name`, `created_at` |
| 后端Java | 小驼峰 | `userName`, `createdAt` |
| 前端JSON | 小驼峰 | `userName`, `createdAt` |
| URL路径 | 短横线命名 | `user-management`, `room-selection` |

#### 4.4 时间格式
- 前后端传输统一使用字符串格式：`yyyy-MM-dd HH:mm:ss`
- 时区：Asia/Shanghai（东八区）
- 前端展示时可自行格式化

### 5. 常见对接问题排查

| 问题现象 | 可能原因 | 排查方式 |
|---------|---------|---------|
| 接口返回401 | token过期或未携带 | 检查请求头Authorization，重新登录获取新token |
| 接口返回403 | 角色权限不足 | 确认当前用户角色是否有接口访问权限 |
| 跨域错误 | CORS配置问题 | 检查后端CORS配置，或使用前端代理 |
| 字段值为null | 字段名不匹配 | 核对接口文档，确认字段名和数据类型 |
| 分页数据不对 | 参数名不一致 | 确认分页参数名：`pageNum`/`pageSize` |
| 日期显示异常 | 时区或格式问题 | 确认时间格式为 `yyyy-MM-dd HH:mm:ss` |

### 6. 接口文档访问

后端启动后，可通过以下地址查看在线接口文档：

- **Knife4j（推荐）**: http://localhost:8081/api/doc.html
- **Swagger UI**: http://localhost:8081/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api/v3/api-docs

---

## API 规范

### 1. 接口命名规范

#### 1.1 URL 命名
- 统一前缀: `/api`
- 使用 RESTful 风格，URL 中使用名词复数形式
- 层级结构清晰，按模块划分

```
GET    /api/users                    # 获取用户列表
GET    /api/users/{id}               # 获取单个用户
POST   /api/users                    # 新增用户
PUT    /api/users/{id}               # 更新用户
DELETE /api/users/{id}               # 删除用户
```

#### 1.2 HTTP 方法
| 方法 | 用途 | 示例 |
|------|------|------|
| GET | 查询资源 | 获取用户列表、获取详情 |
| POST | 创建资源 | 新增用户、创建预约 |
| PUT | 全量更新资源 | 更新用户信息 |
| PATCH | 部分更新资源 | 修改用户状态 |
| DELETE | 删除资源 | 删除用户 |

### 2. 统一响应格式

所有接口统一返回如下格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 业务数据
  },
  "timestamp": 1719900000000
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 提示信息 |
| data | Object | 业务数据（可为空） |
| timestamp | Long | 服务器时间戳 |

#### 分页响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [],
    "total": 100,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": 1719900000000
}
```

### 3. 状态码规范

| 状态码 | 说明 | 备注 |
|--------|------|------|
| 200 | 操作成功 | |
| 400 | 参数错误 | 请求参数校验失败 |
| 401 | 未登录 | token缺失或过期 |
| 403 | 无权限 | 角色权限不足 |
| 404 | 资源不存在 | |
| 500 | 系统错误 | |
| 1001 | 用户不存在 | |
| 1002 | 密码错误 | |
| 1003 | 用户已存在 | |
| 2001 | 自习室不存在 | |
| 2002 | 自习室已关闭 | |
| 2003 | 座位不存在 | |
| 2004 | 座位不可用 | |
| 3001 | 预约记录不存在 | |
| 3002 | 预约时间无效 | |
| 3003 | 今日预约次数已达上限 | |
| 3004 | 预约时长超过最大限制 | |
| 3005 | 预约时间过早 | |
| 3006 | 预约时间过晚 | |
| 3007 | 该时段座位已被预约 | |
| 3008 | 当前状态不允许取消预约 | |
| 4001 | 无法签到 | |
| 4002 | 已签到 | |
| 4003 | 签到时间过早 | |
| 4004 | 已超过签到时间 | |
| 5001 | 已被加入黑名单 | |

### 4. 请求规范

#### 4.1 请求头

```
Content-Type: application/json
Authorization: Bearer {token}
```

#### 4.2 分页请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页条数，默认20，最大100 |
| sortField | String | 否 | 排序字段 |
| sortOrder | String | 否 | 排序方向：asc/desc |

### 5. 鉴权规范

#### 5.1 认证方式
- 采用 JWT (JSON Web Token) 进行身份认证
- 用户登录成功后，服务端签发 token
- 后续请求在 Header 中携带 token

#### 5.2 用户角色

| 角色 | 说明 |
|------|------|
| STUDENT | 学生 |
| ADMIN | 自习室管理员 |
| SUPER_ADMIN | 超级管理员 |

#### 5.3 权限控制
- 使用 `@RequireRole` 注解标注接口所需角色
- 学生只能操作自己的预约记录
- 管理员只能管理授权范围内的空间

---

## 接口清单

### 模块一：用户与权限（成员A负责）

#### 1.1 认证接口

| 接口 | 方法 | 路径 | 说明 | 权限 | 状态 |
|------|------|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 用户名密码登录 | 公开 | 已实现 |
| 注册 | POST | `/api/auth/register` | 注册新用户，默认角色 STUDENT | 公开 | 已实现 |
| 登出 | POST | `/api/auth/logout` | 退出登录 | 所有登录用户 | 已实现 |
| 获取当前用户 | GET | `/api/auth/me` | 获取当前登录用户信息 | 所有登录用户 | 已实现 |

**登录请求:**
```json
{
  "username": "2024001001",
  "password": "password123"
}
```

**登录响应:**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expireAt": 1719986400000,
    "user": {
      "id": 1,
      "username": "2024001001",
      "realName": "张三",
      "role": "STUDENT",
      "avatar": null
    }
  },
  "timestamp": 1719900000000
}
```

#### 1.2 用户管理接口

| 接口 | 方法 | 路径 | 说明 | 权限 | 状态 |
|------|------|------|------|------|------|
| 用户列表 | GET | `/api/users` | 分页查询用户列表 | ADMIN/SUPER_ADMIN | 已实现 |
| 用户详情 | GET | `/api/users/{id}` | 获取用户详情 | ADMIN/SUPER_ADMIN/本人 | 已实现 |
| 新增用户 | POST | `/api/users` | 新增用户（可指定角色） | SUPER_ADMIN | 已实现 |
| 更新用户 | PUT | `/api/users/{id}` | 更新用户信息（ADMIN不可修改ADMIN/SUPER_ADMIN） | ADMIN/SUPER_ADMIN/本人 | 已实现 |
| 删除用户 | DELETE | `/api/users/{id}` | 删除用户（逻辑删除） | SUPER_ADMIN | 已实现 |
| 修改密码 | PUT | `/api/users/{id}/password` | 修改密码（本人需验证旧密码，超级管理员可直接重置） | SUPER_ADMIN/本人 | 已实现 |
| 修改状态 | PATCH | `/api/users/{id}/status` | 启用/禁用用户 | SUPER_ADMIN | 已实现 |

**用户列表查询参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| keyword | String | 否 | 关键词（学号/姓名） |
| role | String | 否 | 角色筛选 |
| status | Integer | 否 | 状态筛选 |

---

### 模块二：空间管理（成员B负责）

#### 2.1 校区管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 校区列表 | GET | `/api/campuses` | 获取校区列表 | 公开 |
| 校区详情 | GET | `/api/campuses/{id}` | 获取校区详情 | 公开 |
| 新增校区 | POST | `/api/campuses` | 新增校区 | 超级管理员 |
| 更新校区 | PUT | `/api/campuses/{id}` | 更新校区 | 超级管理员 |
| 删除校区 | DELETE | `/api/campuses/{id}` | 删除校区 | 超级管理员 |

#### 2.2 楼栋管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 楼栋列表 | GET | `/api/buildings` | 获取楼栋列表 | 公开 |
| 楼栋详情 | GET | `/api/buildings/{id}` | 获取楼栋详情 | 公开 |
| 新增楼栋 | POST | `/api/buildings` | 新增楼栋 | 超级管理员 |
| 更新楼栋 | PUT | `/api/buildings/{id}` | 更新楼栋 | 超级管理员 |
| 删除楼栋 | DELETE | `/api/buildings/{id}` | 删除楼栋 | 超级管理员 |

**查询参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| campusId | Long | 否 | 校区ID筛选 |

#### 2.3 楼层管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 楼层列表 | GET | `/api/floors` | 获取楼层列表 | 公开 |
| 楼层详情 | GET | `/api/floors/{id}` | 获取楼层详情 | 公开 |
| 新增楼层 | POST | `/api/floors` | 新增楼层 | 超级管理员 |
| 更新楼层 | PUT | `/api/floors/{id}` | 更新楼层 | 超级管理员 |
| 删除楼层 | DELETE | `/api/floors/{id}` | 删除楼层 | 超级管理员 |

**查询参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| buildingId | Long | 是 | 楼栋ID |

#### 2.4 自习室管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 自习室列表 | GET | `/api/rooms` | 分页查询自习室列表 | 公开 |
| 自习室详情 | GET | `/api/rooms/{id}` | 获取自习室详情 | 公开 |
| 新增自习室 | POST | `/api/rooms` | 新增自习室 | 管理员 |
| 更新自习室 | PUT | `/api/rooms/{id}` | 更新自习室 | 管理员 |
| 删除自习室 | DELETE | `/api/rooms/{id}` | 删除自习室 | 超级管理员 |
| 修改状态 | PATCH | `/api/rooms/{id}/status` | 修改开放状态 | 管理员 |
| 批量导入 | POST | `/api/rooms/import` | Excel批量导入 | 管理员 |

**自习室列表查询参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| campusId | Long | 否 | 校区ID |
| buildingId | Long | 否 | 楼栋ID |
| floorId | Long | 否 | 楼层ID |
| roomType | String | 否 | 房间类型 |
| status | Integer | 否 | 状态 |
| hasAvailableSeats | Boolean | 否 | 仅显示有空座位的 |
| keyword | String | 否 | 名称关键词 |

#### 2.5 座位管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 座位列表 | GET | `/api/seats` | 获取自习室座位列表 | 公开 |
| 座位详情 | GET | `/api/seats/{id}` | 获取座位详情 | 公开 |
| 批量生成座位 | POST | `/api/rooms/{roomId}/seats/generate` | 按行列批量生成座位 | 管理员 |
| 更新座位 | PUT | `/api/seats/{id}` | 更新座位信息 | 管理员 |
| 删除座位 | DELETE | `/api/seats/{id}` | 删除座位 | 管理员 |
| 批量更新座位标签 | PATCH | `/api/rooms/{roomId}/seats/tags` | 批量更新座位标签 | 管理员 |
| 座位实时状态 | GET | `/api/rooms/{roomId}/seats/status` | 获取自习室所有座位实时状态 | 公开 |

**座位实时状态响应:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "roomId": 1,
    "roomName": "图书馆101自习室",
    "totalSeats": 60,
    "availableSeats": 45,
    "reservedSeats": 10,
    "occupiedSeats": 5,
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

**座位状态枚举:**
| 状态 | 说明 |
|------|------|
| AVAILABLE | 空闲（可预约） |
| RESERVED | 已预约 |
| OCCUPIED | 已占用（使用中） |
| TEMPORARY_LEAVE | 暂离 |
| UNAVAILABLE | 不可用（维护/停用） |

---

### 模块三：预约核心（成员C负责）

#### 3.1 预约管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 创建预约 | POST | `/api/reservations` | 创建预约 | 学生 |
| 取消预约 | POST | `/api/reservations/{id}/cancel` | 取消预约 | 学生/管理员 |
| 我的预约 | GET | `/api/reservations/my` | 获取我的预约列表 | 学生 |
| 预约详情 | GET | `/api/reservations/{id}` | 获取预约详情 | 学生/管理员 |
| 预约列表 | GET | `/api/reservations` | 分页查询预约记录 | 管理员 |
| 座位可用时段 | GET | `/api/seats/{seatId}/available-slots` | 查询座位某天空闲时段 | 公开 |

**创建预约请求:**
```json
{
  "seatId": 1,
  "startTime": "2026-07-03 09:00:00",
  "endTime": "2026-07-03 12:00:00"
}
```

**创建预约响应:**
```json
{
  "code": 200,
  "message": "预约成功",
  "data": {
    "id": 1,
    "seatId": 1,
    "seatCode": "A-01",
    "roomId": 1,
    "roomName": "图书馆101自习室",
    "startTime": "2026-07-03 09:00:00",
    "endTime": "2026-07-03 12:00:00",
    "status": "RESERVED",
    "checkinCode": "QR202607030001",
    "createdAt": "2026-07-02 15:30:00"
  },
  "timestamp": 1719900000000
}
```

**我的预约查询参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| status | String | 否 | 状态筛选 |
| date | String | 否 | 日期筛选（yyyy-MM-dd） |

#### 3.2 预约规则

| 规则项 | 默认值 | 说明 |
|--------|--------|------|
| 单日预约次数上限 | 3次 | 同一天内最多发起3次预约 |
| 单次最大时长 | 8小时 | 单次预约连续占用不超过8小时 |
| 提前预约时间 | 24小时 | 最多可提前24小时预约 |
| 最短提前时间 | 15分钟 | 距离开始时间不足15分钟不可预约 |
| 签到宽限时间 | 15分钟 | 预约开始后15分钟内未签到自动释放 |

---

### 模块四：座位管控（成员D负责）

#### 4.1 签到签退

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 签到 | POST | `/api/checkin` | 签到 | 学生 |
| 签退 | POST | `/api/checkout` | 签退 | 学生 |
| 暂离 | POST | `/api/temporary-leave` | 暂离 | 学生 |
| 返回座位 | POST | `/api/return-seat` | 暂离后返回 | 学生 |

**签到请求:**
```json
{
  "seatCode": "A-01",
  "roomId": 1
}
```

**签到响应:**
```json
{
  "code": 200,
  "message": "签到成功",
  "data": {
    "reservationId": 1,
    "seatCode": "A-01",
    "checkinTime": "2026-07-03 08:55:00",
    "endTime": "2026-07-03 12:00:00",
    "status": "CHECKED_IN"
  },
  "timestamp": 1719900000000
}
```

#### 4.2 黑名单管理

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 黑名单列表 | GET | `/api/blacklist` | 分页查询黑名单 | 管理员 |
| 黑名单详情 | GET | `/api/blacklist/{id}` | 获取详情 | 管理员 |
| 手动加入黑名单 | POST | `/api/blacklist` | 手动添加 | 管理员 |
| 手动移出黑名单 | DELETE | `/api/blacklist/{id}` | 手动解除 | 管理员 |
| 我的黑名单状态 | GET | `/api/blacklist/my` | 查看我的黑名单状态 | 学生 |

**黑名单查询参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页条数 |
| keyword | String | 否 | 学号/姓名关键词 |
| status | Integer | 否 | 状态筛选 |

#### 4.3 爽约记录

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 爽约记录列表 | GET | `/api/no-show-records` | 分页查询爽约记录 | 管理员 |
| 我的爽约记录 | GET | `/api/no-show-records/my` | 查看我的爽约记录 | 学生 |

---

### 模块五：实时看板与数据报表

#### 5.1 实时看板

| 接口 | 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|------|
| 校区使用概览 | GET | `/api/dashboard/campus-overview` | 各校区自习室使用率概览 | 公开 |
| 楼栋使用概览 | GET | `/api/dashboard/building-overview` | 各楼栋自习室使用率 | 公开 |
| 自习室使用详情 | GET | `/api/dashboard/room-detail/{roomId}` | 单个自习室座位实时状态 | 公开 |

**校区使用概览响应:**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "campusId": 1,
      "campusName": "主校区",
      "totalRooms": 10,
      "totalSeats": 600,
      "availableSeats": 420,
      "usageRate": 30.0
    }
  ],
  "timestamp": 1719900000000
}
```

#### 5.2 数据报表

| 接口 | 方法 | 路径 | 说明 | 权限 | 负责人 |
|------|------|------|------|------|--------|
| 日均使用率报表 | GET | `/api/reports/usage-rate` | 日均使用率统计 | 管理员 | 成员C |
| 时段占用分布 | GET | `/api/reports/time-distribution` | 24小时时段占用分布 | 管理员 | 成员C |
| 热门时段排名 | GET | `/api/reports/hot-periods` | 热门时段TOP5 | 管理员 | 成员C |
| 爽约率统计 | GET | `/api/reports/no-show-rate` | 爽约率统计 | 管理员 | 成员D |
| 预约转化率 | GET | `/api/reports/conversion-rate` | 预约转化率 | 管理员 | 成员D |
| 导出报表 | GET | `/api/reports/export` | 导出Excel报表 | 管理员 | 成员C |

**报表查询参数:**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| startDate | String | 是 | 开始日期（yyyy-MM-dd） |
| endDate | String | 是 | 结束日期（yyyy-MM-dd） |
| campusId | Long | 否 | 校区ID |
| buildingId | Long | 否 | 楼栋ID |
| roomId | Long | 否 | 自习室ID |
| dimension | String | 否 | 时间维度: day/week/month |

---

## 数据库设计

### ER图概览

```
sys_user (用户表)
    ├── reservation (预约记录)
    ├── blacklist (黑名单)
    └── no_show_record (爽约记录)

campus (校区)
    └── building (楼栋)
        └── floor (楼层)
            └── study_room (自习室)
                └── seat (座位)
                    └── reservation (预约记录)
```

### 核心表说明

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| sys_user | 用户表 | id, username, real_name, role, status |
| campus | 校区表 | id, name, address, status |
| building | 楼栋表 | id, campus_id, name, floor_count |
| floor | 楼层表 | id, building_id, floor_number |
| study_room | 自习室表 | id, floor_id, name, room_type, total_seats, rows_count, cols_count, status |
| seat | 座位表 | id, room_id, seat_code, row_number, col_number, tags, status |
| reservation | 预约记录表 | id, user_id, seat_id, room_id, start_time, end_time, status, version |
| blacklist | 黑名单表 | id, user_id, reason, start_time, end_time, status |
| no_show_record | 爽约记录表 | id, user_id, reservation_id, reason, record_date |
| operation_log | 操作审计日志 | id, user_id, module, operation, ip, status |

详细建表脚本见: [docs/sql/schema.sql](docs/sql/schema.sql)

---

## 开发规范

### 1. 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 方法名、变量名采用小驼峰命名
- 类名采用大驼峰命名
- 常量全大写，下划线分隔

### 2. 分支规范
- `main` - 主分支，生产环境代码
- `develop` - 开发分支
- `feature/xxx` - 功能开发分支
- `bugfix/xxx` - Bug修复分支

### 3. 提交规范
采用 Conventional Commits 规范:
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建/工具相关
```

---

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.8+
- MySQL 8.0+

### 本地运行

1. 克隆项目
```bash
git clone https://github.com/cai-creator/study-room-booking.git
cd study-room-booking
```

2. 初始化数据库
```bash
mysql -u root -p < docs/sql/schema.sql
```

3. 修改配置文件
编辑 `src/main/resources/application-dev.yml`，修改数据库连接信息。

4. 启动项目
```bash
mvn spring-boot:run
```

5. 访问接口文档
- Knife4j: http://localhost:8081/api/doc.html
- Swagger UI: http://localhost:8081/api/swagger-ui.html

---

## 许可证

MIT License
