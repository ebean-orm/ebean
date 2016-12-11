package org.tests.query.other;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestQueryDistinct extends BaseTestCase {

  @Test
  public void test() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .setDistinct(true)
      .select("name");

    List<Customer> customers = query.findList();

    String generatedSql = sqlOf(query);
    assertThat(generatedSql).contains("select distinct t0.name from o_customer t0");

    for (Customer customer : customers) {

      EntityBeanIntercept ebi = ((EntityBean) customer)._ebean_getIntercept();
      assertTrue(ebi.isDisableLazyLoad());
      assertNull(ebi.getPersistenceContext());

      // lazy loading disabled
      assertNull(customer.getId());
      assertNull(customer.getAnniversary());
    }
  }

  @Test
  public void test_onWhere() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .where().setDistinct(true)
      .select("name");

    query.findList();

    String generatedSql = sqlOf(query);
    assertThat(generatedSql).contains("select distinct t0.name from o_customer t0");
  }

  @Test
  public void testDistinctStatus() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .setDistinct(true)
      .select("status")
      .where().isNotNull("status").query();

    List<Customer> customers = query.findList();

    String generatedSql = sqlOf(query);
    assertThat(generatedSql).contains("select distinct t0.status from o_customer t0");

    for (Customer customer : customers) {

      assertNotNull(customer.getStatus());

      // lazy loading disabled
      assertNull(customer.getId());
      assertNull(customer.getAnniversary());
    }
  }

  @Test
  public void testPagingQuery_expect_doesNotAddOrderBy() {

    ResetBasicData.reset();

    Query<Customer> query = Ebean.find(Customer.class)
      .setMaxRows(10)
      .setDistinct(true)
      .select("name");

    query.findList();

    if (isH2() || isPostgres()) {
      String generatedSql = sqlOf(query);
      assertThat(generatedSql).contains("select distinct t0.name from o_customer t0 limit 10");
    }
  }

}
