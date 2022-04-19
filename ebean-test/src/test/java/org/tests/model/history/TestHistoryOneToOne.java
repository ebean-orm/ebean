package org.tests.model.history;

import io.ebean.DB;
import io.ebean.annotation.Platform;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class TestHistoryOneToOne extends BaseTestCase {

  private Long expectedLessId;
  private Long expectedHistoryId;

  @IgnorePlatform({Platform.ORACLE, Platform.COCKROACH})
  @Test
  void test() throws InterruptedException {
    HistorylessOneToOne historylessOneToOne = new HistorylessOneToOne();
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
    assertThat(history1.getHistorylessOneToOne().getId()).isEqualTo(expectedLessId);
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
    assertThat(oneFetched.getHistorylessOneToOne().getId()).isEqualTo(expectedLessId);
  }
}
