package com.pokergto.curriculum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

  @Id
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "module_id", nullable = false)
  private CourseModule module;

  @Column(nullable = false)
  private String title;

  /** Markdown — rendered client-side, never server-side (§25/§27's DTO boundary keeps rendering a frontend concern). */
  @Column(nullable = false)
  private String content;

  @Column(name = "order_index", nullable = false)
  @Builder.Default
  private int orderIndex = 0;

  @Column(name = "is_published", nullable = false)
  @Builder.Default
  private boolean published = false;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "lesson_topics",
      joinColumns = @JoinColumn(name = "lesson_id"),
      inverseJoinColumns = @JoinColumn(name = "topic_id"))
  @Builder.Default
  private Set<Topic> topics = new HashSet<>();
}
