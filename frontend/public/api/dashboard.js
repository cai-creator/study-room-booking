/**
 * API 模块 - 实时看板
 * 高校自习室智能预约系统
 *
 * @typedef {Object} SeatStatusItem
 * @property {number} seatId       - 座位ID
 * @property {string} seatCode     - 座位编号
 * @property {number} rowNumber    - 行号
 * @property {number} colNumber    - 列号
 * @property {string} status       - 实时状态：AVAILABLE空闲 RESERVED已预约 OCCUPIED已占用 TEMPORARY_LEAVE暂离 UNAVAILABLE不可用
 * @property {string[]} tags       - 标签列表
 *
 * @typedef {Object} CampusOverviewVO
 * @property {number} campusId        - 校区ID
 * @property {string} campusName      - 校区名称
 * @property {number} totalRooms      - 自习室总数
 * @property {number} totalSeats      - 总座位数
 * @property {number} availableSeats  - 可用座位数
 * @property {number} usageRate       - 使用率（百分比，如 30.0 表示30%）
 *
 * @typedef {Object} BuildingOverviewVO
 * @property {number} buildingId      - 楼栋ID
 * @property {string} buildingName    - 楼栋名称
 * @property {number} campusId        - 所属校区ID
 * @property {string} campusName      - 所属校区名称
 * @property {number} totalRooms      - 自习室总数
 * @property {number} totalSeats      - 总座位数
 * @property {number} availableSeats  - 可用座位数
 * @property {number} usageRate       - 使用率（百分比，如 45.5 表示45.5%）
 *
 * @typedef {Object} RoomDetailVO
 * @property {number} roomId          - 自习室ID
 * @property {string} roomName        - 自习室名称
 * @property {string} roomType        - 自习室类型
 * @property {string} floorName       - 楼层名称
 * @property {string} buildingName    - 楼栋名称
 * @property {string} campusName      - 校区名称
 * @property {string} openTime        - 开放时间
 * @property {string} closeTime       - 关闭时间
 * @property {number} totalSeats      - 总座位数
 * @property {number} availableSeats  - 可用座位数
 * @property {number} reservedSeats   - 已预约座位数
 * @property {number} occupiedSeats   - 已占用座位数
 * @property {number} usageRate       - 使用率（百分比）
 * @property {SeatStatusItem[]} seats - 座位实时状态列表
 */

/**
 * @typedef {Object} RoomOverviewVO
 * @property {number} roomId          - 自习室ID
 * @property {string} roomName        - 自习室名称
 * @property {number} buildingId      - 楼栋ID
 * @property {number} floorId         - 楼层ID
 * @property {number} totalSeats      - 总座位数
 * @property {number} availableSeats  - 可用座位数
 * @property {number} reservedSeats   - 已预约座位数
 * @property {number} occupiedSeats   - 已占用座位数
 * @property {number} usageRate       - 使用率（百分比）
 */
(function () {
  'use strict';

  var DashboardAPI = {};

  /**
   * 获取各校区自习室使用率概览
   * @returns {Promise<CampusOverviewVO[]>}
   */
  DashboardAPI.getCampusOverview = function () { return Request.get('/dashboard/campus-overview'); };

  /**
   * 获取各楼栋自习室使用率（可按校区过滤）
   * @param {number} [campusId] 校区ID（可选，不传则返回全部楼栋）
   * @returns {Promise<BuildingOverviewVO[]>}
   */
  DashboardAPI.getBuildingOverview = function (campusId) { return Request.get('/dashboard/building-overview', campusId ? { campusId: campusId } : null); };

  /**
   * 获取单个自习室的座位实时状态与使用详情
   * @param {number} roomId 自习室ID (必填)
   * @returns {Promise<RoomDetailVO>}
   */
  DashboardAPI.getRoomDetail = function (roomId) { return Request.get('/dashboard/room-detail/' + roomId); };

  /**
   * 获取所有自习室的使用概览（批量）
   * @returns {Promise<RoomOverviewVO[]>}
   */
  DashboardAPI.getRoomOverview = function () { return Request.get('/dashboard/room-overview'); };

  window.DashboardAPI = DashboardAPI;
})();
