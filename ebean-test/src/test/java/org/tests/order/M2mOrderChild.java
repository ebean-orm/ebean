package org.tests.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class M2mOrderChild {

  @Id
  long id;

  String name;

  public M2mOrderChild() {
  }

  public M2mOrderChild(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "M2mOrderChild[" + id + "," + name + "]";
  }
}
