import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AlertDto, AlertStatus, Metric, Severity } from '../core/models';
import { alertStatusLabelRu, metricLabelRu, severityLabelRu } from '../core/ui-labels';

@Component({
  selector: 'app-alerts-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './alerts-page.component.html',
  styleUrl: './alerts-page.component.scss'
})
export class AlertsPageComponent implements OnInit {
  readonly alerts = signal<AlertDto[]>([]);
  readonly openOnly = signal(true);

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.api.getAlerts(this.openOnly()).subscribe((alerts) => this.alerts.set(this.sortAlertsRecentFirst(alerts)));
  }

  /** Свежее сверху: по времени окончания/реакции или начала. */
  private sortAlertsRecentFirst(items: AlertDto[]): AlertDto[] {
    return [...items].sort((a, b) => {
      const ta = new Date(a.endedAt || a.startedAt).getTime();
      const tb = new Date(b.endedAt || b.startedAt).getTime();
      if (tb !== ta) return tb - ta;
      return b.id - a.id;
    });
  }

  toggleOpenOnly(): void {
    this.openOnly.set(!this.openOnly());
    this.reload();
  }

  ack(id: number): void {
    this.api.acknowledgeAlert(id).subscribe(() => this.reload());
  }

  close(id: number): void {
    this.api.closeAlert(id).subscribe(() => this.reload());
  }

  metricRu(m: Metric): string {
    return metricLabelRu(m);
  }

  severityRu(s: Severity): string {
    return severityLabelRu(s);
  }

  statusRu(s: AlertStatus): string {
    return alertStatusLabelRu(s);
  }
}
