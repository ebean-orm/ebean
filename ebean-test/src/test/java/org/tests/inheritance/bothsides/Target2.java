package org.tests.inheritance.bothsides;

import jakarta.persistence.Entity;

@Entity
public class Target2 extends TargetBase {
  public Target2(String name) {
    super(name);
  }
}
