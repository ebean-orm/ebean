package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Used to map Enum values to database string/varchar values.
 */
public class EnumToDbStringMap extends EnumToDbValueMap<String> {

	
	
	@Override
	public int getDbType() {
		return Types.VARCHAR;
	}

	@Override
	public EnumToDbStringMap add(Object beanValue, String dbValue) {
		addInternal(beanValue, dbValue);
		return this;
	}

	@Override
	public void bind(DataBind b, Object value) throws SQLException {
		if (value == null){
			b.setNull(Types.VARCHAR);
		} else {
			String s = getDbValue(value);
			b.setString(s);
		}
		
	}

	@Override
	public Object read(DataReader dataReader) throws SQLException {
		String s = dataReader.getString();
		if (s == null){
			return null;
		} else {
			return getBeanValue(s);
		}
	}
	
}
