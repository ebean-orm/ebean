package com.avaje.ebean.config.dbplatform;

import javax.sql.DataSource;

import com.avaje.ebean.BackgroundExecutor;

/**
 * DB2 specific sequence Id Generator.
 */
public class DB2SequenceIdGenerator extends SequenceIdGenerator {

  private final String baseSql;
  private final String unionBaseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public DB2SequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    super(be, ds, seqName, batchSize);
    this.baseSql = "select nextval for " + seqName;
    this.unionBaseSql = " union " + baseSql;
  }

  public String getSql(int batchSize) {

    StringBuilder sb = new StringBuilder();
    sb.append(baseSql);
    for (int i = 1; i < batchSize; i++) {
      sb.append(unionBaseSql);
    }
    return sb.toString();
  }
}
