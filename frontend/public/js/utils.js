/**
 * 通用工具函数
 * 高校自习室智能预约系统
 */
(function () {
  'use strict';

  var Utils = {};

  /* ========== 日期时间 ========== */

  /**
   * 格式化日期字符串
   * @param {string} dateStr - 日期字符串 (yyyy-MM-dd HH:mm:ss)
   * @param {string} [pattern] - 输出格式，默认 'MM-dd HH:mm'
   * @returns {string}
   */
  Utils.formatDate = function (dateStr, pattern) {
    if (!dateStr) return '--';
    pattern = pattern || 'MM-dd HH:mm';
    var d = new Date(dateStr.replace(/-/g, '/'));
    if (isNaN(d.getTime())) return dateStr;
    var map = {
      'yyyy': String(d.getFullYear()),
      'MM': String(d.getMonth() + 1).padStart(2, '0'),
      'dd': String(d.getDate()).padStart(2, '0'),
      'HH': String(d.getHours()).padStart(2, '0'),
      'mm': String(d.getMinutes()).padStart(2, '0'),
      'ss': String(d.getSeconds()).padStart(2, '0'),
    };
    return pattern.replace(/yyyy|MM|dd|HH|mm|ss/g, function (m) { return map[m]; });
  };

  /**
   * 提取时间部分
   * @param {string} dateStr
   * @returns {string} HH:mm
   */
  Utils.formatTime = function (dateStr) {
    return Utils.formatDate(dateStr, 'HH:mm');
  };

  /* ========== 状态映射 ========== */

  /** 座位状态中文映射 */
  var SEAT_STATUS_MAP = {
    'AVAILABLE': '空闲',
    'RESERVED': '已预约',
    'OCCUPIED': '使用中',
    'TEMPORARY_LEAVE': '暂离',
    'UNAVAILABLE': '不可用',
  };

  /** 预约状态中文映射 */
  var RESERVATION_STATUS_MAP = {
    'RESERVED': '已预约',
    'CHECKED_IN': '已签到',
    'TEMPORARY_LEAVE': '暂离',
    'COMPLETED': '已签退',
    'CHECKED_OUT': '已签退',
    'CANCELLED': '已取消',
    'NO_SHOW': '爽约',
  };

  /** 角色中文映射 */
  var ROLE_MAP = {
    'STUDENT': '学生',
    'ADMIN': '管理员',
    'SUPER_ADMIN': '超级管理员',
  };

  /**
   * 获取状态中文名
   * @param {string} status - 状态枚举值
   * @param {string} [type] - 类型: 'seat' | 'reservation' | 'role'
   * @returns {string}
   */
  Utils.getStatusLabel = function (status, type) {
    if (!status) return '--';
    var map;
    switch (type) {
      case 'seat': map = SEAT_STATUS_MAP; break;
      case 'reservation': map = RESERVATION_STATUS_MAP; break;
      case 'role': map = ROLE_MAP; break;
      default: map = SEAT_STATUS_MAP;
    }
    return map[status] || status;
  };

  /* ========== Token / User 管理 ========== */

  Utils.getToken = function () {
    return localStorage.getItem(AppConfig.storageKeys.token);
  };

  Utils.setToken = function (token) {
    localStorage.setItem(AppConfig.storageKeys.token, token);
  };

  Utils.clearToken = function () {
    localStorage.removeItem(AppConfig.storageKeys.token);
  };

  Utils.getRefreshToken = function () {
    return localStorage.getItem(AppConfig.storageKeys.refreshToken);
  };

  Utils.setRefreshToken = function (token) {
    localStorage.setItem(AppConfig.storageKeys.refreshToken, token);
  };

  Utils.clearRefreshToken = function () {
    localStorage.removeItem(AppConfig.storageKeys.refreshToken);
  };

  /** 清除所有登录态（token + refreshToken + user） */
  Utils.clearAuth = function () {
    Utils.clearToken();
    Utils.clearRefreshToken();
    Utils.clearUser();
  };

  Utils.getUser = function () {
    try {
      var raw = localStorage.getItem(AppConfig.storageKeys.user);
      return raw ? JSON.parse(raw) : null;
    } catch (e) {
      return null;
    }
  };

  Utils.setUser = function (user) {
    localStorage.setItem(AppConfig.storageKeys.user, JSON.stringify(user));
  };

  Utils.clearUser = function () {
    localStorage.removeItem(AppConfig.storageKeys.user);
  };

  /** 是否为管理员角色 */
  Utils.isAdmin = function () {
    var user = Utils.getUser();
    return user && (user.role === 'ADMIN' || user.role === 'SUPER_ADMIN');
  };

  /** 是否为超级管理员 */
  Utils.isSuperAdmin = function () {
    var user = Utils.getUser();
    return user && user.role === 'SUPER_ADMIN';
  };

  /* ========== 防抖节流 ========== */

  Utils.debounce = function (fn, delay) {
    var timer = null;
    return function () {
      var context = this, args = arguments;
      clearTimeout(timer);
      timer = setTimeout(function () { fn.apply(context, args); }, delay);
    };
  };

  Utils.throttle = function (fn, delay) {
    var last = 0;
    return function () {
      var now = Date.now();
      if (now - last >= delay) {
        last = now;
        fn.apply(this, arguments);
      }
    };
  };

  /* ========== URL 工具 ========== */

  Utils.getQueryParam = function (name) {
    var params = new URLSearchParams(window.location.search);
    return params.get(name);
  };

  /* ========== DOM 工具 ========== */

  /**
   * 显示 Toast 消息
   * @param {string} message - 消息文本
   * @param {string} [type] - 'success' | 'error' | 'warning' | 'info'
   * @param {number} [duration] - 显示时长（毫秒）
   */
  Utils.showToast = function (message, type, duration) {
    type = type || 'info';
    duration = duration || AppConfig.toastDuration;

    var container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      container.style.cssText = 'position:fixed;top:1rem;right:1rem;z-index:9999;display:flex;flex-direction:column;gap:0.5rem;';
      document.body.appendChild(container);
    }

    var colors = {
      success: { bg: 'var(--state-success-bg)', border: 'var(--state-success)', icon: 'circle-check' },
      error: { bg: 'var(--state-error-bg)', border: 'var(--state-error)', icon: 'circle-x' },
      warning: { bg: 'var(--state-warning-bg)', border: 'var(--state-warning)', icon: 'triangle-alert' },
      info: { bg: 'var(--state-info-bg)', border: 'var(--state-info)', icon: 'info' },
    };
    var c = colors[type] || colors.info;

    var toast = document.createElement('div');
    toast.style.cssText =
      'display:flex;align-items:center;gap:0.5rem;padding:0.75rem 1rem;' +
      'background:' + c.bg + ';border-left:3px solid ' + c.border + ';' +
      'border-radius:var(--radius-md);color:var(--color-text-primary);' +
      'font-size:var(--text-sm);box-shadow:var(--shadow-lg);min-width:240px;' +
      'animation:fadeIn 0.3s ease;';
    toast.innerHTML = '<i data-lucide="' + c.icon + '" style="width:16px;height:16px;flex-shrink:0;"></i>' +
      '<span style="flex:1;">' + Utils.escapeHtml(message) + '</span>';
    container.appendChild(toast);

    // 初始化 Lucide 图标
    if (window.lucide) { lucide.createIcons({ attrs: { width: 16, height: 16 } }); }

    setTimeout(function () {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s';
      setTimeout(function () { toast.remove(); }, 300);
    }, duration);
  };

  /**
   * 确认对话框
   * @param {string} message
   * @returns {Promise<boolean>}
   */
  Utils.confirm = function (message) {
    return new Promise(function (resolve) {
      var result = window.confirm(message);
      resolve(result);
    });
  };

  /** HTML 转义（防 XSS） */
  Utils.escapeHtml = function (str) {
    if (!str) return '';
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(str));
    return div.innerHTML;
  };

  /** 文本截断 */
  Utils.truncate = function (str, len) {
    if (!str) return '';
    return str.length > len ? str.slice(0, len) + '...' : str;
  };

  /** 获取当前页面文件名 */
  Utils.getCurrentPage = function () {
    var path = window.location.pathname;
    return path.substring(path.lastIndexOf('/') + 1);
  };

  /**
   * 权限守卫：要求当前登录用户角色必须在允许列表中
   *  - 未登录（无 token/user）→ 跳登录页
   *  - 已登录但角色不符 → 清除登录态 + 跳登录页（防止跨角色串页：管理员进学生端 / 学生进管理员端）
   * @param {Array<'STUDENT'|'ADMIN'|'SUPER_ADMIN'>} allowedRoles - 允许的角色列表
   * @returns {boolean} true 表示通过守卫，false 表示已触发跳转
   */
  Utils.requireRole = function (allowedRoles) {
    var token = Utils.getToken();
    var user = Utils.getUser();
    var loginUrl = (window.AppConfig && AppConfig.loginUrl) || (window.BasePath ? BasePath.public + '/login.html' : '/public/login.html');

    if (!token || !user) {
      window.location.replace(loginUrl);
      return false;
    }
    var role = user.role;
    var allow = Array.isArray(allowedRoles) && allowedRoles.indexOf(role) !== -1;
    if (!allow) {
      Utils.clearAuth();
      try { sessionStorage.setItem('roleMismatchMsg', '该账号为' + (ROLE_MAP[role] || role) + '账号，无法访问此页面'); } catch (e) {}
      window.location.replace(loginUrl);
      return false;
    }
    return true;
  };

  /**
   * 登录页自动跳转：若已登录则按角色跳到对应首页（避免停留在登录页）
   */
  Utils.redirectIfLoggedIn = function () {
    var token = Utils.getToken();
    var user = Utils.getUser();
    if (!token || !user) return false;
    var role = user.role;
    var target = (window.BasePath ? BasePath.public + '/login.html' : '/public/login.html');
    if (role === 'STUDENT') {
      target = (window.BasePath ? BasePath.student : '/student') + '/pages/home.html';
    } else if (role === 'ADMIN' || role === 'SUPER_ADMIN') {
      target = (window.BasePath ? BasePath.admin : '/admin') + '/pages/space-management.html';
    }
    window.location.replace(target);
    return true;
  };

  /** 取出并清除登录页的错误提示（角色不符等） */
  Utils.takeLoginMessage = function () {
    try {
      var msg = sessionStorage.getItem('roleMismatchMsg');
      if (msg) { sessionStorage.removeItem('roleMismatchMsg'); return msg; }
    } catch (e) {}
    return null;
  };

  /* ========== 本地偏好设置（localStorage） ========== */

  var PREF_KEY = 'app:preference:booking';
  var NOTIF_PREF_KEY = 'app:preference:notification';

  Utils.getPreference = function () {
    try {
      var raw = localStorage.getItem(PREF_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch (e) { return {}; }
  };

  Utils.setPreference = function (pref) {
    try { localStorage.setItem(PREF_KEY, JSON.stringify(pref || {})); } catch (e) {}
  };

  Utils.getNotificationPref = function () {
    try {
      var raw = localStorage.getItem(NOTIF_PREF_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch (e) { return {}; }
  };

  Utils.setNotificationPref = function (pref) {
    try { localStorage.setItem(NOTIF_PREF_KEY, JSON.stringify(pref || {})); } catch (e) {}
  };

  // 导出到全局
  window.Utils = Utils;
})();
