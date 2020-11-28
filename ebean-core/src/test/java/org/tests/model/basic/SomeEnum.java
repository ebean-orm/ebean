package org.tests.model.basic;

public enum SomeEnum {

  ALPHA("Some nice Alpha"),
  BETA("Some nice Beta");

  String description;

  SomeEnum(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return description;
  }
}
