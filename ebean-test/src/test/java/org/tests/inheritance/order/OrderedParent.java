package org.tests.inheritance.order;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;

@Entity
public final class OrderedParent {

  @Id
  Integer id;

  String commonName;

  String orderedAName;

  String orderedBName;

  public String getOrderedBName() {
    return orderedBName;
  }

  public void setOrderedBName(final String orderedBName) {
    this.orderedBName = orderedBName;
  }

  public String getOrderedAName() {
    return orderedAName;
  }

  public void setOrderedAName(final String orderedAName) {
    this.orderedAName = orderedAName;
  }

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(final String commonName) {
    this.commonName = commonName;
  }

  public void setOrderedParentName(String commonName) {
    this.commonName = commonName;
  }
}
