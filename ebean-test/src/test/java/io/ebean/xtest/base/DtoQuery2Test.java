package io.ebean.xtest.base;

import io.ebean.DB;
import io.ebean.Query;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DtoQuery;
import io.ebean.QueryIterator;
import io.ebean.meta.BasicMetricVisitor;
import io.ebean.meta.MetaQueryMetric;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DtoQuery2Test extends BaseTestCase {

  private static final Logger log = LoggerFactory.getLogger(DtoQuery2Test.class);

  @Test
  void dto_findList_fluidAccessors() {
    ResetBasicData.reset();

    List<DCustFluidAccessors> list = server().findDto(DCustFluidAccessors.class, "select id, name from o_customer").findList();

    assertThat(list).isNotEmpty();
    for (DCustFluidAccessors cust: list) {
      assertThat(cust.id()).isNotNull();
      assertThat(cust.name()).isNotNull();
    }
  }

  @Test
  void dto_findList_plainAccessors() {
    ResetBasicData.reset();

    List<DCustPlainAccessors> list = server().findDto(DCustPlainAccessors.class, "select id, name from o_customer").findList();

    assertThat(list).isNotEmpty();
    for (DCustPlainAccessors cust: list) {
      assertThat(cust.id()).isNotNull();
      assertThat(cust.name()).isNotNull();
    }
  }

  @Test
  void dto_findList_constructorMatch() {
    ResetBasicData.reset();

    DtoQuery<DCust> dtoQuery = server().findDto(DCust.class, "select id, name from o_customer");
    List<DCust> list = dtoQuery.findList();

    log.info(list.toString());
    assertThat(list).isNotEmpty();
  }

  @Test
  void dto_findIterator_closeWithResources() {
    ResetBasicData.reset();

    int counter = 0;
    try (QueryIterator<DCust> iterator = server()
      .findDto(DCust.class, "select id, name from o_customer where id > ?")
      .setParameter(0)
      .findIterate()) {

      if (iterator.hasNext()) {
        counter++;
      }
    }

    assertThat(counter).isEqualTo(1);
  }

  @Test
  void dto_findIterator() {
    ResetBasicData.reset();
    final int expectedCount = server().find(Customer.class).findCount();

    LoggedSql.start();
    int counter = 0;
    try (final QueryIterator<DCust> iterator = server().findDto(DCust.class, "select id, name from o_customer where id > :id")
      .setParameter("id", 0)
      .findIterate()) {

      while (iterator.hasNext()) {
        final DCust cust = iterator.next();
        counter++;
        assertThat(cust).isNotNull();
        assertThat(cust.getName()).isNotNull();
      }
    }

    assertThat(counter).isEqualTo(expectedCount);

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select id, name from o_customer where id > ?");
  }

  @Test
  void dto_findStream() {
    ResetBasicData.reset();
    final int expectedCount = server().find(Customer.class).findCount();

    LoggedSql.start();

    try (final Stream<DCust> stream =
           server()
             .findDto(DCust.class, "select id, name from o_customer where id > ?")
             .setParameter(0)
             .findStream()) {

      final List<String> names = stream
        .map(DCust::getName)
        .collect(Collectors.toList());

      assertThat(names.size()).isEqualTo(expectedCount);
    }

    List<String> sql = LoggedSql.stop();
    assertSql(sql.get(0)).contains("select id, name from o_customer where id > ?");
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

    Function<String, String> lowerUnderscore = (String key) -> "prefix:" + key.replace('.','_').toLowerCase();

    // collect without reset
    BasicMetricVisitor basic = new BasicMetricVisitor("db", lowerUnderscore, false, true, true, true);
    server().metaInfo().visitMetrics(basic);

    List<MetaQueryMetric> stats = basic.queryMetrics();
    assertThat(stats).hasSize(1);

    MetaQueryMetric queryMetric = stats.get(0);
    assertThat(queryMetric.label()).isEqualTo("basic");
    assertThat(queryMetric.count()).isEqualTo(3);
    assertThat(queryMetric.name()).isEqualTo("prefix:dto_dcust_basic");


    server().findDto(DCust.class, "select c4.id, c4.name from o_customer c4 where lower(c4.name) = :name")
      .setLabel("basic2")
      .setParameter("name", "rob")
      .findList();

    BasicMetricVisitor metric2 = server().metaInfo().visitBasic();

    stats = metric2.queryMetrics();
    assertThat(stats).hasSize(2);

    log.info("stats " + stats);
  }

  @Test
  void dto_mapping_columnsWithUnderscores() {
    ResetBasicData.reset();

    List<DCustCamelCols0> list = server().findDto(DCustCamelCols0.class,
        "select id as num_with, name as name_for_me from o_customer")
      .findList();

    assertThat(list).isNotEmpty();
    for (DCustCamelCols0 dto : list) {
      assertThat(dto.getNumWith()).isGreaterThan(0);
      assertThat(dto.getNameForMe()).isNotNull();
    }
  }

  @Test
  void dto_mapping_columnsWithNoUnderscores() {
    ResetBasicData.reset();

    List<DCustCamelCols0> list = server().findDto(DCustCamelCols0.class,
        "select id as numwith, name as nameforme from o_customer")
      .findList();

    assertThat(list).isNotEmpty();
    for (DCustCamelCols0 dto : list) {
      assertThat(dto.getNumWith()).isGreaterThan(0);
      assertThat(dto.getNameForMe()).isNotNull();
    }

    // mapping is case-insensitive
    List<DCustCamelCols0> list2 = server().findDto(DCustCamelCols0.class,
        "select id as numWith, name as nameForMe from o_customer")
      .findList();

    assertThat(list2).isNotEmpty();
    for (DCustCamelCols0 dto : list2) {
      assertThat(dto.getNumWith()).isGreaterThan(0);
      assertThat(dto.getNameForMe()).isNotNull();
    }
  }

  @Test
  void dto_mapping_propertyNamesCaseInsensitive() {
    ResetBasicData.reset();

    List<DCustCamelCols2> list = server().findDto(DCustCamelCols2.class,
        "select id as numwith, name as nameforme from o_customer")
      .findList();

    assertThat(list).isNotEmpty();
    for (DCustCamelCols2 dto : list) {
      assertThat(dto.getNumWITH()).isGreaterThan(0);
      assertThat(dto.getNameFORMe()).isNotNull();
    }

    // mapping is case-insensitive
    List<DCustCamelCols2> list2 = server().findDto(DCustCamelCols2.class,
        "select id as numWith, name as nameForMe from o_customer")
      .findList();

    assertThat(list2).isNotEmpty();
    for (DCustCamelCols2 dto : list2) {
      assertThat(dto.getNumWITH()).isGreaterThan(0);
      assertThat(dto.getNameFORMe()).isNotNull();
    }
  }

  @Test
  void dto_mapping_columnsUsingNestedSql() {
    ResetBasicData.reset();

    String sql
      = "select num_with, name_for_me from ( "
      // we don't CARE what the nested SQL is in that the jdbc metaData
      // for the column names/labels comes from the top most select
      + "    select id as num_with, name as name_for_me from o_customer "
      + ") a";

    List<DCustCamelCols0> list = server().findDto(DCustCamelCols0.class,
        sql)
      .findList();

    assertThat(list).isNotEmpty();
    for (DCustCamelCols0 dto : list) {
      assertThat(dto.getNumWith()).isGreaterThan(0);
      assertThat(dto.getNameForMe()).isNotNull();
    }
  }

  @Test
  void dto_findList_relaxedMode() {
    ResetBasicData.reset();

    List<DCust2> list = server().findDto(DCust2.class, "select id, '42' as something_we_cannot_map, name from o_customer")
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

    String sql = "select c.id, c.name, count(o.id) as totalOrders\n" +
      "        from o_customer c\n" +
      "        join o_order o on o.kcustomer_id = c.id\n" +
      "        where c.name like :name\n" +
      "        group by c.id, c.name";

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

  @Test
  void dto3_findList_constructorMatch() {
    ResetBasicData.reset();

    List<DCust3> robs = server().findDto(DCust3.class, "select id, name, 42 as totalOrders from o_customer where name like ?")
      .setParameter("Rob")
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

  @Test
  void dto_test_formula_with_group_by() {
    ResetBasicData.reset();

    Query<Contact> query = server().find(Contact.class)
      .select("concat(first_name, last_name) as custname, count(*) as cnt")
      .orderBy("custname desc");

    List<DCustFormula> ret = query.asDto(DCustFormula.class).findList();

    log.info(query.getGeneratedSql());
    log.info(ret.toString());
    assertThat(ret.get(0).getCustname()).isEqualTo("TracyRed");
    assertThat(ret.get(0).getCnt()).isEqualTo(1L);
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

  public static class DCustFluidAccessors {

    Integer id;
    String name;

    @Override
    public String toString() {
      return "id:" + id + " name:" + name;
    }

    public Integer id() {
      return id;
    }

    public DCustFluidAccessors id(Integer id) {
      this.id = id;
      return this;
    }

    public String name() {
      return name;
    }

    public DCustFluidAccessors name(String name) {
      this.name = name;
      return this;
    }
  }

  public static class DCustPlainAccessors {

    Integer id;
    String name;

    @Override
    public String toString() {
      return "id:" + id + " name:" + name;
    }

    public Integer id() {
      return id;
    }

    public void id(Integer id) {
      this.id = id;
    }

    public String name() {
      return name;
    }

    public void name(String name) {
      this.name = name;
    }
  }

  public static class DCustCamelCols0 {

    Integer numWith;
    String nameForMe;

    public Integer getNumWith() {
      return numWith;
    }

    public DCustCamelCols0 setNumWith(Integer numWith) {
      this.numWith = numWith;
      return this;
    }

    public String getNameForMe() {
      return nameForMe;
    }

    public DCustCamelCols0 setNameForMe(String nameForMe) {
      this.nameForMe = nameForMe;
      return this;
    }
  }

  public static class DCustCamelCols2 {

    Integer numWITH;
    String nameFORMe;

    public Integer getNumWITH() {
      return numWITH;
    }

    public DCustCamelCols2 setNumWITH(Integer numWITH) {
      this.numWITH = numWITH;
      return this;
    }

    public String getNameFORMe() {
      return nameFORMe;
    }

    public DCustCamelCols2 setNameFORMe(String nameFORMe) {
      this.nameFORMe = nameFORMe;
      return this;
    }
  }

  public static class DCustFormula {

    String custname;
    Long cnt;

    public DCustFormula() {}

    public void setCustname(String custname) {
      this.custname = custname;
    }

    public String getCustname() {
      return custname;
    }

    public void setCnt(Long cnt) {
      this.cnt = cnt;
    }

    public Long getCnt() {
      return cnt;
    }

    @Override
    public String toString() {
      return getCustname() + ": " + getCnt();
    }
  }
}
