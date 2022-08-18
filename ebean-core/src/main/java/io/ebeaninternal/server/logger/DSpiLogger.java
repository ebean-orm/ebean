package io.ebeaninternal.server.logger;

import io.ebeaninternal.api.SpiLogger;

import java.lang.System.Logger.Level;

final class DSpiLogger implements SpiLogger {

  private final System.Logger logger;

  DSpiLogger(System.Logger logger) {
    this.logger = logger;
  }

  @Override
  public boolean isDebug() {
    return logger.isLoggable(Level.DEBUG);
  }

  @Override
  public boolean isTrace() {
    return logger.isLoggable(Level.TRACE);
  }

  @Override
  public void debug(String msg) {
    logger.log(Level.DEBUG, msg);
  }

  @Override
  public void trace(String msg) {
    logger.log(Level.TRACE, msg);
  }
}
