package org.tests.inheritance.cascadedelete;

import io.ebean.annotation.NotNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ElementBean {

  @Id
  @GeneratedValue
  private Long id;

  @NotNull
  private String value;

  public ElementBean(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
