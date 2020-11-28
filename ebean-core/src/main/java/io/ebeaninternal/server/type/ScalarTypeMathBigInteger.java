package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;
import io.ebeaninternal.server.core.BasicTypeConverter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for java.math.BigInteger.
 */
public class ScalarTypeMathBigInteger extends ScalarTypeBase<BigInteger> {

  public ScalarTypeMathBigInteger() {
    super(BigInteger.class, false, Types.BIGINT);
  }

  @Override
  public void bind(DataBinder binder, BigInteger value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.BIGINT);
    } else {
      binder.setLong(value.longValue());
    }
  }

  @Override
  public BigInteger read(DataReader reader) throws SQLException {
    Long value = reader.getLong();
    if (value == null) {
      return null;
    }
    return new BigInteger(String.valueOf(value));
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toLong(value);
  }

  @Override
  public BigInteger toBeanType(Object value) {
    return BasicTypeConverter.toMathBigInteger(value);
  }

  @Override
  public String formatValue(BigInteger v) {
    return v.toString();
  }

  @Override
  public BigInteger parse(String value) {
    return new BigInteger(value);
  }

  @Override
  public BigInteger convertFromMillis(long systemTimeMillis) {
    return BigInteger.valueOf(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public BigInteger readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return BigInteger.valueOf(dataInput.readLong());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, BigInteger value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeLong(value.longValue());
    }
  }

  @Override
  public BigInteger jsonRead(JsonParser parser) throws IOException {
    return parser.getDecimalValue().toBigInteger();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, BigInteger value) throws IOException {
    writer.writeNumber(value.longValue());
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.LONG;
  }

}
