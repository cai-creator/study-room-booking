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

  // 模拟已注册用户表，用来校验用户名重复
  var registeredUsernames = {
    '2024001001': true,
    'admin': true,
    'superadmin': true,
  };

  /** 注册 */
  MockAuth.register = function (data) {
    return delay().then(function () {
      if (!data) return fail(400, '参数不能为空');
      if (!data.username) return fail(400, '用户名不能为空');
      if (data.username.length < 3) return fail(400, '用户名至少需要3个字符');
      if (!data.realName) return fail(400, '真实姓名不能为空');
      if (!data.password) return fail(400, '密码不能为空');
      if (data.password.length < 6) return fail(400, '密码至少需要6位');
      if (data.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) return fail(400, '邮箱格式不正确');
      if (data.phone && !/^1\d{10}$/.test(data.phone)) return fail(400, '手机号格式不正确');
      if (registeredUsernames[data.username]) return fail(409, '该用户名/学号已被注册，请直接登录或更换账号');
      registeredUsernames[data.username] = true;
      var newUser = {
        id: Date.now(),
        username: data.username,
        realName: data.realName || data.username,
        role: 'STUDENT',
        email: data.email || null,
        phone: data.phone || null,
      };
      return ok(newUser, '注册成功');
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
