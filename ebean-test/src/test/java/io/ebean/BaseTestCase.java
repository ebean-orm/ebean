package io.ebean;

import io.ebean.annotation.PersistBatch;
import io.ebean.annotation.Platform;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.config.dbplatform.IdType;
import io.ebean.meta.MetaTimedMetric;
import io.ebean.meta.ServerMetrics;
import io.ebean.util.StringHelper;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.api.SpiTransaction;
import io.ebeaninternal.server.core.HelpCreateQueryRequest;
import io.ebeaninternal.server.core.OrmQueryRequest;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.expression.platform.DbExpressionHandler;
import io.ebeaninternal.server.expression.platform.DbExpressionHandlerFactory;
import io.ebeaninternal.server.transaction.TransactionScopeManager;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Country;

import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PlatformCondition.class)
public abstract class BaseTestCase {

  protected static Logger logger = LoggerFactory.getLogger(BaseTestCase.class);

  @AfterEach
  public void checkForLeak(TestInfo testInfo) {
    TransactionScopeManager scope = spiEbeanServer().transactionManager().scope();
    SpiTransaction trans = scope.getInScope();
    if (trans != null) {
      String msg = getClass().getSimpleName() + "." + testInfo.getDisplayName() + " did not clear threadScope:" + trans;
      scope.clearExternal(); // clear for next test
      Assertions.fail(msg);
    }
  }


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

  protected void clearAllL2Cache() {
    server().cacheManager().clearAll();
  }

  protected void resetAllMetrics() {
    server().metaInfo().resetAllMetrics();
  }

  protected ServerMetrics collectMetrics() {
     return server().metaInfo().collectMetrics();
  }

  protected List<MetaTimedMetric> visitTimedMetrics() {
    return collectMetrics().timedMetrics();
  }

  protected List<MetaTimedMetric> sqlMetrics() {
    List<MetaTimedMetric> timedMetrics = visitTimedMetrics();

    return timedMetrics.stream()
      .filter((it) -> it.name().startsWith("sql.") || it.name().startsWith("orm."))
      .collect(Collectors.toList());
  }

  protected SpiTransaction getInScopeTransaction() {
    return spiEbeanServer().transactionManager().scope().getInScope();
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

  protected void assertSqlBind(String sql) {
    assertThat(sql).contains("-- bind");
  }

  protected void assertSqlBind(List<String> sql, int i) {
    assertThat(sql.get(i)).contains("-- bind");
  }

  protected void assertSqlBind(List<String> sql, int from, int to) {
    for (int i = from; i <= to; i++) {
      assertThat(sql.get(i)).contains("-- bind");
    }
  }

  protected AbstractCharSequenceAssert<?, String> assertSql(Query<?> query, int count) {
    return org.assertj.core.api.Assertions.assertThat(sqlOf(query, count));
  }

  protected AbstractCharSequenceAssert<?, String> assertSql(Query<?> query) {
    return org.assertj.core.api.Assertions.assertThat(sqlOf(query));
  }

  protected AbstractCharSequenceAssert<?, String> assertSql(String sql) {
    return org.assertj.core.api.Assertions.assertThat(trimSql(sql));
  }

  protected String trimSql(String sql) {
    if (sql.contains(" c0,") || sql.contains(" c0 ") || sql.contains(" c1,") || sql.contains(" c1 ")) {
      // for oracle we include column alias so lets remove those
      return trimSql(sql, 10);
    }
    return sql;//trimSql(sql, 0);
  }

  /**
   * Trim out column alias if required from the generated sql.
   */
  protected String trimSql(String sql, int columns) {
    for (int i = 0; i <= columns; i++) {
      sql = StringHelper.replace(sql, " c" + i + ",", ",");
    }
    for (int i = 0; i <= columns; i++) {
      sql = StringHelper.replace(sql, " c" + i + " ", " ");
    }
    return sql;
  }

  public boolean isPlatformCaseSensitive() {
    return spiEbeanServer().databasePlatform().isCaseSensitiveCollation();
  }

  /**
   * MS SQL Server does not allow setting explicit values on identity columns
   * so tests that do this need to be skipped for SQL Server.
   */
  public boolean isSqlServer() {
    return Platform.SQLSERVER == platform();
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

  public boolean isNuoDb() {
    return Platform.NUODB == platform();
  }

  public boolean isDb2() {
    return Platform.DB2 == platform();
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

  protected <T> BeanDescriptor<T> getBeanDescriptor(Class<T> cls) {
    return spiEbeanServer().descriptor(cls);
  }

  protected <T> ServerCacheStatistics getBeanCacheStats(Class<T> cls, boolean reset) {
    return server().cacheManager().beanCache(cls).statistics(reset);
  }

  protected Platform platform() {
    return spiEbeanServer().databasePlatform().getPlatform().base();
  }

  protected IdType idType() {
    return spiEbeanServer().databasePlatform().getDbIdentity().getIdType();
  }

  protected SpiEbeanServer spiEbeanServer() {
    return (SpiEbeanServer) DB.getDefault();
  }

  protected Database server() {
    return DB.getDefault();
  }

  protected void loadCountryCache() {

    DB.find(Country.class)
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

  /**
   * Platform specific CONCAT clause.
   */
  protected String concat(String property0, String separator, String property1) {
    return concat(property0, separator, property1, null);
  }

  protected String concat(String property0, String separator, String property1, String suffix) {
    DbExpressionHandler dbExpressionHandler = DbExpressionHandlerFactory.from(spiEbeanServer().databasePlatform());
    return dbExpressionHandler.concat(property0, separator, property1, suffix);
  }

  protected <T> OrmQueryRequest<T> createQueryRequest(SpiQuery.Type type, Query<T> query, Transaction t) {
    return HelpCreateQueryRequest.create(server(), type, query, t);
  }
}
