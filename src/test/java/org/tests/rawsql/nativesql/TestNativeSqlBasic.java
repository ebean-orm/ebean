package org.tests.rawsql.nativesql;

import io.ebean.BaseTestCase;
import io.ebean.BeanState;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.basic.ResetBasicData;

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

  /**
   * Oracle does not support getTableName() via JDBC resultSet meta data
   */
  @Test
  @IgnorePlatform({Platform.SQLSERVER, Platform.ORACLE}) // does only work in 'cursor' mode!
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
  public void partialAssocIncludingOracle() {

    ResetBasicData.reset();

    EbeanServer server = Ebean.getDefaultServer();

    String nativeSql = "select o.id, o.status, o.kcustomer_id from o_order o";

    List<Order> orders = server.findNative(Order.class, nativeSql)
      .findList();

    LoggedSqlCollector.start();

    for (Order order : orders) {
      order.getStatus();
    }

    List<String> sql = LoggedSqlCollector.current();
    assertThat(sql).isEmpty();

    for (Order order : orders) {
      order.getCustomer().getName();
    }

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains(" from o_customer t0 where ");
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
