package io.ebeaninternal.server.type;

import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;

import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Used to map enum values to database integer values.
 */
final class EnumToDbIntegerMap extends EnumToDbValueMap<Integer> {

  @Override
  public int getDbType() {
    return Types.INTEGER;
  }

  /**
   * Construct with allowNulls defaulting to false and integerType=true
   */
  public EnumToDbIntegerMap() {
    this(true);
  }
  /**
   * Construct with  integerType=true
   */
  public EnumToDbIntegerMap(boolean allowNulls) {
    super(allowNulls, true);
  }

  public void add(Object beanValue, Integer dbValue, String name) {
    addInternal(beanValue, dbValue, name);
  }

  @Override
  public Object getBeanValue(Object dbValue) {
    if (dbValue instanceof String) {
      return super.getBeanValue(Integer.parseInt(dbValue.toString()));
    }
    return super.getBeanValue(dbValue);
  }

  @Override
  public EnumToDbIntegerMap add(Object beanValue, String stringDbValue, String name) {
    try {
      Integer value = Integer.valueOf(stringDbValue);
      addInternal(beanValue, value, name);

      return this;

    } catch (Exception e) {
      throw new PersistenceException("Error converted enum [" + beanValue.getClass().getName()
      + "] value[" + beanValue + "] string value [" + stringDbValue + "] to an Integer.", e);
    }
  }

  @Override
  public void bind(DataBinder binder, Object value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.INTEGER);
    } else {
      Integer s = getDbValue(value);
      binder.setInt(s);
    }

  }

  @Override
  public Object read(DataReader reader) throws SQLException {
    Integer i = reader.getInt();
    if (i == null) {
      return null;
    } else {
      return getBeanValue(i);
    }
  }

}
