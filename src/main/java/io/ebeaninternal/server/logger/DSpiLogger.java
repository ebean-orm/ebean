package io.ebeaninternal.server.logger;

import io.ebeaninternal.api.SpiLogger;
import org.slf4j.Logger;

public class DSpiLogger implements SpiLogger {

  private final Logger logger;

  public DSpiLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public boolean isDebug() {
    return logger.isDebugEnabled();
  }

  @Override
  public boolean isTrace() {
    return logger.isTraceEnabled();
  }

  @Override
  public void debug(String msg) {
    logger.debug(msg);
  }

  @Override
  public void trace(String msg) {
    logger.trace(msg);
  }
}
