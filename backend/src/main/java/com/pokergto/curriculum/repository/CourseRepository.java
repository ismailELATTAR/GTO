package com.pokergto.curriculum.repository;

import com.pokergto.curriculum.model.Course;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, UUID> {

  // "published" here matches the entity's field name (Course.published), not its
  // getter (isPublished()) — Spring Data derives queries from the JPA property name.
  List<Course> findByPublishedTrueOrderByOrderIndexAsc();
}
