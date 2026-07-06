/**
 * Mock 数据 - 实时看板模块
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var MockDashboard = {};

  function delay(ms) { return new Promise(function (r) { setTimeout(r, ms || rand(200, 500)); }); }
  function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
  function ok(data, msg) { return { code: 200, message: msg || '操作成功', data: data, timestamp: Date.now() }; }

  /** 校区使用概览 */
  MockDashboard.getCampusOverview = function () {
    return delay().then(function () {
      return ok([
        { campusId: 1, campusName: '主校区', totalRooms: 4, totalSeats: 210, availableSeats: Math.floor(Math.random() * 80 + 60), usageRate: Math.floor(Math.random() * 30 + 40) },
        { campusId: 2, campusName: '东校区', totalRooms: 1, totalSeats: 50, availableSeats: Math.floor(Math.random() * 20 + 15), usageRate: Math.floor(Math.random() * 30 + 30) },
      ]);
    });
  };

  /** 楼栋概览 */
  MockDashboard.getBuildingOverview = function () {
    return delay().then(function () {
      return ok([
        { buildingId: 1, buildingName: '图书馆', campusName: '主校区', totalRooms: 3, totalSeats: 130, availableSeats: rand(30, 60), usageRate: rand(40, 70) },
        { buildingId: 2, buildingName: '教学楼A座', campusName: '主校区', totalRooms: 1, totalSeats: 80, availableSeats: rand(20, 40), usageRate: rand(30, 60) },
        { buildingId: 3, buildingName: '综合楼', campusName: '东校区', totalRooms: 1, totalSeats: 50, availableSeats: rand(10, 30), usageRate: rand(35, 65) },
      ]);
    });
  };

  /** 自习室实时详情 */
  MockDashboard.getRoomDetail = function (roomId) {
    return delay().then(function () {
      var seatStatuses = ['AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'RESERVED', 'OCCUPIED', 'TEMPORARY_LEAVE', 'UNAVAILABLE'];
      var seats = [];
      var labels = 'ABCDEFGH';
      for (var r = 0; r < 8; r++) {
        for (var c = 1; c <= 8; c++) {
          seats.push({
            seatId: r * 8 + c,
            seatCode: labels.charAt(r) + '-' + String(c).padStart(2, '0'),
            rowNumber: r + 1, colNumber: c,
            status: seatStatuses[Math.floor(Math.random() * seatStatuses.length)],
            tags: c === 1 || c === 8 ? ['WINDOW'] : [],
          });
        }
      }
      var counts = { AVAILABLE: 0, RESERVED: 0, OCCUPIED: 0, TEMPORARY_LEAVE: 0, UNAVAILABLE: 0 };
      seats.forEach(function (s) { counts[s.status] = (counts[s.status] || 0) + 1; });
      return ok({
        roomId: Number(roomId) || 1, roomName: '图书馆101自习室',
        totalSeats: 64, availableSeats: counts.AVAILABLE,
        reservedSeats: counts.RESERVED, occupiedSeats: counts.OCCUPIED + counts.TEMPORARY_LEAVE,
        seats: seats,
      });
    });
  };

  window.MockDashboard = MockDashboard;
})();
