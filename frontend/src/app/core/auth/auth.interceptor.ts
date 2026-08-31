import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable, catchError, finalize, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { SessionStore } from '../state/session.store';

// Module-level (not per-call) so it's genuinely shared across every request
// this interceptor ever handles — that's what lets a burst of simultaneous
// 401s share a single in-flight refresh instead of each starting their own.
let refreshInFlight: Observable<unknown> | null = null;

/** Attaches the access token to outgoing API requests, and recovers from it having expired mid-session. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionStore);
  const authService = inject(AuthService);

  const authedReq = withAuthHeader(req, session);

  return next(authedReq).pipe(
    catchError((error: unknown) => {
      // /api/auth/* calls (login, register, the refresh call itself) are
      // never retried here — a 401 from those is a real "wrong password" /
      // "session truly gone" answer for the caller to show, not something
      // a refresh can fix. Retrying them would also risk an infinite loop.
      const isAuthEndpoint = req.url.includes('/auth/');
      if (isAuthEndpoint || !(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      refreshInFlight ??= authService.restoreSession().pipe(finalize(() => (refreshInFlight = null)));

      return refreshInFlight.pipe(
        switchMap((restored) => {
          if (!restored) {
            // Refresh failed too (refresh cookie also expired/invalid) — nothing left to retry with.
            return throwError(() => error);
          }
          return next(withAuthHeader(req, session));
        }),
      );
    }),
  );
};

function withAuthHeader(req: Parameters<HttpInterceptorFn>[0], session: SessionStore) {
  const token = session.accessToken;
  return token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
}
