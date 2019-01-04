package org.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OtoAone {

  @Id
  private String id;

  private String description;

  public OtoAone(String id, String description){
    this.id = id;
    this.description = description;
  }

}
