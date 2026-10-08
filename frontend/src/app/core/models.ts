export type RoleCode = 'ADMIN' | 'AGRONOMIST' | 'OPERATOR' | 'VIEWER';
export type UserStatus = 'ACTIVE' | 'BLOCKED';
export type Metric = 'TEMPERATURE' | 'HUMIDITY' | 'LIGHT';
export type Severity = 'INFO' | 'WARN' | 'CRITICAL';
export type AlertStatus = 'OPEN' | 'ACK' | 'CLOSED';
export type SensorStatus = 'ACTIVE' | 'INACTIVE';
export type GreenhouseAccessLevel = 'VIEW' | 'MANAGE';

export interface GreenhouseAccessEntry {
  greenhouseId: number;
  accessLevel: GreenhouseAccessLevel;
}

export interface GreenhouseAccessSummary {
  scoped: boolean;
  entries: GreenhouseAccessEntry[];
}

export interface GreenhouseAssignmentDto {
  greenhouseId: number;
  greenhouseName: string;
  userId: number;
  userEmail: string;
  accessLevel: GreenhouseAccessLevel;
  assignedAt: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  email: string;
  fullName: string;
  roles: RoleCode[];
}

/** Ответ GET /api/auth/me */
export interface CurrentUserDto {
  userId: number;
  email: string;
  fullName: string;
  roles: RoleCode[];
  greenhouseAccess: GreenhouseAccessSummary;
}

export interface UserDto {
  id: number;
  email: string;
  fullName: string;
  status: UserStatus;
  createdAt: string;
  roles: RoleCode[];
}

export interface GreenhouseDto {
  id: number;
  name: string;
  location: string;
}

export interface ZoneDto {
  id: number;
  greenhouseId: number;
  name: string;
  description: string;
  areaM2: number;
}

export interface SensorDto {
  id: number;
  zoneId: number;
  metric: Metric;
  unit: string;
  name: string;
  status: SensorStatus;
  installedAt: string;
}

export interface ThresholdRuleDto {
  id: number;
  zoneId: number;
  metric: Metric;
  unit: string;
  minValue: number;
  maxValue: number;
  enabled: boolean;
  severity: Severity;
}

export interface AlertDto {
  id: number;
  ruleId: number;
  sensorId: number;
  metric: Metric;
  severity: Severity;
  status: AlertStatus;
  startedAt: string;
  endedAt: string | null;
  lastValue: number;
  unit: string;
  message: string;
  greenhouseId: number;
  greenhouseName: string;
  zoneId: number;
  zoneName: string;
}

export interface MeasurementDto {
  id: number;
  sensorId: number;
  metric: Metric;
  value: number;
  unit: string;
  ts: string;
  source: string;
}
