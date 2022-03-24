package org.tests.model.basic.relates;


import io.ebean.annotation.ChangeLog;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

/**
 * Relation entity
 */
@Entity
@ChangeLog
public class Relation1 {

  @Id
  private UUID id = UUID.randomUUID();

  private String name;

  public Relation1(String name) {
    this.name = name;
  }

  @ManyToOne
  private Relation2 noCascade;

  @ManyToOne(cascade = CascadeType.ALL)
  private Relation2 withCascade;

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

  public Relation2 getNoCascade() {
    return noCascade;
  }

  public void setNoCascade(Relation2 noCascade) {
    this.noCascade = noCascade;
  }

  public Relation2 getWithCascade() {
    return withCascade;
  }

  public void setWithCascade(Relation2 withCascade) {
    this.withCascade = withCascade;
  }

}
