package org.tests.inherit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("2")
public class DIntChild2 extends DIntChildBase {

  @Override
  public String getName() {
    return "Name2";
  }

  public DIntChild2(Integer number, String more) {
    super(number, more);
  }

}
