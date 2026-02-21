/**
 * Minimal multipart/form-data parser for scan upload (no external deps).
 */
function parse(buffer, contentType) {
  const fields = {};
  const files = [];
  if (!contentType || !contentType.includes('boundary=')) {
    return { fields, files };
  }
  const boundary = '--' + contentType.split('boundary=')[1].trim().replace(/[";]\s*$/, '');
  const parts = buffer.toString('utf8').split(boundary);
  for (let i = 0; i < parts.length; i++) {
    const part = parts[i].trim();
    if (!part || part === '--') continue;
    const endOfHeaders = part.indexOf('\r\n\r\n');
    if (endOfHeaders === -1) continue;
    const headers = part.slice(0, endOfHeaders);
    const body = part.slice(endOfHeaders + 4);
    const nameMatch = headers.match(/name="([^"]+)"/);
    const name = nameMatch ? nameMatch[1] : null;
    const filenameMatch = headers.match(/filename="([^"]*)"/);
    if (name) {
      const value = body.replace(/\r\n$/, '').trim();
      fields[name] = { value };
    }
  }
  return { fields, files };
}

module.exports = { parse };
