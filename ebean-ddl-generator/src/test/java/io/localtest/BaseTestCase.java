package io.localtest;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebeaninternal.api.SpiEbeanServer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseTestCase {

  protected static Logger logger = LoggerFactory.getLogger(BaseTestCase.class);

  /**
   * this is the clock delta that may occur between testing machine and db server.
   * If the clock delta of DB server is in future, an "asOf" query may not find the
   * correct entry.
   *
   * Note: That some tests may use a Thread.sleep to wait, so that the local system clock
   * can catch up. So don't set that to a too high value.
   */
  public static final int DB_CLOCK_DELTA;

  static {
    String s = System.getProperty("dbClockDelta");
    if (s != null && !s.isEmpty()) {
      DB_CLOCK_DELTA = Integer.parseInt(s);
    } else {
      DB_CLOCK_DELTA = 100;
    }
    try {
      // First try, if we get the default server. If this fails, all tests will fail.
      DB.getDefault();
    } catch (Throwable e) {
      logger.error("Fatal error while getting ebean-server. Exiting...", e);
      System.exit(1);
    }
  }

  /**
   * MS SQL Server does not allow setting explicit values on identity columns
   * so tests that do this need to be skipped for SQL Server.
   */
  public boolean isSqlServer() {
    return Platform.SQLSERVER == platform();
  }
  
  public boolean isDB2() {
    return Platform.DB2 == platform();
  }

  public boolean isH2() {
    return Platform.H2 == platform();
  }

  public boolean isOracle() {
    return Platform.ORACLE == platform();
  }

  public boolean isPostgres() {
    return Platform.POSTGRES == platform().base();
  }

  public boolean isMySql() {
    return Platform.MYSQL == platform();
  }

  public boolean isMariaDB() {
    return Platform.MARIADB == platform();
  }

  public boolean isHana() {
    return Platform.HANA == platform();
  }

  public boolean isPlatformBooleanNative() {
    return Types.BOOLEAN == spiEbeanServer().databasePlatform().getBooleanDbType();
  }

  public boolean isPlatformOrderNullsSupport() {
    return isH2() || isPostgres();
  }

  public boolean isPlatformSupportsDeleteTableAlias() {
    return spiEbeanServer().databasePlatform().isSupportsDeleteTableAlias();
  }

  public boolean isPersistBatchOnCascade() {
    return spiEbeanServer().databasePlatform().getPersistBatchOnCascade() != PersistBatch.NONE;
  }

  /**
   * Wait for the L2 cache to propagate changes post-commit.
   */
  protected void awaitL2Cache() {
    // do nothing, used to thread sleep
  }

  protected Platform platform() {
    return spiEbeanServer().databasePlatform().getPlatform().base();
  }

  protected SpiEbeanServer spiEbeanServer() {
    return (SpiEbeanServer) DB.getDefault();
  }

  protected Database server() {
    return DB.getDefault();
  }

}
