package com.avaje.ebean;

import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import javax.persistence.PersistenceException;

import static org.assertj.core.api.Assertions.assertThat;

public class EbeanServer_eqlTest extends BaseTestCase {


  @Test
  public void basic() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class, "order by id limit 10");
    query.setMaxRows(100);
    query.findList();

    assertThat(query.getGeneratedSql()).contains("order by t0.id ");
  }

  @Test
  public void basic_via_Ebean_defaultServer() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.createQuery(Customer.class, "order by id limit 10");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("order by t0.id ");
  }

  @Test
  public void orderBy_override() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class, "order by id");

    // use clear() and then effectively override the orderBy clause
    query.orderBy().clear().asc("name");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("order by t0.name");
  }


  @Test
  public void namedParams() {

    ResetBasicData.reset();

    Query<Customer> query = server().createQuery(Customer.class, "where name startsWith :name order by name");
    query.setParameter("name", "Ro");
    query.findList();

    assertThat(query.getGeneratedSql()).contains("where t0.name like ? ");
  }

  @Test(expected = PersistenceException.class)
  public void unboundNamedParams_expect_PersistenceException() {

    Query<Customer> query = server().createQuery(Customer.class, "where name = :name");
    query.findUnique();
  }

}