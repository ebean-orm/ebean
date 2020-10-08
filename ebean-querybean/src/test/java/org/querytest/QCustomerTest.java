package org.querytest;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.PagedList;
import io.ebean.Query;
import io.ebean.QueryIterator;
import io.ebean.Transaction;
import io.ebean.annotation.Transactional;
import io.ebean.types.Inet;
import org.example.domain.ACat;
import org.example.domain.ADog;
import org.example.domain.Address;
import org.example.domain.Animal;
import org.example.domain.Country;
import org.example.domain.Customer;
import org.example.domain.otherpackage.PhoneNumber;
import org.example.domain.otherpackage.ValidEmail;
import org.example.domain.query.QAnimal;
import org.example.domain.query.QContact;
import org.example.domain.query.QCustomer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.domain.query.QAddress.Alias.country;
import static org.example.domain.query.QAddress.Alias.line1;
import static org.example.domain.query.QContact.Alias.lastName;
import static org.example.domain.query.QCustomer.Alias.billingAddress;

public class QCustomerTest {

  @Rule
  public final TestName testName = new TestName();

  @Test
  public void findWithTransaction() {

    final Database database = DB.getDefault();

    try (Transaction txn = database.createTransaction()) {
      Customer customer = new Customer();
      customer.setName("explicitTransaction");

      database.save(customer, txn);

      final Customer found = new QCustomer(txn)
        .name.eq("explicitTransaction")
        .findOne();
      assertThat(found).isNotNull();

      // not found using other transaction
      final Customer foundNot = new QCustomer()
        .name.eq("explicitTransaction")
        .findOne();
      assertThat(foundNot).isNull();

      txn.commit();
    }

  }

  @Test
  public void findSingleAttribute() {

    List<String> names = new QCustomer()
        .setDistinct(true)
        .select(QCustomer.alias().name)
        .status.equalTo(Customer.Status.BAD)
        .findSingleAttributeList();

    assertThat(names).isNotNull();
  }

  @Test
  public void findIterate() {

    Customer cust = new Customer();
    cust.setName("foo");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    List<Long> ids = new QCustomer()
        .status.equalTo(Customer.Status.GOOD)
        .findIds();

    assertThat(ids).isNotEmpty();


    Map<List, Customer> map = new QCustomer()
        .status.equalTo(Customer.Status.GOOD)
        .findMap();

    assertThat(map.size()).isEqualTo(ids.size());

    QueryIterator<Customer> iterate = new QCustomer()
        .status.equalTo(Customer.Status.GOOD)
        .findIterate();

    try {
      while (iterate.hasNext()) {
        Customer customer = iterate.next();
        assertThat(customer.getName()).isNotNull();
      }
    } finally {
      iterate.close();
    }
  }


  @Test
  public void isEmpty() {

    new QCustomer()
        .contacts.isEmpty()
        .findList();

    new QCustomer()
        .contacts.isNotEmpty()
        .findList();
  }

  @Transactional
  @Test
  public void forUpdate() {

    new QCustomer()
        .id.eq(42)
        .forUpdate()
        .findOne();

    new QCustomer()
        .id.eq(42)
        .forUpdateNoWait()
        .findOne();

    new QCustomer()
        .id.eq(42)
        .forUpdateSkipLocked()
        .findOne();
  }


  @Ignore
  @Test
  public void arrayContains() {

    new QContact()
        .phoneNumbers.contains("4312")
        .findList();

    new QCustomer()
        .contacts.phoneNumbers.contains("4312")
        .findList();
  }

  @Test
  public void setIncludeSoftDeletes() {

    new QCustomer()
        .setIdIn(42L)
        .setIncludeSoftDeletes()
        .findList();
  }

  @Test
  public void testIdIn() {

    new QCustomer()
        .setIdIn("1", "2")
        .findList();

    new QCustomer()
        .id.in(1L, 2L, 3L)
        .findList();
  }

  @Test
  public void testIn() {
    new QCustomer()
        .id.in(34L, 33L)
        .name.in("asd", "foo", "bar")
        .registered.in(new Date())
        .findList();
  }

  @Test
  public void usingTransaction() {

    try (Transaction transaction = DB.getDefault().createTransaction()) {

      new QCustomer()
        .registered.isNull()
        .usingTransaction(transaction)
        .findList();
    }
  }

  @Test
  public void usingConnection() throws SQLException {

    Customer cust = new Customer();
    cust.setName("usingConnection");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    DataSource dataSource = DB.getDefault().getPluginApi().getDataSource();

    try (Connection connection = dataSource.getConnection()) {

      List<Customer> foo = new QCustomer()
        .name.eq("usingConnection")
        .usingConnection(connection)
        .findList();

      assertThat(foo).hasSize(1);
    }
  }

  @Test
  public void testAssocOne() {

    Address address = new Address();
    address.setId(41L);

    new QCustomer()
      .billingAddress.eq(address)
      .findList();

    new QCustomer()
      .billingAddress.equalTo(address)
      .findList();

    new QCustomer()
      .billingAddress.ne(address)
      .findList();

    new QCustomer()
      .billingAddress.notEqualTo(address)
      .findList();
  }

  @Test
  public void testInOrEmpty() {

    List<String> names = Arrays.asList("asd", "foo", "bar");

    new QCustomer()
      .registered.before(new Date())
      .name.inOrEmpty(names)
      .findList();

    new QCustomer()
      .registered.before(new Date())
      .name.inOrEmpty(null)
      .findList();

    names = Collections.emptyList();

    new QCustomer()
      .registered.before(new Date())
      .name.inOrEmpty(names)
      .findList();
  }

  @Test
  public void testNotIn() {
    new QCustomer()
        .id.isIn(34L, 33L)
        .name.notIn("asd", "foo", "bar")
        .registered.in(new Date())
        .findList();
  }

  @Test
  public void testQueryBoolean() {

    new QCustomer()
        .name.contains("rob")
        //.setUseDocStore(true)
        .setMaxRows(10)
        .findPagedList();

    new QCustomer()
        .inactive.isFalse()
        .findList();
  }

  @Test
  public void testFindOne() {

    new QCustomer()
      .name.isIn("rob", "foo")
      //.setUseDocStore(true)
      .setMaxRows(1)
      .findOne();

    Optional<Customer> maybe = new QCustomer()
      .name.contains("rob")
      //.setUseDocStore(true)
      .setMaxRows(1)
      .findOneOrEmpty();

    maybe.isPresent();

    new QCustomer()
      .inactive.isFalse()
      .findList();
  }

  private void insertCustomer(String name) {
    Customer cust = new Customer();
    cust.setName(name);
    cust.setStatus(Customer.Status.GOOD);
    cust.save();
  }

  @Test
  public void testFindStream() {
    insertCustomer("stream1");
    insertCustomer("stream2");

    StringJoiner sb = new StringJoiner("|");
    try (Stream<Customer> stream = new QCustomer()
      .name.startsWith("stream")
      .id.asc()
      .findStream()) {

      stream.forEach(it -> sb.add(it.getName()));
    }

    assertThat(sb.toString()).isEqualTo("stream1|stream2");
  }

  @Test
  public void testFindLargeStream() {
    insertCustomer("largeStream1");
    insertCustomer("largeStream2");
    insertCustomer("largeStream3");

    StringJoiner sb = new StringJoiner("|");
    try (Stream<Customer> stream = new QCustomer()
      .name.startsWith("largeStream")
      .id.asc()
      .findLargeStream()) {

      stream.forEach(it -> sb.add(it.getName()));
    }

    assertThat(sb.toString()).isEqualTo("largeStream1|largeStream2|largeStream3");
  }

  @Test
  public void testFilterMany() {

    Customer cust = new Customer();
    cust.setName("Postgres Foo");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    new QCustomer()
      .name.startsWith("Postgres")
      .contacts.filterMany("firstName istartsWith ?", "Rob")
      .findList();

    final LocalDate startDate = LocalDate.now().minusDays(7);
    final LocalDate endDate = LocalDate.now();

    new QCustomer()
      .name.startsWith("Postgres")
      .contacts.filterMany("whenCreated inRange ? to ?", startDate, endDate)
      .findList();
  }

  @Test
  public void testDate_lessThan() {

    assertContains(new QCustomer().registered.lt(new Date()).query(), " where t0.registered < ?");
    assertContains(new QCustomer().registered.before(new Date()).query(), " where t0.registered < ?");
    assertContains(new QCustomer().registered.lessThan(new Date()).query(), " where t0.registered < ?");
  }

  @Test
  public void testDate_lessOrEqualTo() {

    assertContains(new QCustomer().registered.le(new Date()).query(), " where t0.registered <= ?");
    assertContains(new QCustomer().registered.lessOrEqualTo(new Date()).query(), " where t0.registered <= ?");
  }

  @Test
  public void testDate_greaterThan() {

    assertContains(new QCustomer().registered.after(new Date()).query(), " where t0.registered > ?");
    assertContains(new QCustomer().registered.gt(new Date()).query(), " where t0.registered > ?");
    assertContains(new QCustomer().registered.greaterThan(new Date()).query(), " where t0.registered > ?");
  }


  @Test
  public void testDate_greaterOrEqualTo() {

    assertContains(new QCustomer().registered.ge(new Date()).query(), " where t0.registered >= ?");
    assertContains(new QCustomer().registered.greaterOrEqualTo(new Date()).query(), " where t0.registered >= ?");
  }

  private void assertContains(Query<Customer> query, String match) {
    query.findList();
    assertThat(query.getGeneratedSql()).contains(match);
  }

  @Test
  public void query_setAllowLoadErrors() {

    new QCustomer()
      .status.in(Customer.Status.GOOD)
      .setAllowLoadErrors()
      .findList();
  }

  @Test
  public void query_setInheritType() {

    ACat cat = new ACat("C1");
    cat.save();

    ACat cat2 = new ACat("C2");
    cat2.save();

    ADog dog = new ADog("D1", "D878");
    dog.save();

    List<Animal> animals = new QAnimal()
      .id.greaterOrEqualTo(1L)
      .setInheritType(ACat.class)
      .findList();

    System.out.println(animals);
  }

  @Test
  public void select_assocManyToOne() {

    Country nz = new Country("NZ", "New Zealand");
    DB.merge(nz);

    Address address = new Address();
    address.setLine1("42 below");
    address.setCountry(nz);

    Customer customer0 = new Customer();
    customer0.setName("asdBilling0");
    customer0.setBillingAddress(address);
    customer0.save();

    Customer customer1 = new Customer();
    customer1.setName("asdBilling1");
    customer1.setBillingAddress(new Address());
    customer1.save();

    List<Long> billingAddressIds
      = new QCustomer()
      .select(billingAddress)
      .name.startsWith("asdBilling")
      .findSingleAttributeList();

    assertThat(billingAddressIds).hasSize(2);


    Map<Long,Customer> map
      = new QCustomer()
      .billingAddress.id.asMapKey()
      .name.startsWith("asdBilling")
      .findMap();

    assertThat(map).hasSize(2);


    List<Customer> customers = new QCustomer()
      .billingAddress.fetch(line1, country)
      .findList();

    assertThat(customers).isNotEmpty();

  }

  @Test
  public void query_fetchString() {

    Customer cust = new Customer();
    cust.setName("baz");
    cust.setCurrentInet(new Inet("129.1.1.4"));
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    new QCustomer()
      .currentInet.eq(Inet.of("129.1.1.4"))
      .findList();

    new QCustomer()
      .currentInet.in(Inet.setOf("129.1.1.4","129.1.1.5"))
      .findList();

    new QCustomer()
      .contacts.fetch("email")
      .orderBy()
        .name.asc()
        .contacts.email.asc()
      .findList();

    new QCustomer()
      .contacts.fetchQuery("email")
      .orderBy()
        .name.asc()
        .contacts.email.asc()
      .findList();
  }

  @Test
  public void query_setBaseTable() {

    new QCustomer()
      .setBaseTable("BE_CUSTOMER")
      .findList();
  }

  @Test
  public void query_rawOrEmpty() {

    List<String> names = Arrays.asList("A", "B");

    new QCustomer()
      .rawOrEmpty("name in (?1)", names)
      .findList();

    new QCustomer()
      .rawOrEmpty("name in (?1)", null)
      .findList();

    new QCustomer()
      .rawOrEmpty("name in (?1)", new ArrayList<Long>())
      .findList();
  }

  @Test
  public void query_orNull() {

    new QCustomer()
      .name.equalToOrNull("A")
      .findList();

    new QCustomer()
      .name.eqOrNull("A")
      .findList();

    new QCustomer()
      .name.greaterThanOrNull("B")
      .findList();

    new QCustomer()
      .name.lessThanOrNull("C")
      .findList();

    new QCustomer()
      .name.gtOrNull("GT1")
      .name.geOrNull("GE1")
      .name.ltOrNull("LT1")
      .name.leOrNull("LE1")
      .findList();
  }

  @Test
  public void query_inRange() {

    new QCustomer()
      .name.inRange("A", "B")
      .findList();

    new QCustomer()
      .name.greaterOrEqualTo("A")
      .name.lessThan("B")
      .findList();

    new QContact()
      .firstName.inRangeWith(lastName, "B")
      .findList();
  }

  @Test
  public void query_exists() {

    boolean customerExists =
      new QCustomer()
      .name.equalTo("DoesNotExistReally")
      .exists();

    assertThat(customerExists).isFalse();
  }

  @Test
  public void testQuery() {

    QContact contact = QContact.alias();
    QCustomer cust = QCustomer.alias();

    new QCustomer()
        // tune query
        .select(cust.name)
        .status.isIn(Customer.Status.BAD, Customer.Status.BAD)
        .contacts.fetch()
        // predicates
        .findList();

    new QCustomer()
        // tune query
        .select(cust.name)
        .contacts.fetch()
        // predicates
        .findList();

    new QCustomer()
        // tune query
        .select(cust.id, cust.name)
        .contacts.fetch(contact.firstName, contact.lastName, contact.email)
        // predicates
        .id.greaterThan(1)
        .findList();

    PagedList<Customer> pagedList = new QCustomer()
        // tune query
        .select(cust.id, cust.name)
        .contacts.fetch(contact.firstName, contact.lastName, contact.email)
        // predicates
        .id.greaterThan(1)
        .setFirstRow(20)
        .setMaxRows(10)
        .findPagedList();

    pagedList.getList();
    pagedList.getList();

//    new QCustomer()
//        .asDraft()
//        .findList();
//
//    new QCustomer()
//        .includeSoftDeletes()
//        .findList();

//    List<Contact> contacts
//        = new QContact()
//        .email.like("asd")
//        .notes.title.like("asd")
//        .orderBy()
//          .id.desc()
//        .findList();
//

//    List<Customer> customers = new QCustomer()
//        .id.eq(1234)
//        .status.equalTo(Customer.Status.BAD)
//        .status.in(Customer.Status.GOOD, Customer.Status.MIDDLING)
//            //.status.eq(Order.Status.APPROVED)
//        .name.like("asd")
//        .name.istartsWith("ASdf")
//        .registered.after(new Date())
//        .contacts.email.endsWith("@foo.com")
//        .contacts.notes.id.greaterThan(123L)
//        .orderBy().id.asc()
//        .findList();

//    //Customer customer3 =
//    new QCustomer()
//        .id.gt(12)
//        .or()
//          .id.lt(1234)
//          .and()
//            .name.like("one")
//            .name.like("two")
//          .endAnd()
//        .endOr()
//        .orderBy().id.asc()
//        .findList();
//
//    //where t0.id > ?  and (t0.id < ?  or (t0.name like ?  and t0.name like ? ) )  order by t0.id; --bind(12,1234,one,two)
//
////    List<Customer> customers
////        = new QCustomer()
////          .name.like("asd")
////          .findList();
//
//    Customer.find.where()
//        .gt("id", 1234)
//        .disjunction().eq("id", 1234).like("name", "asd")
//        .endJunction().findList();

//    QCustomer cust = QCustomer.I;
//    ExpressionList<Customer> expr = new QCustomer().expr();
//    expr.eq(cust.contacts.email, 123);
  }

  @Test
  public void testFindSet() {

    Set<Customer> customerSet = new QCustomer().findSet();
    Assert.assertNotNull(customerSet);
  }

  @Test
  public void testFindMap() {

    Customer cust = new Customer();
    cust.setName("banana");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    Map<String, Customer> map = new QCustomer()
      .id.greaterOrEqualTo(1L)
      .name.asMapKey()
      .findMap();

    assertThat(map.get("banana")).isNotNull();

  }

  @Test
  public void testAsUpdate() {

    int rows = new QContact()
      .notes.note.startsWith("Make Inactive")
      .email.endsWith("@foo.com")
      .asUpdate()
      .setRaw("email = lower(email)")
      //.set("inactive", true)
      .update();

    assertThat(rows).isEqualTo(0);

  }


  @Test
  public void testSelectFormula() {

    Customer cust = new Customer();
    cust.setName("junk junk");
    cust.setStatus(Customer.Status.GOOD);
    cust.setRegistered(new Date());
    cust.save();

    java.util.Date maxDate =  new QCustomer()
      .select("max(registered)")
      .findSingleAttribute();

    assertThat(maxDate).isNotNull();
  }


  @Test
  public void testFetchByScalarValue() {
    Customer cust = new Customer();
    cust.setName(testName.getMethodName());
    cust.setPhoneNumber(new PhoneNumber("+18005555555"));
    cust.save();
    assertThat(new QCustomer()
      .name.eq(testName.getMethodName())
      .phoneNumber.eq(new PhoneNumber("+18005555555"))
      .findOne()).isNotNull();
  }


  @Test
  public void testFetchByComparableScalarValue() {
    Customer cust = new Customer();
    cust.setName(testName.getMethodName());
    cust.setEmail(new ValidEmail("foo2@example.org"));
    cust.save();
    assertThat(new QCustomer()
      .name.eq(testName.getMethodName())
      .email.eq(new ValidEmail("foo2@example.org"))
      .findOne()).isNotNull();
    assertThat(new QCustomer()
      .name.eq(testName.getMethodName())
      .email.gt(new ValidEmail("foo2@example.org"))
            .findOne()).isNull();
    assertThat(new QCustomer()
      .name.eq(testName.getMethodName())
      .email.gt(new ValidEmail("foo1@example.org"))
            .findOne()).isNotNull();
    assertThat(new QCustomer()
      .name.eq(testName.getMethodName())
      .email.greaterOrEqualTo(new ValidEmail("foo2@example.org"))
            .findOne()).isNotNull();
  }


  @Test
  public void testFetchFormula() {

    Customer cust = new Customer();
    cust.setName("junk junk");
    cust.setStatus(Customer.Status.GOOD);
    cust.setRegistered(new Date());
    cust.save();

    List<C1dto> dtos = new QCustomer()
      .select("id, name")
      .asDto(C1dto.class)
      .findList();

    assertThat(dtos).isNotEmpty();
  }

  public static class C1dto {

    final long id;
    final String name;

    public C1dto(long id, String name) {
      this.id = id;
      this.name = name;
    }
  }

}
