package org.tests.cascade;

import io.ebean.BaseTestCase;
import io.ebean.Transaction;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

public class TestMasterDetailDelete extends BaseTestCase {

  @Test
  public void testDeleteOneMasterTxn() {
    try (Transaction txn = server().beginTransaction()) {
      RelDetail rd = new RelDetail();
      rd.setName("test1");
      server().save(rd);

      RelMaster rm = new RelMaster();
      rm.setDetail(rd);
      server().save(rm);

      List<RelDetail> lst = server().find(RelDetail.class).where().eq("name", "test1").findList();
      assertThat(lst).hasSize(1);

      server().find(RelMaster.class).delete();

      lst = server().find(RelDetail.class).where().eq("name", "test1").findList();
      assertThat(lst).isEmpty();
    } // no commit
  }


  @Test
  public void testDeleteMultiMasterTxn() {
    try (Transaction txn = server().beginTransaction()) {
      RelDetail rd = new RelDetail();
      rd.setName("test2");
      server().save(rd);

      RelMaster rm1 = new RelMaster();
      rm1.setDetail(rd);
      server().save(rm1);

      RelMaster rm2 = new RelMaster();
      rm2.setDetail(rd);
      server().save(rm2);

      List<RelDetail> lst = server().find(RelDetail.class).where().eq("name", "test2").findList();
      assertThat(lst).hasSize(1);

      server().find(RelMaster.class).delete();

      lst = server().find(RelDetail.class).where().eq("name", "test2").findList();
      assertThat(lst).isEmpty();
    } // no commit
  }

  @Test
  public void testDeleteMultiMasterPlain() {
    RelDetail rd = new RelDetail();
    rd.setName("test3");
    server().save(rd);

    RelMaster rm1 = new RelMaster();
    rm1.setDetail(rd);
    server().save(rm1);

    RelMaster rm2 = new RelMaster();
    rm2.setDetail(rd);
    server().save(rm2);

    List<RelDetail> lst = server().find(RelDetail.class).where().eq("name", "test3").findList();
    assertThat(lst).hasSize(1);

    server().find(RelMaster.class).delete();

    lst = server().find(RelDetail.class).where().eq("name", "test3").findList();
    assertThat(lst).isEmpty();
  }
}
