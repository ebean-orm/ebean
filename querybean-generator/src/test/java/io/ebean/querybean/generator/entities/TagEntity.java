package io.ebean.querybean.generator.entities;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "realworld.tag")
public class TagEntity extends Model {
  @Id private final UUID id;
  private final String name;

  public TagEntity(UUID id, String name) {
    this.id = id;
    this.name = name;
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }
}
