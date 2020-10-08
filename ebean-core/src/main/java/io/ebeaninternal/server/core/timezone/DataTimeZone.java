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
   * Return the Calendar to use for Timezone information when reading/writing date.
   * A 'date' only value has normally no timezone information, but some platforms (like MySQL)
   * reqire this.
   */
  default Calendar getDateTimeZone() {
    return null;
  }
}
