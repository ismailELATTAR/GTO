package com.pokergto.curriculum.service;

import com.pokergto.common.exception.ApiException;
import com.pokergto.curriculum.model.Course;
import com.pokergto.curriculum.model.CourseModule;
import com.pokergto.curriculum.model.Lesson;
import com.pokergto.curriculum.dto.CourseDetailDto;
import com.pokergto.curriculum.dto.CourseSummaryDto;
import com.pokergto.curriculum.dto.LessonDetailDto;
import com.pokergto.curriculum.dto.ModuleDetailDto;
import com.pokergto.curriculum.mapper.CurriculumMapper;
import com.pokergto.curriculum.repository.CourseModuleRepository;
import com.pokergto.curriculum.repository.CourseRepository;
import com.pokergto.curriculum.repository.LessonRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only content browsing (§6's GET /api/courses, /api/courses/{id},
 * /api/modules/{id}, /api/lessons/{id}). Only ever returns published
 * content — unpublished rows exist for future admin-authoring workflows
 * (§29) but are invisible to this API until an admin surface exists. That
 * guarantee is enforced by walking the FULL parent chain (lesson → module →
 * course), not just the row being fetched — a lesson under an unpublished
 * course must stay invisible even if the lesson row itself is flagged published.
 */
@Service
@Transactional(readOnly = true)
public class CurriculumService {

  private final CourseRepository courseRepository;
  private final CourseModuleRepository moduleRepository;
  private final LessonRepository lessonRepository;
  private final CurriculumMapper mapper;

  public CurriculumService(
      CourseRepository courseRepository,
      CourseModuleRepository moduleRepository,
      LessonRepository lessonRepository,
      CurriculumMapper mapper) {
    this.courseRepository = courseRepository;
    this.moduleRepository = moduleRepository;
    this.lessonRepository = lessonRepository;
    this.mapper = mapper;
  }

  public List<CourseSummaryDto> listCourses() {
    return courseRepository.findByPublishedTrueOrderByOrderIndexAsc().stream().map(mapper::toSummary).toList();
  }

  public CourseDetailDto getCourse(UUID courseId) {
    Course course = findPublishedCourse(courseId);
    List<CourseModule> modules =
        moduleRepository.findByCourseIdAndPublishedTrueAndCourse_PublishedTrueOrderByOrderIndexAsc(courseId);
    return new CourseDetailDto(
        course.getId(),
        course.getSlug(),
        course.getTitle(),
        course.getDescription(),
        modules.stream().map(mapper::toSummary).toList());
  }

  public ModuleDetailDto getModule(UUID moduleId) {
    CourseModule module = findPublishedModule(moduleId);
    List<Lesson> lessons = lessonRepository.findByModuleIdAndPublishedTrueOrderByOrderIndexAsc(moduleId);
    return new ModuleDetailDto(
        module.getId(),
        module.getTitle(),
        module.getDescription(),
        module.getCourse().getId(),
        lessons.stream().map(mapper::toSummary).toList());
  }

  public LessonDetailDto getLesson(UUID lessonId) {
    Lesson lesson =
        lessonRepository
            .findByIdAndPublishedTrueAndModule_PublishedTrueAndModule_Course_PublishedTrue(lessonId)
            .orElseThrow(() -> ApiException.notFound("Lesson"));
    return mapper.toDetail(lesson);
  }

  private Course findPublishedCourse(UUID courseId) {
    return courseRepository
        .findById(courseId)
        .filter(Course::isPublished)
        .orElseThrow(() -> ApiException.notFound("Course"));
  }

  private CourseModule findPublishedModule(UUID moduleId) {
    return moduleRepository
        .findByIdAndPublishedTrueAndCourse_PublishedTrue(moduleId)
        .orElseThrow(() -> ApiException.notFound("Module"));
  }
}
