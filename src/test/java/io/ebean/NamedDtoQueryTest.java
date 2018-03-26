package io.ebean;

import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaQueryMetric;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class NamedDtoQueryTest extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(NamedDtoQueryTest.class);

  @Test
  public void dto_findList_constructorMatch() {

    ResetBasicData.reset();

    DtoQuery<DCust> dtoQuery = server().createNamedDtoQuery(DCust.class, "findList");

    List<DCust> list = dtoQuery.findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto_findEach_constructorMatch() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();
    server().createNamedDtoQuery(DCust.class, "findGtId")
      .setParameter("id", 0)
      .findEach(it -> log.info("got " + it.getId() + " " + it.getName()));

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql.get(0)).contains("select id, name from o_customer where id > ?");
  }

  @Test
  public void dto_findEachWhile_constructorMatch() {

    ResetBasicData.reset();

    LoggedSqlCollector.start();
    server().createNamedDtoQuery(DCust.class, "findGtIdDesc")
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

    Optional<DCust> rob = server().createNamedDtoQuery(DCust.class, "findByName")
      .setParameter("name", "Fiona")
      .findOneOrEmpty();

    assertThat(rob.isPresent()).isTrue();

    Optional<DCust> oneOrEmpty = server().createNamedDtoQuery(DCust.class, "findByName")
      .setParameter("name", "DoesNotExistMyFriend")
      .findOneOrEmpty();

    assertThat(oneOrEmpty.isPresent()).isFalse();
  }

  @Test
  public void dto_findOne() {

    ResetBasicData.reset();

    DCust fiona = server().createNamedDtoQuery(DCust.class, "findByName")
      .setParameter("name", "Fiona")
      .findOne();

    assertThat(fiona.getName()).isEqualTo("Fiona");

    DCust empty = server().createNamedDtoQuery(DCust.class, "findByName")
      .setParameter("name", "DoesNotExistMyFriend")
      .findOne();

    assertThat(empty).isNull();
  }


  @Test
  public void dto_queryPlanHits() {

    ResetBasicData.reset();

    resetAllMetrics();

    String[] names = {"Rob", "Fiona", "Shrek"};

    for (String name : names) {

      List<DCust> custs = server().createNamedDtoQuery(DCust.class, "findByName_c3")
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


    server().createNamedDtoQuery(DCust.class, "findByName_c4")
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

    List<DCust3> list = server().createNamedDtoQuery(DCust3.class, "findByName_relaxedMode")
      .setRelaxedMode()
      .findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto_findList_relaxedMode_defaultConstructor() {

    ResetBasicData.reset();

    List<DCust2> list = server().createNamedDtoQuery(DCust2.class, "findByName_relaxedMode")
      .setRelaxedMode()
      .findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto_findList_constructorPlusMatch() {

    ResetBasicData.reset();

    List<DCust> dtos = server().createNamedDtoQuery(DCust.class, "findListByName")
      .setParameter("name", "Rob")
      .findList();

    log.info(dtos.toString());
    assertThat(dtos).isNotEmpty();
  }

  @Test
  public void dto_findList_setters() {

    ResetBasicData.reset();

    DtoQuery<DCust2> dtoQuery = server().createNamedDtoQuery(DCust2.class, "findList");

    List<DCust2> list = dtoQuery.findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  public void dto3_findList_constructorMatch() {

    ResetBasicData.reset();

    List<DCust3> robs = server().createNamedDtoQuery(DCust3.class, "findByNameLike")
      .setParameter(1, "Rob")
      .setMaxRows(10)
      .findList();


    log.info(robs.toString());
    assertThat(robs).isNotEmpty();
  }

  @Test
  public void dto3_findList_settersMatch() {

    ResetBasicData.reset();

    List<DCust3> robs = server().createNamedDtoQuery(DCust3.class, "findByName")
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
