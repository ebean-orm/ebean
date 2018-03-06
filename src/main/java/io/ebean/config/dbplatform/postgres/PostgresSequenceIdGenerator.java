package io.ebean.config.dbplatform.postgres;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceBatchIdGenerator;

import javax.sql.DataSource;

/**
 * Postgres specific sequence Id Generator.
 */
public class PostgresSequenceIdGenerator extends SequenceBatchIdGenerator {

  private final String baseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public PostgresSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    super(be, ds, seqName, batchSize);
    this.baseSql = "select nextval('" + seqName + "'), s.generate_series from (select generate_series from generate_series(1,";
  }

  @Override
  public String getSql(int batchSize) {
    return baseSql + batchSize + ") ) as s";
  }
}
