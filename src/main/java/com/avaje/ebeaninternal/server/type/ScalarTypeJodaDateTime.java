package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.sql.Types;

import org.joda.time.DateTime;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for Joda DateTime. This maps to a JDBC Timestamp.
 */
public class ScalarTypeJodaDateTime extends ScalarTypeBaseDateTime<DateTime> {

	public ScalarTypeJodaDateTime() {
		super(DateTime.class, false, Types.TIMESTAMP);
	}
	
  @Override
  public long convertToMillis(Object value) {
    return ((DateTime) value).getMillis();
  }
	 
	@Override
    public DateTime convertFromTimestamp(Timestamp ts) {
        return new DateTime(ts.getTime());
    }

    @Override
    public Timestamp convertToTimestamp(DateTime t) {
        return new Timestamp(t.getMillis());
    }
	
	public Object toJdbcType(Object value) {
		if (value instanceof DateTime){
			return new Timestamp(((DateTime)value).getMillis());
		}
		return BasicTypeConverter.toTimestamp(value);
	}

	public DateTime toBeanType(Object value) {
		if (value instanceof java.util.Date){
			return new DateTime(((java.util.Date)value).getTime());
		}
		return (DateTime)value;
	}
 
}
