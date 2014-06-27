package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.core.BasicTypeConverter;

/**
 * ScalarType for Integer and int.
 */
public class ScalarTypeInteger extends ScalarTypeBase<Integer> {

	public ScalarTypeInteger() {
		super(Integer.class, true, Types.INTEGER);
	}
	
	public void bind(DataBind b, Integer value) throws SQLException {
		if (value == null){
			b.setNull(Types.INTEGER);
		} else {
			b.setInt(value.intValue());
		}
	}

	public Integer read(DataReader dataReader) throws SQLException {
		
		return dataReader.getInt();
	}

    public Object readData(DataInput dataInput) throws IOException {
        return Integer.valueOf(dataInput.readInt());
    }

    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        dataOutput.writeInt((Integer) v);
    }
	
	public Object toJdbcType(Object value) {
		return BasicTypeConverter.toInteger(value);
	}

	public Integer toBeanType(Object value) {
		return BasicTypeConverter.toInteger(value);
	}

    public String formatValue(Integer v) {
        return v.toString();
    }

	public Integer parse(String value) {
		return Integer.valueOf(value);
	}

	public Integer parseDateTime(long systemTimeMillis) {
		throw new TextException("Not Supported");
	}

	public boolean isDateTimeCapable() {
		return false;
	}

    public String jsonToString(Integer value, JsonValueAdapter ctx) {
        return value.toString();
    }
    
    public Integer jsonFromString(String value, JsonValueAdapter ctx) {
        return Integer.valueOf(value);
    }
    
    @Override
    public Object jsonRead(JsonParser ctx, Event event) {
      return Integer.valueOf(ctx.getInt());
    }    
    
    public void jsonWrite(JsonGenerator ctx, String name, Object value) {
      ctx.write(name, (Integer)value);
    }

}
