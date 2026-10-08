import { RoleCode } from './models';

const ALLOWED: RoleCode[] = ['ADMIN', 'AGRONOMIST', 'OPERATOR', 'VIEWER'];

export function normalizeRolesArray(raw: unknown): RoleCode[] {
  if (raw == null) {
    return [];
  }
  if (Array.isArray(raw)) {
    const out = raw
      .map((r) => String(r).trim().toUpperCase())
      .filter((r): r is RoleCode => (ALLOWED as string[]).includes(r));
    return [...new Set(out)];
  }
  if (typeof raw === 'object') {
    return normalizeRolesArray(Object.values(raw as object));
  }
  return [];
}
