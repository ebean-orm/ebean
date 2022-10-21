package io.ebean.joda.time;

import io.ebean.config.JsonConfig;
import io.ebean.core.type.ScalarTypeBaseDate;
import io.ebean.core.type.BasicTypeConverter;
import org.joda.time.DateMidnight;

import java.sql.Date;
import java.sql.Types;

/**
 * ScalarType for Joda DateMidnight. This maps to a JDBC Date.
 */
@SuppressWarnings("deprecation")
final class ScalarTypeJodaDateMidnight extends ScalarTypeBaseDate<DateMidnight> {

  /**
   * Instantiates a new scalar type joda date midnight.
   */
  ScalarTypeJodaDateMidnight(JsonConfig.Date mode) {
    super(mode, org.joda.time.DateMidnight.class, false, Types.DATE);
  }

  @Override
  protected String toIsoFormat(DateMidnight value) {
    return value.toLocalDate().toString();
  }

  @Override
  public long convertToMillis(org.joda.time.DateMidnight value) {
    return value.getMillis();
  }

  @Override
  public org.joda.time.DateMidnight convertFromDate(Date ts) {
    return new org.joda.time.DateMidnight(ts.getTime());
  }

  @Override
  public Date convertToDate(org.joda.time.DateMidnight t) {
    return new Date(t.getMillis());
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof org.joda.time.DateMidnight) {
      return new Date(((org.joda.time.DateMidnight) value).getMillis());
    }
    return BasicTypeConverter.toDate(value);
  }

  @Override
  public org.joda.time.DateMidnight toBeanType(Object value) {
    if (value instanceof java.util.Date) {
      return new org.joda.time.DateMidnight(((java.util.Date) value).getTime());
    }
    return (org.joda.time.DateMidnight) value;
  }
}
