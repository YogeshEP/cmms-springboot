const CmmsApi = (() => {
  const BASE = '';
  const TOKEN_KEY = 'cmms_token';
  const USER_KEY = 'cmms_user';
  const ROLE_KEY = 'cmms_role';

  function getToken() { return localStorage.getItem(TOKEN_KEY); }
  function getUsername() { return localStorage.getItem(USER_KEY); }
  function getRole() { return localStorage.getItem(ROLE_KEY); }

  function setToken(token, username, role) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, username);
    localStorage.setItem(ROLE_KEY, role);
  }

  function clear() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    localStorage.removeItem(ROLE_KEY);
  }

  function logout() {
    clear();
    window.location.href = '/index.html';
  }

  async function login(username, password) {
    const res = await fetch(`${BASE}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    if (!res.ok) {
      const body = await safeJson(res);
      throw new Error(body?.message || `Login failed (${res.status})`);
    }
    return res.json();
  }

  async function request(path, { method = 'GET', body } = {}) {
    const token = getToken();
    if (!token) {
      window.location.href = '/index.html';
      throw new Error('Not authenticated');
    }

    const res = await fetch(`${BASE}${path}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: body ? JSON.stringify(body) : undefined
    });

    if (res.status === 401 || res.status === 403) {
      if (res.status === 401) {
        clear();
        window.location.href = '/index.html';
      }
      const errBody = await safeJson(res);
      throw new Error(errBody?.message || `Request forbidden (${res.status}) — this action may require an ADMIN account.`);
    }

    if (!res.ok) {
      const errBody = await safeJson(res);
      throw new Error(errBody?.message || `Request failed (${res.status})`);
    }

    if (res.status === 204) return null;
    return safeJson(res);
  }

  async function safeJson(res) {
    try { return await res.json(); } catch { return null; }
  }

  const get = (path) => request(path);
  const post = (path, body) => request(path, { method: 'POST', body });
  const put = (path, body) => request(path, { method: 'PUT', body });
  const patch = (path, body) => request(path, { method: 'PATCH', body });
  const del = (path) => request(path, { method: 'DELETE' });

  return { getToken, getUsername, getRole, setToken, clear, logout, login, get, post, put, patch, del };
})();
