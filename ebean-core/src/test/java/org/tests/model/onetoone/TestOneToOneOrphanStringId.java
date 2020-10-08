package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneOrphanStringId extends BaseTestCase {

  @Test
  public void test_updateInsert() {

    OtoAtwo b = new OtoAtwo("b1", "b test");
    Ebean.save(b);

    OtoAone a = new OtoAone("a1", "a test");
    b.setAone(a);

    LoggedSqlCollector.start();

    Ebean.save(b);

    List<String> update = LoggedSqlCollector.current();
    assertThat(update).hasSize(3);
    assertThat(update.get(0)).contains("insert into oto_aone");
    assertSqlBind(update.get(1));
    assertThat(update.get(2)).contains("update oto_atwo set aone_id=? where id=?");

    Ebean.delete(b);

    List<String> deletes = LoggedSqlCollector.stop();
    assertThat(deletes).hasSize(2);
    assertThat(deletes.get(0)).contains("delete from oto_atwo");
    assertThat(deletes.get(1)).contains("delete from oto_aone");
  }

  @Test
  public void test_cascade() {

    OtoAone a = new OtoAone("a2", "a test");
    OtoAtwo b = new OtoAtwo("b2", "b test");

    b.setAone(a);

    LoggedSqlCollector.start();

    Ebean.save(b);

    List<String> inserts = LoggedSqlCollector.current();
    assertThat(inserts).hasSize(3);
    assertThat(inserts.get(0)).contains("insert into oto_aone");
    assertSqlBind(inserts.get(1));
    assertThat(inserts.get(2)).contains("insert into oto_atwo");

    Ebean.delete(b);

    List<String> deletes = LoggedSqlCollector.stop();
    assertThat(deletes).hasSize(2);
    assertThat(deletes.get(0)).contains("delete from oto_atwo");
    assertThat(deletes.get(1)).contains("delete from oto_aone");
  }

  @Test
  public void test_remove() {

    OtoAtwo b = new OtoAtwo("b3", "b test");
    Ebean.save(b);

    OtoAone a = new OtoAone("a3", "a test");
    b.setAone(a);

    Ebean.save(b);

    LoggedSqlCollector.start();

    OtoAone a2 = new OtoAone("a4", "a test");
    b.setAone(a2);
    Ebean.save(b);

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).hasSize(5);
    assertSql(sql.get(0)).contains("insert into oto_aone");
    assertSqlBind(sql.get(1));
    assertSql(sql.get(2)).contains("update oto_atwo set aone_id=? where id=?");
    assertThat(sql.get(3)).contains("delete from oto_aone where id=?");
    assertSqlBind(sql.get(4));

    Ebean.delete(b);

    List<String> deletes = LoggedSqlCollector.stop();
    assertThat(deletes).hasSize(2);
    assertThat(deletes.get(0)).contains("delete from oto_atwo");
    assertThat(deletes.get(1)).contains("delete from oto_aone");
  }
}


