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
  ReportAPI.exportReport = function (params) {
    if (AppConfig.useMock) return MockReport.exportReport(params).then(ok);
    var query = [];
    for (var k in params) {
      if (params[k] !== undefined && params[k] !== null && params[k] !== '') {
        query.push(encodeURIComponent(k) + '=' + encodeURIComponent(params[k]));
      }
    }
    var url = AppConfig.apiBaseUrl + '/reports/export' + (query.length ? '?' + query.join('&') : '');
    var token = Utils.getToken();
    if (token) {
      return fetch(url, { headers: { 'Authorization': 'Bearer ' + token } })
        .then(function (res) { return res.blob().then(function (blob) {
          var disposition = res.headers.get('content-disposition');
          var filename = 'report.xlsx';
          if (disposition) {
            var m = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
            if (m && m[1]) filename = m[1].replace(/['"]/g, '');
          }
          var a = document.createElement('a');
          var objUrl = URL.createObjectURL(blob);
          a.href = objUrl; a.download = filename; a.click();
          setTimeout(function () { URL.revokeObjectURL(objUrl); }, 1000);
        }); });
    }
    window.location.href = url;
    return Promise.resolve();
  };

  window.ReportAPI = ReportAPI;
})();
