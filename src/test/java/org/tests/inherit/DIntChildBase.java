package org.tests.inherit;

import javax.persistence.Entity;

@Entity
public class DIntChildBase extends DIntParent {

  @Override
  public String getName() {
    return "Base name";
  }

  public DIntChildBase(Integer number, String more) {
    super(number, more);
  }

}
