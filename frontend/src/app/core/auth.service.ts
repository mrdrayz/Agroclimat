import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';
import { AuthResponse, CurrentUserDto, GreenhouseAccessSummary, RoleCode } from './models';
import { normalizeRolesArray } from './role-utils';

interface SessionState {
  token: string;
  userId: number;
  email: string;
  fullName: string;
  roles: RoleCode[];
  greenhouseAccess?: GreenhouseAccessSummary;
}

const SESSION_KEY = 'agro-session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBase = '/api';
  private readonly session = signal<SessionState | null>(this.readSession());

  readonly isAuthenticated = computed(() => !!this.session());
  readonly roles = computed(() => this.session()?.roles ?? []);
  readonly userName = computed(() => this.session()?.fullName || this.session()?.email || 'Пользователь');
  readonly token = computed(() => this.session()?.token ?? null);
  readonly greenhouseAccess = computed(
    () => this.session()?.greenhouseAccess ?? { scoped: false, entries: [] }
  );

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${this.apiBase}/auth/login`, { email, password }).pipe(
      tap((response) => this.applyAuth(response)),
      switchMap(() =>
        this.refreshProfile().pipe(
          catchError(() => of(void 0))
        )
      )
    );
  }

  register(email: string, password: string, fullName: string) {
    return this.http.post<AuthResponse>(`${this.apiBase}/auth/register`, { email, password, fullName });
  }

  refreshProfile(): Observable<void> {
    const s = this.session();
    if (!s?.token) {
      return of(void 0);
    }
    return this.http.get<CurrentUserDto>(`${this.apiBase}/auth/me`).pipe(
      tap((me) => {
        const cur = this.session();
        if (!cur) {
          return;
        }
        const next: SessionState = {
          ...cur,
          userId: me.userId,
          email: me.email,
          fullName: me.fullName,
          roles: normalizeRolesArray(me.roles),
          greenhouseAccess: me.greenhouseAccess ?? { scoped: false, entries: [] }
        };
        this.session.set(next);
        localStorage.setItem(SESSION_KEY, JSON.stringify(next));
      }),
      map(() => void 0)
    );
  }

  hasAnyRole(allowed: RoleCode[]): boolean {
    const current = this.roles();
    return allowed.some((role) => current.includes(role));
  }

  canManageGreenhouse(greenhouseId: number): boolean {
    if (this.hasAnyRole(['ADMIN'])) {
      return true;
    }
    if (!this.hasAnyRole(['AGRONOMIST'])) {
      return false;
    }
    const g = this.greenhouseAccess();
    if (!g.scoped) {
      return true;
    }
    const e = g.entries.find((x) => x.greenhouseId === greenhouseId);
    return e?.accessLevel === 'MANAGE';
  }

  canCreateGreenhouse(): boolean {
    if (this.hasAnyRole(['ADMIN'])) {
      return true;
    }
    if (!this.hasAnyRole(['AGRONOMIST'])) {
      return false;
    }
    return !this.greenhouseAccess().scoped;
  }

  canUseWorkbenchEdits(): boolean {
    if (this.hasAnyRole(['ADMIN'])) {
      return true;
    }
    if (!this.hasAnyRole(['AGRONOMIST'])) {
      return false;
    }
    const g = this.greenhouseAccess();
    if (!g.scoped) {
      return true;
    }
    return g.entries.some((e) => e.accessLevel === 'MANAGE');
  }

  logout(): void {
    this.session.set(null);
    localStorage.removeItem(SESSION_KEY);
    this.router.navigate(['/auth']);
  }

  private applyAuth(response: AuthResponse): void {
    const newSession: SessionState = {
      token: response.token,
      userId: response.userId,
      email: response.email,
      fullName: response.fullName,
      roles: normalizeRolesArray(response.roles)
    };
    this.session.set(newSession);
    localStorage.setItem(SESSION_KEY, JSON.stringify(newSession));
  }

  private readSession(): SessionState | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as SessionState;
      return {
        ...parsed,
        roles: normalizeRolesArray(parsed.roles)
      };
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }
}
