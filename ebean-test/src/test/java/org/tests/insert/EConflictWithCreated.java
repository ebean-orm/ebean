package org.tests.insert;

import io.ebean.annotation.Index;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;

/**
 * Used to verify that insert on conflict do update excludes insert-only generated
 * properties (such as {@code @WhenCreated}) from the generated update set clause.
 */
@Entity
public class EConflictWithCreated {

  @Id
  Long id;

  @Index(unique = true)
  String code;

  @WhenCreated
  Instant whenCreated;

  @WhenModified
  Instant whenUpdated;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Instant getWhenCreated() {
    return whenCreated;
  }

  public void setWhenCreated(Instant whenCreated) {
    this.whenCreated = whenCreated;
  }

  public Instant getWhenUpdated() {
    return whenUpdated;
  }

  public void setWhenUpdated(Instant whenUpdated) {
    this.whenUpdated = whenUpdated;
  }
}
