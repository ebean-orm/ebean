package io.ebeaninternal.server.type;

import io.ebean.text.TextException;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Type which maps Jackson's JsonNode to various DB types (Clob, Varchar, Blob) in JSON format.
 */
public abstract class ScalarTypeJsonNode extends ScalarTypeBase<JsonNode> {

  /**
   * Clob storage based implementation.
   */
  public static class Clob extends ScalarTypeJsonNode {

    public Clob(ObjectMapper objectMapper) {
      super(objectMapper, Types.CLOB);
    }

    @Override
    public JsonNode read(DataReader dataReader) throws SQLException {

      String content = dataReader.getStringFromStream();
      if (content == null) {
        return null;
      }
      return parse(content);
    }
  }

  /**
   * Varchar storage based implementation.
   */
  public static class Varchar extends ScalarTypeJsonNode {

    public Varchar(ObjectMapper objectMapper) {
      super(objectMapper, Types.VARCHAR);
    }
  }

  /**
   * Blob storage based implementation.
   */
  public static class Blob extends ScalarTypeJsonNode {

    public Blob(ObjectMapper objectMapper) {
      super(objectMapper, Types.BLOB);
    }

    @Override
    public JsonNode read(DataReader dataReader) throws SQLException {

      InputStream is = dataReader.getBinaryStream();
      if (is == null) {
        return null;
      }
      try (InputStreamReader reader = new InputStreamReader(is)) {
        return parse(reader);
      } catch (IOException e) {
        throw new SQLException("Error reading Blob stream from DB", e);
      }
    }

    @Override
    public void bind(DataBind dataBind, JsonNode value) throws SQLException {

      if (value == null) {
        dataBind.setNull(Types.BLOB);
      } else {
        String rawJson = formatValue(value);
        dataBind.setBlob(rawJson.getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  /**
   * Jackson's ObjectMapper used to read / write JsonNode
   */
  final ObjectMapper objectMapper;

  public ScalarTypeJsonNode(ObjectMapper objectMapper, int jdbcType) {
    super(JsonNode.class, false, jdbcType);
    this.objectMapper = objectMapper;
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
    return true;
  }

  @Override
  public JsonNode read(DataReader dataReader) throws SQLException {

    String rawJson = dataReader.getString();
    if (rawJson == null) {
      return null;
    }
    return parse(rawJson);
  }

  @Override
  public void bind(DataBind dataBind, JsonNode value) throws SQLException {

    if (value == null) {
      dataBind.setNull(Types.VARCHAR);
    } else {
      String rawJson = formatValue(value);
      dataBind.setString(rawJson);
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    return value;
  }

  @Override
  public JsonNode toBeanType(Object value) {
    return (JsonNode) value;
  }

  @Override
  public String formatValue(JsonNode jsonNode) {
    try {
      return objectMapper.writeValueAsString(jsonNode);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public JsonNode parse(String value) {
    try {
      return objectMapper.readValue(value, JsonNode.class);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  public JsonNode parse(Reader reader) {
    try {
      return objectMapper.readValue(reader, JsonNode.class);
    } catch (IOException e) {
      throw new TextException(e);
    }
  }

  @Override
  public JsonNode convertFromMillis(long dateTime) {
    throw new RuntimeException("Should never be called");
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public JsonNode readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return parse(dataInput.readUTF());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, JsonNode value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, format(value));
    }
  }

  @Override
  public void jsonWrite(JsonGenerator writer, JsonNode value) throws IOException {
    objectMapper.writeTree(writer, value);
  }

  @Override
  public JsonNode jsonRead(JsonParser parser) throws IOException {
    return objectMapper.readValue(parser, JsonNode.class);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.OBJECT;
  }

}
