package org.tests.inherit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("A")
public class ChildA extends Parent {

  @Override
  public String getName() {
    return "A Name";
  }

  public ChildA(Integer number, String more) {
    super(number, more);
  }

}
