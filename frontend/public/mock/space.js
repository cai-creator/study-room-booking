/**
 * Mock 数据 - 空间管理模块
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var MockSpace = {};

  function delay(ms) { return new Promise(function (r) { setTimeout(r, ms || rand(200, 500)); }); }
  function rand(min, max) { return Math.floor(Math.random() * (max - min + 1)) + min; }
  function ok(data, msg) { return { code: 200, message: msg || '操作成功', data: data, timestamp: Date.now() }; }
  function fail(code, msg) { return { code: code, message: msg, data: null, timestamp: Date.now() }; }

  var nextId = 100;

  // ===== 校区数据 =====
  var campuses = [
    { id: 1, name: '主校区', address: '北京市海淀区学院路15号', status: 1, createdAt: '2026-01-01 00:00:00' },
    { id: 2, name: '东校区', address: '北京市朝阳区朝阳路20号', status: 1, createdAt: '2026-01-01 00:00:00' },
  ];

  // ===== 楼栋数据 =====
  var buildings = [
    { id: 1, campusId: 1, campusName: '主校区', name: '图书馆', floorCount: 5, status: 1 },
    { id: 2, campusId: 1, campusName: '主校区', name: '教学楼A座', floorCount: 6, status: 1 },
    { id: 3, campusId: 2, campusName: '东校区', name: '综合楼', floorCount: 4, status: 1 },
    { id: 4, campusId: 2, campusName: '东校区', name: '实验楼', floorCount: 3, status: 1 },
  ];

  // ===== 楼层数据 =====
  var floors = [];
  for (var bi = 0; bi <= 4; bi++) {
    for (var fi = 1; fi <= (bi === 1 ? 6 : bi === 2 ? 5 : bi === 3 ? 4 : 3); fi++) {
      floors.push({ id: floors.length + 1, buildingId: bi + 1 || 1, buildingName: (buildings[bi || 0] || buildings[0]).name, floorNumber: fi });
    }
  }

  // ===== 自习室数据 =====
  var roomTypes = ['READING_ROOM', 'SELF_STUDY_ROOM', 'COMPUTER_ROOM', 'GROUP_ROOM'];
  var roomTypeNames = { 'READING_ROOM': '阅览室', 'SELF_STUDY_ROOM': '自习室', 'COMPUTER_ROOM': '电子阅览室', 'GROUP_ROOM': '研讨间' };
  var rooms = [
    { id: 1, floorId: 1, floorNumber: 1, buildingId: 1, buildingName: '图书馆', campusId: 1, campusName: '主校区', name: '图书馆101自习室', roomType: 'SELF_STUDY_ROOM', totalSeats: 60, rowsCount: 8, colsCount: 8, status: 1, hasAvailable: true },
    { id: 2, floorId: 2, floorNumber: 2, buildingId: 1, buildingName: '图书馆', campusId: 1, campusName: '主校区', name: '图书馆201阅览室', roomType: 'READING_ROOM', totalSeats: 40, rowsCount: 5, colsCount: 8, status: 1, hasAvailable: true },
    { id: 3, floorId: 3, floorNumber: 3, buildingId: 1, buildingName: '图书馆', campusId: 1, campusName: '主校区', name: '图书馆301电子阅览室', roomType: 'COMPUTER_ROOM', totalSeats: 30, rowsCount: 5, colsCount: 6, status: 1, hasAvailable: false },
    { id: 4, floorId: 6, floorNumber: 1, buildingId: 2, buildingName: '教学楼A座', campusId: 1, campusName: '主校区', name: '教A101自习室', roomType: 'SELF_STUDY_ROOM', totalSeats: 80, rowsCount: 10, colsCount: 8, status: 1, hasAvailable: true },
    { id: 5, floorId: 10, floorNumber: 1, buildingId: 3, buildingName: '综合楼', campusId: 2, campusName: '东校区', name: '综合楼101自习室', roomType: 'SELF_STUDY_ROOM', totalSeats: 50, rowsCount: 7, colsCount: 8, status: 1, hasAvailable: true },
  ];

  // ===== 座位数据生成 =====
  var seatStatuses = ['AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'AVAILABLE', 'RESERVED', 'OCCUPIED', 'TEMPORARY_LEAVE', 'UNAVAILABLE'];
  function generateSeats(roomId, rows, cols) {
    var seats = [];
    var labels = 'ABCDEFGHJKLMNPQRSTUVWXYZ';
    for (var r = 1; r <= rows; r++) {
      for (var c = 1; c <= cols; c++) {
        var status = seatStatuses[Math.floor(Math.random() * seatStatuses.length)];
        var tags = [];
        if (c === 1 || c === cols) tags.push('WINDOW');
        if (r === 1 || r === rows) tags.push('CORNER');
        if (c % 3 === 0) tags.push('POWER');
        seats.push({
          seatId: seats.length + 1,
          seatCode: labels.charAt(r - 1) + '-' + String(c).padStart(2, '0'),
          rowNumber: r, colNumber: c,
          status: status, tags: tags,
        });
      }
    }
    return seats;
  }

  // 缓存已生成的座位
  var _seatCache = {};

  // ===== API 实现 =====

  /** 校区列表 */
  MockSpace.getCampuses = function () {
    return delay().then(function () { return ok(campuses); });
  };
  MockSpace.getCampus = function (id) {
    return delay().then(function () { return ok(campuses.find(function (c) { return c.id === Number(id); }) || null); });
  };

  /** 楼栋列表 */
  MockSpace.getBuildings = function (params) {
    return delay().then(function () {
      var list = buildings;
      if (params && params.campusId) list = list.filter(function (b) { return b.campusId === Number(params.campusId); });
      return ok(list);
    });
  };
  MockSpace.getBuilding = function (id) {
    return delay().then(function () { return ok(buildings.find(function (b) { return b.id === Number(id); }) || null); });
  };

  /** 楼层列表 */
  MockSpace.getFloors = function (buildingId) {
    return delay().then(function () {
      return ok(floors.filter(function (f) { return f.buildingId === Number(buildingId); }));
    });
  };

  /** 自习室列表 */
  MockSpace.getRooms = function (params) {
    return delay().then(function () {
      var list = rooms.slice();
      params = params || {};
      if (params.campusId) list = list.filter(function (r) { return r.campusId === Number(params.campusId); });
      if (params.buildingId) list = list.filter(function (r) { return r.buildingId === Number(params.buildingId); });
      if (params.floorId) list = list.filter(function (r) { return r.floorId === Number(params.floorId); });
      if (params.hasAvailableSeats === 'true') list = list.filter(function (r) { return r.hasAvailable; });
      if (params.keyword) list = list.filter(function (r) { return r.name.indexOf(params.keyword) !== -1; });
      var pageNum = Number(params.pageNum) || 1;
      var pageSize = Number(params.pageSize) || 20;
      var total = list.length;
      var start = (pageNum - 1) * pageSize;
      return ok({ list: list.slice(start, start + pageSize), total: total, pageNum: pageNum, pageSize: pageSize });
    });
  };
  MockSpace.getRoom = function (id) {
    return delay().then(function () { return ok(rooms.find(function (r) { return r.id === Number(id); }) || null); });
  };

  /** 座位列表 */
  MockSpace.getSeats = function (params) {
    return delay().then(function () {
      var roomId = Number(params.roomId) || 1;
      var room = rooms.find(function (r) { return r.id === roomId; }) || rooms[0];
      if (!_seatCache[roomId]) _seatCache[roomId] = generateSeats(roomId, room.rowsCount, room.colsCount);
      var seats = _seatCache[roomId];
      var pageNum = Number(params.pageNum) || 1;
      var pageSize = Number(params.pageSize) || 100;
      var total = seats.length;
      var start = (pageNum - 1) * pageSize;
      return ok({ list: seats.slice(start, start + pageSize), total: total, pageNum: pageNum, pageSize: pageSize });
    });
  };

  /** 座位实时状态 */
  MockSpace.getSeatStatus = function (roomId) {
    return delay().then(function () {
      var room = rooms.find(function (r) { return r.id === Number(roomId); }) || rooms[0];
      if (!_seatCache[roomId]) _seatCache[roomId] = generateSeats(roomId, room.rowsCount, room.colsCount);
      var seats = _seatCache[roomId];
      var counts = { AVAILABLE: 0, RESERVED: 0, OCCUPIED: 0, TEMPORARY_LEAVE: 0, UNAVAILABLE: 0 };
      seats.forEach(function (s) { counts[s.status] = (counts[s.status] || 0) + 1; });
      return ok({
        roomId: room.id, roomName: room.name,
        totalSeats: seats.length,
        availableSeats: counts.AVAILABLE,
        reservedSeats: counts.RESERVED,
        occupiedSeats: counts.OCCUPIED + counts.TEMPORARY_LEAVE,
        seats: seats,
      });
    });
  };

  /** CRUD 操作 */
  // ===== 校区 CRUD =====
  MockSpace.createCampus = function (data) { return delay().then(function () { var c = Object.assign({}, data, { id: ++nextId, status: data.status || 1, createdAt: new Date().toISOString().slice(0, 19).replace('T', ' ') }); campuses.push(c); return ok(c, '创建校区成功'); }); };
  MockSpace.updateCampus = function (id, data) { return delay().then(function () { var idx = campuses.findIndex(function (c) { return c.id === Number(id); }); if (idx >= 0) Object.assign(campuses[idx], data); return ok(campuses[idx] || null, '更新校区成功'); }); };
  MockSpace.deleteCampus = function (id) { return delay().then(function () { campuses = campuses.filter(function (c) { return c.id !== Number(id); }); return ok(null, '删除校区成功'); }); };

  // ===== 楼栋 CRUD =====
  MockSpace.createBuilding = function (data) { return delay().then(function () { var b = Object.assign({}, data, { id: ++nextId, status: data.status || 1 }); buildings.push(b); return ok(b, '创建楼栋成功'); }); };
  MockSpace.updateBuilding = function (id, data) { return delay().then(function () { var idx = buildings.findIndex(function (b) { return b.id === Number(id); }); if (idx >= 0) Object.assign(buildings[idx], data); return ok(buildings[idx] || null, '更新楼栋成功'); }); };
  MockSpace.deleteBuilding = function (id) { return delay().then(function () { buildings = buildings.filter(function (b) { return b.id !== Number(id); }); return ok(null, '删除楼栋成功'); }); };

  // ===== 楼层 CRUD =====
  MockSpace.getFloor = function (id) { return delay().then(function () { return ok(floors.find(function (f) { return f.id === Number(id); }) || null); }); };
  MockSpace.createFloor = function (data) { return delay().then(function () { var f = Object.assign({}, data, { id: ++nextId }); floors.push(f); return ok(f, '创建楼层成功'); }); };
  MockSpace.updateFloor = function (id, data) { return delay().then(function () { var idx = floors.findIndex(function (f) { return f.id === Number(id); }); if (idx >= 0) Object.assign(floors[idx], data); return ok(floors[idx] || null, '更新楼层成功'); }); };
  MockSpace.deleteFloor = function (id) { return delay().then(function () { floors = floors.filter(function (f) { return f.id !== Number(id); }); return ok(null, '删除楼层成功'); }); };

  // ===== 自习室 CRUD 补充 =====
  MockSpace.createRoom = function (data) { return delay().then(function () { var r = Object.assign({}, data, { id: ++nextId }); rooms.push(r); return ok(r, '创建成功'); }); };
  MockSpace.updateRoom = function (id, data) { return delay().then(function () { var idx = rooms.findIndex(function (r) { return r.id === Number(id); }); if (idx >= 0) Object.assign(rooms[idx], data); return ok(rooms[idx], '更新成功'); }); };
  MockSpace.deleteRoom = function (id) { return delay().then(function () { rooms = rooms.filter(function (r) { return r.id !== Number(id); }); return ok(null, '删除成功'); }); };
  MockSpace.updateRoomStatus = function (id, status) { return delay().then(function () { var idx = rooms.findIndex(function (r) { return r.id === Number(id); }); if (idx >= 0) rooms[idx].status = Number(status) || 0; return ok(rooms[idx] || null, '状态更新成功'); }); };

  // ===== 座位 CRUD 补充 =====
  MockSpace.getSeat = function (id) {
    return delay().then(function () {
      var found = null;
      for (var rid in _seatCache) {
        var s = _seatCache[rid].find(function (x) { return x.seatId === Number(id); });
        if (s) { found = s; break; }
      }
      return ok(found);
    });
  };
  MockSpace.generateSeats = function (roomId, config) {
    return delay().then(function () {
      var rows = Number(config && config.rowsCount) || 6;
      var cols = Number(config && config.colsCount) || 8;
      _seatCache[Number(roomId)] = generateSeats(roomId, rows, cols);
      return ok({ roomId: Number(roomId), generated: rows * cols }, '批量生成座位成功');
    });
  };
  MockSpace.updateSeat = function (id, data) {
    return delay().then(function () {
      for (var rid in _seatCache) {
        var idx = _seatCache[rid].findIndex(function (x) { return x.seatId === Number(id); });
        if (idx >= 0) { Object.assign(_seatCache[rid][idx], data); return ok(_seatCache[rid][idx], '座位更新成功'); }
      }
      return ok(null, '座位不存在');
    });
  };
  MockSpace.deleteSeat = function (id) {
    return delay().then(function () {
      for (var rid in _seatCache) {
        var before = _seatCache[rid].length;
        _seatCache[rid] = _seatCache[rid].filter(function (x) { return x.seatId !== Number(id); });
        if (_seatCache[rid].length !== before) return ok(null, '删除座位成功');
      }
      return ok(null, '删除座位成功');
    });
  };
  MockSpace.updateSeatTags = function (roomId, data) {
    return delay().then(function () {
      var list = (data && data.seats) || [];
      var cache = _seatCache[Number(roomId)] || [];
      list.forEach(function (item) {
        var idx = cache.findIndex(function (s) { return s.seatId === Number(item.seatId); });
        if (idx >= 0) cache[idx].tags = item.tags || [];
      });
      return ok({ roomId: Number(roomId), updated: list.length }, '批量更新座位标签成功');
    });
  };

  window.MockSpace = MockSpace;
})();
