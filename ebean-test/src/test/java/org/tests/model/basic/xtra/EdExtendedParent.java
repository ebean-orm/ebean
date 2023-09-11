package org.tests.model.basic.xtra;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


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
