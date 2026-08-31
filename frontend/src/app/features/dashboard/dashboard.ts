import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { SessionStore } from '../../core/state/session.store';
import { ChipStack } from '../../shared/poker/chip-stack/chip-stack';

@Component({
  selector: 'app-dashboard',
  imports: [ChipStack, RouterLink],
  templateUrl: './dashboard.html',
})
export class Dashboard {
  private readonly session = inject(SessionStore);
  private readonly authService = inject(AuthService);

  protected readonly user = this.session.user;

  // Greeting adapts to time of day rather than always saying "Hello" —
  // a small, low-cost detail that makes the page feel alive rather than static.
  protected readonly timeOfDayGreeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Morning';
    if (hour < 18) return 'Afternoon';
    return 'Evening';
  });

  // PLACEHOLDER — there is no progress/XP/streak API yet (that's Phase 6,
  // per ARCHITECTURE.md §32/§7). Every user sees this same honest zero-state
  // rather than fabricated numbers. Swap this for a `core/api` progress
  // service call once that endpoint exists; the template already reads
  // from here rather than hardcoding values inline, so that'll be a
  // one-place change.
  protected readonly stats = {
    level: { value: '1', active: false },
    accuracy: { value: '—', active: false },
    streak: { value: '0', active: false },
  };

  protected logout(): void {
    this.authService.logout();
  }
}
