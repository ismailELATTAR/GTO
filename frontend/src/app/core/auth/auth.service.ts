import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthenticatedUser, SessionStore } from '../state/session.store';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface AuthResponse {
  accessToken: string;
  user: AuthenticatedUser;
}

/**
 * Talks to /api/auth/* (backed by Keycloak — see ARCHITECTURE.md §1a).
 * `withCredentials: true` on every call is what lets the browser send/accept
 * the httpOnly refreshToken cookie: frontend (:4200) and backend (:8080) are
 * different origins in dev, so cookies aren't included by default.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionStore);
  private readonly router = inject(Router);
  private readonly baseUrl = `${environment.apiBaseUrl}/auth`;

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/login`, request, { withCredentials: true })
      .pipe(tap((res) => this.session.setSession(res.user, res.accessToken)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/register`, request, { withCredentials: true })
      .pipe(tap((res) => this.session.setSession(res.user, res.accessToken)));
  }

  /**
   * Exchanges the refresh cookie for a new access token — used both on app
   * startup (so a page reload doesn't force a re-login) and by the auth
   * interceptor when an access token has expired mid-session. Never throws:
   * no valid cookie just means "not logged in", a normal outcome the caller
   * doesn't need to catch — and it clears any stale session state, so a
   * refresh failure mid-session correctly logs the user out rather than
   * leaving SessionStore pointing at a user who no longer has a valid token.
   */
  restoreSession(): Observable<AuthResponse | null> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/refresh`, {}, { withCredentials: true })
      .pipe(
        tap((res) => this.session.setSession(res.user, res.accessToken)),
        catchError(() => {
          this.session.clearSession();
          return of(null);
        }),
      );
  }

  /**
   * Centralized here (rather than left to each caller) so every place that
   * triggers a logout — the dashboard now, a settings page later — gets the
   * redirect for free instead of risking a stale, half-authenticated screen.
   */
  logout(): void {
    this.http
      .post(`${this.baseUrl}/logout`, {}, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe(() => {
        this.session.clearSession();
        this.router.navigateByUrl('/auth/login');
      });
  }
}
