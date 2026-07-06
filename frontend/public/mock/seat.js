/**
 * Mock 数据 - 座位管控模块（签到签退、黑名单、爽约）
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var MockSeat = {};

  function delay(ms) { return new Promise(function (r) { setTimeout(r, ms || rand(200, 500)); }); }
  function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
  function ok(data, msg) { return { code: 200, message: msg || '操作成功', data: data, timestamp: Date.now() }; }
  function fail(code, msg) { return { code: code, message: msg, data: null, timestamp: Date.now() }; }

  /** 签到 */
  MockSeat.checkin = function (data) {
    return delay().then(function () {
      if (!data.seatCode) return fail(4001, '签到信息不完整');
      return ok({
        reservationId: 1, seatCode: data.seatCode,
        checkinTime: new Date().toISOString().replace('T', ' ').slice(0, 19),
        endTime: '2026-07-05 12:00:00', status: 'CHECKED_IN',
      }, '签到成功');
    });
  };

  /** 签退 */
  MockSeat.checkout = function () {
    return delay().then(function () {
      return ok({ status: 'CHECKED_OUT', checkoutTime: new Date().toISOString().replace('T', ' ').slice(0, 19) }, '签退成功');
    });
  };

  /** 暂离 */
  MockSeat.temporaryLeave = function () {
    return delay().then(function () {
      return ok({ status: 'TEMPORARY_LEAVE', leaveTime: new Date().toISOString().replace('T', ' ').slice(0, 19) }, '已暂离');
    });
  };

  /** 返回 */
  MockSeat.returnSeat = function () {
    return delay().then(function () {
      return ok({ status: 'CHECKED_IN', returnTime: new Date().toISOString().replace('T', ' ').slice(0, 19) }, '已返回座位');
    });
  };

  // ===== 黑名单 =====
  var blacklist = [
    { id: 1, userId: 10, username: '2024001015', realName: '赵七', reason: '累计爽约3次，系统自动加入', startTime: '2026-07-01 00:00:00', endTime: '2026-07-08 00:00:00', status: 1, operatorName: '系统' },
    { id: 2, userId: 11, username: '2024001020', realName: '钱八', reason: '恶意占座', startTime: '2026-07-02 00:00:00', endTime: '2026-07-16 00:00:00', status: 1, operatorName: '李管理' },
    { id: 3, userId: 12, username: '2024001025', realName: '孙九', reason: '扰乱自习室秩序', startTime: '2026-06-20 00:00:00', endTime: '2026-07-05 00:00:00', status: 0, operatorName: '李管理' },
  ];

  MockSeat.getBlacklist = function (params) {
    return delay().then(function () {
      var list = blacklist.slice();
      if (params && params.keyword) list = list.filter(function (b) { return b.username.indexOf(params.keyword) !== -1 || b.realName.indexOf(params.keyword) !== -1; });
      if (params && params.status !== undefined) list = list.filter(function (b) { return b.status === Number(params.status); });
      var pageNum = Number((params && params.pageNum)) || 1;
      var pageSize = Number((params && params.pageSize)) || 20;
      var total = list.length;
      return ok({ list: list.slice((pageNum - 1) * pageSize, pageNum * pageSize), total: total, pageNum: pageNum, pageSize: pageSize });
    });
  };

  MockSeat.getBlacklistDetail = function (id) { return delay().then(function () { return ok(blacklist.find(function (b) { return b.id === Number(id); }) || null); }); };
  MockSeat.addToBlacklist = function (data) { return delay().then(function () { return ok({ id: blacklist.length + 1 }, '已加入黑名单'); }); };
  MockSeat.removeFromBlacklist = function (id) { return delay().then(function () { return ok(null, '已移出黑名单'); }); };
  MockSeat.getMyBlacklistStatus = function () { return delay().then(function () { return ok({ inBlacklist: false, record: null }); }); };

  // ===== 爽约记录 =====
  var noShowRecords = [
    { id: 1, userId: 10, username: '2024001015', realName: '赵七', reservationId: 20, roomName: '图书馆101自习室', seatCode: 'C-03', startTime: '2026-07-01 09:00:00', endTime: '2026-07-01 12:00:00', reason: '超时未签到', recordDate: '2026-07-01' },
    { id: 2, userId: 10, username: '2024001015', realName: '赵七', reservationId: 21, roomName: '图书馆101自习室', seatCode: 'D-01', startTime: '2026-07-02 14:00:00', endTime: '2026-07-02 17:00:00', reason: '超时未签到', recordDate: '2026-07-02' },
  ];

  MockSeat.getNoShowRecords = function (params) {
    return delay().then(function () {
      var list = noShowRecords.slice();
      var pageNum = Number((params && params.pageNum)) || 1;
      var pageSize = Number((params && params.pageSize)) || 20;
      return ok({ list: list, total: list.length, pageNum: pageNum, pageSize: pageSize });
    });
  };
  MockSeat.getMyNoShowRecords = function () { return delay().then(function () { return ok(noShowRecords.slice(0, 1)); }); };

  window.MockSeat = MockSeat;
})();
