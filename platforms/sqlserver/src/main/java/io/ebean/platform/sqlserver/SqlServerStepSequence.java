package io.ebean.platform.sqlserver;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceStepIdGenerator;

import javax.sql.DataSource;

final class SqlServerStepSequence extends SequenceStepIdGenerator {

  private final String nextSql;

  /**
   * Construct where batchSize is the sequence step size.
   */
  SqlServerStepSequence(BackgroundExecutor be, DataSource ds, String seqName, int stepSize) {
    super(be, ds, seqName, stepSize);
    this.nextSql = "select next value for "+seqName;
  }

  @Override
  public String getSql(int batchSize) {
    return nextSql;
  }
}
