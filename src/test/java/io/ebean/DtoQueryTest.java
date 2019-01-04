package io.ebean;

import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaQueryMetric;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.ResetBasicData;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoQueryTest extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(DtoQueryTest.class);

  @Test
  public void dto_findList_constructorMatch() {

    ResetBasicData.reset();

    DtoQuery<DCust> dtoQuery = server().findDto(DCust.class, "select id, name from o_customer");

    List<DCust> list = dtoQuery.findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto_findEach_constructorMatch() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();
    server().findDto(DCust.class, "select id, name from o_customer where id > :id")
      .setParameter("id", 0)
      .findEach(it -> log.info("got " + it.getId() + " " + it.getName()));

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select id, name from o_customer where id > ?");
  }

  @Test
  public void dto_findEachWhile_constructorMatch() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();
    server().findDto(DCust.class, "select id, name from o_customer where id > :id order by id desc")
      .setParameter("id", 0)
      .findEachWhile(customer -> {
        log.info("got " + customer.getId() + " " + customer.getName());
        return customer.getId() > 3;
      });

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select id, name from o_customer where id > ?");
  }

  @Test
  public void dto_findOneEmpty() {

    ResetBasicData.reset();

    Optional<DCust> rob = server().findDto(DCust.class, "select id, name from o_customer where name = :name")
      .setParameter("name", "Fiona")
      .findOneOrEmpty();

    assertThat(rob.isPresent()).isTrue();

    Optional<DCust> oneOrEmpty = server().findDto(DCust.class, "select id, name from o_customer where name = :name")
      .setParameter("name", "DoesNotExistMyFriend")
      .findOneOrEmpty();

    assertThat(oneOrEmpty.isPresent()).isFalse();
  }

  @Test
  public void dto_findOne() {

    ResetBasicData.reset();

    DCust fiona = server().findDto(DCust.class, "select id, name from o_customer where name = :name")
      .setParameter("name", "Fiona")
      .findOne();

    assertThat(fiona.getName()).isEqualTo("Fiona");

    DCust empty = server().findDto(DCust.class, "select id, name from o_customer where name = :name")
      .setParameter("name", "DoesNotExistMyFriend")
      .findOne();

    assertThat(empty).isNull();
  }


  @ForPlatform(Platform.POSTGRES)
  @Test
  public void dto_bindList_usingPostrgesAnyWithPositionedParameter() {

    ResetBasicData.reset();

    List<Integer> ids = Arrays.asList(1, 2);

    List<DCust> list = server().findDto(DCust.class, "select id, name from o_customer where id = any(?)")
      .setParameter(1, ids)
      .findList();

    assertThat(list).isNotEmpty();

    list = server().findDto(DCust.class, "select id, name from o_customer where id in (:idList)")
      .setParameter("idList", ids)
      .findList();

    assertThat(list).isNotEmpty();
  }

  @ForPlatform(Platform.POSTGRES)
  @Test
  public void sql_bindListParam_usingPostrgesAnyWithPositionedParameter() {

    ResetBasicData.reset();

    List<Integer> ids = Arrays.asList(1, 2);

    List<SqlRow> list = server().createSqlQuery("select id, name from o_customer where id = any(?)")
      .setParameter(1, ids)
      .findList();

    assertThat(list).isNotEmpty();

    list = server().createSqlQuery("select id, name from o_customer where id in (:idList)")
      .setParameter("idList", ids)
      .findList();

    assertThat(list).isNotEmpty();
  }

  @ForPlatform(Platform.POSTGRES)
  @Test
  public void sqlUpdate_bindListParam_usingPostrgesAnyWithPositionedParameter() {

    ResetBasicData.reset();

    List<Integer> ids = Arrays.asList(999999999, 999999998);

    int rows = server().createSqlUpdate("update o_customer set name = ? where id = any(?)")
      .setParameter(1, "Junk")
      .setParameter(2, ids)
      .execute();

    assertThat(rows).isEqualTo(0);
  }

  @Test
  public void dto_queryPlanHits() {

    ResetBasicData.reset();

    resetAllMetrics();

    String[] names = {"Rob", "Fiona", "Shrek"};

    for (String name : names) {

      List<DCust> custs = server().findDto(DCust.class, "select c3.id, c3.name from o_customer c3 where c3.name = :name")
        .setLabel("basic")
        .setParameter("name", name)
        .findList();

      log.info("Found " + custs);
    }

    // collect without reset
    BasicMetricVisitor basic = new BasicMetricVisitor(false, true, true);
    server().getMetaInfoManager().visitMetrics(basic);

    List<MetaQueryMetric> stats = basic.getDtoQueryMetrics();
    assertThat(stats).hasSize(1);

    MetaQueryMetric queryMetric = stats.get(0);
    assertThat(queryMetric.getLabel()).isEqualTo("basic");
    assertThat(queryMetric.getCount()).isEqualTo(3);
    assertThat(queryMetric.getName()).isEqualTo("basic");


    server().findDto(DCust.class, "select c4.id, c4.name from o_customer c4 where lower(c4.name) = :name")
      .setLabel("basic2")
      .setParameter("name", "rob")
      .findList();

    BasicMetricVisitor metric2 = server().getMetaInfoManager().visitBasic();

    stats = metric2.getDtoQueryMetrics();
    assertThat(stats).hasSize(2);

    log.info("stats " + stats);

  }

  @Test
  public void dto_findList_relaxedMode() {

    ResetBasicData.reset();

    List<DCust3> list = server().findDto(DCust3.class, "select id, name, 42 as total, '42' as something_we_cannot_map from o_customer")
      .setRelaxedMode()
      .findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto_findList_relaxedMode_defaultConstructor() {

    ResetBasicData.reset();

    List<DCust2> list = server().findDto(DCust2.class, "select id, '42' as something_we_cannot_map, name from o_customer")
      .setRelaxedMode()
      .findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto_findList_constructorPlusMatch() {

    ResetBasicData.reset();

    String sql = "select c.id, c.name, count(o.id) as totalOrders " +
      "from o_customer c " +
      "join o_order o on o.kcustomer_id = c.id " +
      "where c.name like :name " +
      "group by c.id, c.name";

    List<DCust> dtos = server().findDto(DCust.class, sql)
      .setParameter("name", "Rob")
      .findList();

    log.info(dtos.toString());
    assertThat(dtos).isNotEmpty();
  }

  @Test
  public void dto_findList_setters() {

    ResetBasicData.reset();

    DtoQuery<DCust2> dtoQuery = server().findDto(DCust2.class, "select id, name from o_customer");

    List<DCust2> list = dtoQuery.findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto3_findList_constructorMatch() {

    ResetBasicData.reset();

    List<DCust3> robs = server().findDto(DCust3.class, "select id, name, 42 as totalOrders from o_customer where name like ?")
      .setParameter(1, "Rob")
      .setMaxRows(10)
      .findList();


    log.info(robs.toString());
    assertThat(robs).isNotEmpty();
  }

  @Test
  public void dto3_findList_settersMatch() {

    ResetBasicData.reset();

    List<DCust3> robs = server().findDto(DCust3.class, "select id, name from o_customer where name = :name")
      .setParameter("name", "Rob")
      .findList();


    log.info(robs.toString());
    assertThat(robs).isNotEmpty();
  }

  public static class DCust {

    final Integer id;

    final String name;

    int totalOrders;

    public DCust(Integer id, String name) {
      this.id = id;
      this.name = name;
    }

    @Override
    public String toString() {
      return "id:" + id + " name:" + name + " totalOrders:" + totalOrders;
    }

    public Integer getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getTotalOrders() {
      return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
      this.totalOrders = totalOrders;
    }
  }

  public static class DCust2 {

    Integer id;

    String name;

    @Override
    public String toString() {
      return "id:" + id + " name:" + name;
    }

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static class DCust3 {

    Integer id;

    String name;

    int totalOrders;

    public DCust3() {
    }

    public DCust3(Integer id, String name, int totalOrders) {
      this.id = id;
      this.name = name;
      this.totalOrders = totalOrders;
    }

    @Override
    public String toString() {
      return "id:" + id + " name:" + name + " totalOrders:" + totalOrders;
    }

    public Integer getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public int getTotalOrders() {
      return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
      this.totalOrders = totalOrders;
    }

    public void setId(Integer id) {
      this.id = id;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
