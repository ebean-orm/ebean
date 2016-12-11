package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
public class SomeEnumBean {

  @Id
  Long id;

  @Enumerated
  SomeEnum someEnum;

  String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SomeEnum getSomeEnum() {
    return someEnum;
  }

  public void setSomeEnum(SomeEnum someEnum) {
    this.someEnum = someEnum;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
