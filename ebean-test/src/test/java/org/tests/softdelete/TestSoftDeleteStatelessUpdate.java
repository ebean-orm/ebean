package org.tests.softdelete;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.softdelete.EsdDetail;
import org.tests.model.softdelete.EsdMaster;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSoftDeleteStatelessUpdate extends BaseTestCase {

  @Test
  public void test() {

    EsdMaster master = new EsdMaster("m1");
    master.getDetails().add(new EsdDetail("d1"));
    master.getDetails().add(new EsdDetail("d2"));
    master.getDetails().add(new EsdDetail("d3"));

    DB.save(master);

    EsdMaster upd = new EsdMaster("m1-modified");
    upd.setId(master.getId());

    EsdDetail d1 = new EsdDetail("d1");
    d1.setId(master.getDetails().get(0).getId());
    upd.getDetails().add(d1);

    EsdDetail d3 = new EsdDetail("d3-mod");
    d3.setId(master.getDetails().get(2).getId());
    upd.getDetails().add(d3);


    LoggedSql.start();

    DB.update(upd);

    List<String> sql = LoggedSql.collect();
    assertThat(sql).hasSize(6);
    assertSql(sql.get(0)).contains("update esd_master set name=? where id=?");
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(1)).contains("update esd_detail set deleted=true where master_id = ? and not");
    }

    EsdMaster fetchedWithSoftDeletes = DB.find(EsdMaster.class)
      .setId(master.getId())
      .setIncludeSoftDeletes()
      .fetch("details")
      .findOne();

    assertThat(fetchedWithSoftDeletes.getDetails()).hasSize(3);

    sql = LoggedSql.collect();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("left join esd_detail t1 on t1.master_id = t0.id where t0.id = ?");

    EsdMaster fetchedWithOutSoftDeletes = DB.find(EsdMaster.class)
      .setId(master.getId())
      .fetch("details")
      .findOne();

    assertThat(fetchedWithOutSoftDeletes.getDetails()).hasSize(2);

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    if (isPlatformBooleanNative()) {
      assertSql(sql.get(0)).contains("left join esd_detail t1 on t1.master_id = t0.id and t1.deleted = false where t0.id = ?");
    }
  }
}
