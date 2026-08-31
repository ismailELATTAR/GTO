import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'auth/login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'auth/register',
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
    canActivate: [authGuard],
  },
  {
    path: 'curriculum',
    loadComponent: () => import('./features/curriculum/curriculum').then((m) => m.Curriculum),
    canActivate: [authGuard],
  },
  {
    path: 'courses/:id',
    loadComponent: () =>
      import('./features/curriculum/course-detail/course-detail').then((m) => m.CourseDetail),
    canActivate: [authGuard],
  },
  {
    path: 'modules/:id',
    loadComponent: () =>
      import('./features/curriculum/module-detail/module-detail').then((m) => m.ModuleDetail),
    canActivate: [authGuard],
  },
  {
    path: 'lesson/:id',
    loadComponent: () => import('./features/lesson/lesson').then((m) => m.Lesson),
    canActivate: [authGuard],
  },
  {
    path: 'practice',
    loadComponent: () =>
      import('./features/exercise/engine/exercise-entry').then((m) => m.ExerciseEntry),
    canActivate: [authGuard],
  },
  {
    path: 'exercise/:id',
    loadComponent: () =>
      import('./features/exercise/engine/exercise-runner').then((m) => m.ExerciseRunner),
    canActivate: [authGuard],
  },
  {
    path: 'hand-trainer',
    loadComponent: () => import('./features/hand-trainer/hand-trainer').then((m) => m.HandTrainer),
    canActivate: [authGuard],
  },
  {
    path: 'equity-calculator',
    loadComponent: () =>
      import('./features/equity-calculator/equity-calculator').then((m) => m.EquityCalculator),
    canActivate: [authGuard],
  },
  {
    path: 'progress',
    loadComponent: () => import('./features/progress/progress').then((m) => m.ProgressPage),
    canActivate: [authGuard],
  },
  {
    path: 'achievements',
    loadComponent: () =>
      import('./features/achievements/achievements').then((m) => m.Achievements),
    canActivate: [authGuard],
  },
  {
    path: 'poker-demo',
    loadComponent: () => import('./features/poker-demo/poker-demo').then((m) => m.PokerDemo),
    canActivate: [authGuard],
  },
  { path: '**', redirectTo: 'dashboard' },
];
