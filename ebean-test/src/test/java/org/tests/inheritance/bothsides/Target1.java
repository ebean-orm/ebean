package org.tests.inheritance.bothsides;

import jakarta.persistence.Entity;

@Entity
public class Target1 extends TargetBase {

  public Target1(String name) {
    super(name);
  }
}
