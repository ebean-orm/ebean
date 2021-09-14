package org.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
public class OtoAone {

  @Id
  @Size(max = 100)
  private String id;

  private String description;

  public OtoAone(String id, String description){
    this.id = id;
    this.description = description;
  }

}
