/**
 * HTTP 请求封装 - 高校自习室智能预约系统
 * ===========================================================
 *                 前端开发全局约定（必读）
 * ===========================================================
 *
 * 1. 接口 BasePath & URL
 *    - 后端 context-path = /api，已由 AppConfig.apiBaseUrl 统一拼接
 *    - 在 api/*.js 中调用时只写相对路径，如 Request.get('/users')
 *    - 禁止在任何 API 路径里再加 /api 前缀（否则会变成 /api/api/xxx）
 *
 * 2. 统一响应结构 Result<T>（所有接口都包这一层，request.js 已自动解包）
 *    @typedef {Object} Result<T>
 *    @property {number}  code       - 200=成功，400=参数错误/业务失败，401=未登录/Token失效，403=无权限，500=服务端异常，0=网络/超时
 *    @property {string}  message    - 提示信息
 *    @property {T}       data       - 业务数据（调用方只会收到 data 的解包值，通过 .then() 拿到）
 *    @property {number}  timestamp  - 响应时间戳（毫秒）
 *
 *    举例：
 *      UserAPI.getUser(1).then(user => {
 *        // 这里的 user 就是 UserVO，不是 Result<UserVO>
 *        console.log(user.realName);
 *      }).catch(err => {
 *        // err 是完整的 {code, message} 结构
 *        alert(err.message);
 *      });
 *
 * 3. 错误码速查
 *    | code | 说明 | 行为 |
 *    |------|------|------|
 *    | 200  | 成功 | resolve(data) |
 *    | 400  | 参数错误/业务逻辑失败（如用户名已存在、预约冲突） | reject，前端弹 message |
 *    | 401  | 未登录 / Token过期 | request.js 已自动：清token → 跳登录页 |
 *    | 403  | 无权限（如学生想进管理员接口） | reject，前端提示"无权限" |
 *    | 500  | 后端内部错误 | reject，前端弹 message |
 *    | 0    | 网络错误 / 请求超时(15s) | reject，由 request.js 生成 |
 *
 * 4. 认证 & Token
 *    - 登录成功后端返回 LoginVO { token, expireAt, user }
 *    - 请求时自动加 Header：Authorization: Bearer <token>
 *    - /auth/login、/auth/register、白名单 GET 接口（/campuses 等）不需要 token
 *    - 前端不需要手动续期，401 会被 request.js 自动拦截跳登录页
 *
 * 5. 分页参数 & 返回结构
 *    请求参数（Query）：
 *      pageNum   - 页码，默认 1，**从 1 开始**（不是从0）
 *      pageSize  - 每页条数，默认 20
 *    返回 Page<T>：
 *      @typedef {Object} Page<T>
 *      @property {T[]}    records - 当前页数据列表
 *      @property {number} total   - 总条数
 *      @property {number} size    - 每页大小
 *      @property {number} current - 当前页码（从1开始）
 *      @property {number} pages   - 总页数
 *    注意：分页字段名是 records/total/size/current，和 ElementUI 一致
 *
 * 6. 时间格式（严格遵守）
 *    - 日期：yyyy-MM-dd               （如 2025-07-09）
 *    - 日期时间：yyyy-MM-dd HH:mm:ss   （如 2025-07-09 14:30:00）
 *    - 仅时间：HH:mm:ss                 （如 08:00:00，自习室 openTime/closeTime）
 *    - 时间戳：毫秒级 13 位整数         （如 expireAt）
 *    - 时区：全部东八区（后端返回的是本地时区字符串，前端直接显示不用转换）
 *
 * 7. 枚举汇总（所有 status / type / role 字段的取值）
 *    ┌─────────────────────┬──────────────────────────────────────────────────────┐
 *    │ 字段                 │ 取值说明                                              │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ UserVO.role         │ STUDENT 学生                                           │
 *    │                     │ ADMIN  管理员                                          │
 *    │                     │ SUPER_ADMIN 超级管理员                                 │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ UserVO.status       │ 0 禁用（不能登录）                                      │
 *    │                     │ 1 正常                                                 │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ StudyRoom.roomType  │ LIBRARY   图书馆自习室                                 │
 *    │                     │ TEACHING  教学楼自习室                                 │
 *    │                     │ READING   阅览室                                       │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ StudyRoom.status    │ 0 关闭   1 开放   2 维护中                             │
 *    │ /campus|building|   │ 0 停用   1 启用                                       │
 *    │ /floor.status       │                                                        │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ Seat.status         │ 0 不可用   1 可用                                      │
 *    │ Seat.tags（逗号分） │ WINDOW 靠窗 / POWER 有电源 / ACCESSIBLE 无障碍          │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ SeatStatusItem.     │ AVAILABLE 空闲 / RESERVED 已预约 / OCCUPIED 已占用      │
 *    │ status              │ TEMPORARY_LEAVE 暂离 / UNAVAILABLE 不可用              │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ BookingVO.status    │ RESERVED   已预约（未签到）                             │
 *    │                     │ CHECKED_IN 已签到                                       │
 *    │                     │ CHECKED_OUT已签退（正常完成）                           │
 *    │                     │ CANCELLED  已取消                                       │
 *    │                     │ NO_SHOW    爽约（未签到/超时）                           │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ CheckinVO.status    │ CHECKED_IN 已签到 / TEMPORARY_LEAVE 暂离                │
 *    │                     │ COMPLETED 已完成                                        │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ BlacklistVO.status  │ 0 已解除   1 生效中                                    │
 *    ├─────────────────────┼──────────────────────────────────────────────────────┤
 *    │ NoShowRecordVO.     │ NO_CHECKIN 未签到 / TEMPORARY_LEAVE_TIMEOUT 暂离超时    │
 *    │ reason              │                                                        │
 *    └─────────────────────┴──────────────────────────────────────────────────────┘
 *
 * 8. 特殊参数传递（不要和 Body 搞混！）
 *    - Query（URL ? 拼接）：GET 全部参数、PATCH 状态接口的 status、login/register
 *    - JSON Body（请求体）：POST（除login/register）、PUT、批量更新接口
 *    - Path（URL 占位）   ：/users/{id}、/rooms/{roomId}/seats/status
 *    - FormData（文件）   ：/rooms/import （Request.upload 自动处理）
 *
 * 9. 学生端 vs 管理端 路由
 *    - 学生端 pages:     BasePath.student + '/pages/home.html' 等
 *    - 管理端 pages:     BasePath.admin   + '/pages/space-management.html' 等
 *    - 登录页:           AppConfig.loginUrl
 *    - 角色判断:         Utils.getUser().role === 'STUDENT' / 'ADMIN'
 *
 * 10. 座位操作接口（4个）的参数约定：
 *     checkin / checkout / temporaryLeave / returnSeat 全都用 JSON Body：
 *         { roomId: number, seatCode: string }     ← 不是 reservationId！
 * ===========================================================
 */
(function () {
  'use strict';

  var Request = {};

  /** 请求超时时间（毫秒） */
  var TIMEOUT = 15000;

  /**
   * 发起请求
   * @param {string} method  - HTTP 方法
   * @param {string} url     - 接口路径（如 /auth/login，不需要 /api 前缀）
   * @param {object} [data]  - 请求参数：GET => query params；其他 => JSON Body（FormData 特殊处理）
   * @returns {Promise}      - resolve 时返回解包后的 data 字段（不是 Result 外层！）
   */
  function request(method, url, data) {
    // Mock 模式下由各 API 模块自行处理，不会走到这里
    var fullUrl = AppConfig.apiBaseUrl + url;
    var options = {
      method: method,
      headers: {
        'Content-Type': 'application/json',
      },
    };

    // 注入 Token（登录/注册接口不需要）
    if (!url.startsWith('/auth/login') && !url.startsWith('/auth/register')) {
      var token = Utils.getToken();
      if (token) {
        options.headers['Authorization'] = 'Bearer ' + token;
      }
    }

    // 处理请求参数
    if (method === 'GET') {
      if (data) {
        var query = buildQuery(data);
        if (query) fullUrl += '?' + query;
      }
    } else if (data instanceof FormData) {
      // 文件上传：让浏览器自动设置 Content-Type: multipart/form-data + boundary
      options.body = data;
      delete options.headers['Content-Type'];
    } else if (data !== undefined && data !== null) {
      // JSON Body（login/register 现在在 auth.js 里用 query 拼接，不会走到这传 data）
      options.body = JSON.stringify(data);
    }

    return new Promise(function (resolve, reject) {
      var timer = setTimeout(function () {
        reject({ code: 0, message: '请求超时，请稍后重试' });
      }, TIMEOUT);

      fetch(fullUrl, options)
        .then(function (response) {
          clearTimeout(timer);
          // 处理非 JSON 响应（如文件下载）
          var contentType = response.headers.get('content-type');
          if (contentType && contentType.indexOf('application/json') === -1) {
            return response.blob().then(function (blob) {
              return { blob: blob, filename: getFilename(response) };
            });
          }
          return response.json();
        })
        .then(function (result) {
          // 文件下载
          if (result && result.blob) {
            resolve(result);
            return;
          }
          // 统一响应解包
          if (result.code === 200) {
            resolve(result.data);
          } else if (result.code === 401) {
            // token 过期或无效：自动清状态并跳登录
            Utils.clearToken();
            Utils.clearUser();
            window.location.href = AppConfig.loginUrl;
            reject(result);
          } else {
            reject(result);
          }
        })
        .catch(function (err) {
          clearTimeout(timer);
          if (err && err.code !== undefined) {
            reject(err);
          } else {
            reject({ code: 0, message: '网络错误，请检查网络连接' });
          }
        });
    });
  }

  /** 构建 query string（会自动过滤 undefined / null / '' 空值） */
  function buildQuery(params) {
    var parts = [];
    for (var key in params) {
      if (params.hasOwnProperty(key) && params[key] !== undefined && params[key] !== null && params[key] !== '') {
        parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
      }
    }
    return parts.join('&');
  }

  /** 从 Content-Disposition 响应头提取附件文件名 */
  function getFilename(response) {
    var disposition = response.headers.get('content-disposition');
    if (disposition) {
      var match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (match && match[1]) return match[1].replace(/['"]/g, '');
    }
    return 'download.xlsx';
  }

  // 公开方法（直接对应 HTTP 动词）
  Request.get    = function (url, params)   { return request('GET',    url, params);   };
  Request.post   = function (url, data)     { return request('POST',   url, data);     };
  Request.put    = function (url, data)     { return request('PUT',    url, data);     };
  Request.patch  = function (url, data)     { return request('PATCH',  url, data);     };
  Request.del    = function (url)           { return request('DELETE', url);           };
  Request.upload = function (url, formData) { return request('POST',   url, formData); };

  window.Request = Request;
})();
