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

/**
 * A tagged poker concept (e.g. "preflop.rfi") — the taxonomy lessons AND
 * (later) exercises both tag against, per ARCHITECTURE.md §4. That shared
 * tagging is what lets progress tracking answer "what's this user weakest
 * at" across theory and practice, not just within one or the other.
 */
@Entity
@Table(name = "topics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topic {

  @Id
  @UuidGenerator
  private UUID id;

  @Column(nullable = false, unique = true)
  private String slug;

  @Column(nullable = false)
  private String name;

  private String description;
}
