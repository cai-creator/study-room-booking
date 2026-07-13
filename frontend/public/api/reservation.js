/**
 * API 模块 - 预约核心
 * 高校自习室智能预约系统
 *
 * @typedef {Object} CreateBookingRequest
 * @property {number} seatId    - 座位ID (必填)
 * @property {string} [startTime] - 预约开始时间 (单时段时使用，yyyy-MM-dd HH:mm:ss)
 * @property {string} [endTime]   - 预约结束时间 (单时段时使用，yyyy-MM-dd HH:mm:ss)
 * @property {TimeSlotDTO[]} [timeSlots] - 多时段预约列表 (多时段时使用)
 *
 * @typedef {Object} TimeSlotDTO
 * @property {string} startTime - 时段开始时间 (yyyy-MM-dd HH:mm:ss)
 * @property {string} endTime   - 时段结束时间 (yyyy-MM-dd HH:mm:ss)
 *
 * @typedef {Object} BookingVO
 * @property {number} id            - 预约ID
 * @property {number} seatId        - 座位ID
 * @property {string} seatCode      - 座位编号
 * @property {number} roomId        - 自习室ID
 * @property {string} roomName      - 自习室名称
 * @property {string} buildingName  - 楼栋名称
 * @property {string} campusName    - 校区名称
 * @property {string} startTime     - 开始时间
 * @property {string} endTime       - 结束时间
 * @property {string} status        - 状态：RESERVED已预约 CHECKED_IN已签到 TEMPORARY_LEAVE暂离 COMPLETED已签退 CANCELLED已取消 NO_SHOW爽约
 * @property {string} [checkinCode] - 签到码
 * @property {string} createdAt     - 创建时间
 *
 * @typedef {Object} AvailableSlotVO
 * @property {string} startTime - 时段开始时间
 * @property {string} endTime   - 时段结束时间
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

  var ReservationAPI = {};

  /**
   * 创建预约（学生选择座位和时间段，支持多时段）
   * @param {CreateBookingRequest} data
   * @returns {Promise<BookingVO[]>}
   */
  ReservationAPI.createReservation = function (data) { return Request.post('/reservations', data); };

  /**
   * 取消预约（仅RESERVED状态可取消，开始后不可取消）
   * @param {number} id 预约ID (必填)
   * @returns {Promise<void>}
   */
  ReservationAPI.cancelReservation = function (id) { return Request.post('/reservations/' + id + '/cancel'); };

  /**
   * 分页查询当前用户的预约记录
   * @param {Object} [params]
   * @param {number} [params.pageNum=1]   页码
   * @param {number} [params.pageSize=20] 每页条数
   * @param {string} [params.status]      状态筛选：RESERVED/CHECKED_IN/TEMPORARY_LEAVE/COMPLETED/CANCELLED/NO_SHOW
   * @param {string} [params.date]        日期筛选 (yyyy-MM-dd)
   * @returns {Promise<Page<BookingVO>>}
   */
  ReservationAPI.getMyReservations = function (params) { return Request.get('/reservations/my', params); };

  /**
   * 获取预约详情
   * @param {number} id 预约ID (必填)
   * @returns {Promise<BookingVO>}
   */
  ReservationAPI.getReservationDetail = function (id) { return Request.get('/reservations/' + id); };

  /**
   * 管理员分页查询所有预约记录
   * @param {Object} [params]
   * @param {number} [params.pageNum=1]   页码
   * @param {number} [params.pageSize=20] 每页条数
   * @param {number} [params.userId]      用户ID筛选
   * @param {number} [params.roomId]      自习室ID筛选
   * @param {string} [params.status]      状态筛选
   * @param {string} [params.startDate]   开始日期 (yyyy-MM-dd)
   * @param {string} [params.endDate]     结束日期 (yyyy-MM-dd)
   * @returns {Promise<Page<BookingVO>>}
   */
  ReservationAPI.getReservations = function (params) { return Request.get('/reservations', params); };

  /**
   * 查询座位在某一天的可用空闲时段（按1小时粒度划分）
   * @param {number} seatId 座位ID (必填)
   * @param {string} date   查询日期 (必填，yyyy-MM-dd，默认当天)
   * @returns {Promise<AvailableSlotVO[]>}
   */
  ReservationAPI.getAvailableSlots = function (seatId, date) { return Request.get('/seats/' + seatId + '/available-slots', { date: date }); };

  window.ReservationAPI = ReservationAPI;
})();
