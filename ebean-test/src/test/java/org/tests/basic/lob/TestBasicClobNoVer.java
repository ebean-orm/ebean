package org.tests.basic.lob;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.EBasicClobNoVer;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class TestBasicClobNoVer extends BaseTestCase {

  @Test
  void test() {

    EBasicClobNoVer entity = new EBasicClobNoVer();
    entity.setName("test");
    entity.setDescription("initialClobValue");
    DB.save(entity);

    // Clob by default is Fetch Lazy
    Query<EBasicClobNoVer> defaultQuery = DB.find(EBasicClobNoVer.class).setId(entity.getId());
    defaultQuery.findOne();
    String sql = sqlOf(defaultQuery, 2);

    // default SQL select excludes clob
    String sqlNoClob = "select t0.id, t0.name from ebasic_clob_no_ver t0 where t0.id = ?";
    assertThat(sql).contains(sqlNoClob);


    // Explicitly select * including Clob
    Query<EBasicClobNoVer> explicitQuery = DB.find(EBasicClobNoVer.class).setId(entity.getId()).select("*");

    explicitQuery.findOne();
    sql = sqlOf(explicitQuery, 2);

    // Explicitly include Clob
    String sqlWithClob = "select t0.id, t0.name, t0.description from ebasic_clob_no_ver t0 where t0.id = ?";
    assertThat(sql).contains(sqlWithClob);

    // Update description to test refresh

    EBasicClobNoVer updateBean = new EBasicClobNoVer();
    updateBean.setId(entity.getId());
    updateBean.setDescription("modified");
    DB.update(updateBean);


    // Test refresh function

    assertThat(entity.getDescription()).isEqualTo("initialClobValue");

    LoggedSql.start();

    // Refresh query includes all properties
    DB.refresh(entity);

    // Assert all properties fetched in refresh
    List<String> loggedSql = LoggedSql.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 2)).contains(sqlWithClob);
    assertThat(entity.getDescription()).isEqualTo("modified");
  }

  @Test
  void refresh_withSoftDelete() {

    EBasicClobNoVer bean = new EBasicClobNoVer();
    bean.setDescription("hello");
    DB.save(bean);

    DB.refresh(bean);

    LoggedSql.start();
    bean.children().forEach(System.out::println);
    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains(" and t0.deleted =");
  }

  @Test
  void largeValueInsert() {
    EBasicClobNoVer bean = new EBasicClobNoVer();
    String s = largeContent();
    bean.setDescription(s);
    DB.save(bean);
    bean = DB.find(EBasicClobNoVer.class, bean.getId());
    assertThat(bean.getDescription()).isEqualTo(s);
  }

  private String largeContent() {
    Random random = new Random();
    StringBuilder sb = new StringBuilder(1048577);
    for (int i = 0; i < 1048577; i++) {
      sb.append((char) (random.nextInt(26) + 'a'));
    }
    return sb.toString();
  }
}
