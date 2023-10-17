package org.tests.model.onetoone;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneOrphanStringId extends BaseTestCase {

  @Test
  public void test_updateInsert() {

    OtoAtwo b = new OtoAtwo("b1", "b test");
    DB.save(b);

    OtoAone a = new OtoAone("a1", "a test");
    b.setAone(a);

    LoggedSql.start();

    DB.save(b);

    List<String> update = LoggedSql.collect();
    assertThat(update).hasSize(4);
    assertThat(update.get(0)).contains("insert into oto_aone");
    assertSqlBind(update.get(1));
    assertThat(update.get(3)).contains("update oto_atwo set aone_id=? where id=?");

    DB.delete(b);

    List<String> deletes = LoggedSql.stop();
    assertThat(deletes).hasSize(2);
    assertThat(deletes.get(0)).contains("delete from oto_atwo");
    assertThat(deletes.get(1)).contains("delete from oto_aone");
  }

  @Test
  public void test_cascade() {

    OtoAone a = new OtoAone("a2", "a test");
    OtoAtwo b = new OtoAtwo("b2", "b test");

    b.setAone(a);

    LoggedSql.start();

    DB.save(b);

    List<String> inserts = LoggedSql.collect();
    assertThat(inserts).hasSize(4);
    assertThat(inserts.get(0)).contains("insert into oto_aone");
    assertSqlBind(inserts.get(1));
    assertThat(inserts.get(3)).contains("insert into oto_atwo");

    DB.delete(b);

    List<String> deletes = LoggedSql.stop();
    assertThat(deletes).hasSize(2);
    assertThat(deletes.get(0)).contains("delete from oto_atwo");
    assertThat(deletes.get(1)).contains("delete from oto_aone");
  }

  @Test
  public void test_remove() {

    OtoAtwo b = new OtoAtwo("b3", "b test");
    DB.save(b);

    OtoAone a = new OtoAone("a3", "a test");
    b.setAone(a);

    DB.save(b);

    LoggedSql.start();

    OtoAone a2 = new OtoAone("a4", "a test");
    b.setAone(a2);
    DB.save(b);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(7);
    assertSql(sql.get(0)).contains("insert into oto_aone");
    assertSqlBind(sql.get(1));
    assertSql(sql.get(3)).contains("update oto_atwo set aone_id=? where id=?");
    assertThat(sql.get(4)).contains("delete from oto_aone where id=?");
    assertSqlBind(sql.get(5));

    DB.delete(b);

    List<String> deletes = LoggedSql.stop();
    assertThat(deletes).hasSize(2);
    assertThat(deletes.get(0)).contains("delete from oto_atwo");
    assertThat(deletes.get(1)).contains("delete from oto_aone");
  }
}


