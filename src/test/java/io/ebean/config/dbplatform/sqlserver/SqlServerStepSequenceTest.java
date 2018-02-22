package io.ebean.config.dbplatform.sqlserver;

import io.ebean.BackgroundExecutor;
import io.ebean.BaseTestCase;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlServerStepSequenceTest extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(SqlServerStepSequenceTest.class);

  @Ignore
  @ForPlatform(Platform.SQLSERVER)
  @Test
  public void seq() {


    server().createSqlUpdate("drop sequence if exists sqls_testseq_9876").execute();
    server().createSqlUpdate("create sequence sqls_testseq_9876 start with 1 increment by 50").execute();

    BackgroundExecutor be = server().getBackgroundExecutor();
    DataSource ds = server().getPluginApi().getDataSource();

    SqlServerStepSequence s = new SqlServerStepSequence(be, ds, "sqls_testseq_9876", 50);

    Object id = s.nextId(null);
    assertThat(id).isEqualTo(1L);

    for (int i = 0; i < 20; i++) {
      Object val = s.nextId(null);
      log.warn("val: "+val);
    }

    log.warn("here");

    for (int i = 0; i < 20; i++) {
      Object val = s.nextId(null);
      log.warn("val: "+val);
    }

    for (int i = 0; i < 100; i++) {
      Object val = s.nextId(null);
      log.warn("val: "+val);
    }
  }

}
