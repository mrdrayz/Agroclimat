import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { forkJoin, Subscription, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ApiService } from '../core/api.service';
import { AlertDto, GreenhouseDto, MeasurementDto, SensorDto, ZoneDto } from '../core/models';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss'
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  readonly greenhouses = signal<GreenhouseDto[]>([]);
  readonly zones = signal<ZoneDto[]>([]);
  readonly sensors = signal<SensorDto[]>([]);
  readonly alerts = signal<AlertDto[]>([]);
  readonly latestBySensor = signal<Record<number, MeasurementDto>>({});
  readonly selectedGreenhouseId = signal<number | null>(null);
  readonly selectedZoneId = signal<number | null>(null);
  readonly loading = signal(true);

  private pollSub?: Subscription;

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
    this.pollSub = timer(0, 5000)
      .pipe(switchMap(() => this.api.getLatestMeasurements()))
      .subscribe({
        next: (list) => {
          const map: Record<number, MeasurementDto> = {};
          for (const m of list) {
            map[m.sensorId] = m;
          }
          this.latestBySensor.set(map);
        }
      });
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  reload(): void {
    this.loading.set(true);
    forkJoin({
      greenhouses: this.api.getGreenhouses(),
      zones: this.api.getZones(),
      sensors: this.api.getSensors(),
      alerts: this.api.getAlerts(true)
    }).subscribe({
      next: (data) => {
        this.greenhouses.set(data.greenhouses);
        this.zones.set(data.zones);
        this.sensors.set(data.sensors);
        this.alerts.set(data.alerts);
        const qp = this.route.snapshot.queryParamMap;
        const ghQ = qp.get('greenhouseId');
        const zoneQ = qp.get('zoneId');
        if (ghQ) {
          const gh = Number(ghQ);
          if (data.greenhouses.some((g) => g.id === gh)) {
            this.selectedGreenhouseId.set(gh);
            if (zoneQ) {
              const zid = Number(zoneQ);
              if (data.zones.some((z) => z.id === zid && z.greenhouseId === gh)) {
                this.selectedZoneId.set(zid);
              } else {
                this.selectedZoneId.set(null);
              }
            } else {
              this.selectedZoneId.set(null);
            }
            return;
          }
        }
        if (!this.selectedGreenhouseId() && data.greenhouses.length) {
          this.selectedGreenhouseId.set(data.greenhouses[0].id);
        }
      },
      complete: () => this.loading.set(false)
    });
  }

  selectGreenhouse(id: number): void {
    this.selectedGreenhouseId.set(id);
    this.selectedZoneId.set(null);
  }

  selectZone(id: number): void {
    this.selectedZoneId.set(id);
  }

  zoneSensors(zoneId: number): SensorDto[] {
    return this.sensors().filter((sensor) => sensor.zoneId === zoneId);
  }

  greenhouseZones(greenhouseId: number): ZoneDto[] {
    return this.zones().filter((zone) => zone.greenhouseId === greenhouseId);
  }

  activeAlertsForZone(zoneId: number): number {
    const sensorIds = this.zoneSensors(zoneId).map((sensor) => sensor.id);
    return this.alerts().filter((alert) => sensorIds.includes(alert.sensorId)).length;
  }

  zoneGridColumns(greenhouseId: number): string {
    const zs = this.greenhouseZones(greenhouseId);
    if (!zs.length) {
      return '1fr';
    }
    const weights = zs.map((z) => Math.max(Number(z.areaM2) || 1, 0.5));
    const sum = weights.reduce((a, b) => a + b, 0);
    return weights.map((w) => `${(w / sum).toFixed(4)}fr`).join(' ');
  }

  formatValue(sensorId: number): string {
    const m = this.latestBySensor()[sensorId];
    if (!m) {
      return '—';
    }
    const v = Number(m.value);
    if (Number.isNaN(v)) {
      return '—';
    }
    return (Math.round(v * 100) / 100).toFixed(2);
  }

  metricShort(metric: string): string {
    switch (metric) {
      case 'TEMPERATURE':
        return 'Темп.';
      case 'HUMIDITY':
        return 'Влажн.';
      case 'LIGHT':
        return 'Свет';
      default:
        return metric;
    }
  }

  /** Единица из последнего измерения или из датчика (без optional chaining в шаблоне). */
  displayUnit(sensorId: number, sensorFallbackUnit: string): string {
    const m = this.latestBySensor()[sensorId];
    return m ? m.unit : sensorFallbackUnit;
  }
}
