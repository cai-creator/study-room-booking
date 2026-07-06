/**
 * Mock 数据 - 数据报表模块
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var MockReport = {};

  function delay(ms) { return new Promise(function (r) { setTimeout(r, ms || rand(200, 500)); }); }
  function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
  function ok(data, msg) { return { code: 200, message: msg || '操作成功', data: data, timestamp: Date.now() }; }

  /** 日均使用率 */
  MockReport.getUsageRate = function (params) {
    return delay().then(function () {
      var days = [];
      for (var i = 6; i >= 0; i--) {
        var d = new Date(); d.setDate(d.getDate() - i);
        var ds = d.toISOString().slice(0, 10);
        days.push({ date: ds, usageRate: rand(35, 85), totalSeats: 260, usedSeats: rand(90, 220) });
      }
      return ok({ list: days, averageRate: Math.round(days.reduce(function (s, d) { return s + d.usageRate; }, 0) / days.length) });
    });
  };

  /** 时段分布 */
  MockReport.getTimeDistribution = function (params) {
    return delay().then(function () {
      var hours = [];
      for (var h = 6; h <= 22; h++) {
        var rate = h < 8 ? rand(5, 15) : h < 12 ? rand(40, 75) : h < 14 ? rand(20, 40) : h < 18 ? rand(50, 85) : h < 21 ? rand(60, 90) : rand(10, 30);
        hours.push({ hour: h, label: String(h).padStart(2, '0') + ':00', usageRate: rate, userCount: Math.round(rate / 100 * 260) });
      }
      return ok(hours);
    });
  };

  /** 热门时段 */
  MockReport.getHotPeriods = function (params) {
    return delay().then(function () {
      return ok([
        { rank: 1, period: '18:00-21:00', avgUsageRate: 82, totalReservations: 1456 },
        { rank: 2, period: '14:00-17:00', avgUsageRate: 76, totalReservations: 1320 },
        { rank: 3, period: '09:00-12:00', avgUsageRate: 71, totalReservations: 1180 },
        { rank: 4, period: '12:00-14:00', avgUsageRate: 38, totalReservations: 540 },
        { rank: 5, period: '06:00-09:00', avgUsageRate: 18, totalReservations: 210 },
      ]);
    });
  };

  /** 爽约率 */
  MockReport.getNoShowRate = function (params) {
    return delay().then(function () {
      return ok({
        totalReservations: 4520,
        noShowCount: 135,
        noShowRate: 2.99,
        trend: [
          { month: '2026-01', rate: 3.2 }, { month: '2026-02', rate: 2.8 },
          { month: '2026-03', rate: 3.5 }, { month: '2026-04', rate: 3.1 },
          { month: '2026-05', rate: 2.7 }, { month: '2026-06', rate: 2.99 },
        ],
      });
    });
  };

  /** 预约转化率 */
  MockReport.getConversionRate = function (params) {
    return delay().then(function () {
      return ok({
        totalReservations: 4520,
        checkedIn: 3980,
        conversionRate: 88.1,
        trend: [
          { month: '2026-01', rate: 85.2 }, { month: '2026-02', rate: 86.7 },
          { month: '2026-03', rate: 87.3 }, { month: '2026-04', rate: 88.9 },
          { month: '2026-05', rate: 87.5 }, { month: '2026-06', rate: 88.1 },
        ],
      });
    });
  };

  /** 导出报表（Mock返回提示） */
  MockReport.exportReport = function (params) {
    return delay().then(function () { return ok({ message: 'Mock模式下不提供文件下载，请连接真实API' }); });
  };

  window.MockReport = MockReport;
})();
