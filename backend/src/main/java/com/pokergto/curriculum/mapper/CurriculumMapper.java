package com.pokergto.curriculum.mapper;

import com.pokergto.curriculum.model.Course;
import com.pokergto.curriculum.model.CourseModule;
import com.pokergto.curriculum.model.Lesson;
import com.pokergto.curriculum.dto.CourseSummaryDto;
import com.pokergto.curriculum.dto.LessonDetailDto;
import com.pokergto.curriculum.dto.LessonSummaryDto;
import com.pokergto.curriculum.dto.ModuleSummaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct generates the actual entity↔DTO conversion code for this
 * interface at compile time (a `CurriculumMapperImpl` class you'll never
 * hand-write) — `componentModel = "spring"` makes that generated class a
 * Spring bean, so `CurriculumMapper` can just be constructor-injected like
 * any other service.
 */
@Mapper(componentModel = "spring")
public interface CurriculumMapper {

  // Record components line up with entity getters by name (id/slug/title/description/orderIndex),
  // so MapStruct needs no hints here — it generates the field-by-field copy on its own.
  CourseSummaryDto toSummary(Course course);

  ModuleSummaryDto toSummary(CourseModule module);

  LessonSummaryDto toSummary(Lesson lesson);

  // `topics` can't be auto-mapped: the DTO wants topic slugs (List<String>), the
  // entity holds Topic objects (Set<Topic>) — this expression tells MapStruct
  // exactly how to bridge that one field; everything else still maps automatically.
  @Mapping(
      target = "topics",
      expression = "java(lesson.getTopics().stream().map(t -> t.getSlug()).sorted().toList())")
  LessonDetailDto toDetail(Lesson lesson);
}
