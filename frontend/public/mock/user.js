/**
 * Mock 数据 - 用户管理模块
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var MockUser = {};

  function delay(ms) { return new Promise(function (r) { setTimeout(r, ms || rand(200, 500)); }); }
  function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
  function ok(data, msg) { return { code: 200, message: msg || '操作成功', data: data, timestamp: Date.now() }; }
  function fail(code, msg) { return { code: code, message: msg, data: null, timestamp: Date.now() }; }

  var users = [
    { id: 1, username: '2024001001', realName: '张三', role: 'STUDENT', status: 1, phone: '13800001001', email: 'zhangsan@example.edu.cn', createdAt: '2026-01-10 09:00:00' },
    { id: 2, username: '2024001002', realName: '李四', role: 'STUDENT', status: 1, phone: '13800001002', email: 'lisi@example.edu.cn', createdAt: '2026-01-10 09:05:00' },
    { id: 3, username: '2024001003', realName: '王五', role: 'STUDENT', status: 1, phone: '13800001003', email: 'wangwu@example.edu.cn', createdAt: '2026-01-10 09:10:00' },
    { id: 4, username: '2024001004', realName: '赵六', role: 'STUDENT', status: 0, phone: '13800001004', email: 'zhaoliu@example.edu.cn', createdAt: '2026-01-12 10:00:00' },
    { id: 5, username: '2024001005', realName: '钱七', role: 'STUDENT', status: 1, phone: '13800001005', email: 'qianqi@example.edu.cn', createdAt: '2026-01-15 14:00:00' },
    { id: 6, username: '2024001006', realName: '孙八', role: 'STUDENT', status: 1, phone: '13800001006', email: 'sunba@example.edu.cn', createdAt: '2026-01-20 08:30:00' },
    { id: 7, username: '2024001007', realName: '周九', role: 'STUDENT', status: 1, phone: '13800001007', email: 'zhoujiu@example.edu.cn', createdAt: '2026-02-01 11:00:00' },
    { id: 8, username: '2024001008', realName: '吴十', role: 'STUDENT', status: 0, phone: '13800001008', email: 'wushi@example.edu.cn', createdAt: '2026-02-05 16:20:00' },
    { id: 9, username: 'admin', realName: '李管理', role: 'ADMIN', status: 1, phone: '13900000001', email: 'admin@example.edu.cn', createdAt: '2025-12-01 00:00:00' },
    { id: 10, username: 'superadmin', realName: '王超级', role: 'SUPER_ADMIN', status: 1, phone: '13900000002', email: 'superadmin@example.edu.cn', createdAt: '2025-12-01 00:00:00' },
    { id: 11, username: '2024001009', realName: '陈十一', role: 'STUDENT', status: 1, phone: '13800001009', email: 'chen11@example.edu.cn', createdAt: '2026-03-01 09:00:00' },
    { id: 12, username: '2024001010', realName: '刘十二', role: 'STUDENT', status: 1, phone: '13800001010', email: 'liu12@example.edu.cn', createdAt: '2026-03-10 10:30:00' },
  ];
  var nextId = 13;

  /** 用户列表 */
  MockUser.getUsers = function (params) {
    return delay().then(function () {
      var list = users.slice();
      if (params && params.keyword) list = list.filter(function (u) { return u.username.indexOf(params.keyword) !== -1 || u.realName.indexOf(params.keyword) !== -1; });
      if (params && params.role) list = list.filter(function (u) { return u.role === params.role; });
      if (params && params.status !== undefined && params.status !== '') list = list.filter(function (u) { return u.status === Number(params.status); });
      var pageNum = Number((params && params.pageNum)) || 1;
      var pageSize = Number((params && params.pageSize)) || 20;
      var total = list.length;
      return ok({ list: list.slice((pageNum - 1) * pageSize, pageNum * pageSize), total: total, pageNum: pageNum, pageSize: pageSize });
    });
  };

  MockUser.getUser = function (id) { return delay().then(function () { return ok(users.find(function (u) { return u.id === Number(id); }) || null); }); };
  MockUser.createUser = function (data) { return delay().then(function () { var u = Object.assign({}, data, { id: nextId++, createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19) }); users.unshift(u); return ok(u, '创建成功'); }); };
  MockUser.updateUser = function (id, data) { return delay().then(function () { var idx = users.findIndex(function (u) { return u.id === Number(id); }); if (idx >= 0) Object.assign(users[idx], data); return ok(users[idx], '更新成功'); }); };
  MockUser.deleteUser = function (id) { return delay().then(function () { users = users.filter(function (u) { return u.id !== Number(id); }); return ok(null, '删除成功'); }); };
  MockUser.updatePassword = function (id, data) { return delay().then(function () { return ok(null, '密码修改成功'); }); };
  MockUser.updateStatus = function (id, status) { return delay().then(function () { var u = users.find(function (u) { return u.id === Number(id); }); if (u) u.status = status; return ok(null, '状态已更新'); }); };

  window.MockUser = MockUser;
})();
