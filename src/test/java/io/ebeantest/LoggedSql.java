package io.ebeantest;

import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.server.logger.DSpiLogger;

import java.util.List;

public class LoggedSql {

  private static CaptureLogger sqlLogger;

  static SpiLogger register(DSpiLogger logger) {
    if (sqlLogger == null) {
      sqlLogger = new CaptureLogger(logger);
    }
    return sqlLogger;
  }

  public static List<String> start() {
    return sqlLogger.start();
  }

  public static List<String> stop() {
    return sqlLogger.stop();
  }

  public static List<String> collect() {
    return sqlLogger.collect();
  }

}
