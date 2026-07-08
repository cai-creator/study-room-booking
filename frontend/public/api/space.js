/**
 * API 模块 - 空间管理（校区/楼栋/楼层/自习室/座位）
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var SpaceAPI = {};

  // ===== 校区 =====
  SpaceAPI.getCampuses = function () { return AppConfig.useMock ? MockSpace.getCampuses().then(ok) : Request.get('/campuses'); };
  SpaceAPI.getCampus = function (id) { return AppConfig.useMock ? MockSpace.getCampus(id).then(ok) : Request.get('/campuses/' + id); };
  SpaceAPI.createCampus = function (data) { return AppConfig.useMock && MockSpace.createCampus ? MockSpace.createCampus(data).then(ok) : Request.post('/campuses', data); };
  SpaceAPI.updateCampus = function (id, data) { return AppConfig.useMock && MockSpace.updateCampus ? MockSpace.updateCampus(id, data).then(ok) : Request.put('/campuses/' + id, data); };
  SpaceAPI.deleteCampus = function (id) { return AppConfig.useMock && MockSpace.deleteCampus ? MockSpace.deleteCampus(id).then(ok) : Request.del('/campuses/' + id); };

  // ===== 楼栋 =====
  SpaceAPI.getBuildings = function (params) { return AppConfig.useMock ? MockSpace.getBuildings(params).then(ok) : Request.get('/buildings', params); };
  SpaceAPI.getBuilding = function (id) { return AppConfig.useMock ? MockSpace.getBuilding(id).then(ok) : Request.get('/buildings/' + id); };
  SpaceAPI.createBuilding = function (data) { return AppConfig.useMock && MockSpace.createBuilding ? MockSpace.createBuilding(data).then(ok) : Request.post('/buildings', data); };
  SpaceAPI.updateBuilding = function (id, data) { return AppConfig.useMock && MockSpace.updateBuilding ? MockSpace.updateBuilding(id, data).then(ok) : Request.put('/buildings/' + id, data); };
  SpaceAPI.deleteBuilding = function (id) { return AppConfig.useMock && MockSpace.deleteBuilding ? MockSpace.deleteBuilding(id).then(ok) : Request.del('/buildings/' + id); };

  // ===== 楼层 =====
  SpaceAPI.getFloors = function (buildingId) { return AppConfig.useMock ? MockSpace.getFloors(buildingId).then(ok) : Request.get('/floors', { buildingId: buildingId }); };
  SpaceAPI.getFloor = function (id) { return AppConfig.useMock && MockSpace.getFloor ? MockSpace.getFloor(id).then(ok) : Request.get('/floors/' + id); };
  SpaceAPI.createFloor = function (data) { return AppConfig.useMock && MockSpace.createFloor ? MockSpace.createFloor(data).then(ok) : Request.post('/floors', data); };
  SpaceAPI.updateFloor = function (id, data) { return AppConfig.useMock && MockSpace.updateFloor ? MockSpace.updateFloor(id, data).then(ok) : Request.put('/floors/' + id, data); };
  SpaceAPI.deleteFloor = function (id) { return AppConfig.useMock && MockSpace.deleteFloor ? MockSpace.deleteFloor(id).then(ok) : Request.del('/floors/' + id); };

  // ===== 自习室 =====
  SpaceAPI.getRooms = function (params) { return AppConfig.useMock ? MockSpace.getRooms(params).then(ok) : Request.get('/rooms', params); };
  SpaceAPI.getRoom = function (id) { return AppConfig.useMock ? MockSpace.getRoom(id).then(ok) : Request.get('/rooms/' + id); };
  SpaceAPI.createRoom = function (data) { return AppConfig.useMock ? MockSpace.createRoom(data).then(ok) : Request.post('/rooms', data); };
  SpaceAPI.updateRoom = function (id, data) { return AppConfig.useMock ? MockSpace.updateRoom(id, data).then(ok) : Request.put('/rooms/' + id, data); };
  SpaceAPI.deleteRoom = function (id) { return AppConfig.useMock ? MockSpace.deleteRoom(id).then(ok) : Request.del('/rooms/' + id); };
  SpaceAPI.updateRoomStatus = function (id, status) { return AppConfig.useMock && MockSpace.updateRoomStatus ? MockSpace.updateRoomStatus(id, status).then(ok) : Request.patch('/rooms/' + id + '/status?status=' + status); };
  SpaceAPI.importRooms = function (formData) { return Request.upload('/rooms/import', formData); };

  // ===== 座位 =====
  SpaceAPI.getSeats = function (params) { return AppConfig.useMock ? MockSpace.getSeats(params).then(ok) : Request.get('/seats', params); };
  SpaceAPI.getSeat = function (id) { return AppConfig.useMock && MockSpace.getSeat ? MockSpace.getSeat(id).then(ok) : Request.get('/seats/' + id); };
  SpaceAPI.getSeatStatus = function (roomId) { return AppConfig.useMock ? MockSpace.getSeatStatus(roomId).then(ok) : Request.get('/rooms/' + roomId + '/seats/status'); };
  SpaceAPI.generateSeats = function (roomId, config) { return AppConfig.useMock && MockSpace.generateSeats ? MockSpace.generateSeats(roomId, config).then(ok) : Request.post('/rooms/' + roomId + '/seats/generate', config); };
  SpaceAPI.updateSeat = function (id, data) { return AppConfig.useMock && MockSpace.updateSeat ? MockSpace.updateSeat(id, data).then(ok) : Request.put('/seats/' + id, data); };
  SpaceAPI.deleteSeat = function (id) { return AppConfig.useMock && MockSpace.deleteSeat ? MockSpace.deleteSeat(id).then(ok) : Request.del('/seats/' + id); };
  SpaceAPI.updateSeatTags = function (roomId, data) { return AppConfig.useMock && MockSpace.updateSeatTags ? MockSpace.updateSeatTags(roomId, data).then(ok) : Request.patch('/rooms/' + roomId + '/seats/tags', data); };

  function ok(res) { return res.data; }

  window.SpaceAPI = SpaceAPI;
})();
