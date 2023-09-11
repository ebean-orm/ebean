package org.tests.inheritance.cascadedelete;

import io.ebean.annotation.NotNull;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

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
