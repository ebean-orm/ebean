package org.tests.model.array;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDbArray_asSet extends BaseTestCase {

  private final EArraySetBean bean = new EArraySetBean();

  private EArraySetBean found;

  @Test
  @IgnorePlatform(Platform.HANA)
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

    Set<EArrayBean.Status> status = new LinkedHashSet<>();
    status.add(EArrayBean.Status.ONE);
    status.add(EArrayBean.Status.TWO);
    bean.setStatus(status);

    DB.save(bean);

    found = DB.find(EArraySetBean.class, bean.getId());

    assertThat(found.getPhoneNumbers()).containsExactly("4321", "9823");

    if (isPostgresCompatible()) {
      Query<EArraySetBean> query = DB.find(EArraySetBean.class)
        .where()
        .arrayContains("otherIds", 96L, 97L)
        .arrayContains("uids", first)
        .arrayContains("phoneNumbers", "9823")
        .arrayIsNotEmpty("phoneNumbers")
        .query();

      List<EArraySetBean> list = query.findList();

      assertSql(query).contains(" t0.other_ids @> array[?,?]::bigint[] ");
      assertSql(query).contains(" t0.uids @> array[?] ");
      assertSql(query).contains(" t0.phone_numbers @> array[?] ");
      assertSql(query).contains(" coalesce(cardinality(t0.phone_numbers),0) <> 0");
      assertThat(list).hasSize(1);

      query = DB.find(EArraySetBean.class)
        .where()
        .arrayIsEmpty("otherIds")
        .arrayNotContains("uids", first)
        .query();
      query.findList();

      assertSql(query).contains(" coalesce(cardinality(t0.other_ids),0) = 0");
      assertSql(query).contains(" not (t0.uids @> array[?])");
    }

    json_parse_format();
    update_when_notDirty();
    update_when_dirty();
  }

  //@Test//(dependsOnMethods = "insert")
  public void json_parse_format() {

    String asJson = DB.json().toJson(found);
    assertThat(asJson).contains("\"phoneNumbers\":[\"4321\",\"9823\"]");
    assertThat(asJson).contains("\"id\":");

    EArraySetBean fromJson = DB.json().toBean(EArraySetBean.class, asJson);
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getId(), fromJson.getId());
    assertEquals(found.getName(), fromJson.getName());
    assertThat(fromJson.getPhoneNumbers()).containsExactly("4321", "9823");
  }

  //@Test//(dependsOnMethods = "insert")
  public void update_when_notDirty() {

    found.setName("jack");
    LoggedSql.start();
    DB.save(found);
    List<String> sql = LoggedSql.stop();

    // we don't update the phone numbers (as they are not dirty)
    assertSql(sql.get(0)).contains("update earray_set_bean set name=?, version=? where");
  }

  //@Test//(dependsOnMethods = "update_when_notDirty")
  public void update_when_dirty() {

    found.getPhoneNumbers().add("9987");
    found.getUids().add(UUID.randomUUID());

    LoggedSql.start();
    DB.save(found);
    List<String> sql = LoggedSql.stop();

    assertSql(sql.get(0)).contains("update earray_set_bean set phone_numbers=?, uids=?, version=? where");
  }

  @Test
  @IgnorePlatform(Platform.HANA)
  public void insertNulls() {

    EArraySetBean bean = new EArraySetBean();
    bean.setName("some nulls");
    bean.setPhoneNumbers(null);
    bean.setOtherIds(null);
    bean.setUids(null);

    DB.save(bean);
    DB.delete(bean);
  }

  @Test
  @IgnorePlatform(Platform.HANA)
  public void insertAll_when_hasNulls() {

    EArraySetBean bean = new EArraySetBean();
    bean.setName("some nulls");
    bean.setPhoneNumbers(null);
    bean.setOtherIds(null);
    bean.setUids(null);

    Set<EArraySetBean> all = new HashSet<>();
    all.add(bean);

    DB.saveAll(all);
    DB.deleteAll(all);
  }

  @Test
  @IgnorePlatform(Platform.HANA)
  public void hitCache() {

    Set<UUID> uids = new LinkedHashSet<>();
    uids.add(UUID.randomUUID());
    uids.add(UUID.randomUUID());

    Set<EArrayBean.Status> statuses = new LinkedHashSet<>();
    statuses.add(EArrayBean.Status.ONE);
    statuses.add(EArrayBean.Status.THREE);

    EArraySetBean bean = new EArraySetBean();
    bean.setName("hitCache");
    bean.setUids(uids);
    bean.setStatus(statuses);

    DB.save(bean);
    // load cache
    final EArraySetBean entry = DB.find(EArraySetBean.class, bean.getId());
    assertThat(entry.getUids()).hasSameElementsAs(uids);
    assertThat(entry.getStatus()).hasSameElementsAs(statuses);

    // hit cache
    EArraySetBean found = DB.find(EArraySetBean.class, bean.getId());
    assertThat(found.getUids()).hasSameElementsAs(uids);
    assertThat(found.getStatus()).hasSameElementsAs(statuses);
  }
}
