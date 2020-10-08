package io.ebeaninternal.server.type;

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
  public void bind(DataBind b, Object value) throws SQLException {
    if (value == null) {
      b.setNull(Types.INTEGER);
    } else {
      // avoiding the map lookup
      b.setInt(((DayOfWeek) value).getValue());
    }
  }

  /**
   * Read DayOfWeek using int value.
   */
  @Override
  public Object read(DataReader dataReader) throws SQLException {
    Integer i = dataReader.getInt();
    if (i == null) {
      return null;
    } else {
      // avoiding the map lookup
      return DayOfWeek.of(i);
    }
  }
}
