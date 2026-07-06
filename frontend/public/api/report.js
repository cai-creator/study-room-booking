/**
 * API 模块 - 数据报表
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var ReportAPI = {};

  function ok(res) { return res.data; }

  ReportAPI.getUsageRate = function (params) { return AppConfig.useMock ? MockReport.getUsageRate(params).then(ok) : Request.get('/reports/usage-rate', params); };
  ReportAPI.getTimeDistribution = function (params) { return AppConfig.useMock ? MockReport.getTimeDistribution(params).then(ok) : Request.get('/reports/time-distribution', params); };
  ReportAPI.getHotPeriods = function (params) { return AppConfig.useMock ? MockReport.getHotPeriods(params).then(ok) : Request.get('/reports/hot-periods', params); };
  ReportAPI.getNoShowRate = function (params) { return AppConfig.useMock ? MockReport.getNoShowRate(params).then(ok) : Request.get('/reports/no-show-rate', params); };
  ReportAPI.getConversionRate = function (params) { return AppConfig.useMock ? MockReport.getConversionRate(params).then(ok) : Request.get('/reports/conversion-rate', params); };
  ReportAPI.exportReport = function (params) { return AppConfig.useMock ? MockReport.exportReport(params).then(ok) : Request.get('/reports/export', params); };

  window.ReportAPI = ReportAPI;
})();
