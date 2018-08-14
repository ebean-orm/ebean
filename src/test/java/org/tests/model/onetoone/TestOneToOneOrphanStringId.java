package org.tests.model.onetoone;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneOrphanStringId extends BaseTestCase {

  @Test
  public void test() {

    OtoAone a = new OtoAone("a", "a test");
    OtoAtwo b = new OtoAtwo("b", "b test");
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


