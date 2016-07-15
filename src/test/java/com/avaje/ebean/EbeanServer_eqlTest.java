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

  @Test
  public void namedQuery() {

    ResetBasicData.reset();

    Query<Customer> name = server().createNamedQuery(Customer.class, "name");
    name.findList();

    assertThat(name.getGeneratedSql()).contains("select t0.id c0, t0.name c1 from o_customer t0 order by t0.name");
  }

  @Test
  public void namedQuery_withStatus() {

    ResetBasicData.reset();

    Query<Customer> name = server().createNamedQuery(Customer.class, "withStatus");
    name.order().clear().asc("status");
    name.findList();

    assertThat(name.getGeneratedSql()).contains("select t0.id c0, t0.name c1, t0.status c2 from o_customer t0 order by t0.status");
  }

  @Test
  public void namedQuery_withContacts() {

    ResetBasicData.reset();

    Query<Customer> query = server()
        .createNamedQuery(Customer.class, "withContacts")
        .setParameter("id", 1);

    query.findUnique();

    assertThat(query.getGeneratedSql()).contains("from o_customer t0 left outer join contact t1 on t1.customer_id = t0.id ");
  }

  @Test
  public void namedQuery_fromXml() {

    ResetBasicData.reset();

    Query<Customer> query = server()
        .createNamedQuery(Customer.class, "withContactsById")
        .setParameter("id", 1);

    query.findUnique();

    assertThat(query.getGeneratedSql()).contains("from o_customer t0 left outer join contact t1 on t1.customer_id = t0.id ");
  }
}