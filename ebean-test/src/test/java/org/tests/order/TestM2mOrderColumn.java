package org.tests.order;

import io.ebean.DB;
import io.ebean.xtest.base.TransactionalTestCase;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestM2mOrderColumn extends TransactionalTestCase {

  @Test
  void insert_thenOrderIsPersistedAndReturnedInOrder() {
    M2mOrderMaster master = new M2mOrderMaster("m1");
    for (int i = 4; i >= 0; i--) {
      master.getChildren().add(new M2mOrderChild("c" + i));
    }
    DB.save(master);

    // plain findOne() + lazy load of children (secondary query)
    M2mOrderMaster result = DB.find(M2mOrderMaster.class).findOneOrEmpty().orElseThrow();
    assertThat(result.getChildren()).extracting(M2mOrderChild::getName)
      .containsExactly("c4", "c3", "c2", "c1", "c0");
  }

  @Test
  void fetch_thenOrderIsRespected() {
    M2mOrderMaster master = new M2mOrderMaster("mf");
    for (int i = 4; i >= 0; i--) {
      master.getChildren().add(new M2mOrderChild("cf" + i));
    }
    DB.save(master);

    // fetch("children") uses a SQL JOIN rather than a secondary query
    M2mOrderMaster result = DB.find(M2mOrderMaster.class)
      .fetch("children")
      .where().idEq(master.getId())
      .findOneOrEmpty()
      .orElseThrow();

    assertThat(result.getChildren()).extracting(M2mOrderChild::getName)
      .containsExactly("cf4", "cf3", "cf2", "cf1", "cf0");
  }

  @Test
  void fetchQuery_thenOrderIsRespected() {
    M2mOrderMaster master = new M2mOrderMaster("m2");
    for (int i = 4; i >= 0; i--) {
      master.getChildren().add(new M2mOrderChild("cq" + i));
    }
    DB.save(master);

    M2mOrderMaster result = DB.find(M2mOrderMaster.class)
      .fetchQuery("children")
      .where().idEq(master.getId())
      .findOneOrEmpty()
      .orElseThrow();

    assertThat(result.getChildren()).extracting(M2mOrderChild::getName)
      .containsExactly("cq4", "cq3", "cq2", "cq1", "cq0");
  }

  @Test
  void reorder_thenNewOrderPersistedOnReload() {
    M2mOrderMaster master = new M2mOrderMaster("m3");
    for (int i = 0; i < 5; i++) {
      master.getChildren().add(new M2mOrderChild("r" + i));
    }
    DB.save(master);

    M2mOrderMaster result = DB.find(M2mOrderMaster.class).findOneOrEmpty().orElseThrow();
    result.getChildren().sort(Comparator.comparing(M2mOrderChild::getName).reversed());
    DB.save(result);

    M2mOrderMaster reloaded = DB.find(M2mOrderMaster.class).findOneOrEmpty().orElseThrow();
    assertThat(reloaded.getChildren()).extracting(M2mOrderChild::getName)
      .containsExactly("r4", "r3", "r2", "r1", "r0");
  }

  @Test
  void insert_sqlContainsOrderColumn() {
    M2mOrderMaster master = new M2mOrderMaster("m4");
    master.getChildren().add(new M2mOrderChild("s0"));
    master.getChildren().add(new M2mOrderChild("s1"));

    LoggedSql.start();
    DB.save(master);
    List<String> sql = LoggedSql.stop();

    boolean foundIntersectionInsert = sql.stream().anyMatch(s -> s.contains("insert into") && s.contains("sort_order"));
    assertThat(foundIntersectionInsert)
      .as("sql: %s", sql)
      .isTrue();
  }
}
