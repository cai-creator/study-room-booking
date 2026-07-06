/**
 * Mock 数据 - 认证模块
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var MockAuth = {};

  function delay(ms) {
    return new Promise(function (resolve) { setTimeout(resolve, ms || rand(200, 500)); });
  }
  function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }

  function ok(data, message) {
    return { code: 200, message: message || '操作成功', data: data, timestamp: Date.now() };
  }
  function fail(code, message) {
    return { code: code, message: message, data: null, timestamp: Date.now() };
  }

  // 模拟用户数据
  var TEST_USERS = {
    student: {
      id: 1, username: '2024001001', realName: '张三', role: 'STUDENT', avatar: null,
    },
    admin: {
      id: 2, username: 'admin', realName: '李管理', role: 'ADMIN', avatar: null,
    },
    superAdmin: {
      id: 3, username: 'superadmin', realName: '王超级', role: 'SUPER_ADMIN', avatar: null,
    },
  };

  /** 用户名密码登录 */
  MockAuth.login = function (username, password) {
    return delay().then(function () {
      if (username === '2024001001' && password === 'password123') {
        return ok({
          token: 'mock-jwt-student-xxxxx',
          expireAt: Date.now() + 86400000,
          user: TEST_USERS.student,
        }, '登录成功');
      }
      if (username === 'admin' && password === 'admin123') {
        return ok({
          token: 'mock-jwt-admin-xxxxx',
          expireAt: Date.now() + 86400000,
          user: TEST_USERS.admin,
        }, '登录成功');
      }
      if (username === 'superadmin' && password === 'super123') {
        return ok({
          token: 'mock-jwt-superadmin-xxxxx',
          expireAt: Date.now() + 86400000,
          user: TEST_USERS.superAdmin,
        }, '登录成功');
      }
      if (!username) return fail(400, '用户名不能为空');
      return fail(1002, '用户名或密码错误');
    });
  };

  /** CAS 登录 */
  MockAuth.casLogin = function (ticket) {
    return delay().then(function () {
      if (!ticket) return fail(400, '缺少CAS认证票据');
      // 模拟默认返回学生身份
      return ok({
        token: 'mock-jwt-cas-student-xxxxx',
        expireAt: Date.now() + 86400000,
        user: TEST_USERS.student,
      }, 'CAS认证成功');
    });
  };

  /** 登出 */
  MockAuth.logout = function () {
    return delay(100).then(function () { return ok(null, '已退出登录'); });
  };

  /** 获取当前用户 */
  MockAuth.getCurrentUser = function () {
    return delay().then(function () {
      var user = Utils.getUser();
      return ok(user || TEST_USERS.student);
    });
  };

  window.MockAuth = MockAuth;
})();
