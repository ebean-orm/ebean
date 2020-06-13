package io.ebean.config.dbplatform.h2;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceStepIdGenerator;

import javax.sql.DataSource;

class H2DbSequence extends SequenceStepIdGenerator {

  H2DbSequence(BackgroundExecutor be, DataSource ds, String seqName, int stepSize) {
    super(be, ds, seqName, stepSize, "select " + seqName + ".nextval");
  }

}
