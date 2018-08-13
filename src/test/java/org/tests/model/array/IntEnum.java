package org.tests.model.array;

import io.ebean.annotation.DbEnumType;
import io.ebean.annotation.DbEnumValue;

public enum IntEnum {
  ZERO, ONE, TWO;

  @DbEnumValue(storage = DbEnumType.INTEGER)
  public int dbValue() {
    return 100 + ordinal();
  }
}
