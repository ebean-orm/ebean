package org.example.domain;

import javax.persistence.Embeddable;

@Embeddable
public class CaoKey {

  private final int customer;
  private final int type;

  public CaoKey(int customer, int type) {
    this.customer = customer;
    this.type = type;
  }

  public int customer() {
    return customer;
  }

  public int type() {
    return type;
  }

  @Override
  public int hashCode() {
    int hc = 31 * 7 + customer;
    hc = 31 * hc + type;
    return hc;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o instanceof CaoKey) {
      CaoKey k = (CaoKey) o;
      return k.customer == customer && k.type == type;
    }
    return false;
  }
}
