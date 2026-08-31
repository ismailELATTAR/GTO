package com.pokergto.curriculum.repository;

import com.pokergto.curriculum.model.Lesson;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

  List<Lesson> findByModuleIdAndPublishedTrueOrderByOrderIndexAsc(UUID moduleId);

  // A lesson is only visible if it, its module, AND that module's course are
  // all published — checking just the lesson let an unpublished course's or
  // module's lesson leak through via a direct GET /api/lessons/{id}.
  Optional<Lesson> findByIdAndPublishedTrueAndModule_PublishedTrueAndModule_Course_PublishedTrue(UUID id);
}
