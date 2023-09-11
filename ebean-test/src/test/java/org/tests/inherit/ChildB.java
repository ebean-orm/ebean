package org.tests.inherit;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
