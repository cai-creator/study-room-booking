# 用户与权限模块

> 负责人：成员A

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
│   ├── RegisterRequest.java      # 注册请求
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
| 用户注册 | POST | `/api/auth/register` | 公开 | 注册新用户，默认角色 STUDENT |
| 登出 | POST | `/api/auth/logout` | 登录用户 | JWT无状态，客户端清除token |
| 获取当前用户 | GET | `/api/auth/me` | 登录用户 | 获取当前登录用户信息 |
| 用户列表 | GET | `/api/users` | ADMIN+ | 分页查询，支持关键词/角色/状态筛选 |
| 用户详情 | GET | `/api/users/{id}` | 登录用户 | 管理员或本人可查看 |
| 新增用户 | POST | `/api/users` | SUPER_ADMIN | 管理员创建用户 |
| 更新用户 | PUT | `/api/users/{id}` | 登录用户 | 管理员或本人可更新 |
| 删除用户 | DELETE | `/api/users/{id}` | SUPER_ADMIN | 逻辑删除 |
| 修改密码 | PUT | `/api/users/{id}/password` | 登录用户 | 需验证旧密码 |
| 修改状态 | PATCH | `/api/users/{id}/status` | SUPER_ADMIN | 启用/禁用用户 |

## 角色说明

| 角色 | 说明 |
|------|------|
| STUDENT | 学生（默认角色） |
| ADMIN | 管理员 |
| SUPER_ADMIN | 超级管理员 |

## 关键设计

- **密码加密**：使用 BCrypt 加密存储
- **JWT认证**：登录后签发 token，有效期 24 小时
- **权限控制**：通过 `@RequireRole` 注解标注接口所需角色
- **逻辑删除**：用户删除为逻辑删除（deleted=1），不物理删除
