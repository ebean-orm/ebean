package io.ebean.config.dbplatform.db2;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceIdGenerator;

import javax.sql.DataSource;

/**
 * DB2 specific sequence Id Generator.
 */
public class DB2SequenceIdGenerator extends SequenceIdGenerator {

  private final String sql1;
  private final String sql2;

  /**
   * Construct given a dataSource and sql to return the next sequence value.
   */
  public DB2SequenceIdGenerator(BackgroundExecutor be, DataSource ds, String seqName, int batchSize) {
    super(be, ds, seqName, batchSize);
    this.sql1 = "WITH SEQLOOP_ (I) AS (SELECT 1 FROM SYSIBM.SYSDUMMY1 UNION ALL SELECT I + 1 FROM SEQLOOP_ WHERE I < ";
    this.sql2 = ") SELECT NEXTVAL FOR " + seqName +" FROM SEQLOOP_";
  }

  @Override
  public String getSql(int batchSize) {
    StringBuilder sb = new StringBuilder(sql1).append(batchSize).append(sql2);
    return sb.toString();
  }
}
