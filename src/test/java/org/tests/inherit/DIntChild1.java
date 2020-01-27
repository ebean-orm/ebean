package org.tests.inherit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("1")
public class DIntChild1 extends DIntChildBase {

  @Override
  public String getName() {
    return "A Name";
  }

  public DIntChild1(Integer number, String more) {
    super(number, more);
  }

}
