import { Injectable, computed, signal } from '@angular/core';

export interface AuthenticatedUser {
  id: string;
  email: string;
  displayName: string;
}

/**
 * Minimal cross-cutting session state (auth status + current user).
 * Deliberately not NgRx — this is the one piece of state that's
 * genuinely global; everything else stays local to its feature.
 */
@Injectable({ providedIn: 'root' })
export class SessionStore {
  private readonly _user = signal<AuthenticatedUser | null>(null);
  private readonly _accessToken = signal<string | null>(null);

  readonly user = this._user.asReadonly();
  readonly isAuthenticated = computed(() => this._user() !== null);

  setSession(user: AuthenticatedUser, accessToken: string): void {
    this._user.set(user);
    this._accessToken.set(accessToken);
  }

  clearSession(): void {
    this._user.set(null);
    this._accessToken.set(null);
  }

  get accessToken(): string | null {
    return this._accessToken();
  }
}
