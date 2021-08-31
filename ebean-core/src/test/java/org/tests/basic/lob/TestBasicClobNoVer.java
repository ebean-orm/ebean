package org.tests.basic.lob;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.tests.model.basic.EBasicClobNoVer;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBasicClobNoVer extends BaseTestCase {

  @Test
  public void test() {

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

    LoggedSqlCollector.start();

    // Refresh query includes all properties
    DB.refresh(entity);

    // Assert all properties fetched in refresh
    List<String> loggedSql = LoggedSqlCollector.stop();
    assertThat(loggedSql).hasSize(1);
    assertThat(trimSql(loggedSql.get(0), 2)).contains(sqlWithClob);
    assertThat(entity.getDescription()).isEqualTo("modified");
  }

}
