import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { GreenhouseDto, Metric, SensorDto, Severity, ThresholdRuleDto, ZoneDto } from '../core/models';
import { metricLabelRu, severityLabelRu } from '../core/ui-labels';

@Component({
  selector: 'app-zone-workbench',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './zone-workbench-page.component.html',
  styleUrl: './zone-workbench-page.component.scss'
})
export class ZoneWorkbenchPageComponent implements OnInit {
  readonly greenhouses = signal<GreenhouseDto[]>([]);
  readonly zones = signal<ZoneDto[]>([]);
  readonly sensors = signal<SensorDto[]>([]);
  readonly rules = signal<ThresholdRuleDto[]>([]);
  readonly message = signal('');
  readonly metrics: Metric[] = ['TEMPERATURE', 'HUMIDITY', 'LIGHT'];
  readonly severities: Severity[] = ['INFO', 'WARN', 'CRITICAL'];

  readonly editingGhId = signal<number | null>(null);
  readonly editingZoneId = signal<number | null>(null);
  readonly editingSensorId = signal<number | null>(null);
  readonly editingRuleId = signal<number | null>(null);

  readonly greenhouseForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    location: ['', [Validators.required, Validators.maxLength(160)]]
  });
  readonly greenhouseEditForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    location: ['', [Validators.required, Validators.maxLength(160)]]
  });

  readonly zoneForm = this.fb.group({
    greenhouseId: [0, [Validators.required, Validators.min(1)]],
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    description: ['', [Validators.maxLength(240)]],
    areaM2: [1, [Validators.required, Validators.min(0.5), Validators.max(100000)]]
  });
  readonly zoneEditForm = this.fb.group({
    greenhouseId: [0, [Validators.required, Validators.min(1)]],
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(80)]],
    description: ['', [Validators.maxLength(240)]],
    areaM2: [1, [Validators.required, Validators.min(0.5), Validators.max(100000)]]
  });

  readonly sensorForm = this.fb.group({
    zoneId: [0, [Validators.required, Validators.min(1)]],
    metric: ['TEMPERATURE' as Metric, Validators.required],
    name: ['', [Validators.required, Validators.maxLength(80), Validators.pattern(/^[a-zA-Zа-яА-Я0-9 _.-]+$/)]],
    status: ['ACTIVE' as 'ACTIVE' | 'INACTIVE']
  });
  readonly sensorEditForm = this.fb.group({
    zoneId: [0, [Validators.required, Validators.min(1)]],
    metric: ['TEMPERATURE' as Metric, Validators.required],
    name: ['', [Validators.required, Validators.maxLength(80), Validators.pattern(/^[a-zA-Zа-яА-Я0-9 _.-]+$/)]],
    status: ['ACTIVE' as 'ACTIVE' | 'INACTIVE']
  });

  readonly ruleForm = this.fb.group({
    zoneId: [0, [Validators.required, Validators.min(1)]],
    metric: ['TEMPERATURE' as Metric, Validators.required],
    minValue: [0, [Validators.required, Validators.min(-100000), Validators.max(1000000)]],
    maxValue: [35, [Validators.required, Validators.min(-100000), Validators.max(1000000)]],
    enabled: [true],
    severity: ['WARN' as Severity, Validators.required]
  });
  readonly ruleEditForm = this.fb.group({
    zoneId: [0, [Validators.required, Validators.min(1)]],
    metric: ['TEMPERATURE' as Metric, Validators.required],
    minValue: [0, [Validators.required, Validators.min(-100000), Validators.max(1000000)]],
    maxValue: [35, [Validators.required, Validators.min(-100000), Validators.max(1000000)]],
    enabled: [true],
    severity: ['WARN' as Severity, Validators.required]
  });

  constructor(
    private readonly api: ApiService,
    readonly auth: AuthService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    forkJoin({
      greenhouses: this.api.getGreenhouses(),
      zones: this.api.getZones(),
      sensors: this.api.getSensors(),
      rules: this.api.getThresholdRules()
    }).subscribe(({ greenhouses, zones, sensors, rules }) => {
      this.greenhouses.set(greenhouses);
      this.zones.set(zones);
      this.sensors.set(sensors);
      this.rules.set(rules);
    });
  }

  createGreenhouse(): void {
    if (this.greenhouseForm.invalid) return;
    this.api.createGreenhouse(this.greenhouseForm.getRawValue() as { name: string; location: string }).subscribe(() => {
      this.message.set('Теплица добавлена.');
      this.reload();
    });
  }

  createZone(): void {
    if (this.zoneForm.invalid) return;
    this.api
      .createZone(this.zoneForm.getRawValue() as { greenhouseId: number; name: string; description: string; areaM2: number })
      .subscribe(() => {
        this.message.set('Секция добавлена.');
        this.reload();
      });
  }

  createSensor(): void {
    if (this.sensorForm.invalid) return;
    this.api
      .createSensor(this.sensorForm.getRawValue() as { zoneId: number; metric: Metric; name: string; status: 'ACTIVE' | 'INACTIVE' })
      .subscribe(() => {
        this.message.set('Датчик добавлен.');
        this.reload();
      });
  }

  createRule(): void {
    if (this.ruleForm.invalid) return;
    const raw = this.ruleForm.getRawValue() as {
      zoneId: number;
      metric: Metric;
      minValue: number;
      maxValue: number;
      enabled: boolean;
      severity: Severity;
    };
    if (raw.minValue >= raw.maxValue) {
      this.message.set('Минимум должен быть меньше максимума.');
      return;
    }
    this.api.createThresholdRule(raw).subscribe(() => {
      this.message.set('Пороговое правило добавлено.');
      this.reload();
    });
  }

  beginEditGreenhouse(g: GreenhouseDto): void {
    this.cancelAllEdits();
    this.editingGhId.set(g.id);
    this.greenhouseEditForm.patchValue({ name: g.name, location: g.location });
  }

  submitGreenhouseEdit(): void {
    const id = this.editingGhId();
    if (id == null || this.greenhouseEditForm.invalid) return;
    this.api.updateGreenhouse(id, this.greenhouseEditForm.getRawValue() as { name: string; location: string }).subscribe(() => {
      this.message.set('Теплица сохранена.');
      this.editingGhId.set(null);
      this.reload();
    });
  }

  beginEditZone(z: ZoneDto): void {
    this.cancelAllEdits();
    this.editingZoneId.set(z.id);
    this.zoneEditForm.patchValue({
      greenhouseId: z.greenhouseId,
      name: z.name,
      description: z.description ?? '',
      areaM2: z.areaM2
    });
  }

  submitZoneEdit(): void {
    const id = this.editingZoneId();
    if (id == null || this.zoneEditForm.invalid) return;
    const raw = this.zoneEditForm.getRawValue() as {
      greenhouseId: number;
      name: string;
      description: string;
      areaM2: number;
    };
    this.api.updateZone(id, raw).subscribe(() => {
      this.message.set('Секция сохранена.');
      this.editingZoneId.set(null);
      this.reload();
    });
  }

  beginEditSensor(s: SensorDto): void {
    this.cancelAllEdits();
    this.editingSensorId.set(s.id);
    this.sensorEditForm.patchValue({
      zoneId: s.zoneId,
      metric: s.metric,
      name: s.name,
      status: s.status
    });
  }

  submitSensorEdit(): void {
    const id = this.editingSensorId();
    if (id == null || this.sensorEditForm.invalid) return;
    this.api
      .updateSensor(id, this.sensorEditForm.getRawValue() as { zoneId: number; metric: Metric; name: string; status: 'ACTIVE' | 'INACTIVE' })
      .subscribe(() => {
        this.message.set('Датчик сохранён.');
        this.editingSensorId.set(null);
        this.reload();
      });
  }

  beginEditRule(r: ThresholdRuleDto): void {
    this.cancelAllEdits();
    this.editingRuleId.set(r.id);
    this.ruleEditForm.patchValue({
      zoneId: r.zoneId,
      metric: r.metric,
      minValue: r.minValue,
      maxValue: r.maxValue,
      enabled: r.enabled,
      severity: r.severity
    });
  }

  submitRuleEdit(): void {
    const id = this.editingRuleId();
    if (id == null || this.ruleEditForm.invalid) return;
    const raw = this.ruleEditForm.getRawValue() as {
      zoneId: number;
      metric: Metric;
      minValue: number;
      maxValue: number;
      enabled: boolean;
      severity: Severity;
    };
    if (raw.minValue >= raw.maxValue) {
      this.message.set('Минимум должен быть меньше максимума.');
      return;
    }
    this.api.updateThresholdRule(id, raw).subscribe(() => {
      this.message.set('Правило сохранено.');
      this.editingRuleId.set(null);
      this.reload();
    });
  }

  cancelAllEdits(): void {
    this.editingGhId.set(null);
    this.editingZoneId.set(null);
    this.editingSensorId.set(null);
    this.editingRuleId.set(null);
  }

  zoneName(zoneId: number): string {
    return this.zones().find((z) => z.id === zoneId)?.name ?? `#${zoneId}`;
  }

  greenhouseName(ghId: number): string {
    return this.greenhouses().find((g) => g.id === ghId)?.name ?? `#${ghId}`;
  }

  canManageZone(zoneId: number): boolean {
    const z = this.zones().find((x) => x.id === zoneId);
    if (!z) {
      return false;
    }
    return this.auth.canManageGreenhouse(z.greenhouseId);
  }

  metricRu(m: Metric): string {
    return metricLabelRu(m);
  }

  severityRu(s: Severity): string {
    return severityLabelRu(s);
  }
}
