/**
 * API 模块 - 用户管理
 * 高校自习室智能预约系统
 *
 * @typedef {Object} UserVO
 * @property {number} id          - 用户ID
 * @property {string} username    - 用户名/学号
 * @property {string} realName    - 真实姓名
 * @property {string} role        - 角色：STUDENT / ADMIN / SUPER_ADMIN
 * @property {string} [email]     - 邮箱
 * @property {string} [phone]     - 手机号
 * @property {string} [avatar]    - 头像URL
 * @property {number} status      - 状态：0-禁用，1-正常
 * @property {string} createdAt   - 创建时间 (yyyy-MM-dd HH:mm:ss)
 *
 * @typedef {Object} CreateUserRequest
 * @property {string} username  - 用户名/学号 (必填，4-50字符)
 * @property {string} password  - 密码 (必填，6-100字符)
 * @property {string} realName  - 真实姓名 (必填)
 * @property {string} [email]   - 邮箱
 * @property {string} [phone]   - 手机号
 * @property {string} [role]    - 角色：STUDENT / ADMIN / SUPER_ADMIN
 *
 * @typedef {Object} UpdateUserRequest
 * @property {string} [realName] - 真实姓名
 * @property {string} [email]    - 邮箱
 * @property {string} [phone]    - 手机号
 * @property {string} [avatar]   - 头像URL
 * @property {number} [status]   - 状态：0-禁用，1-正常
 * @property {string} [role]     - 角色（仅管理员可修改）
 *
 * @typedef {Object} ChangePasswordRequest
 * @property {string} [oldPassword] - 旧密码（本人修改时必填）
 * @property {string} newPassword   - 新密码 (必填，6-100字符)
 *
 * @template T
 * @typedef {Object} Page
 * @property {T[]}    records - 数据列表
 * @property {number} total   - 总条数
 * @property {number} size    - 每页大小
 * @property {number} current - 当前页码
 * @property {number} pages   - 总页数
 */
(function () {
  'use strict';

  var UserAPI = {};

  /**
   * 分页查询用户列表
   * @param {Object} [params]
   * @param {number} [params.pageNum=1]   页码
   * @param {number} [params.pageSize=20] 每页条数
   * @param {string} [params.keyword]     关键词（用户名/姓名搜索）
   * @param {string} [params.role]        角色筛选：STUDENT/ADMIN/SUPER_ADMIN
   * @param {number} [params.status]      状态：0-禁用，1-正常
   * @returns {Promise<Page<UserVO>>}
   */
  UserAPI.getUsers = function (params) { return Request.get('/users', params); };

  /**
   * 获取用户详情
   * @param {number} id 用户ID (必填)
   * @returns {Promise<UserVO>}
   */
  UserAPI.getUser = function (id) { return Request.get('/users/' + id); };

  /**
   * 新增用户（管理员创建用户）
   * @param {CreateUserRequest} data
   * @returns {Promise<UserVO>}
   */
  UserAPI.createUser = function (data) { return Request.post('/users', data); };

  /**
   * 更新用户信息
   * @param {number} id 用户ID (必填)
   * @param {UpdateUserRequest} data
   * @returns {Promise<UserVO>}
   */
  UserAPI.updateUser = function (id, data) { return Request.put('/users/' + id, data); };

  /**
   * 删除用户（逻辑删除）
   * @param {number} id 用户ID (必填)
   * @returns {Promise<void>}
   */
  UserAPI.deleteUser = function (id) { return Request.del('/users/' + id); };

  /**
   * 修改密码
   * @param {number} id 用户ID (必填)
   * @param {ChangePasswordRequest} data
   * @returns {Promise<void>}
   */
  UserAPI.updatePassword = function (id, data) { return Request.put('/users/' + id + '/password', data); };

  /**
   * 修改用户状态（启用/禁用）
   * @param {number} id     用户ID (必填)
   * @param {number} status 状态：0-禁用，1-正常 (必填)
   * @returns {Promise<void>}
   */
  UserAPI.updateStatus = function (id, status) { return Request.patch('/users/' + id + '/status?status=' + status); };

  window.UserAPI = UserAPI;
})();
