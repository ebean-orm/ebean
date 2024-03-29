package org.tests.ddl;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class DfkOne {

  @Id
  long id;

  String name;

  public DfkOne(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
