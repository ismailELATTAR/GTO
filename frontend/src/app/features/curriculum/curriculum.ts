import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { CurriculumService } from '../../core/api/curriculum.service';

@Component({
  selector: 'app-curriculum',
  imports: [RouterLink],
  templateUrl: './curriculum.html',
})
export class Curriculum {
  private readonly curriculumService = inject(CurriculumService);

  // toSignal turns the HTTP Observable into a signal the template reads
  // directly — no manual subscribe()/unsubscribe(), and it's cleaned up
  // automatically when this component is destroyed.
  protected readonly courses = toSignal(this.curriculumService.listCourses(), { initialValue: [] });
}
