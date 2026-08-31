import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { CurriculumService } from '../../../core/api/curriculum.service';

@Component({
  selector: 'app-course-detail',
  imports: [RouterLink],
  templateUrl: './course-detail.html',
})
export class CourseDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly curriculumService = inject(CurriculumService);

  // route.paramMap emits again whenever the :id in the URL changes (e.g.
  // navigating from one course straight to another) — switchMap re-fetches
  // each time and cancels any still-in-flight previous request.
  protected readonly course = toSignal(
    this.route.paramMap.pipe(switchMap((params) => this.curriculumService.getCourse(params.get('id')!))),
  );
}
