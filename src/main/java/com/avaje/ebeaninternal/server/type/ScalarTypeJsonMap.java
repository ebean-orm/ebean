package com.avaje.ebeaninternal.server.type;

import com.avaje.ebean.text.TextException;
import com.avaje.ebean.text.json.EJson;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * Type which maps Map<String,Object> to various DB types (Clob, Varchar, Blob) in JSON format.
 */
public abstract class ScalarTypeJsonMap extends ScalarTypeBase<Map> {

  public static class Clob extends ScalarTypeJsonMap {

    public Clob() {
      super(Types.CLOB);
    }

    @Override
    public Map read(DataReader dataReader) throws SQLException {

      Reader reader = dataReader.getClobReader();
      if (reader == null) {
        return null;
      }
      try {
        Map map = parse(reader);
        reader.close();
        return map;
      } catch (IOException e) {
        throw new SQLException("Error reading Clob stream from DB", e);
      }
    }
  }

  public static class Varchar extends ScalarTypeJsonMap {

    public Varchar() {
      super(Types.VARCHAR);
    }
  }

  public static class Blob extends ScalarTypeJsonMap {
    public Blob() {
      super(Types.BLOB);
    }

    @Override
    public Map read(DataReader dataReader) throws SQLException {

      InputStream is = dataReader.getBlobInputStream();
      if (is == null) {
        return null;
      }
      try {
        InputStreamReader reader = new InputStreamReader(is);
        Map map = parse(reader);
        reader.close();
        return map;
      } catch (IOException e) {
        throw new SQLException("Error reading Blob stream from DB", e);
      }
    }

    @Override
    public void bind(DataBind b, Map value) throws SQLException {

      if (value == null) {
        b.setNull(Types.BLOB);
      } else {
        String rawJson = formatValue(value);
        InputStream stream = new ByteArrayInputStream(rawJson.getBytes(StandardCharsets.UTF_8));
        b.setBlob(stream);
      }
    }
  }

  public ScalarTypeJsonMap(int jdbcType) {
    super(Map.class, false, jdbcType);
  }

  /**
   * Map is a mutable type. Use the isDirty() method to check for dirty state.
   */
  @Override
  public boolean isMutable() {
    return true;
  }

  /**
   * Return true if the value should be considered dirty (and included in an update).
   */
  @Override
  public boolean isDirty(Object value) {
    return !(value instanceof ModifyAwareOwner) || ((ModifyAwareOwner) value).isMarkedDirty();
  }

  @Override
  public Map read(DataReader dataReader) throws SQLException {

    String rawJson = dataReader.getString();
    if (rawJson == null) {
      return null;
    }
    return parse(rawJson);
  }

  @Override
  public void bind(DataBind b, Map value) throws SQLException {

    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      String rawJson = formatValue(value);
      b.setString(rawJson);
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public Map toBeanType(Object value) {
    return (Map) value;
  }

  @Override
  public String formatValue(Map v) {
    try {
      return EJson.write(v);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map parse(String value) {
    try {
      // return a modify aware map
      return EJson.parseObject(value, true);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  public Map parse(Reader reader) {
    try {
      // return a modify aware map
      return EJson.parseObject(reader, true);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public Map convertFromMillis(long dateTime) {
    throw new RuntimeException("Should never be called");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public Map readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      String json = dataInput.readUTF();
      return parse(json);
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Map v) throws IOException {
    if (v == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      String json = format(v);
      dataOutput.writeUTF(json);
    }
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, Map value) throws IOException {
    // write the field name followed by the Map/JSON Object
    if (value == null) {
      ctx.writeNullField(name);
    } else {
      ctx.writeFieldName(name);
      EJson.write(value, ctx);
    }
  }

  @Override
  public Map jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    // at this point the BeanProperty has read the START_OBJECT token
    // to check for a null value. Pass the START_OBJECT token through to
    // the EJson parsing so that it knows the first token has been read
    return EJson.parseObject(ctx, event);
  }

}
