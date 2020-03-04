package io.ebean.config.dbplatform.postgres;

import io.ebean.BackgroundExecutor;
import io.ebean.config.dbplatform.SequenceStepIdGenerator;

import javax.sql.DataSource;

class PostgresDbSequence extends SequenceStepIdGenerator {

  PostgresDbSequence(BackgroundExecutor be, DataSource ds, String seqName, int stepSize) {
    super(be, ds, seqName, stepSize, "select nextval('\" + seqName + \"')");
  }

}
