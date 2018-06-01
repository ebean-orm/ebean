package io.ebeantest;

import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;
import io.ebeaninternal.server.logger.DSpiLogger;
import org.slf4j.LoggerFactory;

/**
 * Create a logger that captures the SQL and register it for later access in tests.
 */
public class CapturingLoggerFactory implements SpiLoggerFactory {

  @Override
  public SpiLogger create(String name) {

    DSpiLogger logger = new DSpiLogger(LoggerFactory.getLogger(name));
    if (name.equals("io.ebean.SQL")) {
      return LoggedSql.register(logger);
    }
    return logger;
  }
}
