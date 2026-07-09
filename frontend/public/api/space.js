/**
 * API 模块 - 空间管理（校区/楼栋/楼层/自习室/座位）
 * 高校自习室智能预约系统
 *
 * @typedef {Object} Campus
 * @property {number} id          - 校区ID
 * @property {string} name        - 校区名称
 * @property {string} [address]   - 地址
 * @property {number} [longitude] - 经度
 * @property {number} [latitude]  - 纬度
 * @property {number} [sortOrder] - 排序号
 * @property {number} status      - 状态：0-停用，1-启用
 * @property {string} createdAt   - 创建时间
 * @property {string} updatedAt   - 更新时间
 *
 * @typedef {Object} Building
 * @property {number} id          - 楼栋ID
 * @property {number} campusId    - 所属校区ID
 * @property {string} name        - 楼栋名称
 * @property {number} floorCount  - 楼层数
 * @property {number} [sortOrder] - 排序号
 * @property {number} status      - 状态：0-停用，1-启用
 * @property {string} createdAt   - 创建时间
 * @property {string} updatedAt   - 更新时间
 *
 * @typedef {Object} Floor
 * @property {number} id           - 楼层ID
 * @property {number} buildingId   - 所属楼栋ID
 * @property {number} floorNumber  - 楼层号
 * @property {string} [name]       - 楼层名称
 * @property {number} [sortOrder]  - 排序号
 * @property {number} status       - 状态：0-停用，1-启用
 * @property {string} createdAt    - 创建时间
 * @property {string} updatedAt    - 更新时间
 *
 * @typedef {Object} StudyRoom
 * @property {number} id            - 自习室ID
 * @property {number} floorId       - 所属楼层ID
 * @property {string} name          - 自习室名称
 * @property {string} [roomType]    - 类型：LIBRARY-图书馆 TEACHING-教学楼 READING-阅览室
 * @property {number} totalSeats    - 总座位数
 * @property {number} rowsCount     - 行数
 * @property {number} colsCount     - 列数
 * @property {string} [openTime]    - 开放时间 (HH:mm:ss)
 * @property {string} [closeTime]   - 关闭时间 (HH:mm:ss)
 * @property {number} status        - 状态：0-关闭 1-开放 2-维护中
 * @property {string} [description] - 描述
 * @property {string} createdAt     - 创建时间
 * @property {string} updatedAt     - 更新时间
 *
 * @typedef {Object} Seat
 * @property {number} id          - 座位ID
 * @property {number} roomId      - 自习室ID
 * @property {string} [seatCode]  - 座位编号，如 "A-01"
 * @property {number} [rowNumber] - 行号 (1-50)
 * @property {number} [colNumber] - 列号 (1-50)
 * @property {string} [tags]      - 标签（逗号分隔）：WINDOW-靠窗 POWER-有电源 ACCESSIBLE-无障碍
 * @property {number} status      - 状态：0-不可用 1-可用
 * @property {string} createdAt   - 创建时间
 * @property {string} updatedAt   - 更新时间
 *
 * @typedef {Object} SeatStatusItem
 * @property {number} seatId       - 座位ID
 * @property {string} seatCode     - 座位编号
 * @property {number} rowNumber    - 行号
 * @property {number} colNumber    - 列号
 * @property {string} status       - 实时状态：AVAILABLE空闲 RESERVED已预约 OCCUPIED已占用 TEMPORARY_LEAVE暂离 UNAVAILABLE不可用
 * @property {string[]} tags       - 标签列表
 *
 * @typedef {Object} RoomSeatStatusVO
 * @property {number} roomId          - 自习室ID
 * @property {string} roomName        - 自习室名称
 * @property {number} totalSeats      - 总座位数
 * @property {number} availableSeats  - 可用座位数
 * @property {number} reservedSeats   - 已预约座位数
 * @property {number} occupiedSeats   - 已占用座位数
 * @property {SeatStatusItem[]} seats - 座位状态列表
 *
 * @typedef {Object} CampusRequest
 * @property {string} name        - 校区名称 (必填)
 * @property {string} [address]   - 地址
 * @property {number} [longitude] - 经度
 * @property {number} [latitude]  - 纬度
 * @property {number} [sortOrder] - 排序号
 * @property {number} [status]    - 状态：0-停用 1-启用
 *
 * @typedef {Object} BuildingRequest
 * @property {number} campusId    - 所属校区ID (必填)
 * @property {string} name        - 楼栋名称 (必填)
 * @property {number} [sortOrder] - 排序号
 * @property {number} [status]    - 状态：0-停用 1-启用
 *
 * @typedef {Object} FloorRequest
 * @property {number} buildingId   - 所属楼栋ID (必填)
 * @property {number} floorNumber  - 楼层号 (必填)
 * @property {string} [name]       - 楼层名称
 * @property {number} [sortOrder]  - 排序号
 * @property {number} [status]     - 状态：0-停用 1-启用
 *
 * @typedef {Object} RoomRequest
 * @property {number} floorId       - 所属楼层ID (必填)
 * @property {string} name          - 自习室名称 (必填)
 * @property {string} [roomType]    - 类型：LIBRARY/TEACHING/READING
 * @property {string} [openTime]    - 开放时间 (HH:mm:ss)
 * @property {string} [closeTime]   - 关闭时间 (HH:mm:ss)
 * @property {number} [status]      - 状态：0-关闭 1-开放 2-维护中
 * @property {string} [description] - 描述
 *
 * @typedef {Object} RoomQueryRequest
 * @property {number} [campusId]         - 校区ID
 * @property {number} [buildingId]       - 楼栋ID
 * @property {number} [floorId]          - 楼层ID
 * @property {string} [roomType]         - 房间类型
 * @property {number} [status]           - 状态
 * @property {boolean} [hasAvailableSeats] - 仅显示有空位
 * @property {string} [keyword]          - 名称关键词
 * @property {number} [pageNum=1]        - 页码
 * @property {number} [pageSize=20]      - 每页条数
 * @property {string} [sortField]        - 排序字段
 * @property {string} [sortOrder]        - 排序方向：asc/desc
 *
 * @typedef {Object} SeatUpdateRequest
 * @property {string} [seatCode]   - 座位编号，如 "A-01"
 * @property {number} [rowNumber]  - 行号 (1-50)
 * @property {number} [colNumber]  - 列号 (1-50)
 * @property {string} [tags]       - 标签（逗号分隔）
 * @property {number} [status]     - 状态：0-不可用 1-可用
 *
 * @typedef {Object} SeatGenerateRequest
 * @property {number} rows                           - 座位行数 (必填 1-50)
 * @property {number} cols                           - 座位列数 (必填 1-50)
 * @property {Array<{row:number,col:number}>} [emptyPositions]     - 留空位置列表
 * @property {Array<{row:number,col:number,tags:string[]}>} [specialPositions] - 特殊标签位置列表
 *
 * @typedef {Object} SeatTagsUpdateRequest
 * @property {number[]} seatIds  - 要更新的座位ID列表 (必填)
 * @property {string[]} tags     - 标签列表：WINDOW/POWER/ACCESSIBLE (必填)
 *
 * @template T
 * @typedef {Object} Page
 * @property {T[]}    records - 数据列表
 * @property {number} total   - 总条数
 * @property {number} size    - 每页大小
 * @property {number} current - 当前页码
 * @property {number} pages   - 总页数
 */
(function () {
  'use strict';

  var SpaceAPI = {};

  // ===== 校区 =====
  /**
   * 获取校区列表
   * @returns {Promise<Campus[]>}
   */
  SpaceAPI.getCampuses = function () { return Request.get('/campuses'); };

  /**
   * 获取校区详情
   * @param {number} id 校区ID (必填)
   * @returns {Promise<Campus>}
   */
  SpaceAPI.getCampus = function (id) { return Request.get('/campuses/' + id); };

  /**
   * 新增校区
   * @param {CampusRequest} data
   * @returns {Promise<Campus>}
   */
  SpaceAPI.createCampus = function (data) { return Request.post('/campuses', data); };

  /**
   * 更新校区
   * @param {number} id 校区ID (必填)
   * @param {CampusRequest} data
   * @returns {Promise<Campus>}
   */
  SpaceAPI.updateCampus = function (id, data) { return Request.put('/campuses/' + id, data); };

  /**
   * 删除校区
   * @param {number} id 校区ID (必填)
   * @returns {Promise<void>}
   */
  SpaceAPI.deleteCampus = function (id) { return Request.del('/campuses/' + id); };

  // ===== 楼栋 =====
  /**
   * 获取楼栋列表
   * @param {Object} [params]
   * @param {number} [params.campusId] 校区ID（可选筛选）
   * @returns {Promise<Building[]>}
   */
  SpaceAPI.getBuildings = function (params) { return Request.get('/buildings', params); };

  /**
   * 获取楼栋详情
   * @param {number} id 楼栋ID (必填)
   * @returns {Promise<Building>}
   */
  SpaceAPI.getBuilding = function (id) { return Request.get('/buildings/' + id); };

  /**
   * 新增楼栋
   * @param {BuildingRequest} data
   * @returns {Promise<Building>}
   */
  SpaceAPI.createBuilding = function (data) { return Request.post('/buildings', data); };

  /**
   * 更新楼栋
   * @param {number} id 楼栋ID (必填)
   * @param {BuildingRequest} data
   * @returns {Promise<Building>}
   */
  SpaceAPI.updateBuilding = function (id, data) { return Request.put('/buildings/' + id, data); };

  /**
   * 删除楼栋
   * @param {number} id 楼栋ID (必填)
   * @returns {Promise<void>}
   */
  SpaceAPI.deleteBuilding = function (id) { return Request.del('/buildings/' + id); };

  // ===== 楼层 =====
  /**
   * 获取楼层列表
   * @param {number} buildingId 楼栋ID (必填)
   * @returns {Promise<Floor[]>}
   */
  SpaceAPI.getFloors = function (buildingId) { return Request.get('/floors', { buildingId: buildingId }); };

  /**
   * 获取楼层详情
   * @param {number} id 楼层ID (必填)
   * @returns {Promise<Floor>}
   */
  SpaceAPI.getFloor = function (id) { return Request.get('/floors/' + id); };

  /**
   * 新增楼层
   * @param {FloorRequest} data
   * @returns {Promise<Floor>}
   */
  SpaceAPI.createFloor = function (data) { return Request.post('/floors', data); };

  /**
   * 更新楼层
   * @param {number} id 楼层ID (必填)
   * @param {FloorRequest} data
   * @returns {Promise<Floor>}
   */
  SpaceAPI.updateFloor = function (id, data) { return Request.put('/floors/' + id, data); };

  /**
   * 删除楼层
   * @param {number} id 楼层ID (必填)
   * @returns {Promise<void>}
   */
  SpaceAPI.deleteFloor = function (id) { return Request.del('/floors/' + id); };

  // ===== 自习室 =====
  /**
   * 分页查询自习室列表
   * @param {RoomQueryRequest} params
   * @returns {Promise<Page<StudyRoom>>}
   */
  SpaceAPI.getRooms = function (params) { return Request.get('/rooms', params); };

  /**
   * 获取自习室详情
   * @param {number} id 自习室ID (必填)
   * @returns {Promise<StudyRoom>}
   */
  SpaceAPI.getRoom = function (id) { return Request.get('/rooms/' + id); };

  /**
   * 新增自习室
   * @param {RoomRequest} data
   * @returns {Promise<StudyRoom>}
   */
  SpaceAPI.createRoom = function (data) { return Request.post('/rooms', data); };

  /**
   * 更新自习室
   * @param {number} id 自习室ID (必填)
   * @param {RoomRequest} data
   * @returns {Promise<StudyRoom>}
   */
  SpaceAPI.updateRoom = function (id, data) { return Request.put('/rooms/' + id, data); };

  /**
   * 删除自习室
   * @param {number} id 自习室ID (必填)
   * @returns {Promise<void>}
   */
  SpaceAPI.deleteRoom = function (id) { return Request.del('/rooms/' + id); };

  /**
   * 修改自习室状态
   * @param {number} id     自习室ID (必填)
   * @param {number} status 状态：0-关闭 1-开放 2-维护中 (必填)
   * @returns {Promise<void>}
   */
  SpaceAPI.updateRoomStatus = function (id, status) { return Request.patch('/rooms/' + id + '/status?status=' + status); };

  /**
   * Excel批量导入自习室
   * @param {FormData} formData 包含 file 字段（Excel文件）
   * @returns {Promise<Object>} { successCount, failCount, errors? }
   */
  SpaceAPI.importRooms = function (formData) { return Request.upload('/rooms/import', formData); };

  // ===== 座位 =====
  /**
   * 获取自习室座位列表
   * @param {Object} params
   * @param {number} params.roomId 自习室ID (必填)
   * @returns {Promise<Seat[]>}
   */
  SpaceAPI.getSeats = function (params) { return Request.get('/seats', params); };

  /**
   * 获取座位详情
   * @param {number} id 座位ID (必填)
   * @returns {Promise<Seat>}
   */
  SpaceAPI.getSeat = function (id) { return Request.get('/seats/' + id); };

  /**
   * 获取自习室所有座位的实时状态
   * @param {number} roomId 自习室ID (必填)
   * @returns {Promise<RoomSeatStatusVO>}
   */
  SpaceAPI.getSeatStatus = function (roomId) { return Request.get('/rooms/' + roomId + '/seats/status'); };

  /**
   * 批量生成座位（按行列网格）
   * @param {number} roomId  自习室ID (必填)
   * @param {SeatGenerateRequest} config
   * @returns {Promise<Seat[]>}
   */
  SpaceAPI.generateSeats = function (roomId, config) { return Request.post('/rooms/' + roomId + '/seats/generate', config); };

  /**
   * 更新座位信息
   * @param {number} id 座位ID (必填)
   * @param {SeatUpdateRequest} data
   * @returns {Promise<Seat>}
   */
  SpaceAPI.updateSeat = function (id, data) { return Request.put('/seats/' + id, data); };

  /**
   * 删除座位
   * @param {number} id 座位ID (必填)
   * @returns {Promise<void>}
   */
  SpaceAPI.deleteSeat = function (id) { return Request.del('/seats/' + id); };

  /**
   * 批量更新座位标签
   * @param {number} roomId 自习室ID (必填)
   * @param {SeatTagsUpdateRequest} data
   * @returns {Promise<void>}
   */
  SpaceAPI.updateSeatTags = function (roomId, data) { return Request.patch('/rooms/' + roomId + '/seats/tags', data); };

  window.SpaceAPI = SpaceAPI;
})();
