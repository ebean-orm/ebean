package org.tests.model.orphanremoval;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrphanRemoveO2M extends BaseTestCase {

  @Test
  public void clear_expect_deletes() {

    OrpMaster master = new OrpMaster("m","master");
    master.getDetails().add(new OrpDetail("d1", "d1"));
    master.getDetails().add(new OrpDetail("d2", "d2"));

    DB.save(master);

    master.getDetails().clear();

    LoggedSql.start();
    DB.save(master);

    List<String> sql = LoggedSql.stop();

    assertThat(DB.find(OrpDetail.class, "d1")).isNull();
    assertThat(DB.find(OrpDetail.class, "d2")).isNull();

    assertThat(sql).hasSize(3);
    assertSql(sql.get(0)).contains("delete from orp_detail where id=?");
    assertSqlBind(sql, 1, 2);
  }

  @Test
  public void testCacheUse() {

    // add test data first
    OrpMaster m0 = new OrpMaster("m2", "master2");
    m0.getDetails().add(new OrpDetail("d21", "d1"));
    m0.getDetails().add(new OrpDetail("d22", "d2"));

    DB.save(m0);

    OrpMaster m1 = DB.find(OrpMaster.class, "m2");
    m1.getDetails().size();

    m1.getDetails().clear();
    m1.getDetails().add(new OrpDetail("d23", "d3"));
    DB.save(m1);

    m1 = DB.find(OrpMaster.class, "m2");
    // Expect only one.
    assertThat(m1.getDetails()).hasSize(1);
    assertThat(m1.getDetails()).extracting("id").containsExactly("d23");

    m1.getDetails().clear();
    m1.getDetails().add(new OrpDetail("d24", "d4"));
    m1.getDetails().add(new OrpDetail("d25", "d5"));
    DB.save(m1);

    m1 = DB.find(OrpMaster.class, "m2");
    assertThat(m1.getDetails()).hasSize(2);
    assertThat(m1.getDetails()).extracting("id").contains("d24", "d25");


    m1 = DB.find(OrpMaster.class)
      .setId("m2")
      .setUseCache(false)
      .findOne();

    // Expect only one.
    assertThat(m1.getDetails()).hasSize(2);
    assertThat(m1.getDetails()).extracting("id").contains("d24", "d25");
  }
}
