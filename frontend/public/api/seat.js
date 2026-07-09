/**
 * API 模块 - 座位管控（签到签退、黑名单、爽约记录）
 * 高校自习室智能预约系统
 *
 * @typedef {Object} CheckinRequest
 * @property {number} roomId    - 自习室ID (必填)
 * @property {string} seatCode  - 座位编号 (必填，如 "A-01")
 *
 * @typedef {Object} SeatActionRequest
 * @property {number} roomId    - 自习室ID (必填)
 * @property {string} seatCode  - 座位编号 (必填)
 *
 * @typedef {Object} CheckinVO
 * @property {number} reservationId       - 预约ID
 * @property {string} seatCode            - 座位编号
 * @property {number} roomId              - 自习室ID
 * @property {string} roomName            - 自习室名称
 * @property {string} checkinTime         - 签到时间
 * @property {string} [checkoutTime]      - 签退时间
 * @property {string} [temporaryLeaveTime] - 暂离开始时间
 * @property {string} startTime           - 预约开始时间
 * @property {string} endTime             - 预约结束时间
 * @property {string} status              - 状态：CHECKED_IN已签到 TEMPORARY_LEAVE暂离 COMPLETED已完成
 *
 * @typedef {Object} BlacklistRequest
 * @property {number} userId   - 用户ID (必填)
 * @property {string} reason   - 加入原因 (必填)
 * @property {string} endTime  - 黑名单结束时间 (必填，yyyy-MM-dd HH:mm:ss)
 *
 * @typedef {Object} BlacklistVO
 * @property {number} id            - 黑名单ID
 * @property {number} userId        - 用户ID
 * @property {string} username      - 用户名/学号
 * @property {string} realName      - 真实姓名
 * @property {string} reason        - 加入原因
 * @property {number} noShowCount   - 累计爽约次数
 * @property {string} startTime     - 黑名单开始时间
 * @property {string} endTime       - 黑名单结束时间
 * @property {number} status        - 状态：0-已解除 1-生效中
 * @property {string} createdAt     - 创建时间
 *
 * @typedef {Object} NoShowRecordVO
 * @property {number} id            - 记录ID
 * @property {number} userId        - 用户ID
 * @property {string} username      - 用户名/学号
 * @property {string} realName      - 真实姓名
 * @property {number} reservationId - 预约ID
 * @property {string} seatCode      - 座位编号
 * @property {string} roomName      - 自习室名称
 * @property {string} reason        - 爽约原因：NO_CHECKIN-未签到 TEMPORARY_LEAVE_TIMEOUT-暂离超时
 * @property {string} recordDate    - 记录日期
 * @property {string} createdAt     - 创建时间
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

  var SeatAPI = {};

  // ===== 签到签退 =====
  /**
   * 学生签到（在预约时段内到达自习室后签到）
   * @param {CheckinRequest} data
   * @returns {Promise<CheckinVO>}
   */
  SeatAPI.checkin = function (data) { return Request.post('/checkin', data); };

  /**
   * 学生签退（离开自习室时签退）
   * @param {SeatActionRequest} data
   * @returns {Promise<CheckinVO>}
   */
  SeatAPI.checkout = function (data) { return Request.post('/checkout', data); };

  /**
   * 暂离（暂时离开座位，座位将保留一段时间）
   * @param {SeatActionRequest} data
   * @returns {Promise<CheckinVO>}
   */
  SeatAPI.temporaryLeave = function (data) { return Request.post('/temporary-leave', data); };

  /**
   * 返回座位（暂离后返回座位）
   * @param {SeatActionRequest} data
   * @returns {Promise<CheckinVO>}
   */
  SeatAPI.returnSeat = function (data) { return Request.post('/return-seat', data); };

  // ===== 黑名单 =====
  /**
   * 分页查询黑名单列表
   * @param {Object} [params]
   * @param {number} [params.pageNum=1]   页码
   * @param {number} [params.pageSize=20] 每页条数
   * @param {string} [params.keyword]     关键词（原因搜索）
   * @param {number} [params.status]      状态：0-已解除 1-生效中
   * @returns {Promise<Page<BlacklistVO>>}
   */
  SeatAPI.getBlacklist = function (params) { return Request.get('/blacklist', params); };

  /**
   * 获取黑名单详情
   * @param {number} id 黑名单ID (必填)
   * @returns {Promise<BlacklistVO>}
   */
  SeatAPI.getBlacklistDetail = function (id) { return Request.get('/blacklist/' + id); };

  /**
   * 手动将用户加入黑名单
   * @param {BlacklistRequest} data
   * @returns {Promise<BlacklistVO>}
   */
  SeatAPI.addToBlacklist = function (data) { return Request.post('/blacklist', data); };

  /**
   * 移出黑名单（解除黑名单）
   * @param {number} id 黑名单ID (必填)
   * @returns {Promise<void>}
   */
  SeatAPI.removeFromBlacklist = function (id) { return Request.del('/blacklist/' + id); };

  /**
   * 查看当前登录用户的黑名单状态
   * @returns {Promise<BlacklistVO>}
   */
  SeatAPI.getMyBlacklistStatus = function () { return Request.get('/blacklist/my'); };

  // ===== 爽约记录 =====
  /**
   * 分页查询所有爽约记录（管理员）
   * @param {Object} [params]
   * @param {number} [params.pageNum=1]   页码
   * @param {number} [params.pageSize=20] 每页条数
   * @param {number} [params.userId]      用户ID（筛选）
   * @param {string} [params.startDate]   开始日期（yyyy-MM-dd）
   * @param {string} [params.endDate]     结束日期（yyyy-MM-dd）
   * @returns {Promise<Page<NoShowRecordVO>>}
   */
  SeatAPI.getNoShowRecords = function (params) { return Request.get('/no-show-records', params); };

  /**
   * 查看当前登录学生的爽约记录
   * @param {Object} [params]
   * @param {number} [params.pageNum=1]   页码
   * @param {number} [params.pageSize=20] 每页条数
   * @returns {Promise<Page<NoShowRecordVO>>}
   */
  SeatAPI.getMyNoShowRecords = function (params) { return Request.get('/no-show-records/my', params); };

  window.SeatAPI = SeatAPI;
})();
