package io.ebeaninternal.server.logger;

import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;
import org.slf4j.LoggerFactory;

/**
 * Default SpiLoggerFactory implementation.
 */
public class DLoggerFactory implements SpiLoggerFactory {

  /**
   * Just use a standard slf4j Logger.
   */
  @Override
  public SpiLogger create(String name) {
    return new DSpiLogger(LoggerFactory.getLogger(name));
  }
}
