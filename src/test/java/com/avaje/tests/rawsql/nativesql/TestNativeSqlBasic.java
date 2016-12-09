package com.avaje.tests.rawsql.nativesql;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.BeanState;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.Query;
import com.avaje.tests.model.basic.Customer;
import com.avaje.tests.model.basic.Order;
import com.avaje.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNativeSqlBasic extends BaseTestCase {

  @Test
  public void noBindParams() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    String nativeSql = "select id, name from o_customer";

    Query<Customer> query = server.findNative(Customer.class, nativeSql);

    List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();

    BeanState beanState = server.getBeanState(customers.get(0));
    assertThat(beanState.getLoadedProps()).contains("id", "name");
  }

  @Test
  public void selectAll() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    String nativeSql = "select * from o_customer";
    Query<Customer> query = server.findNative(Customer.class, nativeSql);

    List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();

    BeanState beanState = server.getBeanState(customers.get(0));
    assertThat(beanState.getLoadedProps().size()).isGreaterThan(10);
  }

  @Test
  public void selectWithAssocOne() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    String nativeSql = "select c.*, b.city from o_customer c join o_address b on b.id = c.billing_address_id";
    Query<Customer> query = server.findNative(Customer.class, nativeSql);

    List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();
  }

  @Test
  public void namedParam() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    String nativeSql = "select id, name from o_customer where id > :some";

    Query<Customer> query = server.findNative(Customer.class, nativeSql);
    query.setParameter("some", 1);

    List<Customer> customers = query.findList();
    assertThat(customers).isNotEmpty();

    Query<Customer> query2 = server.findNative(Customer.class, nativeSql);
    query2.setParameter("some", 2);

    List<Customer> customers2 = query2.findList();
    assertThat(customers2).isNotEmpty();
  }

  @Test
  public void partialAndLazyLoad() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    String nativeSql = "select id, name from o_customer where id > ?";

    List<Customer> customers = server.findNative(Customer.class, nativeSql)
      .setParameter(1, 1)
      .findList();

    for (Customer customer : customers) {
      customer.getStatus();
    }
  }

  @Test
  public void partialAssoc() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    String nativeSql = "select o.id, o.status, c.id, c.name, c.version from o_order o join o_customer c on c.id = o.kcustomer_id ";

    List<Order> orders = server.findNative(Order.class, nativeSql)
      .findList();

    for (Order order : orders) {
      order.getStatus();
      order.getCustomer().getName();
    }
  }

  @Test
  public void fetchQuery() {

    ResetBasicData.reset();

    String nativeSql = "select * from o_customer where id > ?";
    List<Customer> customers =
        Ebean.findNative(Customer.class, nativeSql)
        .setParameter(1, 1)
        .fetchQuery("contacts")
        .findList();

    assertThat(customers).isNotEmpty();

    BeanState beanState = Ebean.getBeanState(customers.get(0));
    assertThat(beanState.getLoadedProps().size()).isGreaterThan(10);
  }

  @Test
  public void columnAlias() {

    ResetBasicData.reset();

    String nativeSql = "select c.id, 'foo' as name from o_customer c";
    List<Customer> customers =
      Ebean.findNative(Customer.class, nativeSql)
        .findList();

    assertThat(customers).isNotEmpty();

    BeanState beanState = Ebean.getBeanState(customers.get(0));
    assertThat(beanState.getLoadedProps()).contains("id", "name");
  }
}
