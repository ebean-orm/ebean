package org.tests.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import org.tests.model.json.EBasicJsonList;
import org.tests.model.json.PlainBean;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

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

    bean.setPlainBean(new PlainBean("plain", 52));

    List<PlainBean> beanList = new ArrayList<>();
    beanList.add(new PlainBean("one", 1));
    beanList.add(new PlainBean("two", 2));
    bean.setBeanList(beanList);


    Set<PlainBean> beanSet = new LinkedHashSet<>();
    beanSet.add(new PlainBean("A", 1));
    beanSet.add(new PlainBean("B", 2));
    bean.setBeanSet(beanSet);

    bean.getBeanMap().put("key0", new PlainBean("k0", 90));
    bean.getBeanMap().put("key1", new PlainBean("k1", 91));

    Ebean.save(bean);

    found = Ebean.find(EBasicJsonList.class, bean.getId());

    assertThat(found.getTags()).containsExactly("one", "two");
    assertTrue(found.getFlags().contains(42L));
    assertTrue(found.getFlags().contains(43L));
    assertTrue(found.getFlags().contains(44L));
    assertThat(found.getBeanList()).hasSize(2);
    assertThat(found.getBeanSet()).hasSize(2);
    assertThat(found.getBeanMap()).hasSize(2);

    json_parse_format();
    update_when_notDirty();
    update_when_dirty();
    update_when_dirty_flags();
    update_when_dirty_SetListMap();
  }

  //@Test//(dependsOnMethods = "insert")
  public void json_parse_format() {

    String asJson = Ebean.json().toJson(found);
    assertThat(asJson).contains("\"tags\":[\"one\",\"two\"]");
    assertThat(asJson).contains("\"flags\":[42,43,44]");
    assertThat(asJson).contains("\"plainBean\":{\"name\":\"plain\"");
    assertThat(asJson).contains("\"beanSet\":[");
    assertThat(asJson).contains("\"beanList\":[");
    assertThat(asJson).contains("\"beanMap\":{");
    assertThat(asJson).contains("\"id\":");

    EBasicJsonList fromJson = Ebean.json().toBean(EBasicJsonList.class, asJson);
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getName(), fromJson.getName());
    assertThat(fromJson.getTags()).containsExactly("one", "two");
    assertTrue(fromJson.getFlags().contains(42L));
    assertTrue(fromJson.getFlags().contains(43L));
    assertTrue(fromJson.getFlags().contains(44L));

    assertThat(fromJson.getBeanSet()).hasSize(2);
    assertThat(fromJson.getBeanList()).hasSize(2);
    assertThat(fromJson.getBeanMap()).hasSize(2);
  }

  //@Test//(dependsOnMethods = "insert")
  public void update_when_notDirty() {

    found.setName("mod");
    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update ebasic_json_list set name=?, plain_bean=?, version=? where");
  }

  public void update_when_dirty() {

    //found.setName("modAgain");
    found.getTags().add("three");

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update ebasic_json_list set plain_bean=?, tags=?, version=? where id=? and version=?");
  }

  public void update_when_dirty_flags() {

    //found.setName("modAgain");
    found.getFlags().remove(42L);

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update ebasic_json_list set plain_bean=?, flags=?, version=? where id=? and version=?;");
  }

  public void update_when_dirty_SetListMap() {

    //found.setName("modAgain");
    found.getBeanSet().clear();
    found.getBeanList().clear();
    found.getBeanMap().remove("key0");

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update ebasic_json_list set bean_set=?, bean_list=?, bean_map=?, plain_bean=?, version=? where id=? and version=?");
  }

  @Test
  public void insert_fetch_when_null() {

    EBasicJsonList bean = new EBasicJsonList();
    bean.setName("leave some nulls");
    bean.setFlags(null);
    bean.setTags(null);
    bean.setBeanMap(null);

    Ebean.save(bean);

    EBasicJsonList found = Ebean.find(EBasicJsonList.class, bean.getId());

    assertNull(found.getPlainBean());

    String asJson = Ebean.json().toJson(found);
    assertNotNull(asJson);
  }
}
