package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;
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

  /**
   * Bind Month enum value.
   */
  @Override
  public void bind(DataBind b, Object value) throws SQLException {
    if (value == null) {
      b.setNull(Types.INTEGER);
    } else {
      // avoiding the map lookup
      b.setInt(((Month)value).getValue());
    }
  }

  /**
   * Read Month enum from integer.
   */
  @Override
  public Object read(DataReader dataReader) throws SQLException {
    Integer i = dataReader.getInt();
    if (i == null) {
      return null;
    } else {
      // avoiding the map lookup
      return Month.of(i);
    }
  }
}
