package io.ebeaninternal.server.type;

import io.ebean.core.type.ScalarType;

import java.util.UUID;

/**
 * Type conversion for use with ScalarTypeArrayList.
 */
interface ArrayElementConverter<T> {

  /**
   * Convert element it's json serialized form.
   */
  T fromSerialized(Object rawValue);

  /**
   * Convert the array element from it's DB array form.
   */
  T fromDbArray(Object rawValue);

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
    public Long fromSerialized(Object rawValue) {
      return fromDbArray(rawValue);
    }

    @Override
    public Long fromDbArray(Object rawValue) {
      if (rawValue instanceof Long) {
        return (Long) rawValue;
      } else {
        return ((Number) rawValue).longValue();
      }
    }
  }

  class IntegerConverter implements ArrayElementConverter<Integer> {

    @Override
    public Integer fromSerialized(Object rawValue) {
      return ((Number) rawValue).intValue();
    }

    @Override
    public Integer fromDbArray(Object rawValue) {
      if (rawValue instanceof Integer) {
        return (Integer) rawValue;
      } else {
        return ((Number) rawValue).intValue();
      }
    }
  }

  class DoubleConverter implements ArrayElementConverter<Double> {

    @Override
    public Double fromSerialized(Object rawValue) {
      return fromDbArray(rawValue);
    }

    @Override
    public Double fromDbArray(Object rawValue) {
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
  class StringConverter implements ArrayElementConverter<String> {

    public String fromDbArray(Object rawValue) {
      return (String) rawValue;
    }

    @Override
    public String fromSerialized(Object rawValue) {
      return (String) rawValue;
    }
  }

  /**
   * UUID converter.
   */
  class UuidConverter implements ArrayElementConverter<UUID> {

    @Override
    public java.util.UUID fromSerialized(Object rawValue) {
      return java.util.UUID.fromString((String)rawValue);
    }

    @Override
    public java.util.UUID fromDbArray(Object rawValue) {
      if (rawValue instanceof UUID) {
        return (java.util.UUID) rawValue;
      }
      return java.util.UUID.fromString(rawValue.toString());
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
    public Object fromSerialized(Object rawValue) {
      return scalarType.parse((String) rawValue);
    }

    @Override
    public Object fromDbArray(Object rawValue) {
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
