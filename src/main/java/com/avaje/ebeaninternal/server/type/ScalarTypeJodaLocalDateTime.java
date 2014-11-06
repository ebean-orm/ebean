package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.sql.Types;

import org.joda.time.LocalDateTime;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for Joda LocalDateTime. This maps to a JDBC Timestamp.
 */
public class ScalarTypeJodaLocalDateTime extends ScalarTypeBaseDateTime<LocalDateTime> {

	public ScalarTypeJodaLocalDateTime() {
		super(LocalDateTime.class, false, Types.TIMESTAMP);
	}
	
	
	@Override
  public long convertToMillis(Object value) {
    return ((LocalDateTime)value).toDateTime().getMillis();
  }


  @Override
    public LocalDateTime convertFromTimestamp(Timestamp ts) {
        return new LocalDateTime(ts.getTime());
    }

    @Override
    public Timestamp convertToTimestamp(LocalDateTime t) {
        return new Timestamp(t.toDateTime().getMillis());
    }
	
	public Object toJdbcType(Object value) {
		if (value instanceof LocalDateTime){
			return new Timestamp(((LocalDateTime)value).toDateTime().getMillis());
		}
		return BasicTypeConverter.toTimestamp(value);
	}

	public LocalDateTime toBeanType(Object value) {
		if (value instanceof java.util.Date){
			return new LocalDateTime(((java.util.Date)value).getTime());
		}
		return (LocalDateTime)value;
	}

	public LocalDateTime parseDateTime(long systemTimeMillis) {
		return new LocalDateTime(systemTimeMillis);
	}

}
