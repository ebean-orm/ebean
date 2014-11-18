package com.avaje.ebeaninternal.server.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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
public class ScalarTypeMonthDay extends ScalarTypeBase<MonthDay> {

  protected int year;

  /**
   * Construct with a year of 2000 (which is a leap year).
   */
  public ScalarTypeMonthDay() {
    this(2000); // Year 2000 is a leap year
  }

  /**
   * Construct with a base year. The year should be a leap year to
   * allow the 29th Feb value.
   */
  public ScalarTypeMonthDay(int year) {
    super(MonthDay.class, false, Types.DATE);
    this.year = year;
  }

  private MonthDay convertFromDate(Date ts) {
    LocalDate localDate = ts.toLocalDate();
    return MonthDay.of(localDate.getMonthValue(), localDate.getDayOfMonth());
  }

  private Date convertToDate(MonthDay value) {
    return Date.valueOf(LocalDate.of(2000, value.getMonthValue(), value.getDayOfMonth()));
  }

  @Override
  public MonthDay read(DataReader dataReader) throws SQLException {
    Date ts = dataReader.getDate();
    return ts == null ? null : convertFromDate(ts);
  }


  @Override
  public void bind(DataBind b, MonthDay value) throws SQLException {
    if (value == null) {
      b.setNull(Types.DATE);
    } else {
      b.setDate(convertToDate(value));
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
  public MonthDay jsonRead(JsonParser ctx, JsonToken event) throws IOException {
    return parse(ctx.getValueAsString());
  }

  @Override
  public void jsonWrite(JsonGenerator ctx, String name, MonthDay value) throws IOException {
    ctx.writeStringField(name, format(value));
  }


}
