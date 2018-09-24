package org.tests.model.array;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.SqlRow;

public class TestDbArray_basic extends BaseTestCase {

  private EArrayBean bean = new EArrayBean();

  private EArrayBean found;

  @Test
  public void insert() throws SQLException {

    bean.setName("some stuff");

    List<String> phNumbers = bean.getPhoneNumbers();
    phNumbers.add("4321");
    phNumbers.add("9823");


    List<Double> doubles = new ArrayList<>();
    doubles.add(1.3);
    doubles.add(2.4);

    bean.getUids().add(UUID.randomUUID());
    bean.getUids().add(UUID.randomUUID());
    bean.getOtherIds().add(95L);
    bean.getOtherIds().add(96L);
    bean.getOtherIds().add(97L);
    bean.setDoubs(doubles);
    bean.setStatuses(new ArrayList<>());
    bean.getStatuses().add(EArrayBean.Status.ONE);
    bean.getStatuses().add(EArrayBean.Status.THREE);
    bean.getVcEnums().add(VarcharEnum.ONE);
    bean.getVcEnums().add(VarcharEnum.TWO);
    bean.getIntEnums().add(IntEnum.ZERO);
    bean.getIntEnums().add(IntEnum.TWO);

    bean.setStatus2(new LinkedHashSet<>());
    bean.getStatus2().add(EArrayBean.Status.TWO);
    bean.getStatus2().add(EArrayBean.Status.ONE);

    Ebean.save(bean);

    found = Ebean.find(EArrayBean.class, bean.getId());

    assertThat(found.getPhoneNumbers()).containsExactly("4321", "9823");

    if (isPostgres()) {
      Query<EArrayBean> query = Ebean.find(EArrayBean.class)
        .where()
        .arrayContains("otherIds", 96L, 97L)
        .arrayContains("uids", bean.getUids().get(0))
        .arrayContains("phoneNumbers", "9823")
        .arrayIsNotEmpty("phoneNumbers")
        .arrayContains("statuses", EArrayBean.Status.ONE)
        .arrayContains("status2", EArrayBean.Status.TWO)
        .arrayContains("vcEnums", VarcharEnum.TWO)
        .arrayContains("intEnums", IntEnum.ZERO)
        .query();

      List<EArrayBean> list = query.findList();

      List<EArrayBean.Status> statuses = list.get(0).getStatuses();
      Set<EArrayBean.Status> status2 = list.get(0).getStatus2();
      List<IntEnum> intEnums = list.get(0).getIntEnums();
      List<VarcharEnum> varcharEnums = list.get(0).getVcEnums();

      assertThat(statuses).contains(EArrayBean.Status.ONE, EArrayBean.Status.THREE);
      assertThat(status2).contains(EArrayBean.Status.ONE, EArrayBean.Status.TWO);

      assertThat(intEnums).containsExactly(IntEnum.ZERO, IntEnum.TWO);
      assertThat(varcharEnums).containsExactly(VarcharEnum.ONE, VarcharEnum.TWO);

      assertThat(query.getGeneratedSql()).contains(" t0.other_ids @> array[?,?]::bigint[] ");
      assertThat(query.getGeneratedSql()).contains(" t0.uids @> array[?] ");
      assertThat(query.getGeneratedSql()).contains(" t0.phone_numbers @> array[?] ");
      assertThat(query.getGeneratedSql()).contains(" coalesce(cardinality(t0.phone_numbers),0) <> 0");
      assertThat(list).hasSize(1);

      query = Ebean.find(EArrayBean.class)
        .where()
        .arrayIsEmpty("otherIds")
        .arrayNotContains("uids", bean.getUids().get(0))
        .query();
      query.findList();


      final SqlRow row = Ebean.createSqlQuery("select * from earray_bean").findOne();

      final String[] vcs = (String[]) ((java.sql.Array) row.get("vc_enums")).getArray();
      assertThat(vcs).containsExactly("xXxONE", "xXxTWO");

      final Integer[] ints = (Integer[]) ((java.sql.Array) row.get("int_enums")).getArray();
      assertThat(ints).containsExactly(100, 102);

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

    EArrayBean fromJson = Ebean.json().toBean(EArrayBean.class, asJson);
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
    assertThat(sql.get(0)).contains("update earray_bean set name=?, version=? where");
  }

  //@Test//(dependsOnMethods = "update_when_notDirty")
  public void update_when_dirty() {

    found.getPhoneNumbers().add("9987");
    found.getUids().add(UUID.randomUUID());

    LoggedSqlCollector.start();
    Ebean.save(found);
    List<String> sql = LoggedSqlCollector.stop();

    assertThat(sql.get(0)).contains("update earray_bean set phone_numbers=?, uids=?, version=? where");
  }

  @Test
  public void insertNulls() {

    EArrayBean bean = new EArrayBean();
    bean.setName("some nulls");
    bean.setPhoneNumbers(null);
    bean.setOtherIds(null);
    bean.setUids(null);

    Ebean.save(bean);
    Ebean.delete(bean);
  }

  @Test
  public void insertAll_when_hasNulls() {

    EArrayBean bean = new EArrayBean();
    bean.setName("some nulls");
    bean.setPhoneNumbers(null);
    bean.setOtherIds(null);
    bean.setUids(null);

    List<EArrayBean> all = new ArrayList<>();
    all.add(bean);

    Ebean.saveAll(all);
    Ebean.deleteAll(all);
  }
}
