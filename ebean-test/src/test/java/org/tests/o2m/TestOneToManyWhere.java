package org.tests.o2m;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOneToManyWhere extends BaseTestCase {

  @Test
  public void testWithDbTableName() {
    LoggedSql.start();
    DB.find(OmBasicParent.class).where().isNotNull("childrenWithWhere.name").findList();
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("'om_basic_parent' = u1.name");

    LoggedSql.start();
    DB.find(OmBasicParent.class).where().isNotEmpty("childrenWithWhere").findList();
    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("'om_basic_parent' = x.name");
  }

  @Test
  public void testLazyLoad() throws Exception {
    OmBasicParent el = new OmBasicParent("testLazyLoad");
    DB.save(el);
    LoggedSql.start();
    el = DB.find(OmBasicParent.class).select("name").where().eq("name", "testLazyLoad").findOne();
    el.getChildrenWithWhere().size(); // trigger Lazy load
    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name from om_basic_parent");
    assertThat(sql.get(1)).contains("where 'om_basic_parent' = t0.name");
    DB.delete(el);
  }

}
