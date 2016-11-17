package com.avaje.tests.basic.lob;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.EBasicClobNoVer;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBasicClobNoVer extends BaseTestCase {

  @Test
  public void test() {

    EBasicClobNoVer entity = new EBasicClobNoVer();
    entity.setName("test");
    entity.setDescription("initialClobValue");
    EbeanServer server = Ebean.getServer(null);
    server.save(entity);


    String sqlNoClob = "select t0.id, t0.name from ebasic_clob_no_ver t0 where t0.id = ?";
    String sqlWithClob = "select t0.id, t0.name, t0.description from ebasic_clob_no_ver t0 where t0.id = ?";


    // Clob by default is Fetch Lazy
    Query<EBasicClobNoVer> defaultQuery = Ebean.find(EBasicClobNoVer.class).setId(entity.getId());
    defaultQuery.findUnique();
    String sql = sqlOf(defaultQuery, 2);

    Assert.assertTrue("Clob is fetch lazy by default", sql.contains(sqlNoClob));


    // Explicitly select * including Clob
    Query<EBasicClobNoVer> explicitQuery = Ebean.find(EBasicClobNoVer.class).setId(entity.getId()).select("*");

    explicitQuery.findUnique();
    sql = sqlOf(explicitQuery, 2);

    Assert.assertTrue("Explicitly include Clob", sql.contains(sqlWithClob));

    // Update description to test refresh

    EBasicClobNoVer updateBean = new EBasicClobNoVer();
    updateBean.setId(entity.getId());
    updateBean.setDescription("modified");
    Ebean.update(updateBean);


    // Test refresh function

    Assert.assertEquals("initialClobValue", entity.getDescription());

    LoggedSqlCollector.start();

    // Refresh query includes all properties
    server.refresh(entity);

    // Assert all properties fetched in refresh
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());
    assertThat(trimSql(loggedSql.get(0), 2)).contains(sqlWithClob);
    Assert.assertEquals("modified", entity.getDescription());

  }

}
