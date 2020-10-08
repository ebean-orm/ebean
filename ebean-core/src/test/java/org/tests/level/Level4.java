package org.tests.level;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Level4 {
  @Id
  Long id;

  String name;

  public Level4(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
