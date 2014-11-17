package com.avaje.ebeaninternal.server.type;

import java.time.Month;

/**
 * ScalarType mapping for Month enum.
 */
public class ScalarTypeMonth extends ScalarTypeEnumWithMapping {

  static final EnumToDbIntegerMap beanDbMap = new EnumToDbIntegerMap();

  static {
    Month[] values = Month.values();
    for (Month value : values) {
      beanDbMap.add(value, value.getValue());
    }
  }

  public ScalarTypeMonth() {
    super(beanDbMap, Month.class, 1);
  }
}
