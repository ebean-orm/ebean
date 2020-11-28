package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import javax.persistence.EnumType;
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
      beanDbMap.add(value, value.getValue(), value.name());
    }
  }

  public ScalarTypeMonth() {
    super(beanDbMap, Month.class, 1);
  }

  /**
   * We allow this to be overridden by a JPA EnumType.
   */
  @Override
  public boolean isOverrideBy(EnumType type) {
    return type != null;
  }

  /**
   * Bind Month enum value.
   */
  @Override
  public void bind(DataBinder binder, Object value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.INTEGER);
    } else {
      // avoiding the map lookup
      binder.setInt(((Month) value).getValue());
    }
  }

  /**
   * Read Month enum from integer.
   */
  @Override
  public Object read(DataReader reader) throws SQLException {
    Integer i = reader.getInt();
    if (i == null) {
      return null;
    } else {
      // avoiding the map lookup
      return Month.of(i);
    }
  }
}
