package org.tests.model.onetoone;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToOneImportedPkNative extends BaseTestCase {

  @Test
  public void findWithLazyOneToOne() {

    OtoBChild child = new OtoBChild();
    child.setChild("c1");

    OtoBMaster master = new OtoBMaster();
    master.setName("m2");
    master.setChild(child);

    DB.save(master);

    Query<OtoBMaster> query = DB.find(OtoBMaster.class)
      //.select("name")
      .where().idEq(master.getId())
      .query();

    OtoBMaster one = query.findOne();

    String sql = sqlOf(query);
    assertThat(sql).contains("select t0.id, t0.name from oto_bmaster t0 where t0.id ");
    assertThat(sql).doesNotContain("join oto_bchild");

    assertThat(one).isNotNull();

    LoggedSql.start();

    OtoBChild child1 = one.getChild();
    assertThat(child1).isNotNull();
    assertThat(child1.getChild()).isEqualTo("c1");

    List<String> lazyLoadSql = LoggedSql.stop();
    assertThat(lazyLoadSql).hasSize(2);
    assertSql(lazyLoadSql.get(0)).contains("select t0.id, t0.name, t0.id from oto_bmaster t0 where t0.id = ?");
    assertSql(lazyLoadSql.get(1)).contains("select t0.master_id, t0.child, t0.master_id from oto_bchild t0 where t0.master_id = ?");
  }

  @Test
  public void native_with_o2oAndImportedPrimaryKey() {

    Database server = DB.getDefault();
    server.find(OtoBMaster.class).delete();

    OtoBMaster one = new OtoBMaster();
    one.setName("hello");
    DB.save(one);

    OtoBMaster m = server.findNative(OtoBMaster.class, "select * from oto_bmaster").findOne();

    assertThat(m.getId()).isEqualTo(one.getId());
    assertThat(m.getName()).isEqualTo(one.getName());

    OtoBMaster m2 = server.find(OtoBMaster.class, one.getId());
    assertThat(m2.getId()).isEqualTo(one.getId());
    assertThat(m2.getName()).isEqualTo(one.getName());
  }

}
