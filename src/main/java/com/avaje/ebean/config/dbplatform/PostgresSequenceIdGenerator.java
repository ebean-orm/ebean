package com.avaje.ebean.config.dbplatform;

import javax.sql.DataSource;

import com.avaje.ebean.BackgroundExecutor;

/**
 * Postgres specific sequence Id Generator.
 */
public class PostgresSequenceIdGenerator extends SequenceIdGenerator {

  private final String baseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public PostgresSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName,
      int batchSize) {
    super(be, ds, seqName, batchSize);
    this.baseSql = "select nextval('" + seqName + "'), s.generate_series from ("
        + "select generate_series from generate_series(1,";
  }

  public String getSql(int batchSize) {
    return baseSql + batchSize + ") ) as s";
  }
}
