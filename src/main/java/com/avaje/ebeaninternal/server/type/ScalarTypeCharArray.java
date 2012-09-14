package com.avaje.ebeaninternal.server.type;

import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for char[].
 */
public class ScalarTypeCharArray extends ScalarTypeBaseVarchar<char[]>{

	public ScalarTypeCharArray() {
		super(char[].class, false, Types.VARCHAR);
	}
	
	@Override
    public char[] convertFromDbString(String dbValue) {
        return dbValue.toCharArray();
    }

    @Override
    public String convertToDbString(char[] beanValue) {
        return new String(beanValue);
    }

    public void bind(DataBind b, char[] value) throws SQLException {
		if (value == null){
			b.setNull(Types.VARCHAR);
		} else {
			String s = BasicTypeConverter.toString(value);
			b.setString(s);
		}
	}

	public char[] read(DataReader dataReader) throws SQLException {
		String string = dataReader.getString();
		if (string == null){
			return null;
		} else {
			return string.toCharArray();
		}
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toString(value);
	}

	public char[] toBeanType(Object value) {
		String s = BasicTypeConverter.toString(value);
		return s.toCharArray();
	}
	
	public String formatValue(char[] t) {
        return String.valueOf(t);
    }

    public char[] parse(String value) {
		return value.toCharArray();
	}
	
    @Override
    public char[] jsonFromString(String value, JsonValueAdapter ctx) {
        return value.toCharArray();
    }

    @Override
    public String jsonToString(char[] value, JsonValueAdapter ctx) {
        return EscapeJson.escapeQuote(String.valueOf(value));
    }
    
}
