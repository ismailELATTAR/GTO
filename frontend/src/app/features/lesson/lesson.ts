import { Location } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { marked } from 'marked';
import { switchMap } from 'rxjs';
import { CurriculumService } from '../../core/api/curriculum.service';

@Component({
  selector: 'app-lesson',
  templateUrl: './lesson.html',
})
export class Lesson {
  private readonly route = inject(ActivatedRoute);
  private readonly curriculumService = inject(CurriculumService);
  private readonly location = inject(Location);

  protected goBack(): void {
    this.location.back();
  }

  protected readonly lesson = toSignal(
    this.route.paramMap.pipe(switchMap((params) => this.curriculumService.getLesson(params.get('id')!))),
  );

  // Markdown → HTML happens once here (a computed, re-run only when `lesson`
  // changes), not inline in the template on every change-detection pass.
  // Angular's [innerHTML] binding sanitizes this automatically before it
  // ever reaches the DOM, stripping any script/event-handler content.
  protected readonly renderedContent = computed(() => {
    const lesson = this.lesson();
    return lesson ? marked.parse(lesson.content, { async: false }) : '';
  });
}
