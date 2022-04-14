package org.tests.model.history;

import static org.assertj.core.api.Assertions.assertThat;

import io.ebean.DB;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.Test;

public class TestHistoryOneToOne extends BaseTestCase {

  @IgnorePlatform({Platform.ORACLE, Platform.COCKROACH})
  @Test
  public void test() throws InterruptedException {
    HistorylessOneToOne historylessOneToOne = new HistorylessOneToOne();
    historylessOneToOne.setHistoryOneToOne(new HistoryOneToOne("one"));
    DB.save(historylessOneToOne);
    Thread.sleep(20);
    LoggedSql.start();

    HistoryOneToOne oneFetched = DB.find(HistoryOneToOne.class)
                          .asOf(new Timestamp(System.currentTimeMillis()))
                          .findOne();

    assertThat(oneFetched.getHistorylessOneToOne().getHistoryOneToOne().getId()).isEqualTo(historylessOneToOne.getHistoryOneToOne().getId());

    List<String> sql = LoggedSql.stop();
  }
}
