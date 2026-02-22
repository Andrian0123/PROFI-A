/**
 * ПРОФЙ-А Reference Backend — один порт для облака (Render, Railway и т.д.).
 * Все пути на одном сервере: /auth/*, /support/*, /api/v1/scan/*
 * Запуск: PORT=3000 node server-single.js   или на Render/Railway (PORT задаётся автоматически).
 * В приложении: AUTH_SERVER_URL = SUPPORT_SERVER_URL = SCAN_SERVER_URL = один и тот же URL (без порта в пути).
 */

const http = require('http');
const url = require('url');
const multipart = require('./multipart.js');

const PORT = process.env.PORT || 3000;

const users = new Map();
let nextUserId = 1;
const resetTokens = new Map();
const supportTickets = [];
let nextTicketId = 1;

function createAuthResponse(userId, accessToken, refreshToken = null) {
  return JSON.stringify({
    userId: String(userId),
    accessToken: accessToken || `tok-${userId}-${Date.now()}`,
    refreshToken: refreshToken || null
  });
}

function parseJson(req) {
  return new Promise((resolve, reject) => {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      try { resolve(body ? JSON.parse(body) : {}); } catch (e) { reject(e); }
    });
    req.on('error', reject);
  });
}

function findLoginByToken(authHeader) {
  if (!authHeader || !authHeader.startsWith('Bearer ')) return null;
  const token = authHeader.slice(7);
  for (const [login, user] of users) {
    if (token.startsWith(`tok-${user.userId}-`)) return login;
  }
  return null;
}

function getFirstLogin() {
  const first = users.keys().next();
  return first.done ? null : first.value;
}

function stubDimensions(scanId) {
  return {
    scan_id: scanId,
    dimensions: { wall_height_m: 2.7, perimeter_m: 12.0, floor_area_m2: 9.0 },
    coverage: { percentage: 85.5 },
    quality_metrics: { scan_quality: 0.92 }
  };
}

const server = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  const path = parsed.pathname;
  const method = req.method;

  const send = (status, body, contentType = 'application/json') => {
    res.writeHead(status, { 'Content-Type': contentType });
    res.end(typeof body === 'string' ? body : JSON.stringify(body));
  };

  try {
    // ---------- AUTH ----------
    if (path === '/auth/login' && method === 'POST') {
      const body = await parseJson(req);
      const login = (body.login || '').trim();
      const password = (body.password || '').trim();
      if (!login || !password) { send(400, { error: 'login and password required' }); return; }
      const user = users.get(login);
      if (!user || user.password !== password) { send(401, { error: 'Invalid credentials' }); return; }
      send(200, createAuthResponse(user.userId, `tok-${user.userId}-${Date.now()}`, `ref-${user.userId}`));
      return;
    }
    if (path === '/auth/register' && method === 'POST') {
      const body = await parseJson(req);
      const login = (body.login || '').trim();
      const password = (body.password || '').trim();
      if (!login || !password) { send(400, { error: 'login and password required' }); return; }
      if (users.has(login)) { send(409, { error: 'User already exists' }); return; }
      const userId = nextUserId++;
      users.set(login, { password, userId, twoFaEnabled: false });
      send(200, createAuthResponse(userId, `tok-${userId}-${Date.now()}`, `ref-${userId}`));
      return;
    }
    if (path === '/account/change-password' && method === 'POST') {
      const body = await parseJson(req);
      const oldPassword = (body.oldPassword || '').trim();
      const newPassword = (body.newPassword || '').trim();
      if (!oldPassword || !newPassword) { send(400, { error: 'oldPassword and newPassword required' }); return; }
      const login = findLoginByToken(req.headers.authorization) || getFirstLogin();
      if (!login) { send(401, { error: 'Unauthorized' }); return; }
      const user = users.get(login);
      if (!user || user.password !== oldPassword) { send(401, { error: 'Invalid old password' }); return; }
      user.password = newPassword;
      send(200, {});
      return;
    }
    if (path === '/account/2fa' && method === 'POST') {
      const body = await parseJson(req);
      const enabled = Boolean(body.enabled);
      const login = findLoginByToken(req.headers.authorization) || getFirstLogin();
      if (!login) { send(401, { error: 'Unauthorized' }); return; }
      const user = users.get(login);
      if (user) user.twoFaEnabled = enabled;
      send(200, {});
      return;
    }
    if (path === '/account/delete' && method === 'POST') {
      const login = findLoginByToken(req.headers.authorization) || getFirstLogin();
      if (login) users.delete(login);
      send(200, {});
      return;
    }
    if (path === '/auth/request-reset' && method === 'POST') {
      const body = await parseJson(req);
      const loginOrEmail = (body.login || body.email || '').trim();
      if (!loginOrEmail) { send(400, { error: 'login or email required' }); return; }
      const user = users.get(loginOrEmail);
      if (!user) { send(200, { message: 'If account exists, reset instructions were sent' }); return; }
      const token = 'reset-' + Date.now() + '-' + Math.random().toString(36).slice(2);
      resetTokens.set(token, { login: loginOrEmail, expires: Date.now() + 3600000 });
      send(200, { message: 'If account exists, reset instructions were sent', resetToken: token });
      return;
    }
    if (path === '/auth/reset-password' && method === 'POST') {
      const body = await parseJson(req);
      const token = (body.token || '').trim();
      const newPassword = (body.newPassword || '').trim();
      if (!token || !newPassword) { send(400, { error: 'token and newPassword required' }); return; }
      const data = resetTokens.get(token);
      if (!data || data.expires < Date.now()) { send(400, { error: 'Invalid or expired reset token' }); return; }
      resetTokens.delete(token);
      const user = users.get(data.login);
      if (user) user.password = newPassword;
      send(200, {});
      return;
    }

    // ---------- SUPPORT ----------
    if (path === '/support/tickets' && method === 'POST') {
      const body = await parseJson(req);
      const ticket = {
        id: nextTicketId++,
        phone: String(body.phone || ''),
        email: String(body.email || ''),
        description: String(body.description || ''),
        status: 'received',
        createdAt: new Date().toISOString()
      };
      supportTickets.push(ticket);
      send(200, { id: ticket.id, status: ticket.status });
      return;
    }
    if (path === '/support/tickets' && method === 'GET') {
      send(200, { tickets: supportTickets });
      return;
    }

    // ---------- SCAN ----------
    if (path === '/api/v1/scan/process' && method === 'POST') {
      let raw = [];
      req.on('data', chunk => raw.push(chunk));
      req.on('end', () => {
        const buf = Buffer.concat(raw);
        const parsedForm = multipart.parse(buf, req.headers['content-type']);
        const scanId = (parsedForm.fields.scan_id && parsedForm.fields.scan_id.value) || 'scan-1';
        send(200, stubDimensions(scanId));
      });
      return;
    }
    if (path === '/api/v1/scan/finish' && method === 'POST') {
      const body = await parseJson(req);
      const scanId = String(body.scan_id || 'scan-1');
      send(200, stubDimensions(scanId));
      return;
    }

    send(404, { error: 'Not found' });
  } catch (e) {
    send(500, { error: String(e.message) });
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[PROFIA] All-in-one backend http://localhost:${PORT}  (Auth, Support, Scan)`);
});
