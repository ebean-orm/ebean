package org.tests.delete;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteCascadeWithListener extends BaseTestCase {

  @Test
  public void test() {

    DcMaster m0 = new DcMaster("m0");
    m0.getDetails().add(new DcDetail());
    m0.getDetails().add(new DcDetail());
    m0.getDetails().add(new DcDetail());

    Ebean.save(m0);

    DcMaster found = Ebean.find(DcMaster.class, m0.getId());

    LoggedSqlCollector.start();
    Ebean.delete(found);

    List<String> sql = LoggedSqlCollector.stop();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(6);
      assertSql(sql.get(0)).contains("select t0.id from dc_detail t0 where master_id=?");
      assertSql(sql.get(1)).contains("delete from dc_detail where id=?");
      assertSqlBind(sql, 2, 4);
      assertThat(sql.get(5)).contains("delete from dc_master where id=? and version=?");
    }
    awaitListenerPropagation();

    List<Object> beans = DcListener.deletedBeans();
    assertThat(beans).hasSize(3);
  }

  private void awaitListenerPropagation() {
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
}
