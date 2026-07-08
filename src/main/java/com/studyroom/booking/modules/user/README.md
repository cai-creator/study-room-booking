# 用户与权限模块

> 负责人：蔡俊晨

## 模块说明

本模块负责用户认证、授权、账号管理等核心功能，是系统的基础安全模块。

## 目录结构

```
user/
├── controller/
│   ├── AuthController.java       # 认证控制器（登录、注册、登出、获取当前用户）
│   └── UserController.java       # 用户管理控制器（CRUD、改密、状态修改）
├── dto/
│   ├── LoginRequest.java         # 登录请求
│   ├── LoginVO.java              # 登录返回（含token）
│   ├── RegisterRequest.java      # 注册请求（公开注册）
│   ├── CreateUserRequest.java    # 创建用户请求（超级管理员专用）
│   ├── UpdateUserRequest.java    # 更新用户请求
│   ├── ChangePasswordRequest.java# 修改密码请求
│   └── UserVO.java              # 用户视图对象
├── entity/
│   └── User.java                # 用户实体（对应 sys_user 表）
├── mapper/
│   └── UserMapper.java          # MyBatis-Plus Mapper
└── service/
    └── UserService.java         # 用户业务逻辑
```

## 已实现接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 用户登录 | POST | `/api/auth/login` | 公开 | 用户名密码登录，返回JWT token |
| 用户注册 | POST | `/api/auth/register` | 公开 | 注册新用户，强制默认 STUDENT 角色 |
| 登出 | POST | `/api/auth/logout` | 登录用户 | JWT无状态，客户端清除token |
| 获取当前用户 | GET | `/api/auth/me` | 登录用户 | 获取当前登录用户信息 |
| 用户列表 | GET | `/api/users` | ADMIN/SUPER_ADMIN | 分页查询，支持关键词/角色/状态筛选 |
| 用户详情 | GET | `/api/users/{id}` | SUPER_ADMIN/ADMIN(仅STUDENT)/本人 | ADMIN不可查看SUPER_ADMIN和其他ADMIN |
| 新增用户 | POST | `/api/users` | SUPER_ADMIN | 超级管理员创建用户（可指定角色） |
| 更新用户 | PUT | `/api/users/{id}` | ADMIN/SUPER_ADMIN/本人 | 管理员或本人可更新，ADMIN不可修改ADMIN/SUPER_ADMIN |
| 删除用户 | DELETE | `/api/users/{id}` | SUPER_ADMIN | 逻辑删除 |
| 修改密码 | PUT | `/api/users/{id}/password` | SUPER_ADMIN/本人 | 本人需验证旧密码，超级管理员可直接重置 |
| 修改状态 | PATCH | `/api/users/{id}/status` | SUPER_ADMIN | 启用/禁用用户 |

## 角色说明

| 角色 | 说明 | 权限范围 |
|------|------|----------|
| STUDENT | 学生（默认角色） | 预约座位、查看个人信息、修改个人资料和密码 |
| ADMIN | 管理员 | 管理学生用户、查看用户列表（含所有角色）、查看学生详情。不可查看/修改 ADMIN 和 SUPER_ADMIN |
| SUPER_ADMIN | 超级管理员 | 完整权限，可管理所有用户（包括管理员）、创建/删除管理员 |

### 角色层级

```
SUPER_ADMIN > ADMIN > STUDENT
```

- SUPER_ADMIN 可以管理所有用户（包括ADMIN）
- ADMIN 只能管理 STUDENT 用户，不能管理 ADMIN 或 SUPER_ADMIN
- STUDENT 只能管理自己

## 关键设计

- **密码加密**：使用 BCrypt 加密存储
- **JWT认证**：登录后签发 token，有效期 24 小时
- **权限控制**：通过 `@RequireRole` 注解标注接口所需角色
- **逻辑删除**：用户删除为逻辑删除（deleted=1），不物理删除
- **注册限制**：公开注册默认 STUDENT 角色，无法自定义角色
- **密码修改**：本人修改需验证旧密码，超级管理员可直接重置
