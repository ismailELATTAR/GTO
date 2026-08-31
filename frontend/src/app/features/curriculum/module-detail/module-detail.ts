import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { CurriculumService } from '../../../core/api/curriculum.service';

@Component({
  selector: 'app-module-detail',
  imports: [RouterLink],
  templateUrl: './module-detail.html',
})
export class ModuleDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly curriculumService = inject(CurriculumService);

  protected readonly module = toSignal(
    this.route.paramMap.pipe(switchMap((params) => this.curriculumService.getModule(params.get('id')!))),
  );
}
