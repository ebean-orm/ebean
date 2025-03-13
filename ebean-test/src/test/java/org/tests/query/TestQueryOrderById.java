package org.tests.query;

import io.ebean.*;
import io.ebean.bean.EntityBean;
import io.ebean.bean.EntityBeanIntercept;
import io.ebean.bean.InterceptReadOnly;
import io.ebean.test.LoggedSql;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.io.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestQueryOrderById extends BaseTestCase {

  @Test
  void unmodifiableFindOne() {
    UTMaster newBean = new UTMaster("master");
    newBean.addDetail(new UTDetail("d0", 4, 10D));
    DB.save(newBean);

    UTMaster one = DB.find(UTMaster.class)
      .setId(newBean.getId())
      .fetch("details")
      .setUnmodifiable(true)
      .findOne();

    assertThatThrownBy(() -> one.setName("junk"))
      .isInstanceOf(UnmodifiableEntityException.class)
      .hasMessageContaining("Attempting to modify name");

    List<UTDetail> details = one.getDetails();
    assertThat(details).hasSize(1);
    assertThatThrownBy(() -> details.add(new UTDetail()))
      .isInstanceOf(UnsupportedOperationException.class);

    UTDetail utDetail = details.get(0);
    assertThatThrownBy(() -> utDetail.setQty(34))
      .isInstanceOf(UnmodifiableEntityException.class)
      .hasMessageContaining("Attempting to modify qty");

    DB.delete(one);
  }

  @Test
  void unmodifiableFindIteratorSimple() {
    unmodifiableFindIterator(1);
  }

  @Test
  void unmodifiableFindIteratorBatched() {
    unmodifiableFindIterator(100);
  }

  void unmodifiableFindIterator(int batchSize) {
    UTMaster newBean = new UTMaster("unmodifiableFindIterator");
    DB.save(newBean);

    try (QueryIterator<UTMaster> iterate = DB.find(UTMaster.class)
      .setUnmodifiable(true)
      .setLazyLoadBatchSize(batchSize)
      .where().eq("name", "unmodifiableFindIterator")
      .findIterate()) {

      while (iterate.hasNext()) {
        UTMaster bean = iterate.next();
        assertThatThrownBy(() -> bean.setName("junk"))
          .isInstanceOf(UnmodifiableEntityException.class)
          .hasMessageContaining("Attempting to modify name");
      }
    }

    DB.delete(newBean);
  }

  @Test
  void unmodifiableFindMany() throws IOException, ClassNotFoundException {
    ResetBasicData.reset();

    LoggedSql.start();
    List<Customer> result = DB.find(Customer.class)
      .setUnmodifiable(true)
      .select("id,name")
      .fetch("contacts")
      .fetch("contacts.notes")
      .orderBy("id")
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("from o_customer t0 left join contact t1");
    assertThat(sql.get(1)).contains("from contact_note t0 where");

    assertThat(result).isNotEmpty();
    assertThatThrownBy(() -> result.add(new Customer()))
      .isInstanceOf(UnsupportedOperationException.class);

    Customer customer = result.get(0);
    List<Contact> contacts = customer.getContacts();
    assertThat(contacts).isNotEmpty();
    assertThatThrownBy(() -> contacts.add(new Contact()))
      .isInstanceOf(UnsupportedOperationException.class);

    for (Contact contact : contacts) {
      Customer customer1 = contact.getCustomer();
      assertThat(customer1).isSameAs(customer);
      assertThatThrownBy(() -> contact.setFirstName("Attempting to Modify"))
        .isInstanceOf(UnmodifiableEntityException.class);;

      List<ContactNote> notes = contact.getNotes();
      assertThatThrownBy(() -> notes.add(new ContactNote("junk","junk")))
        .isInstanceOf(UnsupportedOperationException.class);
    }

    assertThatThrownBy(customer::getOrders)
      .isInstanceOf(UnloadedPropertyException.class)
      .hasMessageContaining("Property not loaded: orders");

    assertThatThrownBy(() -> customer.setName("Attempting to Modify"))
      .isInstanceOf(UnmodifiableEntityException.class)
      .hasMessageContaining("Attempting to modify name");

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(os);

    oos.writeObject(customer);
    oos.flush();
    oos.close();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(is);

    Customer read = (Customer) ois.readObject();
    assertThat(read.getName()).isEqualTo(customer.getName());
  }

  @Test
  void unmodifiableWithReference() {
    ResetBasicData.reset();

    List<Contact> result = DB.find(Contact.class)
      .setUnmodifiable(true)
      .findList();

    assertThat(result).isNotEmpty();
    for (Contact contact : result) {
      Customer customer = contact.getCustomer();
      assertThat(customer.getId()).isNotNull();
      assertThatThrownBy(() -> customer.setId(42))
        .isInstanceOf(UnsupportedOperationException.class);
    }
  }

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
      .isInstanceOf(UnsupportedOperationException.class);

    List<Contact> contacts = result.get(0).getContacts();
    assertThat(contacts).isNotEmpty();
    assertThatThrownBy(() -> contacts.add(new Contact()))
      .isInstanceOf(UnsupportedOperationException.class);
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
    assertThatThrownBy(() -> customer.getBillingAddress())
      .describedAs("Not loaded property returns null")
      .isInstanceOf(UnloadedPropertyException.class)
      .hasMessageContaining("Property not loaded: billingAddress");

    assertThatThrownBy(() -> customer.setBillingAddress(new Address()))
      .describedAs("Not allowed to mutate a readOnly bean")
      .isInstanceOf(UnloadedPropertyException.class)
      .hasMessageContaining("Property not loaded: billingAddress");

    assertThatThrownBy(customer::getBillingAddress)
      .isInstanceOf(UnloadedPropertyException.class)
      .hasMessageContaining("Property not loaded: billingAddress");

    assertThat(intercept).isInstanceOf(InterceptReadOnly.class);
    assertThatThrownBy(customer::getOrders)
      .isInstanceOf(UnloadedPropertyException.class)
      .hasMessageContaining("Property not loaded: orders");
    assertThatThrownBy(customer::getContacts)
      .isInstanceOf(UnloadedPropertyException.class)
      .hasMessageContaining("Property not loaded: contacts");
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
