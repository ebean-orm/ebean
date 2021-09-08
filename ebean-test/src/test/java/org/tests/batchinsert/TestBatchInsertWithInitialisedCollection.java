package org.tests.batchinsert;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.OCachedBean;

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

    LoggedSql.start();

    Transaction txn = DB.beginTransaction();
    try {
      txn.setBatchMode(true);

      DB.saveAll(list);
      txn.commit();

    } finally {
      txn.end();
    }

    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(4);
    assertThat(loggedSql.get(0)).contains("insert into o_cached_bean (");
    assertThat(loggedSql.get(0)).contains("name) values (?");
  }
}
