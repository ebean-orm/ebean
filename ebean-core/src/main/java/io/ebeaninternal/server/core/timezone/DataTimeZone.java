package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;

/**
 * Define if a Calendar representing the time zone should be used in JDBC calls.
 */
public interface DataTimeZone {

  /**
   * Return the Calendar to use for Timezone information when reading/writing timestamps.
   */
  Calendar getTimeZone();

  /**
   * Return the Calendar to use for Timezone information when reading/writing a time component (date only/time only).
   * A time component has normally no timezone information, but some platforms (like MySQL) reqire this.
   */
  default Calendar getTimeComponentTimeZone() {
    return null;
  }
}
