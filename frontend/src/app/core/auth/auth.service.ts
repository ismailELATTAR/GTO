import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
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
   * Exchanges the refresh cookie for a new access token — used on app
   * startup so a page reload doesn't force a re-login. Never throws: no
   * valid cookie just means "not logged in", which is a normal outcome,
   * not an error the caller needs to handle.
   */
  restoreSession(): Observable<AuthResponse | null> {
    return this.http
      .post<AuthResponse>(`${this.baseUrl}/refresh`, {}, { withCredentials: true })
      .pipe(
        tap((res) => this.session.setSession(res.user, res.accessToken)),
        catchError(() => of(null)),
      );
  }

  logout(): void {
    this.http
      .post(`${this.baseUrl}/logout`, {}, { withCredentials: true })
      .pipe(catchError(() => of(null)))
      .subscribe(() => this.session.clearSession());
  }
}
