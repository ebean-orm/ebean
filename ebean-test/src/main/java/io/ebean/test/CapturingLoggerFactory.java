package io.ebean.test;

import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a logger that captures the SQL and register it for later access in tests.
 * <p>
 * The logged SQL is accessed by LoggedSql.
 * </p>
 */
public class CapturingLoggerFactory implements SpiLoggerFactory {

  @Override
  public SpiLogger create(String name) {
    SpiLogger logger = new LogAdapter(LoggerFactory.getLogger(name));
    if (name.equals("io.ebean.SQL")) {
      return LoggedSql.register(logger);
    }
    return logger;
  }

  private static final class LogAdapter implements SpiLogger {

    private final Logger logger;

    LogAdapter(Logger logger) {
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
}
