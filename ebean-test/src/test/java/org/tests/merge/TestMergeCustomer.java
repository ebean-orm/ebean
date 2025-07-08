package org.tests.merge;

import io.ebean.*;
import io.ebean.test.LoggedSql;
import io.ebean.text.PathProperties;
import io.ebean.xtest.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMergeCustomer extends BaseTestCase {

  private Random random = new Random();

  @Test
  public void customerOnly_defaultOptions_expect_updateOnly() {

    MCustomer mCustomer = partial("cust1", "(id,name,version)");
    mCustomer.setName("NotCust0");

    LoggedSql.start();

    DB.merge(mCustomer);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update mcustomer set name=?, version=? where id=? and version=?");
  }

  /**
   * So this is effectively the same as a stateless update.
   */
  @Test
  public void customerOnly_expect_updateOnly() {


    MCustomer mCustomer = partial("cust1", "(id,name,version)");
    mCustomer.setName("NotCust1");

    MergeOptions options = new MergeOptionsBuilder().build();


    LoggedSql.start();

    DB.merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertSql(sql.get(0)).contains("update mcustomer set name=?, version=? where id=? and version=?");
  }

  @Test
  public void customerOnly_setClientGeneratedIds_expect_selectAndUpdate() {


    MCustomer mCustomer = partial("cust2", "(id,name,version)");
    mCustomer.setName("NotCust2");

    MergeOptions options = new MergeOptionsBuilder().setClientGeneratedIds().build();

    LoggedSql.start();

    server().merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertSql(sql.get(0)).contains("select t0.id from mcustomer t0 where t0.id = ?");
    assertSql(sql.get(1)).contains("update mcustomer set name=?, version=? where id=? and version=?");
  }

  @Test
  public void customerWithAddresses_setClientGeneratedIds_expect_selectAndUpdate() {

    MCustomer mCustomer = partial("cust3", "(id,name,version,shippingAddress(*),billingAddress(*))");
    mCustomer.setName("NotCust3");
    mCustomer.getBillingAddress().setStreet("modBillStreet");
    mCustomer.getShippingAddress().setCity("modShipCity");

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("shippingAddress")
      .addPath("billingAddress")
      .setClientGeneratedIds()
      .build();

    LoggedSql.start();

    server().merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(6);
    assertSql(sql.get(0)).contains("select t0.id, t0.billing_address_id, t0.shipping_address_id from mcustomer t0 where t0.id = ?");
    assertSql(sql.get(1)).contains("update maddress set street=?, city=?, version=? where id=? and version=?");
    assertSqlBind(sql, 2, 3);
    assertThat(sql.get(5)).contains("update mcustomer set name=?, version=?, shipping_address_id=?, billing_address_id=? where id=? and version=?");
  }

  @Test
  public void customerWithAddresses_newAddress_setClientGeneratedIds_expect_insertAddress() {


    MCustomer mCustomer = partial("cust3", "(id,name,version,shippingAddress(*),billingAddress(*))");
    mCustomer.setName("NotCust3");

    // new billing address - no Id value so must be insert
    mCustomer.setBillingAddress(new MAddress("Short", "Mid Wicket"));
    mCustomer.getShippingAddress().setCity("modShipCity");

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("shippingAddress")
      .addPath("billingAddress")
      .setClientGeneratedIds()
      .build();

    LoggedSql.start();

    server().merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(8);
    assertSql(sql.get(0)).contains("select t0.id, t0.billing_address_id, t0.shipping_address_id from mcustomer t0 where t0.id = ?");
    assertSql(sql.get(1)).contains("insert into maddress (id, street, city, version) values (?,?,?,?)");
    assertSqlBind(sql.get(2));
    assertThat(sql.get(4)).contains("update maddress set street=?, city=?, version=? where id=? and version=?");
    assertSqlBind(sql.get(5));
    assertThat(sql.get(7)).contains("update mcustomer set name=?, version=?, shipping_address_id=?, billing_address_id=? where id=? and version=?");
  }

  @Test
  public void customerWithAddresses_newAddressWithId_setClientGeneratedIds_expect_additionalCheckForAddressInsert() {


    MCustomer mCustomer = partial("cust3", "(id,name,version,shippingAddress(*),billingAddress(*))");
    mCustomer.setName("NotCust3");

    // new billing address - has Id value + setClientGeneratedIds ... so extra query to check
    MAddress mAddress = new MAddress("Short", "Mid Wicket");
    mAddress.setId(UUID.randomUUID());
    mCustomer.setBillingAddress(mAddress);
    mCustomer.getShippingAddress().setCity("modShipCity");

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("shippingAddress")
      .addPath("billingAddress")
      .setClientGeneratedIds() // As we are using clientIds ... we don't know if the new UUID is an insert or update without checking
      .build();

    LoggedSql.start();

    server().merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(9);
    assertSql(sql.get(0)).contains("select t0.id, t0.billing_address_id, t0.shipping_address_id from mcustomer t0 where t0.id = ?");

    // Additional check to see if the address with the unknown UUID is 'insert' or 'update'
    assertSql(sql.get(1)).contains("select t0.id from maddress t0 where t0.id = ?");
    assertSql(sql.get(2)).contains("insert into maddress (id, street, city, version) values (?,?,?,?)");
    assertSqlBind(sql.get(3));
    assertThat(sql.get(5)).contains("update maddress set street=?, city=?, version=? where id=? and version=?");
    assertSqlBind(sql.get(6));
    assertThat(sql.get(8)).contains("update mcustomer set name=?, version=?, shipping_address_id=?, billing_address_id=? where id=? and version=?");
  }

  @Test
  public void assocOne_onlineIsNull() {

    MCustomer c = new MCustomer("Null Address");
    c.setBillingAddress(new MAddress("Cow corner", "Mid Wicket"));

    DB.save(c);

    MCustomer mCustomer = rebuildViaJson(c);

    MAddress mAddress = new MAddress("Silly", "Mid Wicket");
    mAddress.setId(UUID.randomUUID());
    mCustomer.setShippingAddress(mAddress);

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("shippingAddress")
      .addPath("billingAddress")
      .setClientGeneratedIds()
      .build();

    DB.merge(mCustomer, options);

  }

  private MCustomer rebuildViaJson(MCustomer input) {
    String asJson = DB.json().toJson(input);
    return DB.json().toBean(MCustomer.class, asJson);
  }

  @Test
  public void whenContacts_isNull_expect_deleteContacts() {

    MCustomer mCustomer = partial("cust6", "(id,name,version,shippingAddress(id),billingAddress(id),contacts(*))");

    // null contacts ... but in path so this means - delete all contacts
    mCustomer.setContacts(null);

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("contacts")
      .build();

    LoggedSql.start();
    server().merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(26);
    }
    assertSql(sql.get(0)).contains("select t0.id, t1.id from mcustomer t0 left join mcontact t1 on t1.customer_id = t0.id where t0.id = ?");
    assertSql(sql.get(1)).contains("delete from mcontact_message where contact_id = ?");
    assertThat(sql.get(4)).contains("delete from mcontact where id=?");
    assertThat(sql.get(25)).contains("update mcustomer set name=?, version=?, shipping_address_id=?, billing_address_id=? where id=? and version=?");
  }


  @Test
  public void whenContacts_isEmpty_expect_deleteContacts() {

    MCustomer mCustomer = partial("cust6", "(id,name,version,shippingAddress(id),billingAddress(id),contacts(*))");

    // empty contacts ... but in path so this means - delete all contacts
    mCustomer.setContacts(new ArrayList<>());

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("contacts")
      .build();

    LoggedSql.start();
    server().merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(26);
    }
    assertSql(sql.get(0)).contains("select t0.id, t1.id from mcustomer t0 left join mcontact t1 on t1.customer_id = t0.id where t0.id = ?");
    assertSql(sql.get(1)).contains("delete from mcontact_message where contact_id = ?");
    assertThat(sql.get(4)).contains("delete from mcontact where id=?");
    assertThat(sql.get(25)).contains("update mcustomer set name=?, version=?, shipping_address_id=?, billing_address_id=? where id=? and version=?");
  }

  @Test
  public void whenContacts_mixed_expect_deleteInsertUpdateContacts() {

    MCustomer mCustomer = partial("cust6", "(id,name,version,shippingAddress(id),billingAddress(id),contacts(*))");
    List<MContact> contacts = mCustomer.getContacts();

    MContact mContact = new MContact("z@a.com", "z", "zed");
    mContact.setId(UUID.randomUUID());

    contacts.add(mContact);

    contacts.get(0).setEmail("a@beta.com");
    contacts.get(1).setEmail("b@beta.com");
    contacts.remove(4);
    contacts.remove(2);

    MContact mContactEnd = new MContact("z@z.com", "zx", "zedXtra");
    mContactEnd.setId(UUID.randomUUID());
    contacts.add(mContactEnd);


    MergeOptions options = new MergeOptionsBuilder()
      .addPath("contacts")
      .build();

    LoggedSql.start();
    server().merge(mCustomer, options);

    List<String> sql = LoggedSql.stop();
    if (isPersistBatchOnCascade()) {
      assertThat(sql).hasSize(20);
      assertSql(sql.get(0)).contains("select t0.id, t1.id from mcustomer t0 left join mcontact t1 on t1.customer_id = t0.id where t0.id = ?");
      assertSql(sql.get(1)).contains("delete from mcontact_message where contact_id = ?");
      assertThat(sql.get(4)).contains("delete from mcontact where id=?");
      assertThat(sql.get(9)).contains("update mcustomer set name=?, version=?, shipping_address_id=?, billing_address_id=? where id=? and version=?");
    }

    if (isPersistBatchOnCascade()) {
      assertThat(sql.get(10)).contains("insert into mcontact");
      assertThat(sql.get(11)).contains("-- bind(");
      assertThat(sql.get(14)).contains("update mcontact set email=?, first_name=?, last_name=?, version=?, customer_id=? where id=? and version=?");
    } else {
      assertThat(sql.get(6)).contains("update mcontact set email=?, first_name=?, last_name=?, version=?, customer_id=? where id=? and version=?");
      assertThat(sql.get(7)).contains("update mcontact set email=?, first_name=?, last_name=?, version=?, customer_id=? where id=? and version=?");
      assertThat(sql.get(10)).contains("insert into mcontact");
      assertThat(sql.get(11)).contains("insert into mcontact");
    }
  }

  @Test
  public void fullMonty() {

    MCustomer cust1 = customer("monty1");

    modify(cust1);

    MergeOptions options = new MergeOptionsBuilder()
      .addPath("billingAddress")
      .addPath("shippingAddress")
      .addPath("contacts")
      .addPath("contacts.messages")
      .setClientGeneratedIds()
      .setDeletePermanent()
      .build();

    LoggedSql.start();
    server().merge(cust1, options);

    List<String> sql = LoggedSql.stop();
    if (isPersistBatchOnCascade()) {

      assertSql(sql.get(0)).contains("select t0.id, t0.shipping_address_id, t0.billing_address_id, t1.id from mcustomer t0 left join mcontact t1 on t1.customer_id = t0.id where t0.id = ? order by t0.id");
      if (isH2() || isHana()) {
        // with nested OneToMany .. we need a second query to read the contact message ids
        assertSql(sql.get(1)).contains("select t0.contact_id, t0.id from mcontact_message t0 where (t0.contact_id) in (?,?,?,?,?,?,?,?,?,?)");
      }
      assertSql(sql.get(2)).contains("delete from mcontact_message where contact_id = ?");
      assertThat(sql.get(5)).contains("delete from mcontact where id=?");
      assertThat(sql.get(6)).contains("delete from mcontact_message where contact_id = ?");
      assertThat(sql.get(9)).contains("delete from mcontact where id=?");

      assertThat(sql.get(10)).contains("update maddress set street=?, city=?, version=? where id=? and version=?");
      assertThat(sql.get(17)).contains("update mcontact set email=?, first_name=?, last_name=?, version=?, customer_id=? where id=? and version=?");
      assertSqlBind(sql, 18, 21);
      assertThat(sql.get(23)).contains("update mcontact_message set title=?, subject=?, notes=?, version=?, contact_id=? where id=? and version=?");
    }
  }

  private void modify(MCustomer cust) {

    List<MContact> contacts = cust.getContacts();
    MContact mContact = new MContact("z@a.com", "z", "zed");
    mContact.setId(UUID.randomUUID());

    contacts.add(mContact);

    contacts.get(0).setEmail("a@beta.com");
    contacts.get(1).setEmail("b@beta.com");
    contacts.remove(4);
    contacts.remove(2);

    cust.setName(cust.getName()+" modified");
    cust.getBillingAddress().setStreet("Short");

    MAddress mAddress = new MAddress("Broken", "Dreams");
    mAddress.setId(UUID.randomUUID());
    cust.setShippingAddress(null);
  }

  private MCustomer partial(String name, String fetchGraph) {
    MCustomer cust1 = buildCustomer(name);
    DB.save(cust1);

    FetchPath fetchPath = PathProperties.parse(fetchGraph);
    String asJson = DB.json().toJson(cust1, fetchPath);
    return DB.json().toBean(MCustomer.class, asJson);
  }

  private MCustomer customer(String name) {

    MCustomer cust1 = buildCustomer(name);
    DB.save(cust1);

    String asJson = DB.json().toJson(cust1);

    return DB.json().toBean(MCustomer.class, asJson);
  }

  private MCustomer buildCustomer(String name) {

    MCustomer c = new MCustomer(name);
    c.setShippingAddress(new MAddress("Fleet st", "London"));
    c.setBillingAddress(new MAddress("Cow corner", "Mid Wicket"));

    c.getContacts().add(addContact("a@a.com", "a", "alligator"));
    c.getContacts().add(addContact("b@a.com", "b", "beaver"));
    c.getContacts().add(addContact("c@a.com", "c", "crow"));
    c.getContacts().add(addContact("d@a.com", "d", "dog"));
    c.getContacts().add(addContact("e@a.com", "e", "ent"));
    c.getContacts().add(addContact("f@a.com", "f", "frog"));

    return c;
  }

  private MContact addContact(String email, String first, String last) {
    MContact mContact = new MContact(email, first, last);
    int i = 1 + random.nextInt(2);
    for (int j = 0; j < i; j++) {
      mContact.getMessages().add(new MContactMessage(first+" "+i, last+" "+i));
    }
    return mContact;
  }
}
