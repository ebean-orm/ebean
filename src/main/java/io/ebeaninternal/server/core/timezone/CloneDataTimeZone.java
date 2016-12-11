package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;

/**
 * Implementation of DataTimeZone that clones the Calendar instance.
 * <p>
 * Used with Oracle JDBC driver as that wants to mutate the Calender.
 * </p>
 */
public class CloneDataTimeZone extends SimpleDataTimeZone {

  public CloneDataTimeZone(String zoneId) {
    super(zoneId);
  }

  @Override
  public Calendar getTimeZone() {
    // return cloned copy for Oracle to muck around with
    return (Calendar) zone.clone();
  }
}
