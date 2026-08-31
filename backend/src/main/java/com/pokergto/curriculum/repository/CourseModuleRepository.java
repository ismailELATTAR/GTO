package com.pokergto.curriculum.repository;

import com.pokergto.curriculum.model.CourseModule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseModuleRepository extends JpaRepository<CourseModule, UUID> {

  // "Course_Published" (underscore) tells Spring Data to walk module.course.published
  // rather than guessing at a "coursePublished" property that doesn't exist — needed
  // once a path has more than one plausible camelCase split.
  List<CourseModule> findByCourseIdAndPublishedTrueAndCourse_PublishedTrueOrderByOrderIndexAsc(UUID courseId);

  // A module is only visible on its own if it AND its parent course are both
  // published — checking just the module (as the original version of this
  // method did) let an unpublished course's module leak through directly.
  Optional<CourseModule> findByIdAndPublishedTrueAndCourse_PublishedTrue(UUID id);
}
