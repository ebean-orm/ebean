package io.ebean.csv.reader;

import io.ebean.text.StringParser;

import java.sql.Time;

/**
 * Parser for TIME types that supports both HH:mm:ss and HH:mm.
 */
public final class TimeStringParser implements StringParser {

  private static final TimeStringParser SHARED = new TimeStringParser();

  /**
   * Return a shared instance as this is thread safe.
   */
  public static TimeStringParser get() {
    return SHARED;
  }

  /**
   * Parse the String supporting both HH:mm:ss and HH:mm formats.
   */
  @Override
  @SuppressWarnings("deprecation")
  public Object parse(String value) {
    if (value == null || value.trim().isEmpty()) {
      return null;
    }

    String s = value.trim();
    int firstColon = s.indexOf(':');

    if (firstColon == -1) {
      throw new java.lang.IllegalArgumentException("No ':' in value [" + s + "]");
    }
    try {
      int second;
      int minute;
      int hour = Integer.parseInt(s.substring(0, firstColon));
      int secondColon = s.indexOf(':', firstColon + 1);

      if (secondColon == -1) {
        minute = Integer.parseInt(s.substring(firstColon + 1, s.length()));
        second = 0;
      } else {
        minute = Integer.parseInt(s.substring(firstColon + 1, secondColon));
        second = Integer.parseInt(s.substring(secondColon + 1));
      }

      return new Time(hour, minute, second);

    } catch (NumberFormatException e) {
      throw new java.lang.IllegalArgumentException("Number format Error parsing time [" + s + "] " + e.getMessage(), e);
    }
  }
}
