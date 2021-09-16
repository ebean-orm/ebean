package org.tests.model.basic.relates;


import java.util.UUID;

import javax.persistence.*;

import io.ebean.annotation.ChangeLog;

/**
 * Relation entity
 */
@Entity
@ChangeLog
public class Relation2 {

  @Id
  private UUID id = UUID.randomUUID();

  private String name;

  public Relation2(String name) {
    this.name = name;
  }

  @ManyToOne
  private Relation3 noCascade;

  @ManyToOne(cascade = CascadeType.ALL)
  private Relation3 withCascade;

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

  public Relation3 getNoCascade() {
    return noCascade;
  }

  public void setNoCascade(Relation3 noCascade) {
    this.noCascade = noCascade;
  }

  public Relation3 getWithCascade() {
    return withCascade;
  }

  public void setWithCascade(Relation3 withCascade) {
    this.withCascade = withCascade;
  }
  
}
