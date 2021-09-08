package io.ebean.server.type;

import io.ebean.annotation.EnumValue;

/**
 * Enum with method overrides (and hence multiple actual classes).
 */
public enum MyEnum {

  @EnumValue("A")Aval {
    @Override
    public String doSomething() {
      return "bar";
    }
  },
  @EnumValue("B")Bval,

  @EnumValue("C")Cval {
    @Override
    public String doSomething() {
      return "baz";
    }
  };

  public String doSomething() {
    return "foo";
  }
}
