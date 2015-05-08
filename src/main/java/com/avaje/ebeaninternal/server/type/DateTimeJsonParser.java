package com.avaje.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeJsonParser {

  private final SimpleDateFormat dateTimeProto;

  public DateTimeJsonParser() {
    this("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  }

  public DateTimeJsonParser(String dateTimeFormat) {
    this.dateTimeProto = new SimpleDateFormat(dateTimeFormat);
    this.dateTimeProto.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  private SimpleDateFormat dtFormat() {
    return (SimpleDateFormat) dateTimeProto.clone();
  }

  public Timestamp parse(String jsonDateTime) {
    try {
      java.util.Date d = dtFormat().parse(jsonDateTime);
      return new Timestamp(d.getTime());
    } catch (ParseException e) {
      throw new RuntimeException("Error parsing Datetime[" + jsonDateTime + "]", e);
    }
  }

  public String format(Date value) {
    return dtFormat().format(value);
  }
}
