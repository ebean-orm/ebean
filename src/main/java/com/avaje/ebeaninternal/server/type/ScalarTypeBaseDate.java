package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.text.json.JsonValueAdapter;
import com.avaje.ebeaninternal.server.text.json.WriteJsonBuffer;

/**
 * Base class for Date types.
 */
public abstract class ScalarTypeBaseDate<T> extends ScalarTypeBase<T> {

    public ScalarTypeBaseDate(Class<T> type, boolean jdbcNative, int jdbcType) {
        super(type, jdbcNative, jdbcType);
    }
    
    public abstract java.sql.Date convertToDate(T t);
    
    public abstract T convertFromDate(java.sql.Date ts);
    
    public void bind(DataBind b, T value) throws SQLException {
        if (value == null){
            b.setNull(Types.DATE);
        } else {
            Date date = convertToDate(value);
            b.setDate(date);
        }
    }

    public T read(DataReader dataReader) throws SQLException {
        
        Date ts = dataReader.getDate();
        if (ts == null){
            return null;
        } else {
            return convertFromDate(ts);
        }
    }
    
    public String formatValue(T t) {
        Date date = convertToDate(t);
        return date.toString();
    }

    public T parse(String value) {
        Date date = Date.valueOf(value);
        return convertFromDate(date);
    }
    
    public T parseDateTime(long systemTimeMillis) {
        Date ts = new Date(systemTimeMillis);
        return convertFromDate(ts);
    }

    public boolean isDateTimeCapable() {
        return true;
    }
    
    @Override
    public Object jsonRead(JsonParser ctx, Event event) {
      if (ctx.isIntegralNumber()) {
        long millis = ctx.getLong();
        return parseDateTime(millis);
      } else {
        String string = ctx.getString();
        throw new RuntimeException("convert "+string);
      }
    }
    
    public void jsonWrite(JsonGenerator ctx, String name, Object value) {
      long millis = convertToMillis(value);
      ctx.write(name, millis);
    }
    
    public abstract long convertToMillis(Object value);

    @Override
    public String jsonToString(T value, JsonValueAdapter ctx) {
        Date date = convertToDate(value);
        return ctx.jsonFromDate(date);
    }
    
    @Override
    public void jsonWrite(WriteJsonBuffer buffer, T value, JsonValueAdapter ctx) {
    	String s = jsonToString(value, ctx);
    	buffer.append(s);
    }

	@Override
    public T jsonFromString(String value, JsonValueAdapter ctx) {
        Date ts = ctx.jsonToDate(value);
        return convertFromDate(ts);
    }

    public Object readData(DataInput dataInput) throws IOException {
        if (!dataInput.readBoolean()) {
            return null;
        } else {
            long val = dataInput.readLong();
            Date date = new Date(val);
            return convertFromDate(date);
        }
    }

    @SuppressWarnings("unchecked")
    public void writeData(DataOutput dataOutput, Object v) throws IOException {
        
        T value = (T)v;
        if (value == null){
            dataOutput.writeBoolean(false);
        } else {
            dataOutput.writeBoolean(true);
            Date date = convertToDate(value);
            dataOutput.writeLong(date.getTime());            
        }
    }
    
}
