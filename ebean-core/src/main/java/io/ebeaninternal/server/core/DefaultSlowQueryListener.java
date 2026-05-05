package io.ebeaninternal.server.core;

import io.avaje.applog.AppLog;
import io.ebean.ProfileLocation;
import io.ebean.config.SlowQueryEvent;
import io.ebean.config.SlowQueryListener;

import java.util.List;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Default slow query listener implementation that logs a warning message.
 */
final class DefaultSlowQueryListener implements SlowQueryListener {

  private static final System.Logger log = AppLog.getLogger("io.ebean.SlowQuery");

  @Override
  public void process(SlowQueryEvent event) {
    ProfileLocation profileLocation = event.getProfileLocation();
    String loc = profileLocation == null ? "" : profileLocation.fullLocation();
    List<Object> bindParams = event.getBindParams();
    log.log(WARNING, "Slow query warning - millis:{0} rows:{1} location:{2} sql[{3}] params{4}",
      event.getTimeMillis(), event.getRowCount(), loc, event.getSql(), bindParams);
  }
}
