package org.tests.model.basic.xtra;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "td_parent")
public class EdExtendedParent extends EdParent {

  private String extendedName;

  public String getExtendedName() {
    return extendedName;
  }

  public void setExtendedName(String extendedName) {
    this.extendedName = extendedName;
  }

}
