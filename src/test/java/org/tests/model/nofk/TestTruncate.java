package org.tests.model.nofk;

import io.ebean.DB;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTruncate {

  @Test
  public void test_truncateByClass() {
    DB.truncate(EUserNoFkSoftDel.class, EUserNoFk.class);

    insertRows();
    assertRowCounts(1);

    DB.truncate(EUserNoFkSoftDel.class, EUserNoFk.class);
    assertRowCounts(0);
  }

  @Test
  public void test_truncateByTable() {
    DB.truncate(EUserNoFkSoftDel.class, EUserNoFk.class);

    insertRows();
    assertRowCounts(1);

    DB.truncate("euser_no_fk_soft_del", "euser_no_fk");
    assertRowCounts(0);
  }

  private void insertRows() {
    EUserNoFk root = new EUserNoFk();
    root.setUserName("root");
    DB.save(root);

    EUserNoFkSoftDel rootSoftDel = new EUserNoFkSoftDel();
    rootSoftDel.setUserName("root");
    DB.save(rootSoftDel);
  }

  private void assertRowCounts(int expected) {
    assertThat(DB.find(EUserNoFk.class).findCount()).isEqualTo(expected);
    assertThat(DB.find(EUserNoFkSoftDel.class).findCount()).isEqualTo(expected);
  }
}
