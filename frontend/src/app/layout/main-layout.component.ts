import { Component, OnInit, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { rolesJoinedRu } from '../core/ui-labels';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent implements OnInit {
  private readonly auth = inject(AuthService);
  readonly userName = this.auth.userName;
  readonly rolesRu = computed(() => rolesJoinedRu(this.auth.roles()));
  readonly canManageUsers = computed(() => this.auth.hasAnyRole(['ADMIN']));
  readonly canWorkbench = computed(() => this.auth.hasAnyRole(['ADMIN', 'AGRONOMIST']));
  readonly canWarnings = computed(() => this.auth.hasAnyRole(['ADMIN', 'AGRONOMIST', 'OPERATOR']));

  ngOnInit(): void {
    this.auth.refreshProfile().subscribe();
  }

  logout(): void {
    this.auth.logout();
  }
}
