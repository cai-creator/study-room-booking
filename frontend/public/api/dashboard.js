/**
 * API 模块 - 实时看板
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var DashboardAPI = {};

  function ok(res) { return res.data; }

  DashboardAPI.getCampusOverview = function () { return AppConfig.useMock ? MockDashboard.getCampusOverview().then(ok) : Request.get('/dashboard/campus-overview'); };
  DashboardAPI.getBuildingOverview = function () { return AppConfig.useMock ? MockDashboard.getBuildingOverview().then(ok) : Request.get('/dashboard/building-overview'); };
  DashboardAPI.getRoomDetail = function (roomId) { return AppConfig.useMock ? MockDashboard.getRoomDetail(roomId).then(ok) : Request.get('/dashboard/room-detail/' + roomId); };

  window.DashboardAPI = DashboardAPI;
})();
