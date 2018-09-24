package io.ebeaninternal.server.grammer;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebeaninternal.api.SpiQuery;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EqlParserTest extends BaseTestCase {


  @Test(expected = IllegalArgumentException.class)
  public void illegal_syntax() {
    parse("find Article where name = :p0");
  }

  @Test
  public void where_eq() {

    Query<Customer> query = parse("where name eq 'Rob'");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  public void where_eq_reverse() {

    Query<Customer> query = parse("where 'Rob' eq name");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  public void where_gt_reverse() {

    Query<Customer> query = parse("where 'Rob' > name");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where t0.name < ?");
  }

  @Test
  public void where_gte_reverse() {

    Query<Customer> query = parse("where 'Rob' >= name");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where t0.name <= ?");
  }

  @Test
  public void where_lt_reverse() {

    Query<Customer> query = parse("where 'Rob' < name");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where t0.name > ?");
  }

  @Test
  public void where_lte_reverse() {

    Query<Customer> query = parse("where 'Rob' <= name");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where t0.name >= ?");
  }

  @Test
  public void where_ieq() {

    Query<Customer> query = parse("where name ieq 'Rob'");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where lower(t0.name) =?");
  }

  @Test
  public void where_ieq_reverse() {

    Query<Customer> query = parse("where 'Rob' ieq name");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where lower(t0.name) =?");
  }

  @Test
  public void where_eq2() {

    Query<Customer> query = parse("where name = 'Rob'");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  public void where_namedParam() {

    Query<Customer> query = parse("where name eq :name");
    query.setParameter("name", "Rob");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  public void where_namedParam_otherOrder() {
    Query<Customer> query = parse("where :nm < name");
    query.setParameter("nm", "Rob");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name > ?");
  }

  @Test
  public void where_namedParam_startsWith() {

    Query<Customer> query = parse("where name startsWith :name");
    query.setParameter("name", "Rob");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name like ");
  }

  @Test
  public void where_or1() {

    Query<Customer> query = parse("where name = 'Rob' or (status = 'NEW' and smallnote is null)");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where (t0.name = ?  or (t0.status = ?  and t0.smallnote is null ) )");
  }

  @Test
  public void where_or2() {

    Query<Customer> query = parse("where (name = 'Rob' or status = 'NEW') and smallnote is null");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where ((t0.name = ?  or t0.status = ? )  and t0.smallnote is null )");
  }

  @Test
  public void test_simplifyExpressions() {

    Query<Customer> query = parse("where not (name = 'Rob' and status = 'NEW')");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where not (t0.name = ?  and t0.status = ? )");

    query = parse("where not ((name = 'Rob' and status = 'NEW'))");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where not (t0.name = ?  and t0.status = ? )");

    query = parse("where not (((name = 'Rob') and (status = 'NEW')))");
    query.findList();
    assertThat(query.getGeneratedSql()).contains("where not (t0.name = ?  and t0.status = ? )");
  }


  @Test
  public void where_in() {

    Query<Customer> query = parse("where name in ('Rob','Jim')");
    query.findList();

    platformAssertIn(query.getGeneratedSql(),"where t0.name");
  }

  @Test
  public void where_in_when_namedParams() {

    Query<Customer> query = parse("where name in (:one, :two)");
    query.setParameter("one", "Foo");
    query.setParameter("two", "Bar");
    query.findList();

    platformAssertIn(query.getGeneratedSql(),"where t0.name");
  }

  @Test
  public void where_in_when_namedParams_withWhitespace() {

    Query<Customer> query = parse("where name in (:one,  :two)");
    query.setParameter("one", "Foo");
    query.setParameter("two", "Bar");
    query.findList();

    platformAssertIn(query.getGeneratedSql(),"where t0.name");
  }

  @Test
  public void where_in_when_namedParams_withNoWhitespace() {

    Query<Customer> query = parse("where name in (:one,:two)");
    query.setParameter("one", "Foo");
    query.setParameter("two", "Bar");
    query.findList();

    platformAssertIn(query.getGeneratedSql(),"where t0.name");
  }

  @Test
  public void where_in_when_namedParamAsList() {

    Query<Customer> query = parse("where name in (:names)");
    query.setParameter("names", Arrays.asList("Baz", "Maz", "Jim"));
    query.findList();

    platformAssertIn(query.getGeneratedSql(),"where t0.name");
  }

  @Test
  public void where_between() {

    Query<Customer> query = parse("where name between 'As' and 'B'");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name between  ? and ? ");
  }

  @Test
  public void where_between_withNamedParams() {

    Query<Customer> query = parse("where name between :one and :two");
    query.setParameter("one", "a");
    query.setParameter("two", "b");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name between  ? and ? ");
  }

  @Test
  public void where_betweenProperty() {

    Query<Customer> query = parse("where 'x' between name and smallnote");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where  ? between t0.name and t0.smallnote");
  }

  @Test
  public void where_betweenProperty_withNamed() {

    Query<Customer> query = parse("where :some between name and smallnote");
    query.setParameter("some", "A");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where  ? between t0.name and t0.smallnote");
  }

  @Test
  public void fetch_basic() {

    Query<Customer> query = parse("fetch billingAddress");
    query.findList();

    assertThat(query.getGeneratedSql()).contains(", t1.id");
  }

  @Test
  public void fetch_withProperty() {

    Query<Customer> query = parse("fetch billingAddress (city)");
    query.findList();

    assertThat(sqlOf(query, 10)).contains(", t1.id, t1.city");
  }

  @Test
  public void fetch_withProperty_noWhitespace() {

    Query<Customer> query = parse("fetch billingAddress(city)");
    query.findList();

    assertThat(sqlOf(query, 10)).contains(", t1.id, t1.city");
  }

  @Test
  public void fetch_basic_multiple() {

    Query<Customer> query = parse("fetch billingAddress fetch shippingAddress");
    query.findList();

    assertThat(query.getGeneratedSql()).contains(", t1.city");
    assertThat(query.getGeneratedSql()).contains(", t2.city");
  }

  @Test
  public void fetch_basic_multiple_withProperties() {

    Query<Customer> query = parse("fetch billingAddress (city) fetch shippingAddress (city)");
    query.findList();

    assertThat(sqlOf(query, 12)).contains(", t1.id, t1.city, t2.id, t2.city");
  }

  @Test
  public void fetch_lazy() {

    Query<Customer> query = parse("fetch lazy billingAddress");
    query.findList();

    assertThat(query.getGeneratedSql()).doesNotContain(", t1.city");
  }

  @Test
  public void fetch_lazy50() {

    Query<Customer> query = parse("fetch lazy(50) billingAddress");
    query.findList();

    assertThat(query.getGeneratedSql()).doesNotContain(", t1.city");
  }

  @Test
  public void fetch_query50() {

    ResetBasicData.reset();
    Query<Customer> query = parse("fetch query(50) billingAddress");
    query.findList();

    assertThat(query.getGeneratedSql()).doesNotContain(", t1.city");
  }

  @Test
  public void fetch_query50_asHint() {

    ResetBasicData.reset();
    Query<Customer> query = parse("fetch billingAddress (+query(50),city)");
    query.findList();

    assertThat(query.getGeneratedSql()).doesNotContain(", t1.city");
  }

  @Test
  public void fetch_lazy50_asHint() {

    ResetBasicData.reset();
    Query<Customer> query = parse("fetch billingAddress (+lazy(50),city) order by id");
    List<Customer> list = query.findList();

    assertThat(query.getGeneratedSql()).doesNotContain(", t1.city");

    Customer customer = list.get(0);
    customer.getBillingAddress().getCity();
  }

  @Test
  public void select() {

    ResetBasicData.reset();

    Query<Customer> query = parse("select (name)");
    query.findList();
    assertThat(sqlOf(query, 1)).contains("select t0.id, t0.name from o_customer t0");
  }

  @Test
  public void selectDistinct() {

    ResetBasicData.reset();

    Query<Customer> query = parse("select distinct (name)");
    query.findList();
    assertThat(sqlOf(query, 1)).contains("select distinct t0.name from o_customer t0");
  }


  @Test
  public void limit() {

    ResetBasicData.reset();

    Query<Customer> query = parse("limit 10");
    query.findList();
    if (isH2()) {
      assertThat(query.getGeneratedSql()).contains(" limit 10");
    }
  }

  @Test
  public void limitOffset() {

    ResetBasicData.reset();

    Query<Customer> query = parse("limit 10 offset 5");
    query.findList();
    if (isH2()) {
      assertThat(query.getGeneratedSql()).contains(" limit 10 offset 5");
    }
  }

  @Test
  public void orderBy() {

    ResetBasicData.reset();

    Query<Customer> query = parse("order by id");
    query.findList();
    if (isH2()) {
      assertThat(query.getGeneratedSql()).contains("from o_customer t0 order by t0.id");
    }
  }

  @Test
  public void orderBy_desc() {

    ResetBasicData.reset();

    Query<Customer> query = parse("order by id desc");
    query.findList();
    if (isH2()) {
      assertThat(query.getGeneratedSql()).contains("from o_customer t0 order by t0.id desc");
    }
  }

  @Test
  public void orderBy_nullsLast() {

    if (!isPlatformOrderNullsSupport()) {
      return;
    }

    ResetBasicData.reset();

    Query<Customer> query = parse("order by id desc nulls last");
    query.findList();
    if (isH2()) {
      assertThat(query.getGeneratedSql()).contains(" from o_customer t0 order by t0.id desc nulls last");
    }
  }

  @Test
  public void orderBy_nullsFirst() {

    if (!isPlatformOrderNullsSupport()) {
      return;
    }
    ResetBasicData.reset();

    Query<Customer> query = parse("order by id nulls first");
    query.findList();
    if (isH2()) {
      assertThat(query.getGeneratedSql()).contains(" from o_customer t0 order by t0.id nulls first");
    }
  }

  @Test
  public void orderBy_multiple() {

    if (!isPlatformOrderNullsSupport()) {
      return;
    }
    ResetBasicData.reset();

    Query<Customer> query = parse("order by billingAddress.city desc nulls last, name, id desc nulls last");
    query.findList();
    if (isH2()) {
      assertThat(query.getGeneratedSql()).contains(" order by t1.city desc nulls last, t0.name, t0.id desc nulls last");
    }
  }

  private Query<Customer> parse(String raw) {

    Query<Customer> query = Ebean.find(Customer.class);
    EqlParser.parse(raw, (SpiQuery<?>) query);
    return query;
  }

}
