package com.avaje.tests.model.basic;

public enum MNonEnum {

  BEGIN("B"),
  END("E");

  String value;

  MNonEnum(String value) {
    this.value = value;
  }

  public String toString() {
    return value;
  }
}
