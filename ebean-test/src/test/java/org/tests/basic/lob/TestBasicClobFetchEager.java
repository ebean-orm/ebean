package org.tests.basic.lob;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import org.tests.model.basic.EBasicClobFetchEager;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBasicClobFetchEager extends BaseTestCase {

  @Test
  public void test() {

    EBasicClobFetchEager entity = new EBasicClobFetchEager();
    entity.setName("test");
    entity.setDescription("initialClobValue");
    Database server = DB.getDefault();
    server.save(entity);


    String expectedSql = "select t0.id, t0.name, t0.title, t0.description, t0.last_update from ebasic_clob_fetch_eager t0 where t0.id = ?";

    // Clob included in fetch as FetchType.EAGER set by annotation
    Query<EBasicClobFetchEager> defaultQuery = DB.find(EBasicClobFetchEager.class).setId(entity.getId());
    defaultQuery.findOne();
    String sql = trimSql(defaultQuery.getGeneratedSql(), 6);

    assertThat(sql).contains(expectedSql);


    LoggedSql.start();

    // Same as previous query - clob included by default based on annotation
    DB.find(EBasicClobFetchEager.class, entity.getId());

    // Assert query same as previous ...
    List<String> loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(trimSql(loggedSql.get(0), 6)).contains(expectedSql);


    // Explicitly select * including Clob
    Query<EBasicClobFetchEager> explicitQuery = DB.find(EBasicClobFetchEager.class).setId(entity.getId()).select("*");

    explicitQuery.findOne();
    sql = sqlOf(explicitQuery, 6);

    assertThat(sql).contains(expectedSql);

    // Update description to test refresh

    EBasicClobFetchEager updateBean = new EBasicClobFetchEager();
    updateBean.setId(entity.getId());
    updateBean.setDescription("modified");
    DB.update(updateBean);


    // Test refresh function

    assertEquals("initialClobValue", entity.getDescription());

    LoggedSql.start();

    // Refresh query includes all properties
    server.refresh(entity);

    // Assert all properties fetched in refresh
    loggedSql = LoggedSql.stop();
    assertEquals(1, loggedSql.size());
    assertThat(trimSql(loggedSql.get(0), 6)).contains(expectedSql);
    assertEquals("modified", entity.getDescription());

  }

}
