package org.tests.inheritance.order;

import jakarta.persistence.Entity;

@Entity
public class OrderedB extends OrderedParent {

  String orderedBName;

  public String getOrderedBName() {
    return orderedBName;
  }

  public void setOrderedBName(final String orderedBName) {
    this.orderedBName = orderedBName;
  }
}
