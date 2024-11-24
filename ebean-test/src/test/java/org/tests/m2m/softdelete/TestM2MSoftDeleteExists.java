package org.tests.m2m.softdelete;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestM2MSoftDeleteExists extends BaseTestCase {

  private List<MsManyA> as = new ArrayList<>();
  private List<MsManyB> bs = new ArrayList<>();

  @Test
  public void test() {

    setup();

    Query<MsManyA> query = DB.find(MsManyA.class)
      .where().isNotEmpty("manybs")
      .orderBy("aid").query();

    List<MsManyA> list = query.findList();

    if (isPlatformBooleanNative()) {
      assertThat(sqlOf(query)).contains("from ms_many_a t0 where exists (select 1 from ms_many_a_many_b x join ms_many_b x2 on x2.bid = x.ms_many_b_bid where x.ms_many_a_aid = t0.aid and x2.deleted = false) and t0.deleted = false");
    }
    assertThat(list).hasSize(3);
    assertThat(list.get(0).getName()).isEqualTo("a0");
    assertThat(list.get(1).getName()).isEqualTo("a1");
    assertThat(list.get(2).getName()).isEqualTo("a2");

    testDeletedB0();

  }

  private void testDeletedB0() {

    DB.delete(bs.get(0));

    List<MsManyA> list = DB.find(MsManyA.class)
      .where().isNotEmpty("manybs")
      .orderBy("aid")
      .findList();

    assertThat(list).hasSize(2);
    assertThat(list.get(0).getName()).isEqualTo("a1");
    assertThat(list.get(1).getName()).isEqualTo("a2");

    testDeletedB1();
  }

  private void testDeletedB1() {

    DB.delete(bs.get(1));

    List<MsManyA> list = DB.find(MsManyA.class)
      .where().isNotEmpty("manybs")
      .orderBy("aid")
      .findList();

    assertThat(list).hasSize(1);
    assertThat(list.get(0).getName()).isEqualTo("a1");

    testEndTotals();
  }

  private void testEndTotals() {

    assertEquals(5, DB.find(MsManyA.class).findCount());
    assertEquals(3, DB.find(MsManyB.class).findCount());
    assertEquals(5, DB.find(MsManyB.class).setIncludeSoftDeletes().findCount());
  }

  private void setup() {
    clean();
    seed();
  }

  private void clean() {

    DB.sqlUpdate("delete from ms_many_a_many_b").execute();
    DB.sqlUpdate("delete from ms_many_a").execute();
    DB.sqlUpdate("delete from ms_many_b").execute();
  }

  private void seed() {
    for (int i = 0; i < 5; i++) {
      as.add(new MsManyA("a" + i));
      bs.add(new MsManyB("b" + i));
    }

    DB.saveAll(as);
    DB.saveAll(bs);

    as.get(0).getManybs().add(bs.get(0));
    as.get(1).getManybs().addAll(bs);
    as.get(2).getManybs().add(bs.get(1));

    DB.save(as.get(0));
    DB.save(as.get(1));
    DB.save(as.get(2));
  }

}
