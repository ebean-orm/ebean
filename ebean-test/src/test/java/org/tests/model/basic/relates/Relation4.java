package org.tests.model.basic.relates;


import io.ebean.annotation.ChangeLog;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

/**
 * Relation entity
 */
@Entity
@ChangeLog
public class Relation4 {

  @Id
  private UUID id = UUID.randomUUID();

  private String name;

  public Relation4(String name) {
    this.name = name;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
