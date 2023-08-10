package org.tests.text.json;

import com.fasterxml.jackson.core.JsonParser;
import io.ebean.BeanMergeOptions;
import io.ebean.BeanState;
import io.ebean.DB;
import io.ebean.ValuePair;
import io.ebean.test.LoggedSql;
import io.ebean.text.json.JsonReadOptions;
import io.ebean.xtest.BaseTestCase;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.api.json.SpiJsonReader;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.json.ReadJson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestJsonBeanDescriptorParse extends BaseTestCase {

  @BeforeAll
  static void before() {
    ResetBasicData.reset();
  }

  @BeforeEach
  void setup() {
    Customer customer = new Customer();
    customer.setName("Hello Roland");
    customer.setId(234);
    Address address = new Address();
    address.setId(234);
    address.setLine1("foo");
    DB.save(address);
    customer.setBillingAddress(address);

    Contact alfred = new Contact("Alfred", "P");
    alfred.setId(789); // set some deterministic id
    ContactNote note = new ContactNote("Drinks", "Order 100l beer");
    note.setId(890);
    alfred.getNotes().add(note);
    customer.getContacts().add(alfred);
    Contact anton = new Contact("Anton", "P");
    anton.setId(790);
    anton.getNotes().add(new ContactNote("Equipment", "Organize barbecue"));
    customer.getContacts().add(anton);
    DB.save(customer);
  }

  @AfterEach
  void teardown() {
    DB.delete(Customer.class, 234);
    DB.delete(UTMaster.class, 1);
  }

  @Test
  public void testJsonRead() throws IOException {
    SpiEbeanServer server = (SpiEbeanServer) DB.getDefault();
    BeanDescriptor<Customer> descriptor = server.descriptor(Customer.class);

    SpiJsonReader readJson = createRead(server, descriptor);
    Customer customer = descriptor.jsonRead(readJson, null);

    assertEquals(Integer.valueOf(123), customer.getId());
    assertEquals("Hello Rob", customer.getName());

    BeanState beanState = DB.beanState(customer);
    Set<String> loadedProps = beanState.loadedProps();
    assertEquals(2, loadedProps.size());
    assertTrue(loadedProps.contains("id"));
    assertTrue(loadedProps.contains("name"));
    assertThat(beanState.dirtyValues()).isEmpty();
  }

  @Test
  public void testJsonManyUpdate() {

    Customer customer = DB.find(Customer.class, 234);
    String json =
      "{\"contacts\": [ "
        + "  {\"id\": 789, \"lastName\": \"Praml\", \"notes\": ["
        + "    {\"id\": 890,\"title\": \"Beer\",\"note\": \"Order 200l beer\",\"version\": 17},"
        + "    {\"title\": \"Food\",\"note\": \"Order 20 steaks\"}"
        + "  ]},"
        + "  {\"id\": 790, \"firstName\": \"Anton\",  \"lastName\": null, \"notes\" : null } "
        + "]}";

    BeanMergeOptions opts = new BeanMergeOptions();
    // example for a merge handler
    //    opts.setMergeHandler((a, b, c, d) -> {
    //      System.out.println("Merge " + d + "." +c);
    //      return true;
    //    });
    Customer jsonBean = DB.json().toBean(Customer.class, json);
    DB.getDefault().mergeBeans(jsonBean, customer, null);
    DB.save(customer);

    customer = DB.find(Customer.class, 234);
    assertThat(customer.getContacts()).hasSize(2);
    customer.getContacts().sort(Comparator.comparing(Contact::getId));

    Contact contact = customer.getContacts().get(0);
    assertThat(contact.getFirstName()).isEqualTo("Alfred");
    assertThat(contact.getLastName()).isEqualTo("Praml");
    assertThat(contact.getNotes()).hasSize(2).extracting(ContactNote::getNote)
      .containsExactlyInAnyOrder("Order 200l beer", "Order 20 steaks");

    contact = customer.getContacts().get(1);
    assertThat(contact.getFirstName()).isEqualTo("Anton");
    assertThat(contact.getLastName()).isEqualTo(null);
    assertThat(contact.getNotes()).hasSize(1);

  }

  @Test
  public void testJsonUpdate() {
    Customer customer = DB.find(Customer.class, 234);
    DB.json().toBean(customer, "{}");
    assertFalse(DB.beanState(customer).isDirty());

    assertEquals("Hello Roland", customer.getName());
    DB.json().toBean(customer, "{\"name\":\"Hello Roland\"}");
    assertFalse(DB.beanState(customer).isDirty());

    DB.json().toBean(customer, "{\"name\":\"Hello Rob\"}");
    assertEquals("Hello Rob", customer.getName());
    assertThat(DB.beanState(customer).changedProps()).containsExactly("name");
    assertTrue(DB.beanState(customer).isDirty());
    DB.json().toBean(customer, "{\"name\":null}");
    assertEquals(null, customer.getName());

    assertEquals("foo", customer.getBillingAddress().getLine1());
    JsonReadOptions opts = new JsonReadOptions();
    opts.setEnableLazyLoading(true);

    Customer jsonBean = DB.json().toBean(Customer.class, "{\"billingAddress\":{\"line1\":\"foo\"}}", opts);
    DB.getDefault().mergeBeans(jsonBean, customer, null);
    assertFalse(DB.beanState(customer.getBillingAddress()).isDirty());

    jsonBean = DB.json().toBean(Customer.class, "{\"billingAddress\":{\"line1\":\"bar\"}}", opts);
    DB.getDefault().mergeBeans(jsonBean, customer, null);
    assertEquals("bar", customer.getBillingAddress().getLine1());
    assertTrue(DB.beanState(customer.getBillingAddress()).isDirty());

    DB.json().toBean(customer, "{\"contacts\":[{\"firstName\":\"Noemi\"},{\"firstName\":\"Roland\"}]}");
    assertThat(customer.getContacts()).hasSize(2);
    // must not change contacts
    DB.json().toBean(customer, "{}");
    assertThat(customer.getContacts()).hasSize(2);
    DB.json().toBean(customer, "{\"contacts\":[]}");
    assertThat(customer.getContacts()).hasSize(0);


/*
    customer.setName("Hello Roland");
    customer.setId(234);
    Address address = new Address();
    address.setLine1("foo");
    DB.save(address);
    customer.setBillingAddress(address);
    DB.save(customer);


    readJson = createRead(server, descriptor);
    descriptor.jsonRead(readJson, null, customer);
    assertEquals(Integer.valueOf(123), customer.getId());
    assertEquals("Hello Rob", customer.getName());
    assertEquals(123, customer.getId());


    // now check, if toJson is routed over intercept for existing models
    customer = DB.find(Customer.class, 234);
    DB.json().toBean(customer, "{}");
    assertFalse(DB.beanState(customer).isDirty());

    assertEquals("Hello Roland", customer.getName());
    DB.json().toBean(customer, "{\"name\":\"Hello Roland\"}");
    assertFalse(DB.beanState(customer).isDirty());


    DB.json().toBean(customer, "{\"name\":\"Hello Rob\"}");
    assertEquals("Hello Roland", customer.getName());
    assertThat(DB.beanState(customer).changedProps()).containsExactly("name");
    assertTrue(DB.beanState(customer).isDirty());
    DB.json().toBean(customer, "{\"name\":null}");
    assertEquals(null, customer.getName());

    assertEquals("foo", customer.getBillingAddress().getLine2());
    DB.json().toBean(customer, "{\"billingAdress\":{\"line2\":\"foo\"}}");
    assertEquals("foo", customer.getBillingAddress().getLine2());
    DB.delete(customer); // cleanup*/
  }

  @Test
  public void testJsonLazyRead() throws IOException {
    JsonReadOptions opts = new JsonReadOptions();
    opts.setEnableLazyLoading(true);
    Customer customer = DB.json().toBean(Customer.class, "{\"id\": 234}", opts);
    assertThat(customer.getBillingAddress().getLine1()).isEqualTo("foo");

  }

  @Test
  public void testJsonCollectionUnpopulated() throws IOException {

    Customer customer = DB.json().toBean(Customer.class, "{\"id\": 234, \"name\" : \"Roland\"}");

    assertThat(customer.getBillingAddress()).isNull();
    List<Contact> contacts = customer.getContacts();

    assertThat(contacts).isEmpty();
    ;

  }

  @Test
  public void testJsonUpdateManyToOne() throws IOException {
    Customer customer = DB.find(Customer.class, 234);
    assertThat(customer.getBillingAddress().getLine1()).isEqualTo("foo");

    Address address = new Address();
    address.setId(987);
    address.setLine1("bar");
    DB.save(address);
    customer.setBillingAddress(address);
    DB.save(customer);

    assertThat(customer.getBillingAddress().getLine1()).isEqualTo("bar");
    JsonReadOptions opts = new JsonReadOptions();

    LoggedSql.start();
    Customer jsonBean = DB.json().toBean(Customer.class, "{\"billingAddress\":{\"id\": 234, \"line1\" : \"bar\"}}", opts);
    DB.getDefault().mergeBeans(jsonBean, customer, null);
    assertThat(LoggedSql.stop()).isEmpty();

    Map<String, ValuePair> dirty = DB.beanState(customer.getBillingAddress()).dirtyValues();
    assertThat(dirty).containsKeys("line1").hasSize(1);
    assertThat(customer.getBillingAddress().getLine1()).isEqualTo("bar");
    assertThat(dirty.get("line1").getOldValue()).isEqualTo("foo");

  }

  @Test
  public void testJsonUpdateWithDbJson() {
    UTMaster master = new UTMaster("m0");
    master.setId(1);
    master.setJournal(new UTMaster.Journal());
    master.getJournal().addEntry();
    master.getJournal().addEntry();
    DB.save(master);

    DB.json().toBean(master, "{\"id\":1,\"name\":\"newName\",\"description\":\"master\",\"journal\":{\"entries\":[\"newEntry\"]},\"details\":[],\"version\":1}");

    assertThat(master.getName()).isEqualTo("newName");
    assertThat(master.getDescription()).isEqualTo("master");
    assertThat(master.getJournal().getEntries()).hasSize(1);

    DB.json().toBean(master, "{\"id\":1,\"name\":\"name\",\"journal\":{}}");

    assertThat(master.getName()).isEqualTo("name");
    assertThat(master.getDescription()).isEqualTo("master");
    assertThat(master.getJournal().getEntries()).hasSize(0);

    DB.json().toBean(master, "{\"id\":1,\"name\":\"newName\",\"description\":\"master\",\"journal\":{\"entries\":[\"newEntry\"]},\"details\":[],\"version\":1}");

    assertThat(master.getName()).isEqualTo("newName");
    assertThat(master.getDescription()).isEqualTo("master");
    assertThat(master.getJournal().getEntries()).hasSize(1);


  }

  private SpiJsonReader createRead(SpiEbeanServer server, BeanDescriptor<Customer> descriptor) {
    StringReader reader = new StringReader("{\"id\":123,\"name\":\"Hello Rob\"}");
    JsonParser parser = server.json().createParser(reader);

    return new ReadJson(descriptor, parser, null, null);
  }

}
