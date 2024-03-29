package org.tests.inheritance.bothsides;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class SourceA extends SourceBase {

  @ManyToOne(cascade = CascadeType.ALL)
  private Target1 target;

  public SourceA(String name, Target1 target, int pos) {
    super(name, pos);
    this.target = target;
  }

  public Target1 getTarget() {
    return target;
  }

}
