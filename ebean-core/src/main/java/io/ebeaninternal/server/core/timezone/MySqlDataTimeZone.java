package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;

/**
 * Implementation of DataTimeZone when single Calendar instance is used with local timezone.
 */
public class MySqlDataTimeZone implements DataTimeZone {

  protected final Calendar zone;

  public MySqlDataTimeZone() {
    this.zone = Calendar.getInstance();
  }

  @Override
  public Calendar getTimeZone() {
    return zone;
  }

  @Override
  public Calendar getDateTimeZone() {
    return zone;
  }
}
