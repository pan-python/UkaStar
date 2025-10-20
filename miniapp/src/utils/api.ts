export function authHeaders() {
  const t = uni.getStorageSync('access_token') || '';
  const h: Record<string, string> = { 'Content-Type': 'application/json' };
  if (t) h['Authorization'] = `Bearer ${t}`;
  return h;
}

export async function get<T=any>(url:string): Promise<T> {
  const [err, res] = await uni.request({ url, method:'GET', header: authHeaders() }) as any;
  if (err) throw err;
  return res.data;
}

export async function post<T=any>(url:string, data?:any): Promise<T> {
  const [err, res] = await uni.request({ url, method:'POST', data, header: authHeaders() }) as any;
  if (err) throw err;
  return res.data;
}

