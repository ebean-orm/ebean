package io.ebean.config.dbplatform.oracle;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceBatchIdGenerator;

import javax.sql.DataSource;

/**
 * Oracle specific sequence Id Generator.
 */
public class OracleSequenceIdGenerator extends SequenceBatchIdGenerator {

  private final String baseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public OracleSequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    super(be, ds, seqName, batchSize);
    this.baseSql = "select " + seqName + ".nextval, a from (select level as a FROM dual CONNECT BY level <= ";
  }

  @Override
  public String getSql(int batchSize) {
    return baseSql + batchSize + ")";
  }
}
