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

  public void bind(DataBind b, BigDecimal value) throws SQLException {
    if (value == null) {
      b.setNull(Types.DECIMAL);
    } else {
      b.setBigDecimal(value);
    }
  }

  public BigDecimal read(DataReader dataReader) throws SQLException {

    return dataReader.getBigDecimal();
  }

  public Object toJdbcType(Object value) {
    return BasicTypeConverter.toBigDecimal(value);
  }

  public BigDecimal toBeanType(Object value) {
    return BasicTypeConverter.toBigDecimal(value);
  }

  public String formatValue(BigDecimal t) {
    return t.toPlainString();
  }

  public BigDecimal parse(String value) {
    return new BigDecimal(value);
  }

  public BigDecimal convertFromMillis(long systemTimeMillis) {
    return BigDecimal.valueOf(systemTimeMillis);
  }

  public boolean isDateTimeCapable() {
    return true;
  }

  public BigDecimal readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      return new BigDecimal(dataInput.readDouble());
    }
  }

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
