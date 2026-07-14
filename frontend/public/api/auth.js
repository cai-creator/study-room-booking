/**
 * API 模块 - 认证
 * 高校自习室智能预约系统
 *
 * @typedef {Object} UserVO
 * @property {number} id          - 用户ID
 * @property {string} username    - 用户名/学号，如 "2024001001"
 * @property {string} realName    - 真实姓名，如 "张三"
 * @property {string} role        - 角色：STUDENT / ADMIN / SUPER_ADMIN
 * @property {string} [email]     - 邮箱
 * @property {string} [phone]     - 手机号
 * @property {string} [avatar]    - 头像URL
 * @property {number} status      - 状态：0-禁用，1-正常
 * @property {string} createdAt   - 创建时间 (yyyy-MM-dd HH:mm:ss)
 *
 * @typedef {Object} LoginVO
 * @property {string}  token             - JWT访问令牌
 * @property {string}  refreshToken      - 刷新令牌
 * @property {number}  expireAt          - 访问令牌过期时间戳（毫秒）
 * @property {number}  refreshExpireAt   - 刷新令牌过期时间戳（毫秒）
 * @property {UserVO}  user              - 当前登录用户信息
 */
(function () {
  'use strict';

  var AuthAPI = {};

  /** 登录成功后的统一处理 */
  function handleLoginSuccess(data) {
    Utils.setToken(data.token);
    if (data.refreshToken) {
      Utils.setRefreshToken(data.refreshToken);
    }
    Utils.setUser(data.user);
    var role = data.user.role;
    if (role === 'STUDENT') {
      window.location.href = BasePath.student + '/pages/home.html';
    } else {
      window.location.href = BasePath.admin + '/pages/space-management.html';
    }
  }

  /**
   * 用户名密码登录
   * @param {string} username               用户名/学号 (必填)
   * @param {string} password               密码 (必填)
   * @param {'STUDENT'|'ADMIN'} [expectedRole]  预期登录角色（防串角色登录：学生TAB拒绝管理员，反之亦然）
   * @returns {Promise<LoginVO>}
   */
  AuthAPI.login = function (username, password, expectedRole) {
    return Request.post('/auth/login', { username: username, password: password }).then(function (data) {
      if (expectedRole) {
        var role = data.user && data.user.role;
        var allow = false;
        if (expectedRole === 'STUDENT') {
          allow = role === 'STUDENT';
        } else if (expectedRole === 'ADMIN') {
          allow = role === 'ADMIN' || role === 'SUPER_ADMIN';
        }
        if (!allow) {
          var tabName = expectedRole === 'STUDENT' ? '学生' : '管理员';
          var err = new Error('该账号非' + tabName + '账号，请切换到正确的登录入口');
          err.name = 'RoleMismatchError';
          err.roleMismatch = true;
          err.userRole = role || 'UNKNOWN';
          throw err;
        }
      }
      handleLoginSuccess(data);
      return data;
    });
  };

  /**
   * 用户注册
   * @param {Object} data
   * @param {string} data.username  用户名/学号 (必填)
   * @param {string} data.password  密码 (必填)
   * @param {string} data.realName  真实姓名 (必填)
   * @param {string} [data.email]   邮箱 (可选)
   * @param {string} [data.phone]   手机号 (可选)
   * @returns {Promise<UserVO>}
   */
  AuthAPI.register = function (data) {
    return Request.post('/auth/register', data);
  };

  /**
   * 用户登出
   * <p>通知后端使当前token加入黑名单并失效所有refreshToken，然后清除前端登录态。
   * <p>即使后端调用失败（如token已过期），也强制清除前端登录态并跳登录页。
   * @returns {Promise<void>}
   */
  AuthAPI.logout = function () {
    return Request.post('/auth/logout')
      .then(function () {
        Utils.clearAuth();
        window.location.href = AppConfig.loginUrl;
      })
      .catch(function () {
        // 登出失败（如token已过期被黑名单拦截），仍清除本地登录态
        Utils.clearAuth();
        window.location.href = AppConfig.loginUrl;
      });
  };

  /**
   * 获取当前登录用户信息
   * @returns {Promise<UserVO>}
   */
  AuthAPI.getCurrentUser = function () {
    return Request.get('/auth/me');
  };

  window.AuthAPI = AuthAPI;
})();
