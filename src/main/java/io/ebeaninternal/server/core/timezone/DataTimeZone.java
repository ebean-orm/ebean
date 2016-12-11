package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;

/**
 * Define if a Calendar representing the time zone should be used in JDBC calls.
 */
public interface DataTimeZone {

  /**
   * Return the Calendar to use for Timezone information.
   */
  Calendar getTimeZone();
}
