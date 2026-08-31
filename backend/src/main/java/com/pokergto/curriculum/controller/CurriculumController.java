package com.pokergto.curriculum.controller;

import com.pokergto.curriculum.dto.CourseDetailDto;
import com.pokergto.curriculum.dto.CourseSummaryDto;
import com.pokergto.curriculum.dto.LessonDetailDto;
import com.pokergto.curriculum.dto.ModuleDetailDto;
import com.pokergto.curriculum.service.CurriculumService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CurriculumController {

  private final CurriculumService curriculumService;

  public CurriculumController(CurriculumService curriculumService) {
    this.curriculumService = curriculumService;
  }

  @GetMapping("/courses")
  public List<CourseSummaryDto> listCourses() {
    return curriculumService.listCourses();
  }

  @GetMapping("/courses/{id}")
  public CourseDetailDto getCourse(@PathVariable UUID id) {
    return curriculumService.getCourse(id);
  }

  @GetMapping("/modules/{id}")
  public ModuleDetailDto getModule(@PathVariable UUID id) {
    return curriculumService.getModule(id);
  }

  @GetMapping("/lessons/{id}")
  public LessonDetailDto getLesson(@PathVariable UUID id) {
    return curriculumService.getLesson(id);
  }
}
