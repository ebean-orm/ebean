package org.tests.inherit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("B")
public class ChildB extends Parent {

  @Override
  public String getName() {
    return "B Name";
  }

  public ChildB(Integer number, String more) {
    super(number, more);
  }
}
