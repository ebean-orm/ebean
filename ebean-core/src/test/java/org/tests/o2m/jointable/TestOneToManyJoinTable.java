package org.tests.o2m.jointable;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToManyJoinTable extends BaseTestCase {

  private JtTroop troop = new JtTroop("Blue");

  private JtMonkey m0 = new JtMonkey("Sim");
  private JtMonkey m1 = new JtMonkey("Tim");
  private JtMonkey m2 = new JtMonkey("Uim");

  private void initialInsert() {
    DB.saveAll(Arrays.asList(troop, m0, m1, m2));
  }

  @Test
  public void base() {

    initialInsert();

    LoggedSqlCollector.start();

    // make m0 dirty ... but no cascade saved?
    m0.setFoodPreference("banana");
    troop.getMonkeys().add(m0);
    troop.getMonkeys().add(m1);

    DB.save(troop);

    List<String> sql = LoggedSqlCollector.current();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(3);
      assertSql(sql.get(0)).contains("insert into troop_monkey (troop_pid, monkey_mid) values (?, ?)");
      assertSqlBind(sql, 1, 2);

    } else {
      assertThat(sql).hasSize(2);
      assertSql(sql.get(0)).contains("insert into troop_monkey (troop_pid, monkey_mid) values (?, ?)");
      assertSql(sql.get(1)).contains("insert into troop_monkey (troop_pid, monkey_mid) values (?, ?)");
    }

    long intersectionRows = DB.sqlQuery("select count(*) as total from troop_monkey where troop_pid = ?")
      .setParameter(1, troop.getPid())
      .findSingleLong();

    assertThat(intersectionRows).isEqualTo(2L);

    LoggedSqlCollector.current();
    JtTroop fetchTroop = DB.find(JtTroop.class)
      .fetch("monkeys")
      .where().idEq(troop.getPid())
      .findOne();

    assertThat(fetchTroop.getMonkeys()).hasSize(2);

    sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("from troop t0 left join troop_monkey t1z_ on t1z_.troop_pid = t0.pid left join monkey t1 on t1.mid = t1z_.monkey_mid where t0.pid = ?");
    assertSql(sql.get(0)).contains("select t0.pid, t0.name, t0.version, t1.mid, t1.name, t1.food_preference, t1.version");

    DB.delete(troop);

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from troop_monkey where troop_pid = ?");
    assertSql(sql.get(1)).contains("delete from troop where pid=? and version=?");


    insertWithCascade();
  }

  private void insertWithCascade() {

    JtTrainer trainer = new JtTrainer("Frank");

    // make m2 dirty ... cascades to an update on Uim
    m2.setFoodPreference("Apple");
    trainer.getMonkeys().add(m2);
    trainer.getMonkeys().add(DB.getReference(JtMonkey.class, m1.getMid()));
    trainer.getMonkeys().add(new JtMonkey("FAlp"));
    trainer.getMonkeys().add(new JtMonkey("FBet"));
    trainer.getMonkeys().add(new JtMonkey("FThe"));

    LoggedSqlCollector.start();
    DB.save(trainer);

    List<String> sql = LoggedSqlCollector.current();

    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(13);
      assertSql(sql.get(0)).contains("insert into trainer ");
      assertSql(sql.get(1)).contains("insert into monkey ");
      assertSqlBind(sql, 2, 4);
      assertThat(sql.get(5)).contains("update monkey set food_preference=?, version=? where mid=? and version=?");
      assertThat(sql.get(6)).contains("-- bind(");
      assertThat(sql.get(7)).contains("insert into trainer_monkey ");
      assertSqlBind(sql, 8, 12);

    } else {
      assertThat(sql).hasSize(10);
      assertSql(sql.get(0)).contains("insert into trainer ");
      assertSql(sql.get(1)).contains("update monkey set food_preference=?, version=? where mid=? and version=?");
      assertSql(sql.get(2)).contains("insert into monkey ");
      assertThat(sql.get(5)).contains("insert into trainer_monkey ");
      assertThat(sql.get(9)).contains("insert into trainer_monkey ");
    }


    int intersectionRows = DB.sqlQuery("select count(*) as total from trainer_monkey where trainer_tid = ?")
      .setParameter(1, trainer.getTid())
      .findOne()
      .getInteger("total");

    assertThat(intersectionRows).isEqualTo(5);


    LoggedSqlCollector.current();
    DB.delete(trainer);

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("delete from trainer_monkey where trainer_tid = ?");
    assertSql(sql.get(1)).contains("delete from trainer where tid=?");

  }
}
