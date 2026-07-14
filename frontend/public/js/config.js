/**
 * 环境配置文件
 * 高校自习室智能预约系统 - 前端公共配置
 */
(function () {
  'use strict';

  // 已禁用 Mock 模式（禁止使用任何本地模拟数据，所有请求必须走真实后端）
  // 历史说明：旧版本曾支持 params.get('mock') === 'true' 开启 Mock，现彻底下线

  window.AppConfig = {
    /** 后端 API 基地址 */
    apiBaseUrl: 'http://localhost:8081/api',

    /** 是否启用 Mock 数据 */
    useMock: false,



    /** 登录页路径 */
    loginUrl: '/public/login.html',

    /** localStorage 键名 */
    storageKeys: {
      token: 'study_room_token',
      refreshToken: 'study_room_refresh_token',
      user: 'study_room_user',
      theme: 'study_room_theme',
    },

    /** Toast 消息默认显示时长（毫秒） */
    toastDuration: 3000,

    /** 默认分页大小 */
    defaultPageSize: 20,

    /** 看板自动刷新间隔（毫秒） */
    dashboardRefreshInterval: 30000,
  };

  /** 自动推导前端工程根目录（兼容 file:// 双击打开 和 http 静态服务器；对目录名/移动位置零依赖） */
  (function () {
    // 原理：config.js 永远位于 {frontend_root}/public/js/config.js
    // 所以拿到当前 script 的 src，向上退两级（/public/js/config.js → /public/ → /）就是根目录
    var scriptSrc;
    if (document.currentScript && document.currentScript.src) {
      scriptSrc = document.currentScript.src;
    } else {
      // 兼容极端场景：降级用最后一个 <script> 的 src，或当前页面目录 + ./js/config.js
      var scripts = document.getElementsByTagName('script');
      var selfScript = scripts[scripts.length - 1];
      scriptSrc = (selfScript && selfScript.src) || (window.location.href.replace(/[^/]+$/, '') + 'js/config.js');
    }
    // 统一用 / 分隔，去掉末尾 query/hash
    var normalized = scriptSrc.replace(/\\/g, '/').replace(/[?#].*$/, '');
    // 去掉文件名 /config.js，留在 {root}/public/js/
    var withoutFile = normalized.substring(0, normalized.lastIndexOf('/') + 1);
    // 再上两级：/js/ → /public/ → / （根）
    withoutFile = withoutFile.replace(/\/$/, '');
    var up1 = withoutFile.substring(0, withoutFile.lastIndexOf('/') + 1);  // → {root}/public/
    var up2 = up1.substring(0, up1.replace(/\/$/, '').lastIndexOf('/') + 1); // → {root}/
    window._BaseHref = up2;
  })();

  /** 页面基础路径（相对于 frontend 根目录，会自动拼 _BaseHref 前缀） */
  window.BasePath = {
    public: _BaseHref + 'public',
    student: _BaseHref + 'student',
    admin: _BaseHref + 'admin',
  };

  // 同步更新 loginUrl，让 401 跳转也兼容 file 协议
  AppConfig.loginUrl = _BaseHref + 'public/login.html';
})();
