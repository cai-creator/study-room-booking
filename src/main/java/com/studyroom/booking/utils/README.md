# 工具类模块

> 负责人：成员A（可由各成员补充）

## 模块说明

存放系统通用工具类，各模块开发中遇到的通用方法可往此目录补充。

## 目录结构

```
utils/
└── JwtUtils.java    # JWT 工具类（生成/解析/验证 token）
```

## JwtUtils 说明

| 方法 | 说明 |
|------|------|
| `generateToken(userId, username, role)` | 生成 JWT token |
| `parseToken(token)` | 解析 token 返回 Claims |
| `getUserId(token)` | 从 token 获取用户 ID |
| `getUsername(token)` | 从 token 获取用户名 |
| `getRole(token)` | 从 token 获取角色 |
| `validateToken(token)` | 验证 token 是否有效 |
| `getExpireTime()` | 获取 token 过期时间（毫秒） |

## 配置项

```yaml
jwt:
  secret: study-room-booking-secret-key-2024-very-long-secret-for-jwt-token
  expire: 86400000    # 24小时（毫秒）
  header: Authorization
  prefix: Bearer
```

## 其他成员补充规范

- 工具类应为 `static` 方法或 Spring `@Component`
- 方法命名清晰，附带 Javadoc 注释
- 通用性强的方法才放入此目录，业务相关的方法请放在对应模块的 service 中
