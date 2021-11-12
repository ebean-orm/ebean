package org.tests.inheritance.bothsides;

import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Entity
public class Target1 extends TargetBase {

  public Target1(String name) {
    super(name);
  }
}
