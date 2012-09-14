package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for java.sql.Timestamp.
 */
public class ScalarTypeTimestamp extends ScalarTypeBaseDateTime<Timestamp> {

	public ScalarTypeTimestamp() {
		super(Timestamp.class, true, Types.TIMESTAMP);
	}
	
	@Override
    public Timestamp convertFromTimestamp(Timestamp ts) {
        return ts;
    }

    @Override
    public Timestamp convertToTimestamp(Timestamp t) {
        return t;
    }

    public void bind(DataBind b, Timestamp value) throws SQLException {
		if (value == null){
			b.setNull(Types.TIMESTAMP);
		} else {
			b.setTimestamp(value);
		}
	}

	public Timestamp read(DataReader dataReader) throws SQLException {
		
		return dataReader.getTimestamp();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toTimestamp(value);
	}

	public Timestamp toBeanType(Object value) {
		return BasicTypeConverter.toTimestamp(value);
	}	
}
