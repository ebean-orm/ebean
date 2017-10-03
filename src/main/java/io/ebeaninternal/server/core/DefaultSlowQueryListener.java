package io.ebeaninternal.server.core;

import io.ebean.bean.ObjectGraphNode;
import io.ebean.config.SlowQueryEvent;
import io.ebean.config.SlowQueryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default slow query listener implementation that logs a warning message.
 */
class DefaultSlowQueryListener implements SlowQueryListener {

  private static final Logger log = LoggerFactory.getLogger("io.ebean.SlowQuery");

  @Override
  public void process(SlowQueryEvent event) {

    String firstStack = "";
    ObjectGraphNode node = event.getOriginNode();
    if (node != null) {
      firstStack = node.getOriginQueryPoint().getFirstStackElement();
    }
    log.warn("Slow query warning - millis:{} rows:{} caller[{}] sql[{}]", event.getTimeMillis(), event.getRowCount(), firstStack, event.getSql());
  }
}
