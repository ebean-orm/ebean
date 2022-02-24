package org.tests.inheritance.bothsides;

import javax.persistence.Entity;

@Entity
public class Target1 extends TargetBase {

  public Target1(String name) {
    super(name);
  }
}
