export function authHeaders() {
  const t = localStorage.getItem('access_token') || '';
  const h: Record<string, string> = { 'Content-Type': 'application/json' };
  if (t) h['Authorization'] = `Bearer ${t}`;
  return h;
}

export async function get<T = any>(url: string): Promise<T> {
  const resp = await fetch(url, { headers: authHeaders() });
  return resp.json();
}

export async function post<T = any>(url: string, body?: any): Promise<T> {
  const resp = await fetch(url, { method: 'POST', headers: authHeaders(), body: body ? JSON.stringify(body) : undefined });
  return resp.json();
}

export async function put<T = any>(url: string, body?: any): Promise<T> {
  const resp = await fetch(url, { method: 'PUT', headers: authHeaders(), body: body ? JSON.stringify(body) : undefined });
  return resp.json();
}

export async function del<T = any>(url: string): Promise<T> {
  const resp = await fetch(url, { method: 'DELETE', headers: authHeaders() });
  return resp.json();
}

