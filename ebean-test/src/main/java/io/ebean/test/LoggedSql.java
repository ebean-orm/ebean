package io.ebean.test;

import io.ebeaninternal.api.SpiLogger;
import io.ebeaninternal.server.logger.DSpiLogger;

import java.util.List;

/**
 * Provides access to the messages logged to <code>io.ebean.SQL</code>.
 * <p>
 * This is here to allow easy access to the SQL that was executed during testing and
 * if desired we can use that to perform asserts in tests.
 * </p>
 * <pre>{@code
 *
 *     // start capturing SQL log messages
 *     LoggedSql.start();
 *
 *     List<Customer> customers =
 *           Customer.find.where()
 *             .name.ilike("rob%")
 *             .findList();
 *
 *       assertNotNull(customers);
 *
 *     // perform an insert
 *     new Product("ad", "asd").save()
 *
 *
 *     // return the captured SQL log messages
 *     // since LoggedSql.start()
 *     List<String> sql = LoggedSql.stop();
 *
 *     assertThat(sql).hasSize(2);
 *     assertThat(sql.get(0)).contains("from customer");
 *     assertThat(sql.get(1)).contains("into product");
 *
 * }</pre>
 */
public class LoggedSql {

  private static CaptureLogger sqlLogger;

  /**
   * Internal use - register the logger for <code>io.ebean.SQL</code>.
   */
  static SpiLogger register(DSpiLogger logger) {
    if (sqlLogger == null) {
      sqlLogger = new CaptureLogger(logger);
    }
    return sqlLogger;
  }

  /**
   * Start the capture of the <code>io.ebean.SQL</code> messages.
   */
  public static List<String> start() {
    return sqlLogger.start();
  }

  /**
   * Stop the capture of the <code>io.ebean.SQL</code> messages and return the messages/sql
   * that was captured since the call to start().
   */
  public static List<String> stop() {
    return sqlLogger.stop();
  }

  /**
   * Collect and return the messages/sql that was captured since the call to start() or collect().
   * <p>
   * Unlike stop() collection of messages will continue.
   * </p>
   */
  public static List<String> collect() {
    return sqlLogger.collect();
  }

}
