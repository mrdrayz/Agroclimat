import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { GreenhouseAssignmentDto, GreenhouseDto, UserDto } from '../core/models';

@Component({
  selector: 'app-admin-greenhouse-assignments-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-greenhouse-assignments-page.component.html',
  styleUrl: './admin-greenhouse-assignments-page.component.scss'
})
export class AdminGreenhouseAssignmentsPageComponent implements OnInit {
  readonly assignments = signal<GreenhouseAssignmentDto[]>([]);
  readonly greenhouses = signal<GreenhouseDto[]>([]);
  readonly users = signal<UserDto[]>([]);
  readonly loading = signal(true);
  readonly message = signal('');

  readonly form = this.fb.group({
    greenhouseId: [null as number | null, Validators.required],
    userId: [null as number | null, Validators.required],
    accessLevel: this.fb.nonNullable.control<'VIEW' | 'MANAGE'>('VIEW', Validators.required)
  });

  constructor(
    private readonly api: ApiService,
    private readonly auth: AuthService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    forkJoin({
      assignments: this.api.getGreenhouseAssignments(),
      greenhouses: this.api.getGreenhouses(),
      users: this.api.getUsers()
    }).subscribe({
      next: ({ assignments, greenhouses, users }) => {
        this.assignments.set(assignments);
        this.greenhouses.set(greenhouses);
        this.users.set(users);
      },
      error: () => {
        this.message.set('Не удалось загрузить данные.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.api
      .upsertGreenhouseAssignment({
        greenhouseId: v.greenhouseId!,
        userId: v.userId!,
        accessLevel: v.accessLevel
      })
      .subscribe({
        next: () => {
          this.message.set('Назначение сохранено.');
          this.auth.refreshProfile().subscribe();
          this.reload();
        },
        error: () => this.message.set('Не удалось сохранить.')
      });
  }

  remove(a: GreenhouseAssignmentDto): void {
    this.api.deleteGreenhouseAssignment(a.greenhouseId, a.userId).subscribe({
      next: () => {
        this.message.set('Назначение удалено.');
        this.auth.refreshProfile().subscribe();
        this.reload();
      },
      error: () => this.message.set('Не удалось удалить.')
    });
  }

  accessRu(level: string): string {
    return level === 'MANAGE' ? 'Управление' : 'Просмотр';
  }

  formatDate(iso: string): string {
    try {
      return new Date(iso).toLocaleString('ru-RU');
    } catch {
      return iso;
    }
  }
}
