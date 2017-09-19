package org.tests.batchinsert;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Transaction;
import io.ebean.annotation.PersistBatch;
import org.tests.model.basic.OCachedBean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBatchInsertWithInitialisedCollection extends BaseTestCase {

  @Test
  public void test() {


    List<OCachedBean> list = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      OCachedBean bean = new OCachedBean();
      bean.setName("name " + i);
      list.add(bean);
    }

    LoggedSqlCollector.start();

    Transaction txn = Ebean.beginTransaction();
    try {
      txn.setBatch(PersistBatch.ALL);

      Ebean.saveAll(list);
      txn.commit();

    } finally {
      txn.end();
    }

    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(3);

    for (String sql : loggedSql) {
      assertThat(sql).contains("insert into o_cached_bean (");
      assertThat(sql).contains("name) values (?");
    }

  }
}
