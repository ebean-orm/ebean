package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.config.dbplatform.DbPlatformType;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebean.text.TextException;
import io.ebean.text.json.EJson;
import io.ebeaninternal.json.ModifyAwareMap;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Postgres Hstore type which maps Map<String,String> to a single 'HStore column' in the DB.
 */
@SuppressWarnings("rawtypes")
final class ScalarTypePostgresHstore extends ScalarTypeBase<Map> {

  ScalarTypePostgresHstore() {
    super(Map.class, false, DbPlatformType.HSTORE);
  }

  @Override
  public boolean isMutable() {
    return true;
  }

  @Override
  public boolean isDirty(Object value) {
    return TypeJsonManager.checkIsDirty(value);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map read(DataReader reader) throws SQLException {
    Object value = reader.getObject();
    if (value == null) {
      return null;
    }
    if (!(value instanceof Map)) {
      throw new RuntimeException("Expecting Hstore to return as Map but got type " + value.getClass());
    }
    return new ModifyAwareMap((Map) value);
  }

  @Override
  public void bind(DataBinder binder, Map value) throws SQLException {
    binder.setObject(value);
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
      return EJson.parseObject(value);
    } catch (IOException e) {
      throw new TextException("Failed to parse JSON [{}] as Object", value, e);
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
      return parse(dataInput.readUTF());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, Map map) throws IOException {
    if (map == null) {
      dataOutput.writeBoolean(false);
    } else {
      ScalarHelp.writeUTF(dataOutput, format(map));
    }
  }

  @Override
  public void jsonWrite(JsonGenerator writer, Map value) throws IOException {
    EJson.write(value, writer);
  }

  @Override
  public Map jsonRead(JsonParser parser) throws IOException {
    // at this point the BeanProperty has read the START_OBJECT token
    // to check for a null value. Pass the START_OBJECT token through to
    // the EJson parsing so that it knows the first token has been read
    return EJson.parseObject(parser, parser.getCurrentToken());
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.OBJECT;
  }
}
