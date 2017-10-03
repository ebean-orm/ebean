package io.ebeaninternal.server.core.timezone;

import java.util.Calendar;

import io.ebean.databind.DataTimeZone;

/**
 * Implementation of DataTimeZone when no time zone is specified.
 */
public class NoDataTimeZone implements DataTimeZone {

  @Override
  public Calendar getTimeZone() {
    // return null so Calendar is not used
    return null;
  }
}
