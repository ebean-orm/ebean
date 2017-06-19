package io.ebean.config.dbplatform.db2;

import io.ebean.BackgroundExecutor;
import io.ebean.config.CurrentTenantProvider;
import io.ebean.config.TenantDataSourceProvider;
import io.ebean.config.dbplatform.SequenceIdGenerator;

/**
 * DB2 specific sequence Id Generator.
 */
public class DB2SequenceIdGenerator extends SequenceIdGenerator {

  private final String baseSql;
  private final String unionBaseSql;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public DB2SequenceIdGenerator(BackgroundExecutor be, TenantDataSourceProvider ds, String seqName, int batchSize, CurrentTenantProvider currentTenantProvider) {
    super(be, ds, seqName, batchSize, currentTenantProvider);
    this.baseSql = "values nextval for " + seqName;
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
