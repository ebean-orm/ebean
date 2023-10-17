package org.tests.o2o;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OtoLevelC {

  @Id
  private Long id;

  private final String name;

  public OtoLevelC(String name) {
    this.name = name;
  }
}
