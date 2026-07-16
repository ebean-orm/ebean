package io.ebean.xtest.internal.server.grammer;

import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import io.ebean.xtest.ForPlatform;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import io.ebean.test.LoggedSql;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.grammer.EqlParser;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.OrderDetail;
import org.tests.model.basic.ResetBasicData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EqlParserTest extends BaseTestCase {

  @Test
  void illegal_syntax() {
    assertThrows(IllegalArgumentException.class, () -> parse("find Article where name = :p0"));
  }

  @Test
  void where_eq() {
    Query<Customer> query = parse("where name eq 'Rob'");
    query.findList();

    assertSql(query).contains("where t0.name = ?");
  }

  @Test
  void where_eqOrNull_bindVal() {
    Query<Customer> query = parse("where name eqOrNull 'Rob'");
    query.findList();

    assertSql(query).contains("where (t0.name = ? or t0.name is null)");
  }

  @Test
  void where_eqOrNull_bindNamed() {
    Query<Customer> query = parse("where name eqOrNull :name");
    query.setParameter("name", "Rob");
    query.findList();

    assertSql(query).contains("where (t0.name = ? or t0.name is null)");
  }

  @Test
  void where_eqOrNull_bindPositioned() {
    final Query<Customer> query = where("name eqOrNull ?", "Rob");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.name = ? or t0.name is null)");
    }
  }

  @Test
  void where_eqOrNull_bindPositioned_asNull() {
    final Query<Customer> query = where("name eqOrNull ?", (String)null);
    query.findList();
    if (isH2()) {
      assertSql(query).contains("(t0.name is null or t0.name is null)");
    }
  }

  @Test
  void where_gtOrNull_bindPositioned() {
    final Query<Customer> query = where("name gtOrNull ?", "Rob");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.name > ? or t0.name is null)");
    }
  }

  @Test
  void where_geOrNull_bindPositioned() {
    final Query<Customer> query = where("name geOrNull ?", "Rob");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.name >= ? or t0.name is null)");
    }
  }

  @Test
  void where_ltOrNull_bindPositioned() {
    final Query<Customer> query = where("name ltOrNull ?", "Rob");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.name < ? or t0.name is null)");
    }
  }

  @Test
  void where_leOrNull_bindPositioned() {
    final Query<Customer> query = where("name leOrNull ?", "Rob");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.name <= ? or t0.name is null)");
    }
  }

  @Test
  void where_eq_reverse() {
    Query<Customer> query = parse("where 'Rob' eq name");
    query.findList();

    assertSql(query).contains("where t0.name = ?");
  }

  @Test
  void where_gt_reverse() {
    Query<Customer> query = parse("where 'Rob' > name");
    query.findList();
    assertSql(query).contains("where t0.name < ?");
  }

  @Test
  void where_gte_reverse() {
    Query<Customer> query = parse("where 'Rob' >= name");
    query.findList();
    assertSql(query).contains("where t0.name <= ?");
  }

  @Test
  void where_lt_reverse() {
    Query<Customer> query = parse("where 'Rob' < name");
    query.findList();
    assertSql(query).contains("where t0.name > ?");
  }

  @Test
  void where_lte_reverse() {
    Query<Customer> query = parse("where 'Rob' <= name");
    query.findList();
    assertSql(query).contains("where t0.name >= ?");
  }

  @Test
  void where_ieq() {
    Query<Customer> query = parse("where name ieq 'Rob'");
    query.findList();

    assertSql(query).contains("where lower(t0.name) = ?");
  }

  @Test
  void where_ine() {
    Query<Customer> query = parse("where name ine 'Rob'");
    query.findList();

    assertSql(query).contains("where lower(t0.name) != ?");
  }

  @Test
  void where_ieq_reverse() {
    Query<Customer> query = parse("where 'Rob' ieq name");
    query.findList();
    assertSql(query).contains("where lower(t0.name) = ?");
  }

  @Test
  void where_ine_reverse() {
    Query<Customer> query = parse("where 'Rob' ine name");
    query.findList();

    assertSql(query).contains("where lower(t0.name) != ?");
  }

  @Test
  void where_eq2() {
    Query<Customer> query = parse("where name = 'Rob'");
    query.findList();

    assertSql(query).contains("where t0.name = ?");
  }

  @Test
  void where_namedParam() {
    Query<Customer> query = parse("where name eq :name");
    query.setParameter("name", "Rob");
    query.findList();

    assertSql(query).contains("where t0.name = ?");
  }

  @Test
  void where_namedParam_otherOrder() {
    Query<Customer> query = parse("where :nm < name");
    query.setParameter("nm", "Rob");
    query.findList();

    assertSql(query).contains("where t0.name > ?");
  }

  @Test
  void where_namedParam_startsWith() {
    Query<Customer> query = parse("where name startsWith :name");
    query.setParameter("name", "Rob");
    query.findList();

    assertSql(query).contains("where t0.name like ");
  }

  @Test
  @IgnorePlatform({Platform.HANA, Platform.DB2}) // The HANA JDBC driver checks the field length on binding and rejects 'NEW'
  void where_or1() {
    Query<Customer> query = parse("where name = 'Rob' or (status = 'NEW' and smallnote is null)");
    query.findList();

    assertSql(query).contains("where (t0.name = ? or (t0.status = ? and t0.smallnote is null))");
  }

  @Test
  @ForPlatform({Platform.HANA, Platform.DB2})
  void where_or1_hana() {
    Query<Customer> query = parse("where name = 'Rob' or (status = 'N' and smallnote is null)");
    query.findList();

    assertSql(query).contains("where (t0.name = ? or (t0.status = ? and t0.smallnote is null))");
  }

  @Test
  @IgnorePlatform({Platform.HANA, Platform.DB2}) // The HANA & DB2 JDBC driver checks the field length on binding and rejects 'NEW'
  void where_or2() {
    Query<Customer> query = parse("where (name = 'Rob' or status = 'NEW') and smallnote is null");
    query.findList();

    assertSql(query).contains("where ((t0.name = ? or t0.status = ?) and t0.smallnote is null)");
  }

  @Test
  @ForPlatform({Platform.HANA, Platform.DB2})
  void where_or2_hana() {
    Query<Customer> query = parse("where (name = 'Rob' or status = 'N') and smallnote is null");
    query.findList();

    assertSql(query).contains("where ((t0.name = ? or t0.status = ?) and t0.smallnote is null)");
  }

  @Test
  @IgnorePlatform({Platform.HANA, Platform.DB2}) // The HANA & DB2 JDBC driver checks the field length on binding and rejects 'NEW'
  void test_simplifyExpressions() {
    Query<Customer> query = parse("where not (name = 'Rob' and status = 'NEW')");
    query.findList();
    assertSql(query).contains("where not (t0.name = ? and t0.status = ?)");

    query = parse("where not ((name = 'Rob' and status = 'NEW'))");
    query.findList();
    assertSql(query).contains("where not (t0.name = ? and t0.status = ?)");

    query = parse("where not (((name = 'Rob') and (status = 'NEW')))");
    query.findList();
    assertSql(query).contains("where not (t0.name = ? and t0.status = ?)");
  }

  @Test
  @ForPlatform({Platform.HANA, Platform.DB2})
  void test_simplifyExpressions_hana() {
    Query<Customer> query = parse("where not (name = 'Rob' and status = 'N')");
    query.findList();
    assertSql(query).contains("where not (t0.name = ? and t0.status = ?)");

    query = parse("where not ((name = 'Rob' and status = 'N'))");
    query.findList();
    assertSql(query).contains("where not (t0.name = ? and t0.status = ?)");

    query = parse("where not (((name = 'Rob') and (status = 'N')))");
    query.findList();
    assertSql(query).contains("where not (t0.name = ? and t0.status = ?)");
  }

  @Test
  void where_in() {
    Query<Customer> query = parse("where name in ('Rob','Jim')");
    query.findList();

    platformAssertIn(query.getGeneratedSql(), "where t0.name");
  }

  @Test
  void where_in_when_namedParams() {
    Query<Customer> query = parse("where name in (:one, :two)");
    query.setParameter("one", "Foo");
    query.setParameter("two", "Bar");
    query.findList();

    platformAssertIn(query.getGeneratedSql(), "where t0.name");
  }

  @Test
  void where_in_when_namedParams_withWhitespace() {
    Query<Customer> query = parse("where name in (:one,  :two)");
    query.setParameter("one", "Foo");
    query.setParameter("two", "Bar");
    query.findList();

    platformAssertIn(query.getGeneratedSql(), "where t0.name");
  }

  @Test
  void where_in_when_namedParams_withNoWhitespace() {
    Query<Customer> query = parse("where name in (:one,:two)");
    query.setParameter("one", "Foo");
    query.setParameter("two", "Bar");
    query.findList();

    platformAssertIn(query.getGeneratedSql(), "where t0.name");
  }

  @Test
  void where_in_when_namedParamAsList() {
    Query<Customer> query = parse("where name in (:names)");
    query.setParameter("names", Arrays.asList("Baz", "Maz", "Jim"));
    query.findList();

    platformAssertIn(query.getGeneratedSql(), "where t0.name");
  }

  @Test
  void where_inOrEmpty_withVals() {
    final Query<Customer> query = where("name inOrEmpty ?", Arrays.asList("Baz", "Maz", "Jim"));
    query.findList();
    platformAssertIn(query.getGeneratedSql(), "where t0.name");
  }

  @Test
  void where_inOrEmpty_withValsAsSet() {
    final Query<Customer> query = where("name inOrEmpty ?", new HashSet<>(Arrays.asList("Baz", "Maz", "Jim")));
    query.findList();
    platformAssertIn(query.getGeneratedSql(), "where t0.name");
  }

  @Test
  void where_inOrEmpty_withEmpty() {
    final Query<Customer> query = where("name inOrEmpty ?", new ArrayList());
    query.findList();
    assertSql(query).doesNotContain("where");
  }

  @Test
  void where_inOrEmpty_withNull() {
    List<String> names = null;
    final Query<Customer> query = where("name inOrEmpty ?", names);
    query.findList();
    assertSql(query).doesNotContain("where");
  }

  @Test
  void query_inOrEmpty_withVals() {
    assertThrows(IllegalArgumentException.class, () -> parse("where name inOrEmpty (:names)"));
  }

  /**
   * This test fails in that we can't use inOrEmpty with named parameters.
   */
  @Test
  void query_inOrEmpty_withNamedParams_expect_IllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> parse("where name inOrEmpty (:names)"));
  }

  @Test
  void where_inrange() {
    Query<Customer> query = parse("where name inrange 'As' to 'B'");
    query.findList();

    assertSql(query).contains("where (t0.name >= ? and t0.name < ?)");
  }

  @Test
  void where_inrange_withNamedParams() {
    Query<Customer> query = parse("where name inrange :one to :two");
    query.setParameter("one", "a");
    query.setParameter("two", "b");
    query.findList();

    assertSql(query).contains("where (t0.name >= ? and t0.name < ?)");
  }

  @Test
  void where_between() {
    Query<Customer> query = parse("where name between 'As' and 'B'");
    query.findList();

    assertSql(query).contains("where t0.name between ? and ?");
  }

  @Test
  void where_between_withNamedParams() {
    Query<Customer> query = parse("where name between :one and :two");
    query.setParameter("one", "a");
    query.setParameter("two", "b");
    query.findList();

    assertSql(query).contains("where t0.name between ? and ?");
  }

  @Test
  void where_betweenProperty() {
    Query<Customer> query = parse("where 'x' between name and smallnote");
    query.findList();

    assertSql(query).contains("where ? between t0.name and t0.smallnote");
  }

  @Test
  void where_betweenProperty_withNamed() {
    Query<Customer> query = parse("where :some between name and smallnote");
    query.setParameter("some", "A");
    query.findList();

    assertSql(query).contains("where ? between t0.name and t0.smallnote");
  }

  @Test
  void selectFetch_basic() {
    Query<Customer> query = parse("select name fetch billingAddress");
    query.findList();

    assertSql(query).contains("select t0.id, t0.name, t1.id, t1.line_1, t1.line_2, t1.city, t1.cretime, t1.updtime, t1.country_code from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id");
  }

  @Test
  void selectFetch_basic2() {
    ResetBasicData.reset();

    Query<Customer> query = parse("select name, status fetch billingAddress");
    query.findList();

    assertSql(query).contains("select t0.id, t0.name, t0.status, t1.id, t1.line_1, t1.line_2, t1.city, t1.cretime, t1.updtime, t1.country_code from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id");
  }

  @Test
  void selectFetch_withProperties() {
    Query<Customer> query = parse("select name fetch billingAddress (line1, city) ");
    query.findList();

    assertSql(query, 12).contains("select t0.id, t0.name, t1.id, t1.line_1, t1.city from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id");
  }

  @Test
  @IgnorePlatform(Platform.SQLSERVER)
  void selectFetchFetchLimit() {
    Query<Customer> query = parse("select name fetch billingAddress (line1, city) fetch shippingAddress (line1) limit 10");
    query.findList();

    assertSql(query, 12).contains("select t0.id, t0.name, t1.id, t1.line_1, t1.city, t2.id, t2.line_1 from o_customer t0 left join o_address t1 on t1.id = t0.billing_address_id left join o_address t2 on t2.id = t0.shipping_address_id");
  }

  @Test
  void selectFetchFetchMany() {
    Query<Customer> query = parse("select name fetch billingAddress (line1, city) fetch contacts");
    query.findList();

    assertSql(query, 16).contains("select t0.id, t0.name, t1.id, t1.line_1, t1.city, t2.id, t2.first_name, t2.last_name, t2.phone, t2.mobile, t2.email, t2.is_member, t2.cretime, t2.updtime, t2.customer_id, t2.group_id from o_customer t0");
  }

  @Test
  void selectFetchFetchManyProperties() {
    Query<Customer> query = parse("select name fetch billingAddress (line1, city) fetch contacts (email)");
    query.findList();

    assertSql(query, 12).contains("select t0.id, t0.name, t1.id, t1.line_1, t1.city, t2.id, t2.email from o_customer t0");
  }

  @Test
  @IgnorePlatform(Platform.SQLSERVER)
  void selectFetchFetchManyPropertiesLimit() {
    ResetBasicData.reset();

    LoggedSql.start();

    Query<Customer> query = parse("select name fetch billingAddress (line1, city) fetch contacts (email) limit 10");
    query.findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id, t0.name, t1.id, t1.line_1, t1.city from o_customer t0 left join o_address");
    assertSql(sql.get(1)).contains("select t0.customer_id, t0.id, t0.email from contact t0 where (t0.customer_id)");
  }

  @Test
  void fetch_basic() {
    Query<Customer> query = parse("fetch billingAddress");
    query.findList();

    assertSql(query).contains(", t1.id");
  }

  @Test
  void fetch_withProperty() {
    Query<Customer> query = parse("fetch billingAddress (city)");
    query.findList();

    assertSql(query, 10).contains(", t1.id, t1.city");
  }

  @Test
  void fetch_withProperty_noWhitespace() {
    Query<Customer> query = parse("fetch billingAddress(city)");
    query.findList();

    assertSql(query, 10).contains(", t1.id, t1.city");
  }

  @Test
  void fetch_basic_multiple() {
    Query<Customer> query = parse("fetch billingAddress fetch shippingAddress");
    query.findList();

    assertSql(query).contains(", t1.city");
    assertSql(query).contains(", t2.city");
  }

  @Test
  void fetch_basic_multiple_withProperties() {
    Query<Customer> query = parse("fetch billingAddress (city) fetch shippingAddress (city)");
    query.findList();

    assertSql(query, 12).contains(", t1.id, t1.city, t2.id, t2.city");
  }

  @Test
  void fetch_lazy() {
    Query<Customer> query = parse("fetch lazy billingAddress");
    query.findList();

    assertSql(query).doesNotContain(", t1.city");
  }

  @Test
  void fetch_lazy50() {
    Query<Customer> query = parse("fetch lazy(50) billingAddress");
    query.findList();

    assertSql(query).doesNotContain(", t1.city");
  }

  @Test
  void fetch_query50() {
    ResetBasicData.reset();
    Query<Customer> query = parse("fetch query(50) billingAddress");
    query.findList();

    assertSql(query).doesNotContain(", t1.city");
  }

  @Test
  void fetch_query50_asHint() {
    ResetBasicData.reset();
    Query<Customer> query = parse("fetch query(50) billingAddress (city)");
    query.findList();

    assertSql(query).doesNotContain(", t1.city");
  }

  @Test
  void fetch_lazy50_asHint() {
    ResetBasicData.reset();
    Query<Customer> query = parse("fetch lazy(50) billingAddress (city) order by id");
    List<Customer> list = query.findList();

    assertSql(query).doesNotContain(", t1.city");

    Customer customer = list.get(0);
    customer.getBillingAddress().getCity();
  }

  @Test
  void select() {
    ResetBasicData.reset();

    Query<Customer> query = parse("select (name)");
    query.findList();
    assertSql(query).contains("select t0.id, t0.name from o_customer t0");

    Query<Customer> query2 = parse("select name");
    query2.findList();
    assertSql(query2).contains("select t0.id, t0.name from o_customer t0");
  }

  @Test
  void select_sum() {
    ResetBasicData.reset();

    Query<Customer> query = parse("select (sum(id))");
    query.findSingleAttribute();
    assertSql(query).contains("select sum(t0.id) from o_customer t0");
  }

  @Test
  void select_max() {
    ResetBasicData.reset();

    Query<Customer> query = parse("select (max(cretime))");
    query.findSingleAttribute();
    assertSql(query).contains("select max(t0.cretime) from o_customer t0");

    Query<Customer> query2 = parse("select max(cretime)");
    query2.findSingleAttribute();
    assertSql(query2).contains("select max(t0.cretime) from o_customer t0");
  }

  @Test
  void select_agg() {
    ResetBasicData.reset();

    Query<Customer> query = parse("select status, max(cretime)");
    List<Customer> customers = query.findList();

    assertThat(customers).isNotEmpty();
    assertSql(query).contains("select t0.status, max(t0.cretime) from o_customer t0 group by t0.status");
  }

  @Test
  void select_agg_sum() {
    ResetBasicData.reset();

    Query<OrderDetail> query = DB.createQuery(OrderDetail.class, "select sum(orderQty) fetch `order` (id)");
    List<OrderDetail> details = query.findList();

    assertThat(details).isNotEmpty();
    assertSql(query).contains("select sum(t0.order_qty), t0.order_id from o_order_detail t0 group by t0.order_id");
  }

  @Test
  void selectDistinct() {
    ResetBasicData.reset();

    Query<Customer> query = parse("select distinct (name)");
    query.findList();
    assertSql(query).contains("select distinct t0.name from o_customer t0");
  }

  @Test
  void limit() {
    ResetBasicData.reset();

    Query<Customer> query = parse("limit 10");
    query.findList();
    if (isH2()) {
      assertSql(query).contains(" limit 10");
    }
  }

  @Test
  void limitOffset() {
    ResetBasicData.reset();

    Query<Customer> query = parse("order by name limit 10 offset 5");
    query.findList();
    if (isH2()) {
      assertSql(query).contains(" limit 10 offset 5");
    }
  }

  @Test
  void orderBy() {
    ResetBasicData.reset();

    Query<Customer> query = parse("order by id");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("from o_customer t0 order by t0.id");
    }
  }

  @Test
  void orderBy_desc() {
    ResetBasicData.reset();

    Query<Customer> query = parse("order by id desc");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("from o_customer t0 order by t0.id desc");
    }
  }

  @Test
  void orderBy_nullsLast() {
    if (!isPlatformOrderNullsSupport()) {
      return;
    }

    ResetBasicData.reset();

    Query<Customer> query = parse("order by id desc nulls last");
    query.findList();
    if (isH2()) {
      assertSql(query).contains(" from o_customer t0 order by t0.id desc nulls last");
    }
  }

  @Test
  void orderBy_nullsFirst() {
    if (!isPlatformOrderNullsSupport()) {
      return;
    }
    ResetBasicData.reset();

    Query<Customer> query = parse("order by id nulls first");
    query.findList();
    if (isH2()) {
      assertSql(query).contains(" from o_customer t0 order by t0.id nulls first");
    }
  }

  @Test
  void orderBy_multiple() {
    if (!isPlatformOrderNullsSupport()) {
      return;
    }
    ResetBasicData.reset();

    Query<Customer> query = parse("order by billingAddress.city desc nulls last, name, id desc nulls last");
    query.findList();
    if (isH2()) {
      assertSql(query).contains(" order by t1.city desc nulls last, t0.name, t0.id desc nulls last");
    }
  }

  private Query<Customer> parse(String raw) {
    Query<Customer> query = DB.find(Customer.class);
    EqlParser.parse(raw, (SpiQuery<?>) query);
    return query;
  }

  @Test
  void where_simple() {
    final Query<Customer> query = where("name isNotNull");
    query.findList();
    if (isH2()) {
      assertSql(query).contains(" from o_customer t0 where t0.name is not null");
    }
  }

  @Test
  void where_withParams() {
    final Query<Customer> query = where("id isNotNull and name = ? and smallnote istartsWith ?", "Rob", "Foo");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.id is not null and t0.name = ? and lower(t0.smallnote) like ? escape'|')");
    }
  }

  @Test
  void where_withParamsQuPos() {
    final Query<Customer> query = where("name = ?1 and smallnote istartsWith ?2 and name like ?1", "Rob", "Foo");
    query.findList();
    if (isH2()) {
      assertSql(query).contains(" where (t0.name = ? and lower(t0.smallnote) like ? escape'|' and t0.name like ? escape'')");
    }
  }

  @Test
  void where_orSimple() {
    final Query<Customer> query = where("id isNotNull or name = ?", "Rob", "Foo");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.id is not null or t0.name = ?)");
    }
  }

  @Test
  void where_orWithParams() {
    final Query<Customer> query = where("(id isNotNull or name = ?) and smallnote istartsWith ?", "Rob", "Foo");
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where ((t0.id is not null or t0.name = ?) and lower(t0.smallnote) like ? escape'|')");
    }
  }

  @Test
  void where_dateInRange() {
    final Query<Customer> query = where("anniversary inrange ? to ?", LocalDate.now().minusDays(7), LocalDate.now());
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.anniversary >= ? and t0.anniversary < ?)");
    }
  }

  @Test
  void where_dateInRange_camelCase() {
    final Query<Customer> query = where("anniversary inRange ? to ?", LocalDate.now().minusDays(7), LocalDate.now());
    query.findList();
    if (isH2()) {
      assertSql(query).contains("where (t0.anniversary >= ? and t0.anniversary < ?)");
    }
  }

  private Query<Customer> where(String where, Object... params) {
    Query<Customer> query = DB.find(Customer.class);
    EqlParser.parseWhere(where, query.where(), query.getExpressionFactory(), params);
    return query;
  }
}
