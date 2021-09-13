package org.tests.inheritance.bothsides;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class SourceB extends SourceBase {

  @ManyToOne(cascade = CascadeType.PERSIST)
  private Target2 target;

  public SourceB(String name, Target2 target, int pos) {
    super(name, pos);
    this.target = target;
  }

  public Target2 getTarget() { return target;}

}
