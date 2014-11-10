package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * ScalarType for String.
 */
public class ScalarTypeString extends ScalarTypeBase<String> {

	public ScalarTypeString() {
		super(String.class, true, Types.VARCHAR);
	}
	
	public void bind(DataBind b, String value) throws SQLException {
		if (value == null){
			b.setNull(Types.VARCHAR);
		} else {
			b.setString(value);
		}
	}

	public String read(DataReader dataReader) throws SQLException {
		
		return dataReader.getString();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toString(value);
	}

	public String toBeanType(Object value) {
		return BasicTypeConverter.toString(value);
	}

	public String formatValue(String t) {
        return t;
    }

    public String parse(String value) {
		return value;
	}
	
	public String parseDateTime(long systemTimeMillis) {
		return String.valueOf(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}

    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            return dataInput.readUTF();
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        String value = (String)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeUTF(value);            
        }
    }
	
    @Override
    public Object jsonRead(JsonParser ctx, JsonToken event) throws IOException {
      return ctx.getValueAsString();
    }
    
    public void jsonWrite(JsonGenerator ctx, String name, Object value) throws IOException {
      ctx.writeStringField(name, (String)value);
    }
}
