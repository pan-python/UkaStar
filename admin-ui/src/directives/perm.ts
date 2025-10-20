import type { DirectiveBinding } from 'vue';
import { useAuthStore } from '../stores/auth';

export const permDirective = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string>) {
    const code = binding.value;
    if (!code) return;
    const auth = useAuthStore();
    const allowed = auth.hasPerm(code);
    if (!allowed) {
      el.style.display = 'none';
    }
  }
};

