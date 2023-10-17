package org.tests.singleTableInheritance.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INT")
public class ZoneInternal extends Zone {

  private String attribute;

  public String getAttribute() {
    return attribute;
  }

  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  @Override
  public String toString() {
    return "ZoneInternal " + getId() + " \"" + getAttribute() + "\"";
  }
}
