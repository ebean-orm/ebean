package io.ebeaninternal.server.logger;

import io.avaje.applog.AppLog;
import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;

/**
 * Default SpiLoggerFactory implementation.
 */
public final class DLoggerFactory implements SpiLoggerFactory {

  /**
   * Just use a standard slf4j Logger.
   */
  @Override
  public SpiLogger create(String name) {
    return new DSpiLogger(AppLog.getLogger(name));
  }
}
