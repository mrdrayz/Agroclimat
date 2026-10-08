import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { RoleCode, UserDto, UserStatus } from '../core/models';
import { roleLabelRu, rolesJoinedRu } from '../core/ui-labels';

@Component({
  selector: 'app-admin-users-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin-users-page.component.html',
  styleUrl: './admin-users-page.component.scss'
})
export class AdminUsersPageComponent implements OnInit {
  readonly users = signal<UserDto[]>([]);
  readonly loading = signal(true);
  readonly message = signal('');
  readonly allRoles: RoleCode[] = ['ADMIN', 'AGRONOMIST', 'OPERATOR', 'VIEWER'];

  readonly createForm = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80), Validators.pattern(/^[a-zA-Zа-яА-Я0-9 .'-]+$/)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(64)]],
    roles: this.fb.nonNullable.control<RoleCode[]>(['VIEWER'], [Validators.required])
  });

  constructor(
    private readonly api: ApiService,
    private readonly auth: AuthService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  roleRu(code: RoleCode): string {
    return roleLabelRu(code);
  }

  formatUserRoles(user: UserDto): string {
    return rolesJoinedRu(user.roles);
  }

  statusRu(s: UserStatus): string {
    return s === 'ACTIVE' ? 'Активен' : 'Заблокирован';
  }

  reload(): void {
    this.loading.set(true);
    this.api.getUsers().subscribe({
      next: (users) => this.users.set(users),
      complete: () => this.loading.set(false)
    });
  }

  toggleStatus(user: UserDto): void {
    const status: UserStatus = user.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
    this.api.changeUserStatus(user.id, status).subscribe(() => this.reload());
  }

  saveRoles(user: UserDto): void {
    const sel = document.querySelector<HTMLSelectElement>(`#roles-${user.id}`);
    if (!sel) {
      return;
    }
    const selected = Array.from(sel.selectedOptions).map((o) => o.value as RoleCode);
    if (!selected.length) {
      this.message.set('Выберите хотя бы одну роль.');
      return;
    }
    this.api.updateUserRoles(user.id, selected).subscribe({
      next: () => {
        this.message.set('Роли сохранены.');
        this.reload();
        this.auth.refreshProfile().subscribe();
      },
      error: () => this.message.set('Не удалось сохранить роли.')
    });
  }

  registerUserByAdmin(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }
    const { fullName, email, password, roles } = this.createForm.getRawValue();
    this.auth.register(email ?? '', password ?? '', fullName ?? '').subscribe({
      next: (authResponse) => {
        this.api.updateUserRoles(authResponse.userId, roles ?? ['VIEWER']).subscribe({
          next: () => {
            this.message.set('Пользователь создан и роли назначены.');
            this.reload();
          }
        });
      },
      error: () => this.message.set('Ошибка создания пользователя.')
    });
  }
}
