package com.avaje.ebeaninternal.server.type;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import com.avaje.ebean.config.dbplatform.PostgresPlatform;

/**
 * Postgres Hstore type which maps Map<String,String> to a single 'HStore column' in the DB.
 */
@SuppressWarnings("rawtypes")
public class ScalarTypePostgresHstore extends ScalarTypeBase<Map> {

  public static final String KEY = "hstore";
  
  public static final int HSTORE_TYPE = PostgresPlatform.TYPE_HSTORE;
  
  public ScalarTypePostgresHstore() {
    super(Map.class, false, HSTORE_TYPE);
  }
  
  @Override
  public boolean isMutable() {
    return true;
  }
  
  @Override
  public boolean isDirty(Object value) {
    if (value instanceof ModifyAwareOwner) {
      return ((ModifyAwareOwner)value).isMarkedDirty();
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map read(DataReader dataReader) throws SQLException {
    
    Object value = dataReader.getObject();
    if (value == null) {
      return null;
    }
    if (value instanceof Map == false) {
      throw new RuntimeException("Expecting Hstore to return as Map but got type "+value.getClass());
    }
    return new ModifyAwareMap((Map)value);
  }

  @Override
  public void bind(DataBind b, Map value) throws SQLException {
    b.setObject(value);
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public Map toBeanType(Object value) {
    return (Map)value;
  }
  
  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Object value) {
    // TODO Auto-generated method stub  
  }

  @Override
  public Object jsonRead(JsonParser ctx, Event event) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String formatValue(Map v) {
    // TODO format as json
    return null;
  }

  @Override
  public Map parse(String value) {
    // TODO parse json into map
    return null;
  }

  @Override
  public Map parseDateTime(long dateTime) {
    throw new RuntimeException("Should never be called");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Object readData(DataInput dataInput) throws IOException {
    return null;
  }

  @Override
  public void writeData(DataOutput dataOutput, Object v) throws IOException {
    
  }

  
}
