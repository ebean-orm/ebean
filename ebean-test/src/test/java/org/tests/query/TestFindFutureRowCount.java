package org.tests.query;

import io.ebean.DB;
import io.ebean.FutureRowCount;
import io.ebean.Transaction;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;

class TestFindFutureRowCount extends BaseTestCase {

  @Test
  void count_when_inTransaction() throws Exception {
    try (Transaction transaction = DB.beginTransaction()) {
      EBasic basic = new EBasic("count_when_inTransaction");
      DB.save(basic);

      int count = DB.find(EBasic.class)
        .where().eq("name", "count_when_inTransaction")
        .findCount();

      assertThat(count).isEqualTo(1);

      var pagedList = DB.find(EBasic.class)
        .where().eq("name", "count_when_inTransaction")
        .setMaxRows(100)
        .findPagedList();

      pagedList.loadCount();
      assertThat(pagedList.getList()).hasSize(1);
      assertThat(pagedList.getTotalCount()).isEqualTo(1);

      FutureRowCount<EBasic> futureCount = DB.find(EBasic.class)
        .where().eq("name", "count_when_inTransaction")
        .findFutureCount();

      assertThat(futureCount.get()).isEqualTo(1);

      FutureRowCount<EBasic> futureCountUsingTxn = DB.find(EBasic.class)
        .usingTransaction(transaction)
        .where().eq("name", "count_when_inTransaction")
        .findFutureCount();

      assertThat(futureCountUsingTxn.get()).isEqualTo(1);
    }
  }
}
