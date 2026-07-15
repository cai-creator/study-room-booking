/**
 * API 模块 - 通知管理
 * 高校自习室智能预约系统
 *
 * @typedef {Object} NotificationPreferenceVO
 * @property {boolean} bookingReminder  - 预约提醒
 * @property {boolean} checkinReminder  - 签到提醒
 * @property {boolean} systemNotice     - 系统通知
 * @property {boolean} blacklistAlert   - 黑名单预警
 */
(function () {
  'use strict';

  var NotificationAPI = {};

  /**
   * 获取当前用户的通知偏好设置
   * @returns {Promise<NotificationPreferenceVO>}
   */
  NotificationAPI.getPreference = function () { return Request.get('/notifications/preference'); };

  /**
   * 保存当前用户的通知偏好设置
   * @param {NotificationPreferenceVO} data
   * @returns {Promise<NotificationPreferenceVO>}
   */
  NotificationAPI.savePreference = function (data) { return Request.put('/notifications/preference', data); };

  /**
   * 发布系统通知（管理员专用）
   * @param {{title: string, content: string, targetRole?: string}} data
   * @returns {Promise<{sentCount: number}>}
   */
  NotificationAPI.broadcast = function (data) { return Request.post('/notifications/broadcast', data); };

  window.NotificationAPI = NotificationAPI;
})();