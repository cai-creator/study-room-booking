/**
 * 管理端导航栏脚本（单独文件确保被正常执行，避免 innerHTML 插入 script 不执行的问题）
 * 由所有管理端页面引入：<script src="../../public/js/admin-navbar.js"></script>
 */
(function () {
  'use strict';

  // ========= 全局函数注册（立即注册，供 inline onclick 立即调用）=========
  window.toggleUserMenu = function () {
    var dd = document.getElementById('user-dropdown');
    if (!dd) return;
    dd.classList.toggle('hidden');
    if (window.lucide) lucide.createIcons();
  };

  window.toggleMobileMenu = function () {
    var menu = document.getElementById('mobile-menu');
    if (!menu) return;
    menu.classList.toggle('hidden');
  };

  window.handleLogout = function () {
    if (window.AuthAPI && AuthAPI.logout) {
      AuthAPI.logout();
      return;
    }
    var tokenKey = (window.AppConfig && AppConfig.storageKeys && AppConfig.storageKeys.token) || 'study_room_token';
    var refreshTokenKey = (window.AppConfig && AppConfig.storageKeys && AppConfig.storageKeys.refreshToken) || 'study_room_refresh_token';
    var userKey = (window.AppConfig && AppConfig.storageKeys && AppConfig.storageKeys.user) || 'study_room_user';
    var loginUrl;
    if (window.AppConfig && AppConfig.loginUrl) {
      loginUrl = AppConfig.loginUrl;
    } else if (window.BasePath) {
      loginUrl = BasePath.public + '/login.html';
    } else {
      loginUrl = '../../public/login.html';
    }
    try { localStorage.removeItem(tokenKey); } catch (e) {}
    try { localStorage.removeItem(refreshTokenKey); } catch (e) {}
    try { localStorage.removeItem(userKey); } catch (e) {}
    window.location.href = loginUrl;
  };

  // 点击其他区域关闭下拉菜单
  document.addEventListener('click', function (e) {
    var trigger = document.getElementById('user-menu-trigger');
    var dd = document.getElementById('user-dropdown');
    if (dd && trigger && !trigger.contains(e.target) && !dd.contains(e.target)) {
      dd.classList.add('hidden');
    }
  });

  // ========= navbar HTML 插入后执行（填充用户信息 + 高亮当前页）=========
  window.initAdminNavbarUI = function () {
    var user = null;
    var userKey = (window.AppConfig && AppConfig.storageKeys && AppConfig.storageKeys.user) || 'study_room_user';
    try {
      var raw = localStorage.getItem(userKey);
      if (raw) user = JSON.parse(raw);
    } catch (e) { user = null; }

    if (user) {
      var avatarEl = document.getElementById('navbar-user-avatar');
      var nameEl = document.getElementById('navbar-user-name');
      var dropdownName = document.getElementById('dropdown-name');
      var dropdownRole = document.getElementById('dropdown-role');

      if (avatarEl) avatarEl.textContent = (user.realName || user.username || 'A').charAt(0);
      if (nameEl) nameEl.textContent = user.realName || user.username || '';
      if (dropdownName) dropdownName.textContent = user.realName || user.username || '';

      var roleMap = { STUDENT: '学生', ADMIN: '管理员', SUPER_ADMIN: '超级管理员' };
      if (dropdownRole) dropdownRole.textContent = roleMap[user.role] || user.role || '';
    }

    // 高亮当前页
    var currentPage = window.location.pathname.replace(/^.*[\\/]/, '');
    var links = document.querySelectorAll('.nav-link');
    links.forEach(function (link) {
      var page = link.getAttribute('data-page');
      if (!page) return;
      if (page === currentPage) {
        link.style.color = 'var(--brand-primary)';
        link.style.background = 'var(--brand-primary-lighter)';
      } else {
        link.style.color = 'var(--color-text-secondary)';
        link.style.background = 'transparent';
      }
      link.addEventListener('mouseenter', function () {
        if (link.getAttribute('data-page') !== currentPage) {
          link.style.background = 'var(--color-bg-tertiary)';
        }
      });
      link.addEventListener('mouseleave', function () {
        if (link.getAttribute('data-page') !== currentPage) {
          link.style.background = 'transparent';
        }
      });
    });

    if (window.lucide) lucide.createIcons();
  };
})();
