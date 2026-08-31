package com.pokergto.curriculum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(name = "order_index", nullable = false)
  @Builder.Default
  private int orderIndex = 0;

  @Column(name = "is_published", nullable = false)
  @Builder.Default
  private boolean published = false;
}
