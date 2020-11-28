package org.tests.inheritance.order;

import javax.persistence.Entity;

@Entity
public class OrderedA extends OrderedParent {

  String orderedAName;

  public String getOrderedAName() {
    return orderedAName;
  }

  public void setOrderedAName(final String orderedAName) {
    this.orderedAName = orderedAName;
  }
}
