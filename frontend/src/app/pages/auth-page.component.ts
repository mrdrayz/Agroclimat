import { Component, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../core/auth.service';

/** Буквы (включая ё/Ё), цифры, пробел, точка, апостроф, дефис — как в русских ФИО. */
const FULL_NAME_PATTERN = /^[a-zA-Zа-яА-ЯёЁ0-9 .'\-]+$/;

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth-page.component.html',
  styleUrl: './auth-page.component.scss'
})
export class AuthPageComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly mode = signal<'login' | 'register'>('login');
  readonly error = signal('');
  readonly loading = signal(false);

  /** После успешной регистрации: показ на вкладке «Вход» с обратным отсчётом. */
  readonly registerSuccess = signal<{ secondsLeft: number } | null>(null);

  private lastRegisteredEmail = '';
  private successDismissTimer?: ReturnType<typeof setInterval>;

  readonly loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
    password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(64)]]
  });

  readonly registerForm = this.fb.group(
    {
      fullName: [
        '',
        [Validators.required, Validators.minLength(3), Validators.maxLength(120), Validators.pattern(FULL_NAME_PATTERN)]
      ],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
      password: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(64)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: AuthPageComponent.passwordsMatchValidator }
  );

  ngOnDestroy(): void {
    this.clearSuccessTimer();
  }

  switchMode(mode: 'login' | 'register'): void {
    this.mode.set(mode);
    this.error.set('');
  }

  login(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    const { email, password } = this.loginForm.getRawValue();
    this.auth.login(email ?? '', password ?? '').subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: () => {
        this.error.set('Ошибка входа. Проверьте логин и пароль.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false)
    });
  }

  register(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    const { fullName, email, password } = this.registerForm.getRawValue();
    this.loading.set(true);
    this.error.set('');
    this.auth
      .register(email ?? '', password ?? '', (fullName ?? '').trim())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.lastRegisteredEmail = email ?? '';
          this.registerForm.reset();
          this.mode.set('login');
          this.loginForm.patchValue({ email: this.lastRegisteredEmail, password: '' });
          this.loginForm.get('email')?.markAsPristine();
          this.loginForm.get('password')?.markAsPristine();
          this.startSuccessBanner();
        },
        error: () =>
          this.error.set('Не удалось зарегистрироваться. Возможно, email уже занят или сервер недоступен.')
      });
  }

  dismissRegisterSuccess(): void {
    this.clearSuccessTimer();
    this.registerSuccess.set(null);
  }

  private startSuccessBanner(): void {
    this.clearSuccessTimer();
    this.registerSuccess.set({ secondsLeft: 10 });
    this.successDismissTimer = setInterval(() => {
      const cur = this.registerSuccess();
      if (!cur) {
        this.clearSuccessTimer();
        return;
      }
      if (cur.secondsLeft <= 1) {
        this.registerSuccess.set(null);
        this.clearSuccessTimer();
        return;
      }
      this.registerSuccess.set({ secondsLeft: cur.secondsLeft - 1 });
    }, 1000);
  }

  private clearSuccessTimer(): void {
    if (this.successDismissTimer != null) {
      clearInterval(this.successDismissTimer);
      this.successDismissTimer = undefined;
    }
  }

  private static passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
    const pass = group.get('password')?.value as string | undefined;
    const confirm = group.get('confirmPassword')?.value as string | undefined;
    if (confirm == null || confirm.length === 0) {
      return null;
    }
    return pass === confirm ? null : { passwordMismatch: true };
  }
}
