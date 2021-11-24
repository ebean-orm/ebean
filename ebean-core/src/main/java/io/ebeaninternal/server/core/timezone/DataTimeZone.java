package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;

/**
 * Define if a Calendar representing the time zone should be used in JDBC calls.
 */
public interface DataTimeZone {

  /**
   * Return the Calendar to use for Timezone information when reading/writing
   * timestamps/Instants
   */
  Calendar getTimeZone();

  /**
   * Return the Calendar to use for Timezone information when reading/writing
   * LocalDate / LocalTime / LocalDateTime. A local date only value has normally
   * no timezone information - and no conversion will occur. but some platforms
   * (like MySQL) require this.
   */
  default Calendar getLocalTimeZone() {
    return null;
  }
}
