package io.ebeaninternal.server.type;

import io.ebean.core.type.BasicTypeConverter;
import io.ebean.core.type.ScalarType;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
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
   * Convert the array element from its DB array form.
   */
  T fromDbArray(Object rawValue);

  default Object[] toDbArray(Object[] objects) {
    return objects;
  }

  ArrayElementConverter<UUID> UUID = new UuidConverter();
  ArrayElementConverter<String> STRING = new StringConverter();
  ArrayElementConverter<Long> LONG = new LongConverter();
  ArrayElementConverter<Integer> INTEGER = new IntegerConverter();
  ArrayElementConverter<Double> DOUBLE = new DoubleConverter();
  ArrayElementConverter<Float> FLOAT = new FloatConverter();
  ArrayElementConverter<BigDecimal> BIG_DECIMAL = new BigDecimalConverter();
  ArrayElementConverter<Instant> INSTANT = new InstantConverter();
  ArrayElementConverter<LocalDate> LOCAL_DATE = new LocalDateConverter();

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

  class FloatConverter implements ArrayElementConverter<Float> {

    @Override
    public Float fromSerialized(Object rawValue) {
      return fromDbArray(rawValue);
    }

    @Override
    public Float fromDbArray(Object rawValue) {
      if (rawValue instanceof Float) {
        return (Float) rawValue;
      } else {
        return ((Number) rawValue).floatValue();
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

  class InstantConverter implements ArrayElementConverter<Instant> {

    @Override
    public Instant fromSerialized(Object rawValue) {
      return fromDbArray(rawValue);
    }

    @Override
    public Instant fromDbArray(Object rawValue) {
      if (rawValue instanceof Instant) {
        return (Instant) rawValue;
      } else if (rawValue instanceof Timestamp) {
        return ((Timestamp) rawValue).toInstant();
      } else if (rawValue instanceof String) {
        return Instant.parse((String) rawValue);
      } else {
        throw new IllegalStateException("Instant fromDbArray unsupported for " + rawValue);
      }
    }
  }

  class LocalDateConverter implements ArrayElementConverter<LocalDate> {

    @Override
    public LocalDate fromSerialized(Object rawValue) {
      return fromDbArray(rawValue);
    }

    @Override
    public LocalDate fromDbArray(Object rawValue) {
      if (rawValue instanceof LocalDate) {
        return (LocalDate) rawValue;
      } else if (rawValue instanceof java.sql.Date) {
        return ((java.sql.Date) rawValue).toLocalDate();
      } else if (rawValue instanceof String) {
        return LocalDate.parse((String) rawValue);
      } else {
        throw new IllegalStateException("Instant fromDbArray unsupported for " + rawValue);
      }
    }
  }

  class BigDecimalConverter implements ArrayElementConverter<BigDecimal> {

    @Override
    public BigDecimal fromSerialized(Object rawValue) {
      return fromDbArray(rawValue);
    }

    @Override
    public BigDecimal fromDbArray(Object rawValue) {
      if (rawValue instanceof BigDecimal) {
        return (BigDecimal) rawValue;
      } else {
        return BasicTypeConverter.toBigDecimal(rawValue);
      }
    }
  }

  /**
   * String converter (noop based).
   */
  class StringConverter implements ArrayElementConverter<String> {

    @Override
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
      return java.util.UUID.fromString((String) rawValue);
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
