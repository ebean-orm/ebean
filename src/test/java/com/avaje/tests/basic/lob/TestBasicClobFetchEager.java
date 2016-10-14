package com.avaje.tests.basic.lob;

import java.util.List;

import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.EBasicClobFetchEager;

import static org.assertj.core.api.Assertions.assertThat;

public class TestBasicClobFetchEager extends BaseTestCase {

  @Test
  public void test() {

    EBasicClobFetchEager entity = new EBasicClobFetchEager();
    entity.setName("test");
    entity.setDescription("initialClobValue");
    EbeanServer server = Ebean.getServer(null);
    server.save(entity);

    
    String expectedSql = "select t0.id, t0.name, t0.title, t0.description, t0.last_update from ebasic_clob_fetch_eager t0 where t0.id = ?";
    
    // Clob included in fetch as FetchType.EAGER set by annotation
    Query<EBasicClobFetchEager> defaultQuery = Ebean.find(EBasicClobFetchEager.class).setId(entity.getId());
    defaultQuery.findUnique();
    String sql = trimSql(defaultQuery.getGeneratedSql(), 6);

    assertThat(sql).contains(expectedSql);

    
    LoggedSqlCollector.start();
    
    // Same as previous query - clob included by default based on annotation
    Ebean.find(EBasicClobFetchEager.class, entity.getId());

    // Assert query same as previous ...
    List<String> loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());
    assertThat(trimSql(loggedSql.get(0), 6)).contains(expectedSql);
    


    // Explicitly select * including Clob
    Query<EBasicClobFetchEager> explicitQuery = Ebean.find(EBasicClobFetchEager.class).setId(entity.getId()).select("*");

    explicitQuery.findUnique();
    sql = sqlOf(explicitQuery, 6);

    assertThat(sql).contains(expectedSql);

    // Update description to test refresh
    
    EBasicClobFetchEager updateBean = new EBasicClobFetchEager();
    updateBean.setId(entity.getId());
    updateBean.setDescription("modified");
    Ebean.update(updateBean);
    
    
    // Test refresh function
    
    Assert.assertEquals("initialClobValue", entity.getDescription());

    LoggedSqlCollector.start();
    
    // Refresh query includes all properties
    server.refresh(entity);
    
    // Assert all properties fetched in refresh
    loggedSql = LoggedSqlCollector.stop();
    Assert.assertEquals(1, loggedSql.size());
    assertThat(trimSql(loggedSql.get(0), 6)).contains(expectedSql);
    Assert.assertEquals("modified", entity.getDescription());

  }

}
