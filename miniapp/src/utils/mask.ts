export function maskName(name?: string){
  if (!name) return '';
  if (name.length<=1) return '*';
  if (name.length===2) return name[0] + '*';
  return name[0] + '*'.repeat(name.length-2) + name[name.length-1];
}

export function maskPhone(phone?: string){
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

