import { AlertStatus, Metric, RoleCode, Severity } from './models';

const ROLE_ORDER: RoleCode[] = ['ADMIN', 'AGRONOMIST', 'OPERATOR', 'VIEWER'];

const ROLE_RU: Record<RoleCode, string> = {
  ADMIN: 'Администратор',
  AGRONOMIST: 'Агроном',
  OPERATOR: 'Оператор',
  VIEWER: 'Наблюдатель'
};

export function sortRoles(roles: RoleCode[]): RoleCode[] {
  return [...roles].sort((a, b) => ROLE_ORDER.indexOf(a) - ROLE_ORDER.indexOf(b));
}

export function roleLabelRu(code: RoleCode): string {
  return ROLE_RU[code] ?? code;
}

export function rolesJoinedRu(roles: RoleCode[]): string {
  return sortRoles(roles)
    .map(roleLabelRu)
    .join(', ');
}

export function metricLabelRu(m: Metric): string {
  switch (m) {
    case 'TEMPERATURE':
      return 'Температура';
    case 'HUMIDITY':
      return 'Влажность';
    case 'LIGHT':
      return 'Освещённость';
    default:
      return m;
  }
}

export function severityLabelRu(s: Severity): string {
  switch (s) {
    case 'INFO':
      return 'Информация';
    case 'WARN':
      return 'Внимание';
    case 'CRITICAL':
      return 'Критично';
    default:
      return s;
  }
}

export function alertStatusLabelRu(st: AlertStatus): string {
  switch (st) {
    case 'OPEN':
      return 'Открыто';
    case 'ACK':
      return 'Принято к сведению';
    case 'CLOSED':
      return 'Закрыто';
    default:
      return st;
  }
}
