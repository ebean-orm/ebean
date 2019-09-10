package io.ebeaninternal.server.type;

import io.ebeaninternal.server.core.BasicTypeConverter;
import io.ebeanservice.docstore.api.mapping.DocPropertyType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Types;

/**
 * ScalarType for BigDecimal.
 */
public class ScalarTypeBigDecimal extends ScalarTypeBase<BigDecimal> {

  public ScalarTypeBigDecimal() {
    super(BigDecimal.class, true, Types.DECIMAL);
  }

  @Override
  public void bind(DataBind b, BigDecimal value) throws SQLException {
    if (value == null) {
      b.setNull(Types.DECIMAL);
    } else {
      b.setBigDecimal(value);
    }
  }

  @Override
  public BigDecimal read(DataReader dataReader) throws SQLException {
    return dataReader.getBigDecimal();
  }

  @Override
  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toBigDecimal(value);
  }

  @Override
  public BigDecimal toBeanType(Object value) {
    return BasicTypeConverter.toBigDecimal(value);
  }

  @Override
  public String formatValue(BigDecimal t) {
    return t.toPlainString();
  }

  @Override
  public BigDecimal parse(String value) {
    return new BigDecimal(value);
  }

  @Override
  public BigDecimal convertFromMillis(long systemTimeMillis) {
    return BigDecimal.valueOf(systemTimeMillis);
  }

  @Override
  public boolean isDateTimeCapable() {
    return true;
  }

  @Override
  public BigDecimal readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return new BigDecimal(dataInput.readDouble());
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, BigDecimal b) throws IOException {

    if (b == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.writeDouble(b.doubleValue());
    }
  }

  @Override
  public BigDecimal jsonRead(JsonParser parser) throws IOException {
    return parser.getDecimalValue();
  }

  @Override
  public void jsonWrite(JsonGenerator writer, BigDecimal value) throws IOException {
    writer.writeNumber(value);
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.DOUBLE;
  }

}
