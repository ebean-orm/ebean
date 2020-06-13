package io.ebean.config.dbplatform.nuodb;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceStepIdGenerator;

import javax.sql.DataSource;

class NuoDbSequence extends SequenceStepIdGenerator {

  NuoDbSequence(BackgroundExecutor be, DataSource ds, String seqName, int stepSize) {
    super(be, ds, seqName, stepSize, "select next value for " + seqName + " from dual");
  }

}
