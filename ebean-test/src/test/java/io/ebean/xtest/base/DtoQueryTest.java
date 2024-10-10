package io.ebean.xtest.base;

import io.ebean.meta.MetricNamingMatch;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.DtoQuery;
import io.ebean.SqlRow;
import io.ebean.xtest.ForPlatform;
import io.ebean.annotation.Platform;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.meta.ServerMetrics;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasicLog;
import org.tests.model.basic.ResetBasicData;

import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class DtoQueryTest extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(DtoQueryTest.class);

  private final AtomicInteger batchCount = new AtomicInteger();
  private final AtomicInteger rowCount = new AtomicInteger();

  @Test
  void dto_findList_constructorMatch() {
    ResetBasicData.reset();
    resetAllMetrics();

    DtoQuery<DCust> dtoQuery = server().findDto(DCust.class, "select id, name from o_customer");

    List<DCust> list = dtoQuery.findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();

    ServerMetrics metrics = collectMetrics();

    List<MetaQueryMetric> stats = metrics.queryMetrics();
    for (MetaQueryMetric stat : stats) {
      long meanMicros = stat.mean();
      assertThat(meanMicros).isLessThan(900_000);
    }

    assertThat(stats).hasSize(1);
    assertThat(stats.get(0).count()).isEqualTo(1);
  }

  @Test
  void dto_findEach_constructorMatch() {
    ResetBasicData.reset();

    LoggedSql.start();
    server().findDto(DCust.class, "select id, name from o_customer where id > :id")
      .setParameter("id", 0)
      .findEach(it -> log.info("got " + it.getId() + " " + it.getName()));

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select id, name from o_customer where id > ?");
  }

  @Test
  void dto_findEachWhile_constructorMatch() {
    ResetBasicData.reset();

    LoggedSql.start();
    server().findDto(DCust.class, "select id, name from o_customer where id > :id order by id desc")
      .setParameter("id", 0)
      .findEachWhile(customer -> {
        log.info("got " + customer.getId() + " " + customer.getName());
        return customer.getId() > 3;
      });

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select id, name from o_customer where id > ?");
  }

  private void resetFindEachCounts() {
    batchCount.set(0);
    rowCount.set(0);
  }

  @Test
  void dto_findEachBatch() {
    seedData(); // 15 rows inserted to fetch

    resetFindEachCounts();
    findEachWithBatch(5);
    assertThat(batchCount.get()).isEqualTo(3);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(10);
    assertThat(batchCount.get()).isEqualTo(2);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(14);
    assertThat(batchCount.get()).isEqualTo(2);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(15);
    assertThat(batchCount.get()).isEqualTo(1);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(16);
    assertThat(batchCount.get()).isEqualTo(1);
    assertThat(rowCount.get()).isEqualTo(15);

    resetFindEachCounts();
    findEachWithBatch(20);
    assertThat(batchCount.get()).isEqualTo(1);
    assertThat(rowCount.get()).isEqualTo(15);
  }

  private void findEachWithBatch(int batchSize) {
    server().findDto(DCust.class, "select id, name from e_basic_log where name like ?")
      .setParameter("dtoFindEachBatch%")
      .findEach(batchSize, batch -> {
        int batchId = batchCount.incrementAndGet();
        int rows = rowCount.addAndGet(batch.size());
        log.info("batch {} rows {}", batchId, rows);
      });
  }

  private void seedData() {
    for (int i = 0; i < 15; i++) {
      EBasicLog log = new EBasicLog("dtoFindEachBatch "+i);
      DB.save(log);
    }
  }

  @Test
  void dto_findOneEmpty() {
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
  void dto_findOne() {
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

  @Test
  void setParameter() {
    ResetBasicData.reset();

    final List<DCust> list =
      server().findDto(DCust.class, "select id, name from o_customer where id > ? and name like ? and status = ?")
      .setParameter(0)
      .setParameter("Rob%")
      .setParameter(Customer.Status.NEW)
      .findList();

    assertThat(list).isNotEmpty();
  }

  @Test
  void setParameters() {
    ResetBasicData.reset();

    final List<DCust> list =
      server().findDto(DCust.class, "select id, name from o_customer where id > ? and name like ? and status = ?")
        .setParameters(0, "Rob%", Customer.Status.NEW)
        .findList();

    assertThat(list).isNotEmpty();
  }

  @ForPlatform(Platform.POSTGRES)
  @Test
  void dto_bindList_usingPostrgesAnyWithPositionedParameter() {
    ResetBasicData.reset();

    LoggedSql.start();
    List<Integer> ids = Arrays.asList(1, 2);

    List<DCust> list = DB.findDto(DCust.class, "select id, name from o_customer where id = any(?)")
      .setParameter(ids)
      .findList();

    assertThat(list).isNotEmpty();

    List<DCust> list1 = DB.findDto(DCust.class, "select id, name from o_customer where id in (:idList)")
      .setParameter("idList", ids)
      .findList();

    assertThat(list1).isNotEmpty();


    List<DCust> list2 = DB.findDto(DCust.class, "select id, name from o_customer where id = any(:idList)")
      .setArrayParameter("idList", ids)
      .findList();

    assertThat(list2).isNotEmpty();

    List<SqlRow> list3 = DB.sqlQuery("select id, name from o_customer where id in (:idList)")
      .setParameter("idList", ids)
      .findList();
    assertThat(list3).isNotEmpty();

    List<SqlRow> list4 = DB.sqlQuery("select id, name from o_customer where id = any(:idList)")
      .setArrayParameter("idList", ids)
      .findList();
    assertThat(list4).isNotEmpty();

    List<SqlRow> list5 = DB.sqlQuery("select id, name from o_customer where id = any(?) and name like ?")
      .setArrayParameter(1, ids)
      .setParameter(2, "foo%")
      .findList();
    assertThat(list5).isEmpty();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(6);
    assertThat(sql.get(0)).contains(" id = any(?)");
    assertThat(sql.get(1)).contains(" id in (?,?)");
    assertThat(sql.get(2)).contains(" id = any(?)");
    assertThat(sql.get(3)).contains(" id in (?,?)");
    assertThat(sql.get(4)).contains(" id = any(?)");
    assertThat(sql.get(5)).contains(" id = any(?) and name like ?");
  }

  @ForPlatform(Platform.POSTGRES)
  @Test
  void sql_bindListParam_usingPostrgesAnyWithPositionedParameter() {
    ResetBasicData.reset();

    List<Integer> ids = Arrays.asList(1, 2);

    List<SqlRow> list = DB.sqlQuery("select id, name from o_customer where id = any(?)")
      .setParameter(1, ids)
      .findList();

    assertThat(list).isNotEmpty();

    list = server().sqlQuery("select id, name from o_customer where id in (:idList)")
      .setParameter("idList", ids)
      .findList();

    assertThat(list).isNotEmpty();
  }

  @ForPlatform(Platform.POSTGRES)
  @Test
  void sqlUpdate_bindListParam_usingPostrgesAnyWithPositionedParameter() {
    ResetBasicData.reset();

    List<Integer> ids = Arrays.asList(999999999, 999999998);

    int rows = server().sqlUpdate("update o_customer set name = ? where id = any(?)")
      .setParameter(1, "Junk")
      .setParameter(2, ids)
      .execute();

    assertThat(rows).isEqualTo(0);
  }

  @Test
  void dto_queryPlanHits() {
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
    BasicMetricVisitor basic = new BasicMetricVisitor("db", MetricNamingMatch.INSTANCE, false, true, true, true);
    server().metaInfo().visitMetrics(basic);

    List<MetaQueryMetric> stats = basic.queryMetrics();
    assertThat(stats).hasSize(1);

    MetaQueryMetric queryMetric = stats.get(0);
    assertThat(queryMetric.label()).isEqualTo("basic");
    assertThat(queryMetric.count()).isEqualTo(3);
    assertThat(queryMetric.name()).isEqualTo("dto.DCust_basic");

    server().findDto(DCust.class, "select c4.id, c4.name from o_customer c4 where lower(c4.name) = :name")
      .setLabel("basic2")
      .setParameter("name", "rob")
      .findList();

    ServerMetrics metric2 = server().metaInfo().collectMetrics();

    stats = metric2.queryMetrics();
    assertThat(stats).hasSize(2);

    log.info("stats " + stats);

    String asJson = metric2.asJson().withHash(false).withNewLine(false).json();
    assertThat(asJson).contains("dto.DCust_basic2");
  }

  @Test
  void dto_findList_relaxedMode() {
    ResetBasicData.reset();

    List<DCust3> list = server().findDto(DCust3.class, "select id, name, 42 as total, '42' as something_we_cannot_map from o_customer")
      .setRelaxedMode()
      .findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  void dto_findList_relaxedMode_defaultConstructor() {
    ResetBasicData.reset();

    List<DCust2> list = server().findDto(DCust2.class, "select id, '42' as something_we_cannot_map, name from o_customer")
      .setRelaxedMode()
      .findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  void dto_findList_constructorPlusMatch() {
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
  void dto_findList_setters() {
    ResetBasicData.reset();

    DtoQuery<DCust2> dtoQuery = server().findDto(DCust2.class, "select id, name from o_customer");

    List<DCust2> list = dtoQuery.findList();
    assertThat(list).isNotEmpty();
  }

  @ForPlatform(Platform.H2)
  @Test
  void dto3_setNullByPosition() {
    ResetBasicData.reset();

    List<DCust3> robs = server().findDto(DCust3.class, "select id, name, nvl(cast(? as int), 42) as totalOrders from o_customer where name like ?")
      .setNullParameter(1, Types.INTEGER)
      .setParameter(2, "Rob")
      .setMaxRows(10)
      .findList();

    log.info(robs.toString());
    assertThat(robs).isNotEmpty();
    assertThat(robs.get(0).getTotalOrders()).isEqualTo(42);
  }

  @ForPlatform(Platform.H2)
  @Test
  void dto3_setNullByName() {
    ResetBasicData.reset();

    List<DCust3> robs = server().findDto(DCust3.class, "select id, name, nvl(cast(:foo as int), 42) as totalOrders from o_customer where name like :bar")
      .setNullParameter("foo", Types.INTEGER)
      .setParameter("bar", "Rob")
      .setMaxRows(10)
      .findList();

    log.info(robs.toString());
    assertThat(robs).isNotEmpty();
    assertThat(robs.get(0).getTotalOrders()).isEqualTo(42);
  }

  @Test
  void dto3_findList_constructorMatch() {
    ResetBasicData.reset();

    List<DCust3> robs = server().findDto(DCust3.class, "select id, name, 42 as totalOrders from o_customer where name like ?")
      .setParameter(1, "Rob")
      .setMaxRows(10)
      .findList();

    log.info(robs.toString());
    assertThat(robs).isNotEmpty();
  }

  @Test
  void dto3_findList_settersMatch() {
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
