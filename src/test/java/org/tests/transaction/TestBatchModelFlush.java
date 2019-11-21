package org.tests.transaction;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.config.dbplatform.IdType;

import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.m2m.MnyB;
import org.tests.model.m2m.MnyTopic;
import org.tests.model.m2m.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBatchModelFlush extends BaseTestCase {

  @SuppressWarnings("static-access")
  @Transactional(batchSize = 50)
  @Test
  public void insert() {

    new MnyB("TestBatchModelFlush_0").save();
    new MnyB("TestBatchModelFlush_1").save();

    MnyB bean = new MnyB("TestBatchModelFlush_2");
    bean.save();
    bean.db().currentTransaction().flush();

    MnyB bean2 = new MnyB("TestBatchModelFlush_3");
    bean2.save();
    bean2.db().flush();

    new MnyB("TestBatchModelFlush_4").save();
    Ebean.flush();

    // the rest is flushed on commit
    new MnyB("TestBatchModelFlush_5").save();
  }

  @Test
  public void multipleTopLevel_expect_singleFlush() {

    LoggedSqlCollector.start();

    // 2 unrelated "top level" beans being persisted
    MnyB m0 = new MnyB("BatchMultipleTop_0");
    MnyB m1 = new MnyB("BatchMultipleTop_1");
    Role r0 = new Role("Role_0");
    Role r1 = new Role("Role_1");

    MnyTopic t0 = new MnyTopic("MnyTopic_0");
    MnyTopic t1 = new MnyTopic("MnyTopic_1");

    try (Transaction transaction = DB.beginTransaction()) {
      transaction.setBatchMode(true);

      m0.save();
      DB.save(r0);
      DB.save(t0);
      DB.save(t1);

      m1.save();
      DB.save(r1);

      transaction.commit();
    }

    List<String> sql = LoggedSqlCollector.stop();

    // DEBUG io.ebean.SUM - txn[1001] BatchControl flush [MnyB:100 i:2, Role:101 i:2, MnyTopic:102 i:2]

    assertThat(sql).hasSize(9);

    // first saved to batch - (depth 100)
    assertThat(sql.get(0)).contains("insert into mny_b");
    if (idType() == IdType.IDENTITY) {
      assertThat(sql.get(1)).contains(" -- bind(BatchMultipleTop_0");
      assertThat(sql.get(2)).contains(" -- bind(BatchMultipleTop_1");
    }
    // second saved to batch - (depth 101)
    assertThat(sql.get(3)).contains("insert into mt_role");
    assertThat(sql.get(4)).contains(" -- bind(");
    assertThat(sql.get(5)).contains(" -- bind(");
    // third saved to batch - (depth 102)
    assertThat(sql.get(6)).contains("insert into mny_topic");
    if (idType() == IdType.IDENTITY) {
      assertThat(sql.get(7)).contains(" -- bind(MnyTopic_0");
      assertThat(sql.get(8)).contains(" -- bind(MnyTopic_1");
    }

    DB.delete(t0);
    DB.delete(t1);
    DB.delete(r0);
    DB.delete(r1);
    DB.delete(m0);
    DB.delete(m1);
  }
}
