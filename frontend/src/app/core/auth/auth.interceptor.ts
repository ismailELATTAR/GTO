import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionStore } from '../state/session.store';

/** Attaches the JWT access token to outgoing API requests. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(SessionStore).accessToken;
  if (!token) {
    return next(req);
  }
  return next(
    req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }),
  );
};
