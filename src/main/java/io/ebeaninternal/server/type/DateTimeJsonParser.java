package io.ebeaninternal.server.type;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeJsonParser {

  private final SimpleDateFormat dateTimeProto20;
  private final SimpleDateFormat dateTimeProto22;
  private final SimpleDateFormat dateTimeProto23;
  private final SimpleDateFormat dateTimeProto24;


  public DateTimeJsonParser() {
    dateTimeProto20 = init("yyyy-MM-dd'T'HH:mm:ss'Z'");
    dateTimeProto22 = init("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    dateTimeProto23 = init("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
    dateTimeProto24 = init("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  }

  private SimpleDateFormat init(String dateTimeFormat) {
    SimpleDateFormat sdf = new SimpleDateFormat(dateTimeFormat);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    return sdf;
  }

  private SimpleDateFormat dtFormat(int length) {
    switch (length) {
      case 24:
        return (SimpleDateFormat) dateTimeProto24.clone();
      case 23:
        return (SimpleDateFormat) dateTimeProto23.clone();
      case 22:
        return (SimpleDateFormat) dateTimeProto22.clone();
      case 20:
        return (SimpleDateFormat) dateTimeProto20.clone();
      default:
        return (SimpleDateFormat) dateTimeProto24.clone();
    }
  }

  public Timestamp parse(String jsonDateTime) {
    try {
      java.util.Date d = dtFormat(jsonDateTime.length()).parse(jsonDateTime);
      return new Timestamp(d.getTime());
    } catch (ParseException e) {
      throw new RuntimeException("Error parsing Datetime[" + jsonDateTime + "]", e);
    }
  }

  public String format(Date value) {
    // always format with millisecond precision
    return dtFormat(24).format(value);
  }
}
