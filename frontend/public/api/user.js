/**
 * API 模块 - 用户管理
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var UserAPI = {};

  function ok(res) { return res.data; }

  UserAPI.getUsers = function (params) { return AppConfig.useMock ? MockUser.getUsers(params).then(ok) : Request.get('/users', params); };
  UserAPI.getUser = function (id) { return AppConfig.useMock ? MockUser.getUser(id).then(ok) : Request.get('/users/' + id); };
  UserAPI.createUser = function (data) { return AppConfig.useMock ? MockUser.createUser(data).then(ok) : Request.post('/users', data); };
  UserAPI.updateUser = function (id, data) { return AppConfig.useMock ? MockUser.updateUser(id, data).then(ok) : Request.put('/users/' + id, data); };
  UserAPI.deleteUser = function (id) { return AppConfig.useMock ? MockUser.deleteUser(id).then(ok) : Request.del('/users/' + id); };
  UserAPI.updatePassword = function (id, data) { return AppConfig.useMock ? MockUser.updatePassword(id, data).then(ok) : Request.put('/users/' + id + '/password', data); };
  UserAPI.updateStatus = function (id, status) { return AppConfig.useMock ? MockUser.updateStatus(id, status).then(ok) : Request.patch('/users/' + id + '/status?status=' + status); };

  window.UserAPI = UserAPI;
})();
