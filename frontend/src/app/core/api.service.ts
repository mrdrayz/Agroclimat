import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AlertDto,
  GreenhouseAssignmentDto,
  GreenhouseDto,
  MeasurementDto,
  Metric,
  RoleCode,
  SensorDto,
  Severity,
  ThresholdRuleDto,
  UserDto,
  UserStatus,
  ZoneDto
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiBase = '/api';

  constructor(private readonly http: HttpClient) {}

  getGreenhouses() {
    return this.http.get<GreenhouseDto[]>(`${this.apiBase}/greenhouses`);
  }
  createGreenhouse(payload: { name: string; location: string }) {
    return this.http.post<GreenhouseDto>(`${this.apiBase}/greenhouses`, payload);
  }
  updateGreenhouse(id: number, payload: { name: string; location: string }) {
    return this.http.put<GreenhouseDto>(`${this.apiBase}/greenhouses/${id}`, payload);
  }

  getZones(greenhouseId?: number) {
    const params = greenhouseId ? new HttpParams().set('greenhouseId', greenhouseId) : undefined;
    return this.http.get<ZoneDto[]>(`${this.apiBase}/zones`, { params });
  }
  createZone(payload: { greenhouseId: number; name: string; description: string; areaM2: number }) {
    return this.http.post<ZoneDto>(`${this.apiBase}/zones`, payload);
  }
  updateZone(id: number, payload: { greenhouseId: number; name: string; description: string; areaM2: number }) {
    return this.http.put<ZoneDto>(`${this.apiBase}/zones/${id}`, payload);
  }

  getSensors(zoneId?: number) {
    const params = zoneId ? new HttpParams().set('zoneId', zoneId) : undefined;
    return this.http.get<SensorDto[]>(`${this.apiBase}/sensors`, { params });
  }
  createSensor(payload: { zoneId: number; metric: Metric; name: string; status: 'ACTIVE' | 'INACTIVE' }) {
    return this.http.post<SensorDto>(`${this.apiBase}/sensors`, payload);
  }
  updateSensor(id: number, payload: { zoneId: number; metric: Metric; name: string; status: 'ACTIVE' | 'INACTIVE' }) {
    return this.http.put<SensorDto>(`${this.apiBase}/sensors/${id}`, payload);
  }

  getThresholdRules(zoneId?: number) {
    const params = zoneId ? new HttpParams().set('zoneId', zoneId) : undefined;
    return this.http.get<ThresholdRuleDto[]>(`${this.apiBase}/threshold-rules`, { params });
  }
  createThresholdRule(payload: {
    zoneId: number;
    metric: Metric;
    minValue: number;
    maxValue: number;
    enabled: boolean;
    severity: Severity;
  }) {
    return this.http.post<ThresholdRuleDto>(`${this.apiBase}/threshold-rules`, payload);
  }
  updateThresholdRule(
    id: number,
    payload: {
      zoneId: number;
      metric: Metric;
      minValue: number;
      maxValue: number;
      enabled: boolean;
      severity: Severity;
    }
  ) {
    return this.http.put<ThresholdRuleDto>(`${this.apiBase}/threshold-rules/${id}`, payload);
  }

  getMeasurements(sensorId: number): Observable<MeasurementDto[]> {
    return this.http.get<MeasurementDto[]>(`${this.apiBase}/sensors/${sensorId}/measurements`);
  }

  /** Последнее измерение по каждому датчику (для опроса ~каждые 5 с). */
  getLatestMeasurements(): Observable<MeasurementDto[]> {
    return this.http.get<MeasurementDto[]>(`${this.apiBase}/measurements/latest`);
  }

  getAlerts(openOnly = false): Observable<AlertDto[]> {
    const params = openOnly ? new HttpParams().set('openOnly', true) : undefined;
    return this.http.get<AlertDto[]>(`${this.apiBase}/alerts`, { params });
  }
  acknowledgeAlert(id: number) {
    return this.http.post<AlertDto>(`${this.apiBase}/alerts/${id}/acknowledge`, {});
  }
  closeAlert(id: number) {
    return this.http.post<AlertDto>(`${this.apiBase}/alerts/${id}/close`, {});
  }

  getUsers() {
    return this.http.get<UserDto[]>(`${this.apiBase}/users`);
  }
  changeUserStatus(id: number, status: UserStatus) {
    return this.http.patch<UserDto>(`${this.apiBase}/users/${id}/status`, null, {
      params: new HttpParams().set('status', status)
    });
  }
  updateUserRoles(id: number, roles: RoleCode[]) {
    return this.http.patch<UserDto>(`${this.apiBase}/users/${id}/roles`, { roles });
  }

  getGreenhouseAssignments() {
    return this.http.get<GreenhouseAssignmentDto[]>(`${this.apiBase}/admin/greenhouse-assignments`);
  }

  upsertGreenhouseAssignment(payload: {
    greenhouseId: number;
    userId: number;
    accessLevel: 'VIEW' | 'MANAGE';
  }) {
    return this.http.post<GreenhouseAssignmentDto>(`${this.apiBase}/admin/greenhouse-assignments`, payload);
  }

  deleteGreenhouseAssignment(greenhouseId: number, userId: number) {
    const params = new HttpParams().set('greenhouseId', greenhouseId).set('userId', userId);
    return this.http.delete<void>(`${this.apiBase}/admin/greenhouse-assignments`, { params });
  }
}
