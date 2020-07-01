package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import org.tests.model.json.EBasicHstore;
import org.assertj.core.api.Assertions;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestHstore extends BaseTestCase {

  private EBasicHstore bean;

  /**
   * For Postgres this test requires the hstore extension to be installed.
   */
  @Test
  public void insert() {

    bean = new EBasicHstore("one");
    bean.getMap().put("home", "123");
    bean.getMap().put("work", "987");

    DB.save(bean);

    json_parse_format();
    update_when_notDirty();
    update_when_dirty();
    insert_when_null();
  }

  void json_parse_format() {

    String asJson = DB.json().toJson(bean);
    assertThat(asJson).contains("\"map\":{\"home\":\"123\",\"work\":\"987\"}");

    EBasicHstore fromJson = DB.json().toBean(EBasicHstore.class, asJson);
    assertEquals(bean.getId(), fromJson.getId());
    assertEquals(bean.getName(), fromJson.getName());
    Assertions.assertThat(fromJson.getMap().keySet()).containsExactly("home", "work");
  }

  void update_when_notDirty() {

    EBasicHstore found = DB.find(EBasicHstore.class, bean.getId());
    found.setName("modName");

    LoggedSqlCollector.start();
    DB.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the map as it is not dirty
    assertSql(sql.get(0)).contains("update ebasic_hstore set name=?, version=? where");
  }

  void update_when_dirty() {

    EBasicHstore found = DB.find(EBasicHstore.class, bean.getId());
    found.setName("modNamePlus");
    found.getMap().put("foo", "9987");

    LoggedSqlCollector.start();
    DB.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    assertSql(sql.get(0)).contains("update ebasic_hstore set name=?, map=?, version=? where id=? and version=?");
  }

  void insert_when_null() {

    EBasicHstore bean = new EBasicHstore("one");
    bean.setMap(null);
    DB.save(bean);
  }
}
