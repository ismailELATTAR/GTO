import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

// These interfaces mirror the backend's DTOs exactly (curriculum/dto/*.java) —
// this file's only job is fetching that shape, never reshaping or adding
// business logic (that stays the backend's job, per the content-as-data principle).
export interface CourseSummary {
  id: string;
  slug: string;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface ModuleSummary {
  id: string;
  title: string;
  description: string | null;
  orderIndex: number;
}

export interface CourseDetail {
  id: string;
  slug: string;
  title: string;
  description: string | null;
  modules: ModuleSummary[];
}

export interface LessonSummary {
  id: string;
  title: string;
  orderIndex: number;
}

export interface ModuleDetail {
  id: string;
  title: string;
  description: string | null;
  courseId: string;
  lessons: LessonSummary[];
}

export interface LessonDetail {
  id: string;
  title: string;
  content: string;
  topics: string[];
}

@Injectable({ providedIn: 'root' })
export class CurriculumService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  listCourses(): Observable<CourseSummary[]> {
    return this.http.get<CourseSummary[]>(`${this.baseUrl}/courses`);
  }

  getCourse(id: string): Observable<CourseDetail> {
    return this.http.get<CourseDetail>(`${this.baseUrl}/courses/${id}`);
  }

  getModule(id: string): Observable<ModuleDetail> {
    return this.http.get<ModuleDetail>(`${this.baseUrl}/modules/${id}`);
  }

  getLesson(id: string): Observable<LessonDetail> {
    return this.http.get<LessonDetail>(`${this.baseUrl}/lessons/${id}`);
  }
}
