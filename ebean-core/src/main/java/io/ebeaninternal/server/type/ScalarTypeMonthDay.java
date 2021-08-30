package io.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import io.ebean.core.type.DataBinder;
import io.ebean.core.type.DataReader;
import io.ebean.core.type.DocPropertyType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.MonthDay;

/**
 * ScalarType for MonthDay stored as a SQL Date using a fixed year.
 * <p>
 * Note that the year used should be a leap year.
 * </p>
 */
final class ScalarTypeMonthDay extends ScalarTypeBase<MonthDay> {

  /**
   * Construct with a base year. The year should be a leap year to
   * allow the 29th Feb value.
   */
  ScalarTypeMonthDay() {
    super(MonthDay.class, false, Types.DATE);
  }

  private MonthDay convertFromDate(Date ts) {
    LocalDate localDate = ts.toLocalDate();
    return MonthDay.of(localDate.getMonthValue(), localDate.getDayOfMonth());
  }

  private Date convertToDate(MonthDay value) {
    return Date.valueOf(LocalDate.of(2000, value.getMonthValue(), value.getDayOfMonth()));
  }

  @Override
  public MonthDay read(DataReader reader) throws SQLException {
    Date ts = reader.getDate();
    return ts == null ? null : convertFromDate(ts);
  }

  @Override
  public void bind(DataBinder binder, MonthDay value) throws SQLException {
    if (value == null) {
      binder.setNull(Types.DATE);
    } else {
      binder.setDate(convertToDate(value));
    }
  }

  @Override
  public Object toJdbcType(Object value) {
    if (value instanceof Date) return value;
    return convertToDate((MonthDay) value);
  }

  @Override
  public MonthDay toBeanType(Object value) {
    if (value instanceof MonthDay) return (MonthDay) value;
    if (value == null) return null;
    return convertFromDate((Date) value);
  }

  @Override
  public String formatValue(MonthDay value) {
    return value.toString();
  }

  @Override
  public MonthDay parse(String value) {
    return MonthDay.parse(value);
  }

  @Override
  public boolean isDateTimeCapable() {
    return false;
  }

  @Override
  public MonthDay convertFromMillis(long dateTime) {
    throw new RuntimeException("Not supported on this type");
  }

  @Override
  public MonthDay readData(DataInput dataInput) throws IOException {
    if (!dataInput.readBoolean()) {
      return null;
    } else {
      int month = dataInput.readInt();
      int day = dataInput.readInt();
      return MonthDay.of(month, day);
    }
  }

  @Override
  public void writeData(DataOutput dataOutput, MonthDay value) throws IOException {
    if (value == null) {
      dataOutput.writeBoolean(false);
    } else {
      dataOutput.writeBoolean(true);
      dataOutput.write(value.getMonthValue());
      dataOutput.write(value.getDayOfMonth());
    }
  }

  @Override
  public MonthDay jsonRead(JsonParser parser) throws IOException {
    return parse(parser.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator writer, MonthDay value) throws IOException {
    writer.writeString(format(value));
  }

  @Override
  public DocPropertyType getDocType() {
    return DocPropertyType.KEYWORD;
  }

}
