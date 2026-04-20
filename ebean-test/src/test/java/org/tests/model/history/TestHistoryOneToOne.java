package org.tests.model.history;

import io.ebean.DB;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestHistoryOneToOne extends BaseTestCase {

  private Long expectedLessId;
  private Long expectedHistoryId;

  @IgnorePlatform({Platform.ORACLE, Platform.COCKROACH})
  @Test
  void test() throws InterruptedException {
    HistorylessOneToOne historylessOneToOne = new HistorylessOneToOne("less");
    historylessOneToOne.setHistoryOneToOne(new HistoryOneToOne("one"));
    DB.save(historylessOneToOne);
    Thread.sleep(20);

    expectedLessId = historylessOneToOne.getId();
    expectedHistoryId = historylessOneToOne.getHistoryOneToOne().getId();

    findViaHistory();
    findViaLess();

    findViaLess_fetch();
    findViaHistory_fetch();
  }

  @IgnorePlatform({Platform.ORACLE, Platform.COCKROACH})
  @Test
  void testVersionsWithHistoryOverHistoryless() throws InterruptedException {
    HistorylessOneToOne historylessOneToOne = new HistorylessOneToOne("less");
    historylessOneToOne.setHistoryOneToOne(new HistoryOneToOne("one"));
    historylessOneToOne.setHistoryManyToOne(new HistoryManyToOne("many"));
    DB.save(historylessOneToOne);

    LoggedSql.start();
    int count = DB.find(HistoryOneToOne.class)
      .where()
      .eq("historylessOneToOne.historyManyToOne.deleted", false)
      .findVersions()
      .size();
    List<String> sql = LoggedSql.stop();

    assertThat(count)
      .describedAs("sql was: " + sql.get(0))
      .isEqualTo(1);
    assertSql(sql.get(0)).contains("this does not exist");
  }



  private void findViaLess() {
    HistorylessOneToOne lessFetched = DB.find(HistorylessOneToOne.class)
      .asOf(new Timestamp(System.currentTimeMillis()))
      .findOne();

    expected_findViaLess(lessFetched);
  }


  private void findViaLess_fetch() {
    HistorylessOneToOne lessFetched = DB.find(HistorylessOneToOne.class)
      .asOf(new Timestamp(System.currentTimeMillis()))
      .fetch("historyOneToOne")
      .findOne();

    expected_findViaLess(lessFetched);
  }

  private void expected_findViaLess(HistorylessOneToOne lessFetched) {
    assert lessFetched != null;
    assertThat(lessFetched.getId()).isEqualTo(expectedLessId);
    HistoryOneToOne history1 = lessFetched.getHistoryOneToOne();
    assertThat(history1.getId()).isEqualTo(expectedHistoryId);
    assertThat(history1.less().getId()).isEqualTo(expectedLessId);
    assertThat(history1.less().getName()).isEqualTo("less");
    assertThat(history1.getName()).isEqualTo("one");
  }


  private void findViaHistory_fetch() {

    HistoryOneToOne oneFetched = DB.find(HistoryOneToOne.class)
      .asOf(new Timestamp(System.currentTimeMillis()))
      .fetch("historylessOneToOne")
      .findOne();

    expected_viaHistory(oneFetched);
  }

  private void findViaHistory() {
    HistoryOneToOne oneFetched = DB.find(HistoryOneToOne.class)
                          .asOf(new Timestamp(System.currentTimeMillis()))
                          .findOne();

    expected_viaHistory(oneFetched);
  }

  private void expected_viaHistory(HistoryOneToOne oneFetched) {
    assert oneFetched != null;
    assertThat(oneFetched.getId()).isEqualTo(expectedHistoryId);
    assertThat(oneFetched.less().getId()).isEqualTo(expectedLessId);
    assertThat(oneFetched.less().getName()).isEqualTo("less");
    assertThat(oneFetched.getName()).isEqualTo("one");
  }
}
