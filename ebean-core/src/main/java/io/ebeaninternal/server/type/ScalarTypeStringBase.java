package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Base ScalarType for String type using Varchar, Clob and LongVarchar.
 */
public abstract class ScalarTypeStringBase extends ScalarTypeBase<String> {

  ScalarTypeStringBase(boolean jdbcNative, int jdbcType) {
    super(String.class, jdbcNative, jdbcType);
  }

  @Override
  public void bind(DataBind b, String value) throws SQLException {
    if (value == null) {
      b.setNull(Types.VARCHAR);
    } else {
      b.setString(value);
    }
  }

  @Override
  public String read(DataReader dataReader) throws SQLException {
    return dataReader.getString();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public String toBeanType(Object value) {
    return BasicTypeConverter.toString(value);
  }

  @Override
  public String formatValue(String value) {
    return value;
  }

  @Override
  public String parse(String value) {
    return value;
  }

  @Override
  public String convertFromMillis(long systemTimeMillis) {
    return String.valueOf(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public String readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return dataInput.readUTF();
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, String value) throws IOException {

    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeUTF(value);
    }
  }

  @Override
  public String jsonRead(JsonParser parser) throws IOException {
    return parser.getValueAsString();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, String value) throws IOException {
    writer.writeString(value);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.TEXT;
  }
}
