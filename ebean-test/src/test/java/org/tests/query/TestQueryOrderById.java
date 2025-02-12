package org.tests.query;

import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadOnly;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryOrderById extends BaseTestCase {

  @Test
  public void orderById_default_expectNotOrderById() {
    ResetBasicData.reset();

    Query<Customer> query = DB.find(Customer.class)
      .select("id,name")
      .orderBy("id")
      .setFirstRow(1)
      .setMaxRows(5);

    query.setReadOnly(true).setDisableLazyLoading(true);
    List<Customer> list = query.findList();
    if (isSqlServer() || isDb2()) {
      assertSql(query).isEqualTo("select t0.id, t0.name from o_customer t0 order by t0.id offset 1 rows fetch next 5 rows only");
    } else if (!isOracle()) {
      assertSql(query).isEqualTo("select t0.id, t0.name from o_customer t0 order by t0.id limit 5 offset 1");
    }

    assertThat(list).isNotEmpty();
    Customer customer = list.get(0);
    EntityBeanIntercept intercept = ((EntityBean) customer)._ebean_getIntercept();
    assertThat(intercept).isInstanceOf(InterceptReadOnly.class);
    assertThat(customer.getOrders()).isSameAs(Collections.EMPTY_LIST);
    assertThat(customer.getContacts()).isSameAs(Collections.EMPTY_LIST);
  }

  @Test
  public void orderById_whenTrue_expectOrderById() {

    Query<Customer> query = DB.find(Customer.class)
      .select("id,name")
      .setFirstRow(1)
      .setMaxRows(5)
      .orderById(true);

    query.findList();
    if (isSqlServer() || isDb2()) {
      assertSql(query).isEqualTo("select t0.id, t0.name from o_customer t0 order by t0.id offset 1 rows fetch next 5 rows only");
    } else if (!isOracle()) {
      assertSql(query).isEqualTo("select t0.id, t0.name from o_customer t0 order by t0.id limit 5 offset 1");
    }
  }
}
