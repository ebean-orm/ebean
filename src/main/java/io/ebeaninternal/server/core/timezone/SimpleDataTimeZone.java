package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Implementation of DataTimeZone when single Calendar instance is used.
 */
public class SimpleDataTimeZone implements DataTimeZone {

  protected final Calendar zone;

  public SimpleDataTimeZone(String zoneId) {
    this.zone = Calendar.getInstance(TimeZone.getTimeZone(zoneId));
  }

  @Override
  public Calendar getTimeZone() {
    return zone;
  }
}
