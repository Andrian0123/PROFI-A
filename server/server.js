/**
 * PROFI-A Reference Backend (Auth + Support + Scan)
 * Implements contracts from docs/BACKEND_API_CONTRACTS.md
 * Run: node server.js  →  Auth: http://localhost:3001  Support: http://localhost:3002  Scan: http://localhost:3003
 * For Android emulator use AUTH_SERVER_URL=http://10.0.2.2:3001 (etc.)
 */

const http = require('http');
const url = require('url');

// ---------- In-memory stores (dev only) ----------
const users = new Map(); // login -> { password, userId, twoFaEnabled }
let nextUserId = 1;
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

// ---------- AUTH server (port 3001) ----------
const AUTH_PORT = 3001;
const authServer = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  const path = parsed.pathname;
  const method = req.method;

  const send = (status, body, contentType = 'application/json') => {
    res.writeHead(status, { 'Content-Type': contentType });
    res.end(typeof body === 'string' ? body : JSON.stringify(body));
  };

  try {
    if (path === '/auth/login' && method === 'POST') {
      const body = await parseJson(req);
      const login = (body.login || '').trim();
      const password = (body.password || '').trim();
      if (!login || !password) {
        send(400, { error: 'login and password required' });
        return;
      }
      const user = users.get(login);
      if (!user || user.password !== password) {
        send(401, { error: 'Invalid credentials' });
        return;
      }
      send(200, createAuthResponse(user.userId, `tok-${user.userId}-${Date.now()}`, `ref-${user.userId}`));
      return;
    }

    if (path === '/auth/register' && method === 'POST') {
      const body = await parseJson(req);
      const login = (body.login || '').trim();
      const password = (body.password || '').trim();
      if (!login || !password) {
        send(400, { error: 'login and password required' });
        return;
      }
      if (users.has(login)) {
        send(409, { error: 'User already exists' });
        return;
      }
      const userId = nextUserId++;
      users.set(login, { password, userId, twoFaEnabled: false });
      send(200, createAuthResponse(userId, `tok-${userId}-${Date.now()}`, `ref-${userId}`));
      return;
    }

    if (path === '/account/change-password' && method === 'POST') {
      const body = await parseJson(req);
      const oldPassword = (body.oldPassword || '').trim();
      const newPassword = (body.newPassword || '').trim();
      if (!oldPassword || !newPassword) {
        send(400, { error: 'oldPassword and newPassword required' });
        return;
      }
      const login = findLoginByToken(req.headers.authorization) || getFirstLogin();
      if (!login) { send(401, { error: 'Unauthorized' }); return; }
      const user = users.get(login);
      if (!user || user.password !== oldPassword) {
        send(401, { error: 'Invalid old password' });
        return;
      }
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

    send(404, { error: 'Not found' });
  } catch (e) {
    send(500, { error: String(e.message) });
  }
});

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

// ---------- SUPPORT server (port 3002) ----------
const SUPPORT_PORT = 3002;
const supportServer = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  const path = parsed.pathname;
  const method = req.method;

  const send = (status, body, contentType = 'application/json') => {
    res.writeHead(status, { 'Content-Type': contentType });
    res.end(typeof body === 'string' ? body : JSON.stringify(body));
  };

  if (path === '/support/tickets' && method === 'POST') {
    try {
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
    } catch (e) {
      send(500, { error: String(e.message) });
    }
    return;
  }

  if (path === '/support/tickets' && method === 'GET') {
    send(200, { tickets: supportTickets });
    return;
  }

  send(404, { error: 'Not found' });
});

// ---------- SCAN server (port 3003) ----------
const multipart = require('./multipart.js');
const SCAN_PORT = 3003;
const scanServer = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  const path = parsed.pathname;
  const method = req.method;

  const send = (status, body, contentType = 'application/json') => {
    res.writeHead(status, { 'Content-Type': contentType });
    res.end(typeof body === 'string' ? body : JSON.stringify(body));
  };

  const stubDimensions = (scanId) => ({
    scan_id: scanId,
    dimensions: { wall_height_m: 2.7, perimeter_m: 12.0, floor_area_m2: 9.0 },
    coverage: { percentage: 85.5 },
    quality_metrics: { scan_quality: 0.92 }
  });

  if (path === '/api/v1/scan/process' && method === 'POST') {
    let scanId = '';
    let raw = [];
    req.on('data', chunk => raw.push(chunk));
    req.on('end', () => {
      const buf = Buffer.concat(raw);
      const parsedForm = multipart.parse(buf, req.headers['content-type']);
      scanId = (parsedForm.fields.scan_id && parsedForm.fields.scan_id.value) || 'scan-1';
      send(200, stubDimensions(scanId));
    });
    return;
  }

  if (path === '/api/v1/scan/finish' && method === 'POST') {
    try {
      const body = await parseJson(req);
      const scanId = String(body.scan_id || 'scan-1');
      send(200, stubDimensions(scanId));
    } catch (e) {
      send(500, { error: String(e.message) });
    }
    return;
  }

  send(404, { error: 'Not found' });
});

// ---------- Start ----------
authServer.listen(AUTH_PORT, '0.0.0.0', () => {
  console.log(`[Auth]    http://localhost:${AUTH_PORT}  (emulator: http://10.0.2.2:${AUTH_PORT})`);
});
supportServer.listen(SUPPORT_PORT, '0.0.0.0', () => {
  console.log(`[Support] http://localhost:${SUPPORT_PORT}  (emulator: http://10.0.2.2:${SUPPORT_PORT})`);
});
scanServer.listen(SCAN_PORT, '0.0.0.0', () => {
  console.log(`[Scan]    http://localhost:${SCAN_PORT}  (emulator: http://10.0.2.2:${SCAN_PORT})`);
});
