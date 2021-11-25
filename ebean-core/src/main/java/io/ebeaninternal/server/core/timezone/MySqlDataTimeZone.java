package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Implementation of DataTimeZone when single Calendar instance is used with local timezone.
 */
public class MySqlDataTimeZone implements DataTimeZone {

  protected final Calendar zone;
  protected final Calendar localZone;

  public MySqlDataTimeZone(String zoneId) {
    this.zone = Calendar.getInstance(TimeZone.getTimeZone(zoneId));
    this.localZone = Calendar.getInstance();
  }
  public MySqlDataTimeZone() {
    this.zone = Calendar.getInstance();
    this.localZone = zone;
  }

  @Override
  public Calendar getTimeZone() {
    return zone;
  }

  @Override
  public Calendar getLocalTimeZone() {
    return localZone;
  }
}
