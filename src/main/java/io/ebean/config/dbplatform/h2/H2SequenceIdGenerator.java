package io.ebean.config.dbplatform.h2;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceBatchIdGenerator;

import javax.sql.DataSource;

/**
 * H2 specific sequence Id Generator.
 */
public class H2SequenceIdGenerator extends SequenceBatchIdGenerator {

  private final String baseSql;
  private final String unionBaseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public H2SequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    super(be, ds, seqName, batchSize);
    this.baseSql = "select " + seqName + ".nextval";
    this.unionBaseSql = " union " + baseSql;
  }

  @Override
  public String getSql(int batchSize) {

    StringBuilder sb = new StringBuilder();
    sb.append(baseSql);
    for (int i = 1; i < batchSize; i++) {
      sb.append(unionBaseSql);
    }
    return sb.toString();
  }
}
