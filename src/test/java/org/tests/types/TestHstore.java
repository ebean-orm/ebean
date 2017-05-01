package org.tests.types;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.json.EBasicHstore;
import org.assertj.core.api.Assertions;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestHstore extends BaseTestCase {

  private EBasicHstore bean;

  @Test
  public void insert() {

    if (isPostgres()) {
      // run this manually for Postgres with the HSTORE extension installed
      // psql mydb -c 'create extension hstore;'
      return;
    }

    bean = new EBasicHstore("one");
    bean.getMap().put("home", "123");
    bean.getMap().put("work", "987");

    Ebean.save(bean);

    json_parse_format();
    update_when_notDirty();
    update_when_dirty();
    insert_when_null();
  }

  void json_parse_format() {

    String asJson = Ebean.json().toJson(bean);
    assertThat(asJson).contains("\"map\":{\"home\":\"123\",\"work\":\"987\"}");

    EBasicHstore fromJson = Ebean.json().toBean(EBasicHstore.class, asJson);
    assertEquals(bean.getId(), fromJson.getId());
    assertEquals(bean.getName(), fromJson.getName());
    Assertions.assertThat(fromJson.getMap().keySet()).containsExactly("home", "work");
  }

  void update_when_notDirty() {

    EBasicHstore found = Ebean.find(EBasicHstore.class, bean.getId());
    found.setName("modName");

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the map as it is not dirty
    assertThat(sql.get(0)).contains("update ebasic_hstore set name=?, version=? where");
  }

  void update_when_dirty() {

    EBasicHstore found = Ebean.find(EBasicHstore.class, bean.getId());
    found.setName("modNamePlus");
    found.getMap().put("foo", "9987");

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).contains("update ebasic_hstore set name=?, map=?, version=? where id=? and version=?");
  }

  void insert_when_null() {

    EBasicHstore bean = new EBasicHstore("one");
    bean.setMap(null);
    Ebean.save(bean);
  }
}
