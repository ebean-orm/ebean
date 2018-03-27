package io.ebean;

import io.ebean.annotation.Platform;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.MetricType;
import io.ebean.util.StringHelper;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.core.HelpCreateQueryRequest;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import org.avaje.agentloader.AgentLoader;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Country;

import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ConditionalTestRunner.class)
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
    logger.debug("... preStart");
    if (!AgentLoader.loadAgentFromClasspath("ebean-agent", "debug=1")) {
      logger.info("avaje-ebeanorm-agent not found in classpath - not dynamically loaded");
    }
    try {
      // First try, if we get the default server. If this fails, all tests will fail.
      Ebean.getDefaultServer();
    } catch (Throwable e) {
      logger.error("Fatal error while getting ebean-server. Exiting...", e);
      System.exit(1);
    }
  }

  protected void resetAllMetrics() {
    server().getMetaInfoManager().resetAllMetrics();
  }

  protected BasicMetricVisitor visitMetricsBasic() {
     return server().getMetaInfoManager().visitBasic();
  }

  protected List<MetaTimedMetric> visitTimedMetrics() {
    return visitMetricsBasic().getTimedMetrics();
  }

  protected List<MetaTimedMetric> sqlMetrics() {
    List<MetaTimedMetric> timedMetrics = visitTimedMetrics();

    return timedMetrics.stream()
      .filter((it) -> it.getMetricType() == MetricType.SQL)
      .collect(Collectors.toList());
  }

  /**
   * Return the generated sql trimming column alias if required.
   */
  protected String sqlOf(Query<?> query) {
    return trimSql(query.getGeneratedSql());
  }

  /**
   * Return the generated sql trimming column alias if required.
   */
  protected String sqlOf(Query<?> query, int columns) {
    return trimSql(query.getGeneratedSql(), columns);
  }

  protected String trimSql(String sql) {

    if (sql.contains(" c1,")) {
      // for oracle we include column alias so lets remove those
      return trimSql(sql, 10);
    }
    return trimSql(sql, 0);
  }

  /**
   * Trim out column alias if required from the generated sql.
   */
  protected String trimSql(String sql, int columns) {
    for (int i = 0; i <= columns; i++) {
      sql = StringHelper.replaceString(sql, " c" + i + ",", ",");
    }
    for (int i = 0; i <= columns; i++) {
      sql = StringHelper.replaceString(sql, " c" + i + " ", " ");
    }
    return sql;
  }

  /**
   * MS SQL Server does not allow setting explicit values on identity columns
   * so tests that do this need to be skipped for SQL Server.
   */
  public boolean isSqlServer() {
    return Platform.SQLSERVER17 == platform();
  }

  public boolean isH2() {
    return Platform.H2 == platform();
  }

  public boolean isHSqlDb() {
    return Platform.HSQLDB == platform();
  }

  public boolean isOracle() {
    return Platform.ORACLE == platform();
  }

  public boolean isDb2() {
    return Platform.DB2 == platform();
  }

  public boolean isPostgres() {
    return Platform.POSTGRES == platform();
  }

  public boolean isMySql() {
    return Platform.MYSQL == platform();
  }

  public boolean isPlatformBooleanNative() {
    return Types.BOOLEAN == spiEbeanServer().getDatabasePlatform().getBooleanDbType();
  }

  public boolean isPlatformOrderNullsSupport() {
    return isH2() || isPostgres();
  }

  /**
   * Wait for the L2 cache to propagate changes post-commit.
   */
  protected void awaitL2Cache() {
    // do nothing, used to thread sleep
  }

  protected <T> BeanDescriptor<T> getBeanDescriptor(Class<T> cls) {
    return spiEbeanServer().getBeanDescriptor(cls);
  }

  protected Platform platform() {
    return spiEbeanServer().getDatabasePlatform().getPlatform();
  }

  protected SpiEbeanServer spiEbeanServer() {
    return (SpiEbeanServer) Ebean.getDefaultServer();
  }

  protected EbeanServer server() {
    return Ebean.getDefaultServer();
  }

  protected void loadCountryCache() {

    Ebean.find(Country.class)
      .setBeanCacheMode(CacheMode.PUT)
      .findList();
  }

  /**
   * Platform specific IN clause assert.
   */
  protected void platformAssertIn(String sql, String containsIn) {
    if (isPostgres()) {
      assertThat(sql).contains(containsIn+" = any(");
    } else {
      assertThat(sql).contains(containsIn+" in ");
    }
    // H2 contains("where t0.name in (select * from table(x varchar = ?)");
  }

  /**
   * Platform specific NOT IN clause assert.
   */
  protected void platformAssertNotIn(String sql, String containsIn) {
    if (isPostgres()) {
      assertThat(sql).contains(containsIn+" != all(");
    } else {
      assertThat(sql).contains(containsIn+" not in ");
    }
  }

  protected <T> OrmQueryRequest<T> createQueryRequest(SpiQuery.Type type, Query<T> query, Transaction t) {
    return HelpCreateQueryRequest.create(server(), type, query, t);
  }
}
