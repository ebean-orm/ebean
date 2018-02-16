package io.ebeaninternal.server.type;

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
  public void bind(DataBind b, Object value) throws SQLException {
    if (value == null) {
      b.setNull(Types.INTEGER);
    } else {
      // avoiding the map lookup
      b.setInt(((Month) value).getValue());
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
