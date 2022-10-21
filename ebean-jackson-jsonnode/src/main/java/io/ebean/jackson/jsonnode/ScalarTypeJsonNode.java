package io.ebean.jackson.jsonnode;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.core.type.ScalarTypeBase;
import io.ebean.text.TextException;
import io.ebean.util.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Type which maps Jackson's JsonNode to various DB types (Clob, Varchar, Blob) in JSON format.
 */
abstract class ScalarTypeJsonNode extends ScalarTypeBase<JsonNode> {

  /**
   * Clob storage based implementation.
   */
  static final class Clob extends ScalarTypeJsonNode {

    Clob(ObjectMapper objectMapper) {
      super(objectMapper, Types.CLOB);
    }

    @Override
    public JsonNode read(DataReader reader) throws SQLException {
      String content = reader.getStringFromStream();
      if (content == null) {
        return null;
      }
      return parse(content);
    }
  }

  /**
   * Varchar storage based implementation.
   */
  static final class Varchar extends ScalarTypeJsonNode {

    public Varchar(ObjectMapper objectMapper) {
      super(objectMapper, Types.VARCHAR);
    }
  }

  /**
   * Blob storage based implementation.
   */
  static class Blob extends ScalarTypeJsonNode {

    Blob(ObjectMapper objectMapper) {
      super(objectMapper, Types.BLOB);
    }

    @Override
    public JsonNode read(DataReader dataReader) throws SQLException {
      InputStream is = dataReader.getBinaryStream();
      if (is == null) {
        return null;
      }
      try (Reader reader = IOUtils.newReader(is)) {
        return parse(reader);
      } catch (IOException e) {
        throw new SQLException("Error reading Blob stream from DB", e);
      }
    }

    @Override
    public void bind(DataBinder binder, JsonNode value) throws SQLException {
      if (value == null) {
        binder.setNull(Types.BLOB);
      } else {
        String rawJson = formatValue(value);
        binder.setBlob(rawJson.getBytes(StandardCharsets.UTF_8));
      }
    }
  }

  /**
   * Jackson's ObjectMapper used to read / write JsonNode
   */
  final ObjectMapper objectMapper;

  ScalarTypeJsonNode(ObjectMapper objectMapper, int jdbcType) {
    super(JsonNode.class, false, jdbcType);
    this.objectMapper = objectMapper;
  }

  /**
   * Map is a mutable type. Use the isDirty() method to check for dirty state.
   */
  @Override
  public boolean mutable() {
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
  public JsonNode read(DataReader reader) throws SQLException {
    String rawJson = reader.getString();
    if (rawJson == null) {
      return null;
    }
    return parse(rawJson);
  }

  @Override
  public void bind(DataBinder binder, JsonNode value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.VARCHAR);
    } else {
      String rawJson = formatValue(value);
      binder.setString(rawJson);
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
      throw new TextException("Failed to parse JSON [{}] as JsonNode", value, e);
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
      dataOutput.writeBoolean(true);
      dataOutput.writeUTF(format(value));
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
  public DocPropertyType docType() {
    return DocPropertyType.OBJECT;
  }

}
