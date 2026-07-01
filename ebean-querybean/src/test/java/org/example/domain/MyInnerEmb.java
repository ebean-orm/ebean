package org.example.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class MyInnerEmb {

  @Embeddable
  public static class EmbAddr {
    final long streetNum;
    final String line1;

    public EmbAddr(long streetNum, String line1) {
      this.streetNum = streetNum;
      this.line1 = line1;
    }
  }

  @Id
  long id;
  String one;

  @Embedded
  EmbAddr address;

  public long id() {
    return id;
  }

  public MyInnerEmb id(long id) {
    this.id = id;
    return this;
  }

  public String one() {
    return one;
  }

  public MyInnerEmb one(String beanOne) {
    this.one = beanOne;
    return this;
  }

  public EmbAddr address() {
    return address;
  }

  public MyInnerEmb setAddress(EmbAddr address) {
    this.address = address;
    return this;
  }
}
