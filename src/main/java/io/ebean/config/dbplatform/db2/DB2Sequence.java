package io.ebean.config.dbplatform.db2;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceStepIdGenerator;

import javax.sql.DataSource;

/**
 * DB2 specific sequence Id Generator.
 */
class DB2Sequence extends SequenceStepIdGenerator {

  DB2Sequence(BackgroundExecutor be, DataSource ds, String seqName, int stepSize) {
    super(be, ds, seqName, stepSize, "values nextval for " + seqName);
  }

}
