package io.ebeaninternal.server.type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class UtilDateParser {

  private static final SimpleDateFormat dateTimeProto = new SimpleDateFormat("yyyy-MM-dd");

  private static SimpleDateFormat formatter() {
    return (SimpleDateFormat) dateTimeProto.clone();
  }

  static Date parse(String jsonDateTime) {
    try {
      return formatter().parse(jsonDateTime);
    } catch (ParseException e) {
      throw new RuntimeException("Error parsing Date[" + jsonDateTime + "]", e);
    }
  }

  static String format(Date value) {
    return formatter().format(value);
  }
}
