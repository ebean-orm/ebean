package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

/**
 * Base ScalarType for types which converts to and from a VARCHAR database column.
 */
public abstract class ScalarTypeBaseVarchar<T> extends ScalarTypeBase<T> {

	public ScalarTypeBaseVarchar(Class<T> type) {
		super(type, false, Types.VARCHAR);
	}
	
    public ScalarTypeBaseVarchar(Class<T> type, boolean jdbcNative, int jdbcType) {
        super(type, jdbcNative, jdbcType);
    }

	public abstract String formatValue(T v);
	
    public abstract T parse(String value);
    
    public abstract T convertFromDbString(String dbValue);
    
    public abstract String convertToDbString(T beanValue);
    
	public void bind(DataBind b, T value) throws SQLException { 
		if (value == null){
			b.setNull(Types.VARCHAR);
			
		} else {
			String s = convertToDbString(value);
			b.setString(s);
		}
	}

	public T read(DataReader dataReader) throws SQLException {
		String s = dataReader.getString();
		if (s == null){
			return null;
		} else {
		    return convertFromDbString(s);
		}
	}
	
	@SuppressWarnings("unchecked")
    public T toBeanType(Object value) {
	    if (value == null){
	        return null;
	    }
		if (value instanceof String){
		    return parse((String)value);
		}
		return (T)value;
	}
	
	public Object toJdbcType(Object value){
	    if (value instanceof String){
	        return parse((String)value);
	    }
	    return value;
	}
	
	public T parseDateTime(long systemTimeMillis) {
		throw new TextException("Not Supported");
	}
	
	public boolean isDateTimeCapable() {
		return false;
	}
	
    @SuppressWarnings("unchecked")
    public String format(Object v) {
        return formatValue((T) v);
    }
	   
    public T jsonFromString(String value, JsonValueAdapter ctx) {
        return parse(value);
    }

    @Override
    public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx) {
    	String s = format(value);
    	EscapeJson.escapeQuote(s, buffer);
    }

	public String toJsonString(Object value, JsonValueAdapter ctx) {
        String s = format(value);
        return EscapeJson.escapeQuote(s);
    }
    
    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            String val = dataInput.readUTF();
            return convertFromDbString(val);
        }
    }

    @SuppressWarnings("unchecked")
    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        T value = (T)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            String s = convertToDbString(value);
            dataOutput.writeUTF(s);            
        }
    }
}
