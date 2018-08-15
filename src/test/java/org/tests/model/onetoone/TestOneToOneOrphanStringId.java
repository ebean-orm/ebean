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
    assertThat(update).hasSize(2);
    assertThat(update.get(0)).contains("insert into oto_aone");
    assertThat(update.get(1)).contains("update oto_atwo set aone_id=? where id=?");

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
    assertThat(inserts).hasSize(2);
    assertThat(inserts.get(0)).contains("insert into oto_aone");
    assertThat(inserts.get(1)).contains("insert into oto_atwo");

    Ebean.delete(b);

    List<String> deletes = LoggedSqlCollector.stop();
    assertThat(deletes).hasSize(2);
    assertThat(deletes.get(0)).contains("delete from oto_atwo");
    assertThat(deletes.get(1)).contains("delete from oto_aone");
  }
}


