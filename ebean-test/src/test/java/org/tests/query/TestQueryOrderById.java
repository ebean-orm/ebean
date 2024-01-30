package org.tests.query;

import io.ebean.Transaction;
import io.ebean.bean.FrozenBeans;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadOnly;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestQueryOrderById extends BaseTestCase {


  private Set<Integer> cachedCustomerIds;

  @Test
  void cachedBeanContext_attach() {
    ResetBasicData.reset();

    FrozenBeans cachedBeanContext = testSerialiseCachedBeans(buildCachedBeans());

    testSerialiseCustomer(cachedCustomers.get(0));

    try (Transaction txn = DB.beginTransaction()) {

      txn.attach(cachedBeanContext);

      List<Order> orders = DB.find(Order.class)
        .findList();

      for (Order order : orders) {
        Customer customer = order.getCustomer();
        if (cachedCustomerIds.contains(customer.getId())) {
          // Illegal to access customer.orders as that was not loaded and no lazy loading is allowed
          // List<Order> orders1 = customer.getOrders();
          // assertThat(orders1).isEmpty();
          // assertThat(orders1).isSameAs(Collections.EMPTY_LIST);

          List<Contact> contacts = customer.getContacts();
          assertThat(contacts).isNotNull();

          // customer.setContacts(new ArrayList<>());
          // customer.setName("modified");
          Address billingAddress = customer.getBillingAddress();
          if (billingAddress != null) {
            String line1 = billingAddress.getLine1();
            Country country = billingAddress.getCountry();
          }
          Address shippingAddress = customer.getShippingAddress();
//          if (shippingAddress != null) {
//            // shippingAddress.getCountry();
//          }
        }
      }
    }
  }

  private static FrozenBeans testSerialiseCachedBeans(FrozenBeans source) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(source);
      oos.close();

      byte[] byteArray = os.toByteArray();

      var is = new ByteArrayInputStream(byteArray);
      ObjectInputStream ois = new ObjectInputStream(is);
      FrozenBeans result = (FrozenBeans)ois.readObject();
      assertThat(result).isNotNull();

      return result;

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void testSerialiseCustomer(Customer source) {
    try {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(source);
      oos.close();

      byte[] byteArray = os.toByteArray();

      var is = new ByteArrayInputStream(byteArray);
      ObjectInputStream ois = new ObjectInputStream(is);
      Customer customer = (Customer)ois.readObject();

      assertThat(customer.getId()).isEqualTo(source.getId());
      assertThat(customer.getName()).isEqualTo(source.getName());

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private List<Customer> cachedCustomers;

  private FrozenBeans buildCachedBeans() {
    try (Transaction txn = DB.beginTransaction()) {

      List<Customer> list = DB.find(Customer.class)
        .setReadOnly(true)
        //.setDisableLazyLoading(true)
        .orderBy("id")
        .setMaxRows(3)
        .findList();

      // perform some lazy loading if we desire
      for (Customer customer : list) {
        customer.getContacts().size();
        Address billingAddress = customer.getBillingAddress();
        if (billingAddress != null) {
          Country country = billingAddress.getCountry();
          if (country != null) {
            country.getName();
          }
        }
      }

      // can selectively use DisableLazyLoad which is "lazy loading is NO-operation"
      //for (Customer customer : list) {
      //  DB.beanState(customer).setDisableLazyLoad(true);
      //}

      cachedCustomerIds = list.stream()
        .map(BasicDomain::getId)
        .collect(Collectors.toSet());

      cachedCustomers = list;
      return txn.freezeAndDetach();
    }
  }

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
