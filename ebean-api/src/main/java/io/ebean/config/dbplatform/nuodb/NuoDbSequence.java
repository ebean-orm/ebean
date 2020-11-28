package io.ebean.config.dbplatform.nuodb;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceStepIdGenerator;

import javax.sql.DataSource;

public class NuoDbSequence extends SequenceStepIdGenerator {

  private final String nextSql;

  /**
   * Construct where batchSize is the sequence step size.
   */
  public NuoDbSequence(BackgroundExecutor be, DataSource ds, String seqName, int stepSize) {
    super(be, ds, seqName, stepSize);
    this.nextSql = "select next value for " + seqName + " from dual";
  }

  @Override
  public String getSql(int batchSize) {
    return nextSql;
  }
}
