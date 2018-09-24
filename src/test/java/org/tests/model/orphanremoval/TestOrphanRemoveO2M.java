package org.tests.model.orphanremoval;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrphanRemoveO2M extends BaseTestCase {

  @Test
  public void clear_expect_deletes() {

    OrpMaster master = new OrpMaster("m","master");
    master.getDetails().add(new OrpDetail("d1", "d1"));
    master.getDetails().add(new OrpDetail("d2", "d2"));

    Ebean.save(master);

    master.getDetails().clear();

    LoggedSqlCollector.start();
    Ebean.save(master);

    List<String> sql = LoggedSqlCollector.stop();

    assertThat(Ebean.find(OrpDetail.class, "d1")).isNull();
    assertThat(Ebean.find(OrpDetail.class, "d2")).isNull();

    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("delete from orp_detail where id=?");
  }
}
