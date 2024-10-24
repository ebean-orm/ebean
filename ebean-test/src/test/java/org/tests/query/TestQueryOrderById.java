package org.tests.query;

import io.ebean.bean.BeanCollection;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadOnly;
import io.ebean.xtest.BaseTestCase;
import io.ebean.DB;
import io.ebean.Query;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.ResetBasicData;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestQueryOrderById extends BaseTestCase {

  @Test
  void immutableResult() {
    ResetBasicData.reset();

    List<Customer> result = DB.find(Customer.class)
      .setReadOnly(true).setDisableLazyLoading(true)
      .select("id,name")
      .fetch("contacts")
      .findList();

    assertThat(result).isNotEmpty();
    assertThatThrownBy(() -> result.add(new Customer()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ReadOnly");

    List<Contact> contacts = result.get(0).getContacts();
    assertThat(contacts).isNotEmpty();
    assertThat(contacts).isInstanceOf(BeanCollection.class);
    BeanCollection<?> bc = (BeanCollection<?>)contacts;
    assertThat(bc.isReadOnly()).isTrue();
    assertThatThrownBy(() -> contacts.add(new Contact()))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ReadOnly");
  }

  @Test
  void orderById_default_expectNotOrderById() {
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
    int pos = intercept.findProperty("billingAddress");
    assertThat(intercept.isLoadedProperty(pos)).isFalse();
    assertThat(customer.getBillingAddress())
      .describedAs("Not loaded property returns null")
      .isNull();

    assertThatThrownBy(() -> customer.setBillingAddress(new Address()))
      .describedAs("Not allowed to mutate a readOnly bean")
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("ReadOnly");

    intercept.errorOnLazyLoad(true);

    assertThatThrownBy(customer::getBillingAddress)
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Property not loaded: billingAddress");

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
