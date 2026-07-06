/**
 * HTTP 请求封装
 * 高校自习室智能预约系统
 * 基于 fetch + Promise，封装了 token 注入、响应解包、401 拦截
 */
(function () {
  'use strict';

  var Request = {};

  /** 请求超时时间（毫秒） */
  var TIMEOUT = 15000;

  /**
   * 发起请求
   * @param {string} method  - HTTP 方法
   * @param {string} url     - 接口路径（如 /auth/login）
   * @param {object} [data]  - 请求体（GET 请求时作为 query params）
   * @returns {Promise}      - resolve 时返回解包后的 data 字段
   */
  function request(method, url, data) {
    // Mock 模式下由各 API 模块自行处理，不会走到这里
    var fullUrl = AppConfig.apiBaseUrl + url;
    var options = {
      method: method,
      headers: {
        'Content-Type': 'application/json',
      },
    };

    // 注入 Token（登录接口不需要）
    if (!url.startsWith('/auth/login') && !url.startsWith('/auth/cas')) {
      var token = Utils.getToken();
      if (token) {
        options.headers['Authorization'] = 'Bearer ' + token;
      }
    }

    // 处理请求体
    if (method === 'GET') {
      if (data) {
        var query = buildQuery(data);
        if (query) fullUrl += '?' + query;
      }
    } else if (data instanceof FormData) {
      options.body = data;
      delete options.headers['Content-Type']; // 让浏览器自动设置 multipart
    } else if (data) {
      options.body = JSON.stringify(data);
    }

    return new Promise(function (resolve, reject) {
      var timer = setTimeout(function () {
        reject({ code: 0, message: '请求超时，请稍后重试' });
      }, TIMEOUT);

      fetch(fullUrl, options)
        .then(function (response) {
          clearTimeout(timer);
          // 处理非 JSON 响应（如文件下载）
          var contentType = response.headers.get('content-type');
          if (contentType && contentType.indexOf('application/json') === -1) {
            return response.blob().then(function (blob) {
              return { blob: blob, filename: getFilename(response) };
            });
          }
          return response.json();
        })
        .then(function (result) {
          // 文件下载
          if (result && result.blob) {
            resolve(result);
            return;
          }
          // 统一响应解包
          if (result.code === 200) {
            resolve(result.data);
          } else if (result.code === 401) {
            // token 过期或无效
            Utils.clearToken();
            Utils.clearUser();
            window.location.href = AppConfig.loginUrl;
            reject(result);
          } else {
            reject(result);
          }
        })
        .catch(function (err) {
          clearTimeout(timer);
          if (err && err.code !== undefined) {
            reject(err);
          } else {
            reject({ code: 0, message: '网络错误，请检查网络连接' });
          }
        });
    });
  }

  /** 构建查询字符串 */
  function buildQuery(params) {
    var parts = [];
    for (var key in params) {
      if (params.hasOwnProperty(key) && params[key] !== undefined && params[key] !== null && params[key] !== '') {
        parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(params[key]));
      }
    }
    return parts.join('&');
  }

  /** 从 Content-Disposition 头提取文件名 */
  function getFilename(response) {
    var disposition = response.headers.get('content-disposition');
    if (disposition) {
      var match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
      if (match && match[1]) return match[1].replace(/['"]/g, '');
    }
    return 'download.xlsx';
  }

  // 公开方法
  Request.get = function (url, params) { return request('GET', url, params); };
  Request.post = function (url, data) { return request('POST', url, data); };
  Request.put = function (url, data) { return request('PUT', url, data); };
  Request.patch = function (url, data) { return request('PATCH', url, data); };
  Request.del = function (url) { return request('DELETE', url); };
  Request.upload = function (url, formData) { return request('POST', url, formData); };

  // 导出
  window.Request = Request;
})();
