package com.avaje.tests.json;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.tests.model.json.EBasicJsonList;
import org.avaje.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDbJson_List extends BaseTestCase {

  private EBasicJsonList bean = new EBasicJsonList();

  private EBasicJsonList found;

  @Test
  public void insert() {

    bean.setName("stuff");

    bean.getTags().add("one");
    bean.getTags().add("two");

    bean.getFlags().add(42L);
    bean.getFlags().add(43L);
    bean.getFlags().add(44L);

    Ebean.save(bean);

    found = Ebean.find(EBasicJsonList.class, bean.getId());

    assertThat(found.getTags()).containsExactly("one", "two");
    assertTrue(found.getFlags().contains(42L));
    assertTrue(found.getFlags().contains(43L));
    assertTrue(found.getFlags().contains(44L));

    json_parse_format();
    update_when_notDirty();
    update_when_dirty();
    update_when_dirty_flags();
  }

  //@Test//(dependsOnMethods = "insert")
  public void json_parse_format() {

    String asJson = Ebean.json().toJson(found);
    assertThat(asJson).contains("\"tags\":[\"one\",\"two\"]");
    assertThat(asJson).contains("\"flags\":[42,43,44]");
    assertThat(asJson).contains("\"id\":");

    EBasicJsonList fromJson = Ebean.json().toBean(EBasicJsonList.class, asJson);
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getName(), fromJson.getName());
    assertThat(fromJson.getTags()).containsExactly("one", "two");
    assertTrue(fromJson.getFlags().contains(42L));
    assertTrue(fromJson.getFlags().contains(43L));
    assertTrue(fromJson.getFlags().contains(44L));

  }

  //@Test//(dependsOnMethods = "insert")
  public void update_when_notDirty() {

    found.setName("mod");
    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update ebasic_json_list set name=?, version=? where");
  }

  public void update_when_dirty() {

    //found.setName("modAgain");
    found.getTags().add("three");

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update ebasic_json_list set tags=?, version=? where");
  }

  public void update_when_dirty_flags() {

    //found.setName("modAgain");
    found.getFlags().remove(42L);

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update ebasic_json_list set flags=?, version=? where");
  }

}
