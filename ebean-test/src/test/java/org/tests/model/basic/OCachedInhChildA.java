package org.tests.model.basic;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


/**
 * Child bean A
 */
@Entity
@DiscriminatorValue("childA")
public class OCachedInhChildA extends OCachedInhRoot {

  String childAData;

  /**
   * @return the childAData
   */
  public String getChildAData() {
    return childAData;
  }

  /**
   * @param childAData
   *          the childAData to set
   */
  public void setChildAData(String childAData) {
    this.childAData = childAData;
  }
}
