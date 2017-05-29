package io.ebean.config.dbplatform.oracle;

import io.ebean.BackgroundExecutor;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.dbplatform.SequenceIdGenerator;

/**
 * Oracle specific sequence Id Generator.
 */
public class OracleSequenceIdGenerator extends SequenceIdGenerator {

  private final String baseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public OracleSequenceIdGenerator(BackgroundExecutor be, TenantDataSourceProvider ds, String seqName, int batchSize, CurrentTenantProvider currentTenantProvider) {
    super(be, ds, seqName, batchSize, currentTenantProvider);
    this.baseSql = "select " + seqName + ".nextval, a from (select level as a FROM dual CONNECT BY level <= ";
  }

  @Override
  public String getSql(int batchSize) {
    return baseSql + batchSize + ")";
  }
}
