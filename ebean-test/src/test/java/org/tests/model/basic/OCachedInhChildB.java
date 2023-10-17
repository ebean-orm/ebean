package org.tests.model.basic;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


/**
 * Child bean B
 */
@Entity
@DiscriminatorValue("childB")
public class OCachedInhChildB extends OCachedInhRoot {
  String childBData;

  /**
   * @return the childBData
   */
  public String getChildBData() {
    return childBData;
  }

  /**
   * @param childBData
   *          the childBData to set
   */
  public void setChildBData(String childBData) {
    this.childBData = childBData;
  }
}
