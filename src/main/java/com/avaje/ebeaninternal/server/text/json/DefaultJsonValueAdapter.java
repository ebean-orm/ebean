package com.avaje.ebeaninternal.server.text.json;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.avaje.ebean.text.json.JsonValueAdapter;

public class DefaultJsonValueAdapter implements JsonValueAdapter {

  private final SimpleDateFormat dateTimeProto;

  public DefaultJsonValueAdapter(String dateTimeFormat) {
    this.dateTimeProto = new SimpleDateFormat(dateTimeFormat);
    this.dateTimeProto.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public DefaultJsonValueAdapter() {
    this("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  }

  private SimpleDateFormat dtFormat() {
    return (SimpleDateFormat) dateTimeProto.clone();
  }

  public String jsonFromDate(Date date) {
    return "\"" + date.toString() + "\"";
  }

  public String jsonFromTimestamp(Timestamp date) {
    return "\"" + dtFormat().format(date) + "\"";
  }

  public Date jsonToDate(String jsonDate) {
    try {
      long utc = Long.parseLong(jsonDate);
      return new java.sql.Date(utc);
    } catch (NumberFormatException ex) {
      return Date.valueOf(jsonDate);
    }
  }

  public Timestamp jsonToTimestamp(String jsonDateTime) {
    try {
      long utc = Long.parseLong(jsonDateTime);
      return new Timestamp(utc);
    } catch (NumberFormatException ex) {
      try {
        java.util.Date d = dtFormat().parse(jsonDateTime);
        return new Timestamp(d.getTime());
      } catch (Exception e) {
        String m = "Error parsing Datetime[" + jsonDateTime + "]";
        throw new RuntimeException(m, e);
      }
    }
  }

}
