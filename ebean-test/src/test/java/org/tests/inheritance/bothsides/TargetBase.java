package org.tests.inheritance.bothsides;

import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;

@Entity
@Inheritance
public abstract class TargetBase extends WithAutoGeneratedUUID {

  private String name;

  public TargetBase(String name) {
    this.name = name;
  }

  public String getName() { return name;}

}
