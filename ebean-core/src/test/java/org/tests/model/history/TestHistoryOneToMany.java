package org.tests.model.history;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHistoryOneToMany extends BaseTestCase {

  @IgnorePlatform(Platform.ORACLE)
  @Test
  public void test() throws InterruptedException {

    HiTOne one = new HiTOne("one");

    HiTThree _31 = new HiTThree("3.1");
    HiTThree _32 = new HiTThree("3.2");
    HiTThree _33 = new HiTThree("3.3");
    HiTThree _34 = new HiTThree("3.4");

    HiTTwo _21 = new HiTTwo("2.1");
    _21.getThrees().add(_31);
    _21.getThrees().add(_32);
    _21.getThrees().add(_33);

    HiTTwo _22 = new HiTTwo("2.2");
    _22.getThrees().add(_34);

    one.getTwos().add(_21);
    one.getTwos().add(_22);


    DB.save(one);
    Thread.sleep(20);

    LoggedSqlCollector.start();

    List<HiTOne> list = DB.find(HiTOne.class)
      .fetch("twos")
      .fetch("twos.threes")
      .where().ilike("name", "on%")
      .setMaxRows(10)
      .asOf(new Timestamp(System.currentTimeMillis()))
      .findList();

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getTwos()).hasSize(2);
    assertThat(list.get(0).getTwos().get(0).getThrees()).hasSize(3);

    if (isH2()) {
      assertThat(sql).hasSize(2);
      assertSql(sql.get(0)).contains("from hi_tone_with_history t0 where (t0.sys_period_start <= ? and (t0.sys_period_end is null or t0.sys_period_end > ?)) and lower(t0.name) like ? escape''  limit 10");
      assertSql(sql.get(1)).contains("from hi_ttwo_with_history t0 left join hi_tthree_with_history t1 on t1.hi_ttwo_id = t0.id and (t1.sys_period_start <= ? and (t1.sys_period_end is null or t1.sys_period_end > ?)) where (t0.sys_period_start <= ? and (t0.sys_period_end is null or t0.sys_period_end > ?)) and (t0.hi_tone_id) in (?)");
    }
  }
}
