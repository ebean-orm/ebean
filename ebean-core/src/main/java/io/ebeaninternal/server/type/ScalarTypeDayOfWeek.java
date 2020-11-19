package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import javax.persistence.EnumType;
import java.sql.SQLException;
import java.sql.Types;
import java.time.DayOfWeek;

/**
 * ScalarType mapping for Month enum.
 */
public class ScalarTypeDayOfWeek extends ScalarTypeEnumWithMapping {

  static final EnumToDbIntegerMap beanDbMap = new EnumToDbIntegerMap();

  static {
    DayOfWeek[] values = DayOfWeek.values();
    for (DayOfWeek value : values) {
      beanDbMap.add(value, value.getValue(), value.name());
    }
  }

  public ScalarTypeDayOfWeek() {
    super(beanDbMap, DayOfWeek.class, 1);
  }

  /**
   * We allow this to be overridden by a JPA EnumType.
   */
  @Override
  public boolean isOverrideBy(EnumType type) {
    return type != null;
  }

  /**
   * Bind DayOfWeek enum using getValue().
   */
  @Override
  public void bind(DataBinder binder, Object value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.INTEGER);
    } else {
      // avoiding the map lookup
      binder.setInt(((DayOfWeek) value).getValue());
    }
  }

  /**
   * Read DayOfWeek using int value.
   */
  @Override
  public Object read(DataReader reader) throws SQLException {
    Integer i = reader.getInt();
    if (i == null) {
      return null;
    } else {
      // avoiding the map lookup
      return DayOfWeek.of(i);
    }
  }
}
