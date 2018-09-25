package org.tests.o2m.jointable;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToManyJoinTableNoTableName extends BaseTestCase {

  private JtMonkeyGroup troop = new JtMonkeyGroup("Pink");

  private JtMonkey m0 = new JtMonkey("Sim3");
  private JtMonkey m1 = new JtMonkey("Tim3");
  private JtMonkey m2 = new JtMonkey("Uim3");

  private void initialInsert() {
    Ebean.saveAll(Arrays.asList(troop, m0, m1, m2));
  }

  @Test
  public void base() {

    initialInsert();

    LoggedSqlCollector.start();

    // make m0 dirty ... but no cascade saved?
    m0.setFoodPreference("camera");
    troop.getMonkeys().add(m0);
    troop.getMonkeys().add(m1);

    Ebean.save(troop);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("insert into mkeygroup_monkey (mkeygroup_pid, monkey_mid) values (?, ?)");

    int intersectionRows = Ebean.createSqlQuery("select count(*) as total from mkeygroup_monkey where mkeygroup_pid = ?")
      .setParameter(1, troop.getPid())
      .findOne()
      .getInteger("total");

    assertThat(intersectionRows).isEqualTo(2);

    LoggedSqlCollector.current();
    JtMonkeyGroup fetchTroop = Ebean.find(JtMonkeyGroup.class)
      .fetch("monkeys")
      .where().idEq(troop.getPid())
      .findOne();

    assertThat(fetchTroop.getMonkeys()).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertThat(trimSql(sql.get(0))).contains("from mkeygroup t0 left join mkeygroup_monkey t1z_ on t1z_.mkeygroup_pid = t0.pid  left join monkey t1 on t1.mid = t1z_.monkey_mid  where t0.pid = ?");
    assertThat(trimSql(sql.get(0))).contains("select t0.pid, t0.name, t0.version, t1.mid, t1.name, t1.food_preference, t1.version");

    Ebean.delete(troop);

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from mkeygroup_monkey where mkeygroup_pid = ?");
    assertThat(sql.get(1)).contains("delete from mkeygroup where pid=? and version=?");

  }

}
