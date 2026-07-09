/**
 * API 模块 - 数据报表
 * 高校自习室智能预约系统
 *
 * @typedef {Object} ReportQueryParams
 * @property {string} startDate    - 开始日期 (必填，yyyy-MM-dd)
 * @property {string} endDate      - 结束日期 (必填，yyyy-MM-dd)
 * @property {number} [campusId]   - 校区ID（可选筛选）
 * @property {number} [buildingId] - 楼栋ID（可选筛选）
 * @property {number} [roomId]     - 自习室ID（可选筛选）
 *
 * @typedef {Object} UsageRateItem
 * @property {string} date        - 日期 (yyyy-MM-dd)
 * @property {number} roomId      - 自习室ID
 * @property {string} roomName    - 自习室名称
 * @property {number} usedSeats   - 已使用座位数（已签到/已完成预约数）
 * @property {number} totalSeats  - 总座位数
 * @property {number} usageRate   - 使用率（百分比，如 30.5 表示30.5%）
 *
 * @typedef {Object} TimeDistributionItem
 * @property {number} hour        - 小时 (0-23)
 * @property {string} label       - 小时描述，如 "09:00"
 * @property {number} count       - 预约数量
 *
 * @typedef {Object} HotPeriodItem
 * @property {number} hour        - 小时 (0-23)
 * @property {string} label       - 时段描述，如 "09:00"（返回 List 顺序即为 TOP5 排名：第1条=第1名）
 * @property {number} count       - 预约数量
 *
 * @typedef {Object} NoShowRateResult
 * @property {string} startDate           - 开始日期 (yyyy-MM-dd)
 * @property {string} endDate             - 结束日期 (yyyy-MM-dd)
 * @property {number} totalReservations   - 预约总数
 * @property {number} noShowReservations  - 爽约预约数（NO_SHOW状态的预约数）
 * @property {number} totalNoShowRecords  - 爽约记录条数（累计爽约记录）
 * @property {number} noShowRate          - 爽约率（百分比，如 5.2 表示5.2%）
 *
 * @typedef {Object} ConversionRateResult
 * @property {string} startDate           - 开始日期 (yyyy-MM-dd)
 * @property {string} endDate             - 结束日期 (yyyy-MM-dd)
 * @property {number} totalReservations   - 预约总数
 * @property {number} checkedInCount      - 签到数（已签到/暂离/完成）
 * @property {number} completedCount      - 完成数
 * @property {number} checkinRate         - 签到率（百分比）
 * @property {number} completionRate      - 完成率（签到→完成的转化率，百分比）
 */
(function () {
  'use strict';

  var ReportAPI = {};

  /**
   * 日均使用率统计（统计时间范围内每天每个自习室的座位使用率）
   * @param {ReportQueryParams} params
   * @returns {Promise<UsageRateItem[]>}
   */
  ReportAPI.getUsageRate = function (params) { return Request.get('/reports/usage-rate', params); };

  /**
   * 时段占用分布（统计时间范围内每个小时段的预约数量，24小时分布）
   * @param {ReportQueryParams} params
   * @returns {Promise<TimeDistributionItem[]>}
   */
  ReportAPI.getTimeDistribution = function (params) { return Request.get('/reports/time-distribution', params); };

  /**
   * 热门时段TOP5（统计时间范围内预约数最多的5个时段）
   * @param {ReportQueryParams} params
   * @returns {Promise<HotPeriodItem[]>}
   */
  ReportAPI.getHotPeriods = function (params) { return Request.get('/reports/hot-periods', params); };

  /**
   * 爽约率统计（统计指定时间段内的爽约率）
   * @param {ReportQueryParams} params
   * @returns {Promise<NoShowRateResult>}
   */
  ReportAPI.getNoShowRate = function (params) { return Request.get('/reports/no-show-rate', params); };

  /**
   * 预约转化率（统计 预约 → 签到 → 完成 的转化率）
   * @param {ReportQueryParams} params
   * @returns {Promise<ConversionRateResult>}
   */
  ReportAPI.getConversionRate = function (params) { return Request.get('/reports/conversion-rate', params); };

  /**
   * 导出Excel报表（导出时间范围内的预约数据为Excel文件）
   * @param {ReportQueryParams} params
   * @returns {Promise<void>} 触发浏览器文件下载
   */
  ReportAPI.exportReport = function (params) {
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
