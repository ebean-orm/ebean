package io.ebean.config.dbplatform.postgres;

import io.ebean.BackgroundExecutor;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.dbplatform.SequenceIdGenerator;

/**
 * Postgres specific sequence Id Generator.
 */
public class PostgresSequenceIdGenerator extends SequenceIdGenerator {

  private final String baseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public PostgresSequenceIdGenerator(BackgroundExecutor be, TenantDataSourceProvider ds, String seqName, int batchSize, CurrentTenantProvider currentTenantProvider) {
    super(be, ds, seqName, batchSize, currentTenantProvider);
    this.baseSql = "select nextval('" + seqName + "'), s.generate_series from (select generate_series from generate_series(1,";
  }

  @Override
  public String getSql(int batchSize) {
    return baseSql + batchSize + ") ) as s";
  }
}
