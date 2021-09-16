package org.tests.model.basic.relates;


import java.util.UUID;

import javax.persistence.*;

import io.ebean.annotation.ChangeLog;

/**
 * Relation entity
 */
@Entity
@ChangeLog
public class Relation3 {

  @Id
  private UUID id = UUID.randomUUID();

  private String name;

  public Relation3(String name) {
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
