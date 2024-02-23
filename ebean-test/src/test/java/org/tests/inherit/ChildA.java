package org.tests.inherit;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

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
