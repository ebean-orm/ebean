package org.tests.model.basic.xtra;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


@Entity
@DiscriminatorValue("EXTENDED")
public class EdExtendedParent extends EdParent {

  private String extendedName;

  public String getExtendedName() {
    return extendedName;
  }

  public void setExtendedName(String extendedName) {
    this.extendedName = extendedName;
  }

}
