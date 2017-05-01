package org.tests.model.array;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class TestDbArray_asSet extends BaseTestCase {

  private EArraySetBean bean = new EArraySetBean();

  private EArraySetBean found;

  @Test
  public void insert() {

    bean.setName("some stuff");

    Set<String> phNumbers = bean.getPhoneNumbers();
    phNumbers.add("4321");
    phNumbers.add("9823");


    Set<Double> doubles = new LinkedHashSet<>();
    doubles.add(1.3);
    doubles.add(2.4);

    UUID first = UUID.randomUUID();
    bean.getUids().add(first);
    bean.getUids().add(UUID.randomUUID());
    bean.getOtherIds().add(95L);
    bean.getOtherIds().add(96L);
    bean.getOtherIds().add(97L);
    bean.setDoubs(doubles);

    Ebean.save(bean);

    found = Ebean.find(EArraySetBean.class, bean.getId());

    assertThat(found.getPhoneNumbers()).containsExactly("4321", "9823");

    if (isPostgres()) {
      Query<EArraySetBean> query = Ebean.find(EArraySetBean.class)
        .where()
        .arrayContains("otherIds", 96L, 97L)
        .arrayContains("uids", first)
        .arrayContains("phoneNumbers", "9823")
        .arrayIsNotEmpty("phoneNumbers")
        .query();

      List<EArraySetBean> list = query.findList();

      assertThat(query.getGeneratedSql()).contains(" t0.other_ids @> array[?,?]::bigint[] ");
      assertThat(query.getGeneratedSql()).contains(" t0.uids @> array[?] ");
      assertThat(query.getGeneratedSql()).contains(" t0.phone_numbers @> array[?] ");
      assertThat(query.getGeneratedSql()).contains(" coalesce(cardinality(t0.phone_numbers),0) <> 0");
      assertThat(list).hasSize(1);

      query = Ebean.find(EArraySetBean.class)
        .where()
        .arrayIsEmpty("otherIds")
        .arrayNotContains("uids", first)
        .query();
      query.findList();

      assertThat(query.getGeneratedSql()).contains(" coalesce(cardinality(t0.other_ids),0) = 0");
      assertThat(query.getGeneratedSql()).contains(" not (t0.uids @> array[?])");
    }

    json_parse_format();
    update_when_notDirty();
    update_when_dirty();
  }

  //@Test//(dependsOnMethods = "insert")
  public void json_parse_format() {

    String asJson = Ebean.json().toJson(found);
    assertThat(asJson).contains("\"phoneNumbers\":[\"4321\",\"9823\"]");
    assertThat(asJson).contains("\"id\":");

    EArraySetBean fromJson = Ebean.json().toBean(EArraySetBean.class, asJson);
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getName(), fromJson.getName());
    assertThat(fromJson.getPhoneNumbers()).containsExactly("4321", "9823");
  }

  //@Test//(dependsOnMethods = "insert")
  public void update_when_notDirty() {

    found.setName("jack");
    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertThat(sql.get(0)).contains("update earray_set_bean set name=?, version=? where");
  }

  //@Test//(dependsOnMethods = "update_when_notDirty")
  public void update_when_dirty() {

    found.getPhoneNumbers().add("9987");
    found.getUids().add(UUID.randomUUID());

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).contains("update earray_set_bean set phone_numbers=?, uids=?, version=? where");
  }

  @Test
  public void insertNulls() {

    EArraySetBean bean = new EArraySetBean();
    bean.setName("some nulls");
    bean.setPhoneNumbers(null);
    bean.setOtherIds(null);
    bean.setUids(null);

    Ebean.save(bean);
    Ebean.delete(bean);
  }

  @Test
  public void insertAll_when_hasNulls() {

    EArraySetBean bean = new EArraySetBean();
    bean.setName("some nulls");
    bean.setPhoneNumbers(null);
    bean.setOtherIds(null);
    bean.setUids(null);

    Set<EArraySetBean> all = new HashSet<>();
    all.add(bean);

    Ebean.saveAll(all);
    Ebean.deleteAll(all);
  }
}
