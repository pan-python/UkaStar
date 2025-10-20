import { defineStore } from 'pinia';

export interface RbacProfile {
  accountId: number;
  tenantId: number;
  username: string;
  dataScope: string;
  roles: string[];
  menuPermissions: string[];
  buttonPermissions: string[];
  apiPermissions: string[];
  menus: Array<{ code:string; name:string; path:string; icon:string; children: any[] }>;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    profile: null as RbacProfile | null
  }),
  actions: {
    async fetchProfile() {
      try {
        const t = localStorage.getItem('access_token') || '';
        const resp = await fetch('/api/rbac/profile', { headers: { 'Authorization': `Bearer ${t}` } });
        const data = await resp.json();
        if (resp.ok && data?.code === 'OK') {
          this.profile = data.data as RbacProfile;
        }
      } catch {}
    },
    hasPerm(code: string) {
      return !!this.profile?.apiPermissions?.includes(code) || !!this.profile?.buttonPermissions?.includes(code) || !!this.profile?.menuPermissions?.includes(code);
    }
  }
});

