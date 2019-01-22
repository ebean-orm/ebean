package org.tests.o2o;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class OtoLevelC {

  @Id
  private Long id;

  private final String name;

  public OtoLevelC(String name) {
    this.name = name;
  }
}
