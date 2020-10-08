package io.ebeaninternal.server.type;

import java.util.UUID;

/**
 * Type conversion for use with ScalarTypeArrayList.
 */
interface ArrayElementConverter<T> {

  /**
   * Convert the array element to the logical type.
   */
  T toElement(Object rawValue);

  default Object[] toDbArray(Object[] objects) {
    return objects;
  }

  /**
   * The UUID converter implementation.
   */
  ArrayElementConverter<UUID> UUID = new UuidConverter();

  /**
   * The String converter implementation.
   */
  ArrayElementConverter<String> STRING = new StringConverter();

  /**
   * The Long converter implementation.
   */
  ArrayElementConverter<Long> LONG = new LongConverter();

  /**
   * The Integer converter implementation.
   */
  ArrayElementConverter<Integer> INTEGER = new IntegerConverter();

  /**
   * The Double converter implementation.
   */
  ArrayElementConverter<Double> DOUBLE = new DoubleConverter();

  class LongConverter implements ArrayElementConverter<Long> {

    @Override
    public Long toElement(Object rawValue) {
      if (rawValue instanceof Long) {
        return (Long) rawValue;
      } else {
        return ((Number) rawValue).longValue();
      }
    }
  }

  class IntegerConverter implements ArrayElementConverter<Integer> {

    @Override
    public Integer toElement(Object rawValue) {
      if (rawValue instanceof Integer) {
        return (Integer) rawValue;
      } else {
        return ((Number) rawValue).intValue();
      }
    }
  }

  class DoubleConverter implements ArrayElementConverter<Double> {

    @Override
    public Double toElement(Object rawValue) {
      if (rawValue instanceof Double) {
        return (Double) rawValue;
      } else {
        return ((Number) rawValue).doubleValue();
      }
    }
  }

  /**
   * String converter (noop based).
   */
  class StringConverter extends NoopConverter<String> {
  }

  /**
   * UUID converter (noop based).
   */
  class UuidConverter extends NoopConverter<UUID> {
  }

  class NoopConverter<T> implements ArrayElementConverter<T> {

    @SuppressWarnings("unchecked")
    @Override
    public T toElement(Object rawValue) {
      return (T) rawValue;
    }
  }

  /**
   * String converter (noop based).
   */
  @SuppressWarnings("rawtypes")
  class EnumConverter implements ArrayElementConverter {

    private final ScalarType<?> scalarType;

    EnumConverter(ScalarType<?> scalarType) {
      this.scalarType = scalarType;
    }

    @Override
    public Object toElement(Object rawValue) {
      return scalarType.toBeanType(rawValue);
    }

    @Override
    public Object[] toDbArray(Object[] objects) {
      Object[] dbArray = new Object[objects.length];
      for (int i = 0; i < objects.length; i++) {
        dbArray[i] = scalarType.toJdbcType(objects[i]);
      }
      return dbArray;
    }
  }

}
