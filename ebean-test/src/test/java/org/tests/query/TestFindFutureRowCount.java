package org.tests.query;

import io.ebean.*;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasic;

import static org.assertj.core.api.Assertions.assertThat;

class TestFindFutureRowCount extends BaseTestCase {

  @Test
  void count_when_inTransaction() {
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

  @Test
  void findFutureIds_when_inTransaction() throws Exception {
    try (Transaction transaction = DB.beginTransaction()) {
      EBasic basic = new EBasic("findFutureIds_when_inTransaction");
      DB.save(basic);

      List<Object> ids = DB.find(EBasic.class)
        .where().eq("name", "findFutureIds_when_inTransaction")
        .findIds();

      Object expectedIdValue = ids.get(0);

      FutureIds<EBasic> futureIds = DB.find(EBasic.class)
        .where().eq("name", "findFutureIds_when_inTransaction")
        .findFutureIds();

      List<Object> fids = futureIds.get();
      assertThat(fids).hasSize(1);
      assertThat(fids.get(0)).isEqualTo(expectedIdValue);

      FutureIds<EBasic> futureIdsUsingTxn = DB.find(EBasic.class)
        .usingTransaction(transaction)
        .where().eq("name", "findFutureIds_when_inTransaction")
        .findFutureIds();

      List<Object> fids2 = futureIdsUsingTxn.get();
      assertThat(fids2).hasSize(1);
      assertThat(fids2.get(0)).isEqualTo(expectedIdValue);
    }
  }

  @Test
  void findFutureList_when_inTransaction() throws Exception {
    try (Transaction transaction = DB.beginTransaction()) {
      EBasic basic = new EBasic("findFutureList_when_inTransaction");
      DB.save(basic);

      List<EBasic> list = DB.find(EBasic.class)
        .where().eq("name", "findFutureList_when_inTransaction")
        .findList();

      Object expectedIdValue = list.get(0).getId();

      FutureList<EBasic> futureIds = DB.find(EBasic.class)
        .where().eq("name", "findFutureList_when_inTransaction")
        .findFutureList();

      List<EBasic> fids = futureIds.get();
      assertThat(fids).hasSize(1);
      assertThat(fids.get(0).getId()).isEqualTo(expectedIdValue);

      FutureList<EBasic> futureUsingTxn = DB.find(EBasic.class)
        .usingTransaction(transaction)
        .where().eq("name", "findFutureList_when_inTransaction")
        .findFutureList();

      List<EBasic> fids2 = futureUsingTxn.get();
      assertThat(fids2).hasSize(1);
      assertThat(fids2.get(0).getId()).isEqualTo(expectedIdValue);
    }
  }

  @Test
  void findFutures_when_newTransaction() throws Exception {
    EBasic basic = new EBasic("findFutures_when_newTransaction");
    DB.save(basic);

    List<EBasic> list = DB.find(EBasic.class)
      .where().eq("name", "findFutures_when_newTransaction")
      .findList();

    Object expectedIdValue = list.get(0).getId();

    FutureList<EBasic> futureList = DB.find(EBasic.class)
      .where().eq("name", "findFutures_when_newTransaction")
      .findFutureList();

    List<EBasic> flist = futureList.get();
    assertThat(flist).hasSize(1);
    assertThat(flist.get(0).getId()).isEqualTo(expectedIdValue);

    FutureIds<EBasic> futureIds = DB.find(EBasic.class)
      .where().eq("name", "findFutures_when_newTransaction")
      .findFutureIds();

    List<Object> fids = futureIds.get();
    assertThat(fids).hasSize(1);
    assertThat(fids.get(0)).isEqualTo(expectedIdValue);

    FutureRowCount<EBasic> futureCount = DB.find(EBasic.class)
      .where().eq("name", "findFutures_when_newTransaction")
      .findFutureCount();

    assertThat(futureCount.get()).isEqualTo(1);

    DB.delete(basic);
  }

}
