package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import javax.validation.constraints.Size;

@Entity
public class ECustomId {

  @Id
  @GeneratedValue(generator = "shortUid")
  @Size(max=127)
  String id;

  String name;

  public ECustomId(String name) {
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
