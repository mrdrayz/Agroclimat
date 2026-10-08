import { Component, OnDestroy, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subscription } from 'rxjs';
import { AuthService } from '../core/auth.service';
import { ApiService } from '../core/api.service';
import { GreenhouseDto, ZoneDto } from '../core/models';
import { downloadObservationsPdf, downloadObservationsXlsx } from '../core/observation-export.util';

interface ObservationReport {
  id: number;
  createdAt: string;
  author: string;
  greenhouse: string;
  zone: string;
  details: string;
}

const REPORTS_KEY = 'agro-observation-reports';
const DETAILS_MAX = 200;

@Component({
  selector: 'app-reports-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './reports-page.component.html',
  styleUrl: './reports-page.component.scss'
})
export class ReportsPageComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly api = inject(ApiService);
  private ghSub?: Subscription;
  private nextId = 1;

  readonly reports = signal<ObservationReport[]>([]);
  readonly greenhouses = signal<GreenhouseDto[]>([]);
  readonly zones = signal<ZoneDto[]>([]);
  readonly author = computed(() => this.auth.userName());
  readonly detailsMax = DETAILS_MAX;

  /** id записи, которую редактируем, или null */
  readonly editingId = signal<number | null>(null);
  readonly editDetails = signal('');
  readonly editValidationError = signal('');

  readonly form = this.fb.group({
    greenhouseId: [null as number | null, Validators.required],
    zoneId: [{ value: null as number | null, disabled: true }, Validators.required],
    details: [
      '',
      [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(DETAILS_MAX),
        Validators.pattern(/^[a-zA-Zа-яА-ЯёЁ0-9 .,;:!?()"'%\n\r-]+$/)
      ]
    ]
  });

  ngOnInit(): void {
    this.loadReports();
    this.api.getGreenhouses().subscribe((list) => this.greenhouses.set(list));
    this.ghSub = this.form.get('greenhouseId')?.valueChanges.subscribe((id) => this.applyGreenhouseSelection(id));
  }

  ngOnDestroy(): void {
    this.ghSub?.unsubscribe();
  }

  private applyGreenhouseSelection(id: number | null): void {
    const zoneCtrl = this.form.get('zoneId');
    if (id == null) {
      this.zones.set([]);
      zoneCtrl?.reset(null, { emitEvent: false });
      zoneCtrl?.disable({ emitEvent: false });
      return;
    }
    this.api.getZones(id).subscribe((list) => {
      this.zones.set(list);
      zoneCtrl?.enable({ emitEvent: false });
      const first = list[0]?.id ?? null;
      zoneCtrl?.setValue(first, { emitEvent: false });
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const ghId = raw.greenhouseId;
    const zId = raw.zoneId;
    const gh = this.greenhouses().find((g) => g.id === ghId);
    const z = this.zones().find((x) => x.id === zId);
    if (!gh || !z) {
      return;
    }
    const next: ObservationReport = {
      id: this.allocId(),
      createdAt: new Date().toISOString(),
      author: this.author(),
      greenhouse: gh.name,
      zone: z.name,
      details: raw.details ?? ''
    };
    const all = [next, ...this.reports()];
    this.persist(all);
    this.form.patchValue({ details: '' });
    this.form.get('details')?.markAsPristine();
  }

  startEdit(r: ObservationReport): void {
    this.editValidationError.set('');
    this.editingId.set(r.id);
    this.editDetails.set(r.details);
  }

  cancelEdit(): void {
    this.editValidationError.set('');
    this.editingId.set(null);
    this.editDetails.set('');
  }

  onEditDetailsChange(value: string): void {
    this.editDetails.set(value);
    this.editValidationError.set('');
  }

  saveEdit(id: number): void {
    const text = this.editDetails().trim();
    if (text.length < 3) {
      this.editValidationError.set('Минимум 3 символа.');
      return;
    }
    if (text.length > DETAILS_MAX) {
      this.editValidationError.set('Не длиннее ' + DETAILS_MAX + ' символов.');
      return;
    }
    const pattern = /^[a-zA-Zа-яА-ЯёЁ0-9 .,;:!?()"'%\n\r-]+$/;
    if (!pattern.test(text)) {
      this.editValidationError.set('Недопустимые символы в тексте.');
      return;
    }
    const all = this.reports().map((x) => (x.id === id ? { ...x, details: text } : x));
    this.persist(all);
    this.cancelEdit();
  }

  deleteReport(id: number): void {
    if (!confirm('Удалить эту запись из журнала?')) {
      return;
    }
    const all = this.reports().filter((x) => x.id !== id);
    this.persist(all);
    if (this.editingId() === id) {
      this.cancelEdit();
    }
  }

  exportXlsx(): void {
    const list = this.reports();
    if (list.length === 0) {
      return;
    }
    downloadObservationsXlsx(list);
  }

  exportPdf(): void {
    const list = this.reports();
    if (list.length === 0) {
      return;
    }
    void downloadObservationsPdf(list).catch(() =>
      alert('Не удалось сформировать PDF. Попробуйте экспорт в Excel.')
    );
  }

  private persist(all: ObservationReport[]): void {
    this.reports.set(all);
    localStorage.setItem(REPORTS_KEY, JSON.stringify(all));
  }

  private loadReports(): void {
    const raw = localStorage.getItem(REPORTS_KEY);
    if (!raw) {
      return;
    }
    try {
      const parsed = JSON.parse(raw) as unknown;
      if (!Array.isArray(parsed)) {
        this.reports.set([]);
        return;
      }
      const normalized = (parsed as ObservationReport[]).map((r, i) => ({
        ...r,
        id: typeof r.id === 'number' && !Number.isNaN(r.id) ? r.id : Date.now() + i
      }));
      this.reports.set(normalized);
      this.nextId =
        normalized.length === 0 ? 1 : normalized.reduce((m, r) => Math.max(m, r.id), 0) + 1;
    } catch {
      this.reports.set([]);
    }
  }

  private allocId(): number {
    const id = this.nextId++;
    return id;
  }
}
