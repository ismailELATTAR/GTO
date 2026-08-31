import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// These interfaces mirror the backend's DTOs exactly (exercise/dto/*.java).
// ExerciseDetail is deliberately missing `explanation` and each option's
// `correct`/`correctFrequency` — that's the answer key, only revealed in
// AttemptResponse after submitting.
export type ExerciseType =
  | 'MULTIPLE_CHOICE'
  | 'NUMERICAL'
  | 'RANGE_SELECTION'
  | 'RANGE_CONSTRUCTION'
  | 'HAND_READING'
  | 'EQUITY_ESTIMATION'
  | 'POT_ODDS'
  | 'EV_CALCULATION'
  | 'STREET_DECISION'
  | 'FULL_HAND';

export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';

export interface ExerciseOption {
  id: string;
  label: string;
}

export interface ExerciseDetail {
  id: string;
  type: ExerciseType;
  difficulty: Difficulty;
  prompt: string;
  xpReward: number;
  options: ExerciseOption[];
  /** Display-only, e.g. "%"/"BB" — only set for `NUMERICAL` exercises. Not the answer key. */
  answerUnit: string | null;
}

export interface AttemptRequest {
  selectedOptionId?: string;
  numericAnswer?: number;
  responseTimeMs?: number;
}

export interface AttemptOptionResult {
  id: string;
  label: string;
  correct: boolean;
  correctFrequency: number | null;
}

export interface AttemptResponse {
  correct: boolean;
  score: number;
  explanation: string;
  options: AttemptOptionResult[];
  /** Only set for `NUMERICAL` exercises — the answer key, safe now that an attempt exists. */
  correctAnswer: number | null;
}

@Injectable({ providedIn: 'root' })
export class ExerciseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  getNext(topicId?: string, difficulty?: Difficulty, type?: ExerciseType): Observable<ExerciseDetail> {
    const params: Record<string, string> = {};
    if (topicId) params['topicId'] = topicId;
    if (difficulty) params['difficulty'] = difficulty;
    if (type) params['type'] = type;
    return this.http.get<ExerciseDetail>(`${this.baseUrl}/exercises/next`, { params });
  }

  getExercise(id: string): Observable<ExerciseDetail> {
    return this.http.get<ExerciseDetail>(`${this.baseUrl}/exercises/${id}`);
  }

  submitAttempt(id: string, request: AttemptRequest): Observable<AttemptResponse> {
    return this.http.post<AttemptResponse>(`${this.baseUrl}/exercises/${id}/attempt`, request);
  }
}
