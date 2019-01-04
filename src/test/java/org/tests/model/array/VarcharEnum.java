package org.tests.model.array;

import io.ebean.annotation.DbEnumType;
import io.ebean.annotation.DbEnumValue;

public enum VarcharEnum {
  ZERO, ONE, TWO;

  @DbEnumValue(storage = DbEnumType.VARCHAR)
  public String dbValue() {
    return "xXx" + name();
  }
}
