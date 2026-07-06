/**
 * API 模块 - 座位管控（签到签退、黑名单、爽约记录）
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var SeatAPI = {};

  function ok(res) { return res.data; }

  // ===== 签到签退 =====
  SeatAPI.checkin = function (data) { return AppConfig.useMock ? MockSeat.checkin(data).then(ok) : Request.post('/checkin', data); };
  SeatAPI.checkout = function (data) { return AppConfig.useMock ? MockSeat.checkout(data).then(ok) : Request.post('/checkout', data); };
  SeatAPI.temporaryLeave = function (data) { return AppConfig.useMock ? MockSeat.temporaryLeave(data).then(ok) : Request.post('/temporary-leave', data); };
  SeatAPI.returnSeat = function (data) { return AppConfig.useMock ? MockSeat.returnSeat(data).then(ok) : Request.post('/return-seat', data); };

  // ===== 黑名单 =====
  SeatAPI.getBlacklist = function (params) { return AppConfig.useMock ? MockSeat.getBlacklist(params).then(ok) : Request.get('/blacklist', params); };
  SeatAPI.getBlacklistDetail = function (id) { return AppConfig.useMock ? MockSeat.getBlacklistDetail(id).then(ok) : Request.get('/blacklist/' + id); };
  SeatAPI.addToBlacklist = function (data) { return AppConfig.useMock ? MockSeat.addToBlacklist(data).then(ok) : Request.post('/blacklist', data); };
  SeatAPI.removeFromBlacklist = function (id) { return AppConfig.useMock ? MockSeat.removeFromBlacklist(id).then(ok) : Request.del('/blacklist/' + id); };
  SeatAPI.getMyBlacklistStatus = function () { return AppConfig.useMock ? MockSeat.getMyBlacklistStatus().then(ok) : Request.get('/blacklist/my'); };

  // ===== 爽约记录 =====
  SeatAPI.getNoShowRecords = function (params) { return AppConfig.useMock ? MockSeat.getNoShowRecords(params).then(ok) : Request.get('/no-show-records', params); };
  SeatAPI.getMyNoShowRecords = function () { return AppConfig.useMock ? MockSeat.getMyNoShowRecords().then(ok) : Request.get('/no-show-records/my'); };

  window.SeatAPI = SeatAPI;
})();
