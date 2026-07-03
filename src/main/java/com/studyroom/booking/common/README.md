# 公共组件模块

> 负责人：成员A

## 模块说明

本模块为系统基础设施层，提供统一响应格式、异常处理、配置类、自定义注解、拦截器等公共能力，被所有业务模块共享。

## 目录结构

```
common/
├── Result.java                    # 统一响应格式（code/message/data/timestamp）
├── ResultCode.java               # 状态码枚举（200/400/401/403/404/500/1xxx-6xxx）
├── annotation/
│   └── RequireRole.java          # 角色权限注解（标注接口所需角色）
├── config/
│   ├── Knife4jConfig.java        # Knife4j/OpenAPI 文档配置
│   ├── MybatisPlusConfig.java    # MyBatis-Plus 配置（分页插件等）
│   └── WebMvcConfig.java         # Web MVC 配置（拦截器注册、跨域等）
├── context/
│   └── UserContext.java          # 用户上下文（线程级用户信息存储）
├── exception/
│   ├── BusinessException.java    # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理器
└── interceptor/
    ├── JwtInterceptor.java       # JWT 认证拦截器
    └── RoleInterceptor.java      # 角色权限拦截器
```

## 统一响应格式

所有接口统一返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1719900000000
}
```

## 状态码规划

| 码段 | 模块 |
|------|------|
| 200 | 成功 |
| 400/401/403/404/500 | 通用HTTP状态 |
| 1xxx | 用户与权限 |
| 2xxx | 空间管理 |
| 3xxx | 预约核心 |
| 4xxx | 座位管控 |
| 5xxx | 黑名单 |
| 6xxx | 空间扩展 |

## 权限控制机制

1. `@RequireRole({"ADMIN", "SUPER_ADMIN"})` 标注在 Controller 方法上
2. `JwtInterceptor` 解析 token，将 userId/role 存入 request attribute
3. `RoleInterceptor` 校验当前用户角色是否满足注解要求

## 其他成员使用指南

- 业务异常请抛出 `BusinessException(ResultCode.XXX)`
- 需要角色控制的接口加 `@RequireRole` 注解
- 分页查询使用 MyBatis-Plus 的 `Page` 对象
- 各模块自定义状态码请往 `ResultCode` 枚举中补充
