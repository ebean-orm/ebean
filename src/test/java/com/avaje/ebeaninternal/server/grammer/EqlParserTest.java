package com.avaje.ebeaninternal.server.grammer;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.tests.model.basic.Customer;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class EqlParserTest {

  @Test
  public void where_eq() throws Exception {

    Query<Customer> query = parse("where name eq 'Rob'");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  public void where_ieq() throws Exception {

    Query<Customer> query = parse("where name ieq 'Rob'");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where lower(t0.name) =?");
  }

  @Test
  public void where_eq2() throws Exception {

    Query<Customer> query = parse("where name = 'Rob'");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  public void where_namedParam() throws Exception {

    Query<Customer> query = parse("where name eq :name");
    query.setParameter("name", "Rob");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name = ?");
  }

  @Test
  public void where_namedParam_startsWith() throws Exception {

    Query<Customer> query = parse("where name startsWith :name");
    query.setParameter("name", "Rob");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name like ?");
  }

  @Test
  public void where_or1() throws Exception {

    Query<Customer> query = parse("where name = 'Rob' or (status = 'NEW' and smallnote is null)");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where (t0.name = ?  or (t0.status = ?  and t0.smallnote is null ) )");
  }

  @Test
  public void where_or2() throws Exception {

    Query<Customer> query = parse("where (name = 'Rob' or status = 'NEW') and smallnote is null");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where ((t0.name = ?  or t0.status = ? )  and t0.smallnote is null )");
  }

  @Test
  public void test_simplifyExpressions() throws Exception {

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
  public void where_in() throws Exception {

    Query<Customer> query = parse("where name in ('Rob','Jim')");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name in (?, ? )");
  }

  @Test
  public void where_in_when_namedParams() throws Exception {

    Query<Customer> query = parse("where name in (:one, :two)");
    query.setParameter("one", "Foo");
    query.setParameter("two", "Bar");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name in (?, ? )");
  }

  @Test
  public void where_in_when_namedParamAsList() throws Exception {

    Query<Customer> query = parse("where name in (:names)");
    query.setParameter("names", Arrays.asList("Baz","Maz","Jim"));
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name in (?, ?, ? )");
  }

  private Query<Customer> parse(String raw) {

    Query<Customer> query = Ebean.find(Customer.class);
    EqlParser.parse(raw, (SpiQuery)query);
    return query;
  }

}