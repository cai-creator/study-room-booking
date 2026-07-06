/**
 * Mock 数据 - 预约模块
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var MockReservation = {};

  function delay(ms) { return new Promise(function (r) { setTimeout(r, ms || rand(200, 500)); }); }
  function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
  function ok(data, msg) { return { code: 200, message: msg || '操作成功', data: data, timestamp: Date.now() }; }
  function fail(code, msg) { return { code: code, message: msg, data: null, timestamp: Date.now() }; }

  var nextId = 100;

  var reservations = [
    { id: 1, seatId: 1, seatCode: 'A-01', roomId: 1, roomName: '图书馆101自习室', buildingName: '图书馆', campusName: '主校区',
      startTime: '2026-07-05 09:00:00', endTime: '2026-07-05 12:00:00', status: 'CHECKED_IN', checkinCode: 'QR202607050001', createdAt: '2026-07-04 20:30:00' },
    { id: 2, seatId: 3, seatCode: 'A-03', roomId: 1, roomName: '图书馆101自习室', buildingName: '图书馆', campusName: '主校区',
      startTime: '2026-07-05 14:00:00', endTime: '2026-07-05 18:00:00', status: 'RESERVED', checkinCode: 'QR202607050002', createdAt: '2026-07-05 10:00:00' },
    { id: 3, seatId: 10, seatCode: 'B-02', roomId: 2, roomName: '图书馆201阅览室', buildingName: '图书馆', campusName: '主校区',
      startTime: '2026-07-04 08:00:00', endTime: '2026-07-04 11:30:00', status: 'CHECKED_OUT', checkinCode: 'QR202607040001', createdAt: '2026-07-03 22:00:00' },
    { id: 4, seatId: 22, seatCode: 'C-06', roomId: 4, roomName: '教A101自习室', buildingName: '教学楼A座', campusName: '主校区',
      startTime: '2026-07-03 10:00:00', endTime: '2026-07-03 12:00:00', status: 'NO_SHOW', checkinCode: 'QR202607030001', createdAt: '2026-07-02 15:00:00' },
    { id: 5, seatId: 35, seatCode: 'E-03', roomId: 5, roomName: '综合楼101自习室', buildingName: '综合楼', campusName: '东校区',
      startTime: '2026-07-06 09:00:00', endTime: '2026-07-06 17:00:00', status: 'RESERVED', checkinCode: 'QR202607060001', createdAt: '2026-07-05 18:00:00' },
  ];

  /** 创建预约 */
  MockReservation.createReservation = function (data) {
    return delay().then(function () {
      if (!data.seatId || !data.startTime || !data.endTime) return fail(400, '请完善预约信息');
      var r = {
        id: ++nextId, seatId: data.seatId, seatCode: 'A-05', roomId: 1, roomName: '图书馆101自习室',
        buildingName: '图书馆', campusName: '主校区',
        startTime: data.startTime, endTime: data.endTime, status: 'RESERVED',
        checkinCode: 'QR' + new Date().toISOString().replace(/\D/g, '').slice(0, 14),
        createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19),
      };
      reservations.unshift(r);
      return ok(r, '预约成功');
    });
  };

  /** 取消预约 */
  MockReservation.cancelReservation = function (id) {
    return delay().then(function () {
      var r = reservations.find(function (r) { return r.id === Number(id); });
      if (r) r.status = 'CANCELLED';
      return ok(null, '取消成功');
    });
  };

  /** 我的预约 */
  MockReservation.getMyReservations = function (params) {
    return delay().then(function () {
      var list = reservations.slice();
      if (params && params.status) list = list.filter(function (r) { return r.status === params.status; });
      if (params && params.date) list = list.filter(function (r) { return r.startTime.indexOf(params.date) === 0; });
      list.sort(function (a, b) { return b.id - a.id; });
      var pageNum = Number((params && params.pageNum)) || 1;
      var pageSize = Number((params && params.pageSize)) || 20;
      var total = list.length;
      var start = (pageNum - 1) * pageSize;
      return ok({ list: list.slice(start, start + pageSize), total: total, pageNum: pageNum, pageSize: pageSize });
    });
  };

  /** 预约详情 */
  MockReservation.getReservationDetail = function (id) {
    return delay().then(function () { return ok(reservations.find(function (r) { return r.id === Number(id); }) || null); });
  };

  /** 全部预约（管理员） */
  MockReservation.getReservations = function (params) {
    return MockReservation.getMyReservations(params);
  };

  /** 可用时段 */
  MockReservation.getAvailableSlots = function (seatId, date) {
    return delay().then(function () {
      return ok([
        { startTime: (date || '2026-07-05') + ' 08:00:00', endTime: (date || '2026-07-05') + ' 10:00:00' },
        { startTime: (date || '2026-07-05') + ' 10:00:00', endTime: (date || '2026-07-05') + ' 12:00:00' },
        { startTime: (date || '2026-07-05') + ' 13:00:00', endTime: (date || '2026-07-05') + ' 15:00:00' },
        { startTime: (date || '2026-07-05') + ' 15:00:00', endTime: (date || '2026-07-05') + ' 17:00:00' },
        { startTime: (date || '2026-07-05') + ' 18:00:00', endTime: (date || '2026-07-05') + ' 21:00:00' },
      ]);
    });
  };

  window.MockReservation = MockReservation;
})();
