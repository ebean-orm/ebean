package com.avaje.ebeaninternal.server.type;

import java.time.DayOfWeek;

/**
 * ScalarType mapping for Month enum.
 */
public class ScalarTypeDayOfWeek extends ScalarTypeEnumWithMapping {

  static final EnumToDbIntegerMap beanDbMap = new EnumToDbIntegerMap();

  static {
    DayOfWeek[] values = DayOfWeek.values();
    for (DayOfWeek value : values) {
      beanDbMap.add(value, value.getValue());
    }
  }

  public ScalarTypeDayOfWeek() {
    super(beanDbMap, DayOfWeek.class, 1);
  }
}
