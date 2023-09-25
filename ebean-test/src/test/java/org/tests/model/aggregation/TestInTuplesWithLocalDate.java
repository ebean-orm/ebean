package org.tests.model.aggregation;

import io.ebean.DB;
import io.ebean.InTuples;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import io.ebean.xtest.IgnorePlatform;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestInTuplesWithLocalDate extends BaseTestCase {

  @IgnorePlatform({Platform.SQLSERVER, Platform.DB2})
  @Test
  void inTuples_localDate() {
    DOrg org = new DOrg("inTuple");
    org.save();

    var machine = new DMachine(org, "inTuple");
    machine.save();

    LocalDate date = LocalDate.of(2023,8,16);

    var allStats = new ArrayList<DMachineStats>();
    for (int i = 0; i < 8; i++) {
      DMachineStats stats = new DMachineStats(machine, date);

      long offset = i;
      stats.setHours(offset * 3);
      stats.setTotalKms(offset * 100);
      stats.setCost(BigDecimal.valueOf(offset * 50));
      stats.setRate(BigDecimal.valueOf(offset * 2));
      allStats.add(stats);
      if (i > 2) {
        date = date.plusDays(1);
      }
    }

    DB.saveAll(allStats);

    LocalDate today = LocalDate.of(2023,8,16);

    var in = InTuples.of("date", "hours")
      .add(today, 0)
      .add(today, 3)
      .add(today, 9)
      .add(today.plusDays(1), 12)
      .add(today.plusDays(2), 15);

    LoggedSql.start();

    List<DMachineStats> result = DB.find(DMachineStats.class)
      .where()
      .inTuples(in)
      .eq("machine.name", "inTuple")
      .findList();

    assertThat(result).hasSize(5);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("where (t0.edate,t0.hours) in ((?,?),(?,?),(?,?),(?,?),(?,?)) and t1.name = ?");

    DB.deleteAll(allStats);
    DB.delete(machine);
    DB.delete(org);
  }

}
