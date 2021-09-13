package org.tests.model.basic.test;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Named queries both local to the entity bean and via external xml source.
 */
public class NamedLocalAndExternalTest extends BaseTestCase {

  private Database server() {
    return DB.getDefault();
  }

  private String sqlOf(Query<?> query) {
    return query.getGeneratedSql();
  }

  @Test
  public void namedQuery() {

    Query<Customer> name = server().createNamedQuery(Customer.class, "name");
    name.findList();

    assertThat(sqlOf(name)).contains("select t0.id, t0.name from o_customer t0 order by t0.name");
  }

  @Test
  public void namedQuery_withStatus() {

    Query<Customer> name = server().createNamedQuery(Customer.class, "withStatus");
    name.order().clear().asc("status");
    name.findList();

    assertThat(sqlOf(name)).contains("select t0.id, t0.name, t0.status from o_customer t0 order by t0.status");
  }

  @Test
  public void namedQuery_fromXml() {

    Query<Customer> query = server()
      .createNamedQuery(Customer.class, "withContactsById")
      .setParameter("id", 1);

    query.setUseCache(false);
    query.findOne();

    assertThat(sqlOf(query)).contains("from o_customer t0 left join contact t1 on t1.customer_id = t0.id ");
  }

  @Test
  public void namedQuery_fromCustomXmlLocations() {

    Query<Customer> query = server()
      .createNamedQuery(Customer.class, "withContactsById2")
      .setParameter("id", 1);

    query.setUseCache(false);
    query.findOne();

    assertThat(sqlOf(query)).contains("from o_customer t0 left join contact t1 on t1.customer_id = t0.id ");
  }
}
