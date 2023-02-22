package io.ebean.test;

import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.api.SpiLoggerFactory;
import io.ebeaninternal.server.logger.DLoggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCaptureLogger {

  @Test
  void testDefaultLogger() {
    SpiLogger logger = new DLoggerFactory().create("io.ebean.test.loggerTest");
    doSomeLogs(logger);

    CaptureLogger captureLogger = new CaptureLogger(logger);
    captureLogger.start();
    doSomeLogs(captureLogger);
    assertThat(captureLogger.stop()).containsExactly(
      "test {0}", "test {}", "test {bla}", "test {bla} {0}", "test {bla} {0}", "test {bla} 1", "test '{bla}' {0}");
  }

  void doSomeLogs(SpiLogger logger) {
    logger.debug("test {0}"); // Returns "test {0}"
    logger.debug("test {}"); // Returns "test {}"
    logger.debug("test {bla}"); // Returns "test {bla}"
    logger.debug("test {bla} {0}"); // Returns "test {bla} {0}"
    logger.debug("test {bla} {0}", new Object[]{}); // Returns "test {bla} {0}"
    //logger.debug("Test {bla} {0}", 1); // fails: "can't parse argument number: bla"
    logger.debug("test '{bla}' {0}", 1); // Returns "test {bla} 1"
    logger.debug("test '{bla}' {0}"); // Returns "test '{bla}' {0}"
  }

}
