package io.ebeaninternal.server.core;

import io.avaje.applog.AppLog;
import io.ebean.bean.ObjectGraphNode;
import io.ebean.config.SlowQueryEvent;
import io.ebean.config.SlowQueryListener;

import static java.lang.System.Logger.Level.WARNING;

/**
 * Default slow query listener implementation that logs a warning message.
 */
final class DefaultSlowQueryListener implements SlowQueryListener {

  private static final System.Logger log = AppLog.getLogger("io.ebean.SlowQuery");

  @Override
  public void process(SlowQueryEvent event) {
    String firstStack = "";
    ObjectGraphNode node = event.getOriginNode();
    if (node != null) {
      firstStack = node.getOriginQueryPoint().getTopElement();
    }
    log.log(WARNING, "Slow query warning - millis:{0} rows:{1} caller[{2}] sql[{3}]", event.getTimeMillis(), event.getRowCount(), firstStack, event.getSql());
  }
}
