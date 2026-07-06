/**
 * API 模块 - 预约核心
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var ReservationAPI = {};

  function ok(res) { return res.data; }

  ReservationAPI.createReservation = function (data) { return AppConfig.useMock ? MockReservation.createReservation(data).then(ok) : Request.post('/reservations', data); };
  ReservationAPI.cancelReservation = function (id) { return AppConfig.useMock ? MockReservation.cancelReservation(id).then(ok) : Request.post('/reservations/' + id + '/cancel'); };
  ReservationAPI.getMyReservations = function (params) { return AppConfig.useMock ? MockReservation.getMyReservations(params).then(ok) : Request.get('/reservations/my', params); };
  ReservationAPI.getReservationDetail = function (id) { return AppConfig.useMock ? MockReservation.getReservationDetail(id).then(ok) : Request.get('/reservations/' + id); };
  ReservationAPI.getReservations = function (params) { return AppConfig.useMock ? MockReservation.getReservations(params).then(ok) : Request.get('/reservations', params); };
  ReservationAPI.getAvailableSlots = function (seatId, date) { return AppConfig.useMock ? MockReservation.getAvailableSlots(seatId, date).then(ok) : Request.get('/seats/' + seatId + '/available-slots', { date: date }); };

  window.ReservationAPI = ReservationAPI;
})();
