/**
 * API 模块 - 认证
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var AuthAPI = {};

  /** 登录成功后的统一处理 */
  function handleLoginSuccess(data) {
    Utils.setToken(data.token);
    Utils.setUser(data.user);
    // 根据角色跳转
    var role = data.user.role;
    if (role === 'STUDENT') {
      window.location.href = BasePath.student + '/pages/home.html';
    } else {
      window.location.href = BasePath.admin + '/pages/space-management.html';
    }
  }

  /** 用户名密码登录 */
  AuthAPI.login = function (username, password) {
    if (AppConfig.useMock) return MockAuth.login(username, password).then(function (res) { handleLoginSuccess(res.data); return res; });
    return Request.post('/auth/login', { username: username, password: password }).then(function (data) { handleLoginSuccess(data); return data; });
  };

  /** CAS 统一认证登录 */
  AuthAPI.casLogin = function (ticket) {
    if (AppConfig.useMock) return MockAuth.casLogin(ticket).then(function (res) { handleLoginSuccess(res.data); return res; });
    return Request.post('/auth/cas', { ticket: ticket }).then(function (data) { handleLoginSuccess(data); return data; });
  };

  /** 登出 */
  AuthAPI.logout = function () {
    var p = AppConfig.useMock ? MockAuth.logout() : Request.post('/auth/logout');
    return p.then(function () {
      Utils.clearToken();
      Utils.clearUser();
      window.location.href = AppConfig.loginUrl;
    });
  };

  /** 获取当前用户信息 */
  AuthAPI.getCurrentUser = function () {
    if (AppConfig.useMock) return MockAuth.getCurrentUser().then(function (res) { return res.data; });
    return Request.get('/auth/me');
  };

  window.AuthAPI = AuthAPI;
})();
