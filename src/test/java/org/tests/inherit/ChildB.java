package org.tests.inherit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity
@DiscriminatorValue("B")
public class ChildB extends Parent {

  @Lob
  private String lobData;

  @Override
  public String getName() {
    return "B Name";
  }

  public ChildB(Integer number, String more) {
    super(number, more);
  }

  public String getLobData() {
    return lobData;
  }

  public void setLobData(String lobData) {
    this.lobData = lobData;
  }
}
