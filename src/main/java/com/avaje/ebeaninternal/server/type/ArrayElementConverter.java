package com.avaje.ebeaninternal.server.type;

import java.util.UUID;

/**
 * Type conversion for use with ScalarTypeArrayList.
 */
public interface ArrayElementConverter<T> {

  /**
   * Convert the array element to the logical type.
   */
  T toElement(Object rawValue);

  /**
   * The UUID converter implementation.
   */
  ArrayElementConverter UUID = new UuidConverter();

  /**
   * The String converter implementation.
   */
  ArrayElementConverter STRING = new StringConverter();

  /**
   * The Long converter implementation.
   */
  ArrayElementConverter LONG = new LongConverter();

  /**
   * The Integer converter implementation.
   */
  ArrayElementConverter INTEGER = new IntegerConverter();

  class LongConverter implements ArrayElementConverter<Long> {

    @Override
    public Long toElement(Object rawValue) {
      if (rawValue instanceof Long) {
        return (Long)rawValue;
      } else {
        return ((Number)rawValue).longValue();
      }
    }
  }

  class IntegerConverter implements ArrayElementConverter<Integer> {

    @Override
    public Integer toElement(Object rawValue) {
      if (rawValue instanceof Integer) {
        return (Integer)rawValue;
      } else {
        return ((Number)rawValue).intValue();
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

    @Override
    public T toElement(Object rawValue) {
      return (T)rawValue;
    }
  }

}
