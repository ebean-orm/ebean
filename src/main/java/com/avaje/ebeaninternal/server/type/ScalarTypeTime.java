package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;

import com.avaje.ebeaninternal.server.core.BasicTypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * ScalarType for java.sql.Time.
 */
public class ScalarTypeTime extends ScalarTypeBase<Time> {

	public ScalarTypeTime() {
		super(Time.class, true, Types.TIME);
	}
	
	public void bind(DataBind b, Time value) throws SQLException {
		if (value == null){
			b.setNull(Types.TIME);
		} else {
			b.setTime(value);
		}
	}

	public Time read(DataReader dataReader) throws SQLException {
		
		return dataReader.getTime();
	}
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toTime(value);
	}

	public Time toBeanType(Object value) {
		return BasicTypeConverter.toTime(value);
	}

	
	public String formatValue(Time v) {
        return v.toString();
    }

    public Time parse(String value) {
		return Time.valueOf(value);
	}
	
	public Time parseDateTime(long systemTimeMillis) {
		return new Time(systemTimeMillis);
	}

	public boolean isDateTimeCapable() {
		return true;
	}
    
    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            String val = dataInput.readUTF();
            return parse(val);
        }
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        Time value = (Time)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            dataOutput.writeUTF(format(value));            
        }
    }

  @Override
  public Object jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return parse(ctx.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Object value) throws IOException {
    ctx.writeStringField(name, value.toString());
  }
  
}
