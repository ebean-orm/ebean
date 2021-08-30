package io.ebeaninternal.server.type;

import javax.persistence.PersistenceException;

/**
 * ScalarType for Class that persists it to VARCHAR column.
 */
@SuppressWarnings({"rawtypes"})
final class ScalarTypeClass extends ScalarTypeBaseVarchar<Class> {

  public ScalarTypeClass() {
    super(Class.class);
  }

  @Override
  public int getLength() {
    return 255;
  }

  @Override
  public Class<?> convertFromDbString(String dbValue) {
    return parse(dbValue);
  }

  @Override
  public String convertToDbString(Class beanValue) {
    return beanValue.getCanonicalName();
  }

  @Override
  public String formatValue(Class v) {
    return v.getCanonicalName();
  }

  @Override
  public Class<?> parse(String value) {
    try {
      return Class.forName(value);
    } catch (Exception e) {
      throw new PersistenceException("Unable to find Class " + value, e);
    }
  }

}
