package org.querytest;

import io.ebean.*;
import io.ebean.annotation.Transactional;
import io.ebean.test.LoggedSql;
import io.ebean.types.Inet;
import io.ebeaninternal.api.SpiQuery;
import org.example.domain.Address;
import org.example.domain.Country;
import org.example.domain.Customer;
import org.example.domain.otherpackage.PhoneNumber;
import org.example.domain.otherpackage.ValidEmail;
import org.example.domain.query.QContact;
import org.example.domain.query.QCustomer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.ebean.StdOperators.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.domain.query.QAddress.Alias.country;
import static org.example.domain.query.QAddress.Alias.line1;
import static org.example.domain.query.QContact.Alias.firstName;
import static org.example.domain.query.QContact.Alias.lastName;
import static org.example.domain.query.QCustomer.Alias.*;
import static org.example.domain.query.QCustomer.Alias.billingAddress;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QCustomerTest {

  @Test
  public void findWithTransaction() {

    final Database database = DB.getDefault();

    try (Transaction txn = database.createTransaction()) {
      Customer customer = new Customer();
      customer.setName("explicitTransaction");

      database.save(customer, txn);

      final Customer found = new QCustomer()
        .name.eq("explicitTransaction")
        .usingTransaction(txn)
        .findOne();
      assertThat(found).isNotNull();

      // not found using other transaction
      final Customer foundNot = new QCustomer()
        .name.eq("explicitTransaction")
        .withLock(Query.LockType.SHARE)
        .findOne();
      assertThat(foundNot).isNull();

      txn.commit();
    }

  }

  @Test
  public void copy() {
    var origin = new QCustomer()
      .setDistinct(true)
      .status.equalTo(Customer.Status.BAD);

    var copy1 = origin.copy().name.isNotNull();
    var q1 = copy1.query();
    copy1.findList();

    assertThat(q1.getGeneratedSql()).contains("from be_customer t0 where t0.status = ? and t0.name is not null");

    var copy2 = origin.copy().version.ge(1L);
    var q2 = copy2.query();
    copy2.findList();

    assertThat(q2.getGeneratedSql()).contains("from be_customer t0 where t0.status = ? and t0.version >= ?");
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
  public void findEachBatch() {

    for (int i = 0; i < 22; i++) {
      Customer customer = new Customer();
      customer.setStatus(Customer.Status.MIDDLING);
      customer.setName("findEachBatch_a_" + i);
      customer.save();
    }

    final List<Integer> batchSizes = new ArrayList<>();

    final AtomicInteger counter = new AtomicInteger();
    new QCustomer()
      .status.eq(Customer.Status.MIDDLING)
      .name.startsWith("findEachBatch_a_")
      .findEach(10, customers -> {
        batchSizes.add(customers.size());
        System.out.println("Batch " + counter.incrementAndGet() + " size:" + customers.size());
      });

    assertThat(batchSizes).hasSize(3);
    assertThat(batchSizes.get(0)).isEqualTo(10);
    assertThat(batchSizes.get(1)).isEqualTo(10);
    assertThat(batchSizes.get(2)).isEqualTo(2);
  }

  @Test
  public void findEachBatch_when_lastBatchEmpty() {

    for (int i = 0; i < 18; i++) {
      Customer customer = new Customer();
      customer.setStatus(Customer.Status.MIDDLING);
      customer.setName("findEachBatch_b_" + i);
      customer.save();
    }

    final List<Integer> batchSizes = new ArrayList<>();

    final AtomicInteger counter = new AtomicInteger();
    new QCustomer()
      .status.eq(Customer.Status.MIDDLING)
      .name.startsWith("findEachBatch_b_")
      .findEach(9, customers -> {
        batchSizes.add(customers.size());
        System.out.println("Batch " + counter.incrementAndGet() + " size:" + customers.size());
      });

    assertThat(batchSizes).hasSize(2);
    assertThat(batchSizes.get(0)).isEqualTo(9);
    assertThat(batchSizes.get(1)).isEqualTo(9);
  }

  // using QCustomer.forFetchGroup() ... does not need any Ebean Database initialisation etc
  // and so is good for when we want to build a static final OrderBy
  static final OrderBy<Customer> orderBy = QCustomer.forFetchGroup()
    .name.asc()
    .email.desc()
    .query().orderBy();

  @Test
  void findWithPaging() {
    // OrderBy<Customer> orderBy = new QCustomer().name.asc().email.desc().query().orderBy();
    OrderBy<Customer> orderBy = OrderBy.of("name, email desc");
    var paging = Paging.of(2, 10, QCustomerTest.orderBy);

    LoggedSql.start();

    new QCustomer()
      .name.isNotNull()
      .setPaging(paging)
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(sql.get(0)).contains("where t0.name is not null order by t0.name, t0.email desc limit 10 offset 20");
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


    Map<Long, Customer> map = new QCustomer()
      .status.equalTo(Customer.Status.GOOD)
      .findMap();

    assertThat(map.size()).isEqualTo(ids.size());

    try (QueryIterator<Customer> iterate =
           new QCustomer()
             .status.equalTo(Customer.Status.GOOD)
             .findIterate()) {

      while (iterate.hasNext()) {
        Customer customer = iterate.next();
        assertThat(customer.getName()).isNotNull();
      }
    }
  }

  @Test
  void equalTo_byProperty() {
    Query<Customer> query = new QCustomer()
      .select(id)
      .billingAddress.city.eq(shippingAddress.city)
      .query();

    query.findList();
    String generatedSql = query.getGeneratedSql();

    // select t0.id from be_customer t0
    // left join o_address t2 on t2.id = t0.shipping_address_id
    // left join o_address t1 on t1.id = t0.billing_address_id
    // where t1.city = t2.city
    assertThat(generatedSql).contains("where t1.city = t2.city");
  }

  @Test
  void notEqual_byProperty() {
    Query<Customer> query = new QCustomer()
      .select(id)
      .billingAddress.city.ne(shippingAddress.city)
      .query();

    query.findList();
    String generatedSql = query.getGeneratedSql();

    // select t0.id from be_customer t0
    // left join o_address t2 on t2.id = t0.shipping_address_id
    // left join o_address t1 on t1.id = t0.billing_address_id
    // where t1.city <> t2.city
    assertThat(generatedSql).contains("where t1.city <> t2.city");
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

  @Test
  public void distinctOn() {
    var c = QContact.alias();
    var q = new QContact()
      .distinctOn(c.customer)
      .select(c.lastName, c.whenCreated)
      .orderBy()
      .customer.id.asc()
      .whenCreated.desc()
      .query();

    SpiQuery<?> spiQuery = (SpiQuery<?>) q;
    assertThat(spiQuery.distinctOn()).isEqualTo("customer");
    assertThat(spiQuery.isDistinct()).isTrue();
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

  @Disabled
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

  static final FetchGroup<Customer> FGCustomerContacts = QCustomer.forFetchGroup()
    .select(name)
    .contacts.fetch(firstName, lastName)
    .buildFetchGroup();

  @Test
  void filterMany() {

    var q = new QCustomer()
      .select(FGCustomerContacts)
      .contacts.filterMany(contacts -> contacts
        .firstName.startsWith("r")
        .email.isNotNull())
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).isEqualTo("select /* QCustomerTest.filterMany */ t0.id, t0.name, t1.id, t1.first_name, t1.last_name from be_customer t0 left join be_contact t1 on t1.customer_id = t0.id where (t1.id is null or (t1.first_name like ? escape'|' and t1.email is not null)) order by t0.id");
  }

  @Test
  void filterManySingle() {

    var q = new QCustomer()
      .select(FGCustomerContacts)
      .contacts.filterMany(c -> c.firstName.startsWith("r"))
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).isEqualTo("select /* QCustomerTest.filterManySingle */ t0.id, t0.name, t1.id, t1.first_name, t1.last_name from be_customer t0 left join be_contact t1 on t1.customer_id = t0.id where (t1.id is null or (t1.first_name like ? escape'|')) order by t0.id");
  }

  @Test
  void filterManySeparateQuery() {
    Customer cust = new Customer();
    cust.setName("filterManySeparateQuery");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    var q = new QCustomer()
      .select(FGCustomerContacts)
      .contacts.filterManyRaw("firstName like ?", "R%")
      .contacts.filterMany(c -> c.firstName.startsWith("R")) // same as filterManyRaw() expression
      .setMaxRows(10) // force the ToMany path to be in a separate secondary query
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).isEqualTo("select /* QCustomerTest.filterManySeparateQuery */ t0.id, t0.name from be_customer t0 limit 10");
  }

  @Test
  void filterManySingleQuery() {
    Customer cust = new Customer();
    cust.setName("filterManySingleQuery");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    var q = new QCustomer()
      .select(FGCustomerContacts)
      .contacts.filterManyRaw("firstName like ?", "R%")
      .contacts.filterMany(c -> c.firstName.startsWith("R")) // same as filterManyRaw() expression
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).contains(" from be_customer t0 left join be_contact t1 on t1.customer_id = t0.id where (t1.id is null or (t1.first_name like ? and t1.first_name like ? escape'|')) order by t0.id");
  }

  @Test
  void filterManyOr() {
    var q = new QCustomer()
      .contacts.filterMany(c ->
        c.or()
          .firstName.startsWith("R")
          .lastName.startsWith("R")
          .endOr())
      .query();

    q.findList();
    assertThat(q.getGeneratedSql()).contains(" from be_customer t0 left join be_contact t1 on t1.customer_id = t0.id where (t1.id is null or ((t1.first_name like ? escape'|' or t1.last_name like ? escape'|'))) order by t0.id");
  }

  @Test
  public void testIdIn() {

    List<Integer> ids = new ArrayList<>();
    ids.add(1);
    ids.add(2);

    new QCustomer()
      .setIdIn(ids) // collection argument
      .findList();

    new QCustomer()
      .setIdIn("1", "2") // varargs argument
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
  public void usingMaster() {
    new QCustomer()
      .registered.isNull()
      .usingMaster()
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

    DataSource dataSource = DB.getDefault().dataSource();

    try (Connection connection = dataSource.getConnection()) {

      List<Customer> foo = new QCustomer()
        .name.eq("usingConnection")
        .usingConnection(connection)
        .findList();

      connection.rollback();
      assertThat(foo).hasSize(1);
    }

    Customer customer = DB.find(Customer.class, cust.getId());
    assert customer != null;
    String customerToString = customer.toString();
    assertThat(customerToString).contains("Customer@0(id:");
    assertThat(customerToString).contains(", status:GOOD, inactive:false, name:usingConnection, version:1, whenCreated:");
  }

  @Test
  public void testAssocOne() {
    DB.getDefault();
    Address address = new Address();
    address.setId(41L);

    LoggedSql.start();
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

    new QCustomer()
      .billingAddress.eqIfPresent(address)
      .name.isNull()
      .findList();

    new QCustomer()
      .billingAddress.eqIfPresent(null)
      .name.isNull()
      .findList();


    List<String> sql = LoggedSql.stop();

    assertThat(sql).hasSize(6);
    assertThat(sql.get(0)).contains("where t0.billing_address_id = ?");
    assertThat(sql.get(1)).contains("where t0.billing_address_id = ?");
    assertThat(sql.get(2)).contains("where t0.billing_address_id <> ?");
    assertThat(sql.get(3)).contains("where t0.billing_address_id <> ?");
    assertThat(sql.get(4)).contains("where t0.billing_address_id = ? and t0.name is null");
    assertThat(sql.get(5)).contains("where t0.name is null");
  }

  @Test
  public void testAssocOne_in() {
    DB.getDefault();
    Address address = new Address();
    address.setId(41L);
    Address address2 = new Address();
    address2.setId(41L);

    LoggedSql.start();
    new QCustomer()
      .billingAddress.in(address, address2)
      .findList();

    new QCustomer()
      .billingAddress.in(List.of(address, address2))
      .findList();

    new QCustomer()
      .billingAddress.inOrEmpty(List.of(address, address2))
      .name.isNull()
      .findList();

    new QCustomer()
      .billingAddress.inOrEmpty(List.of())
      .name.isNull()
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(4);
    assertThat(sql.get(0)).contains("where t0.billing_address_id in (?,?)");
    assertThat(sql.get(1)).contains("where t0.billing_address_id in (?,?)");
    assertThat(sql.get(2)).contains("where t0.billing_address_id in (?,?) and t0.name is null");
    assertThat(sql.get(3)).contains("where t0.name is null");
  }

  @Test
  public void testAssocOne_notIn() {
    DB.getDefault();
    Address address = new Address();
    address.setId(41L);
    Address address2 = new Address();
    address2.setId(41L);

    LoggedSql.start();
    new QCustomer()
      .billingAddress.notIn(address, address2)
      .findList();

    new QCustomer()
      .billingAddress.notIn(List.of(address, address2))
      .findList();

    List<String> sql = LoggedSql.stop();
    assertThat(sql).hasSize(2);
    assertThat(sql.get(0)).contains("where t0.billing_address_id not in (?,?)");
    assertThat(sql.get(1)).contains("where t0.billing_address_id not in (?,?)");
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
  public void testTwoDiffQueryTypes_expect_NoLineNumbers() {
    new QCustomer()
      .id.isIn(34L)
      .findList();

    new QContact()
      .email.isNull()
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
  public void testFilterMany() {

    Customer cust = new Customer();
    cust.setName("Postgres Foo");
    cust.setStatus(Customer.Status.GOOD);
    cust.save();

    new QCustomer()
      .name.startsWith("Postgres")
      .contacts.filterManyRaw("firstName like ?", "Rob%")
      .findList();

    final LocalDate startDate = LocalDate.now().minusDays(7);
    final LocalDate endDate = LocalDate.now();

    new QCustomer()
      .name.startsWith("Postgres")
      .contacts.filterManyRaw("whenCreated >= ? and whenCreated < ?", startDate, endDate)
      .findList();
  }

  @Test
  public void testDate_lessThan() {

    assertContains(new QCustomer().registered.lt(new Date()).query(), " where t0.registered < ?");
    assertContains(new QCustomer().registered.before(new Date()).query(), " where t0.registered < ?");
    assertContains(new QCustomer().registered.lessThan(new Date()).query(), " where t0.registered < ?");
    assertContains(new QCustomer().whenCreated.lt(whenUpdated).query(), " where t0.when_created < t0.when_updated");
  }

  @Test
  public void testDate_lessOrEqualTo() {

    assertContains(new QCustomer().registered.le(new Date()).query(), " where t0.registered <= ?");
    assertContains(new QCustomer().registered.lessOrEqualTo(new Date()).query(), " where t0.registered <= ?");
    assertContains(new QCustomer().whenCreated.le(whenUpdated).query(), " where t0.when_created <= t0.when_updated");
  }

  @Test
  public void testDate_greaterThan() {

    assertContains(new QCustomer().registered.after(new Date()).query(), " where t0.registered > ?");
    assertContains(new QCustomer().registered.gt(new Date()).query(), " where t0.registered > ?");
    assertContains(new QCustomer().whenCreated.gt(whenUpdated).query(), " where t0.when_created > t0.when_updated");
    assertContains(new QCustomer().registered.greaterThan(new Date()).query(), " where t0.registered > ?");
  }


  @Test
  public void testDate_greaterOrEqualTo() {

    assertContains(new QCustomer().registered.ge(new Date()).query(), " where t0.registered >= ?");
    assertContains(new QCustomer().registered.greaterOrEqualTo(new Date()).query(), " where t0.registered >= ?");
    assertContains(new QCustomer().whenCreated.ge(whenUpdated).query(), " where t0.when_created >= t0.when_updated");
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


    Map<Long, Customer> map
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
      .currentInet.in(Inet.setOf("129.1.1.4", "129.1.1.5"))
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

    new QContact()
      .firstName.between("A", "B")
      .findList();
  }

  @Test
  void betweenProperties() {
    var query = new QContact()
      .firstName.betweenProperties(lastName, "B");

    query.findList();
    assertThat(query.getGeneratedSql()).contains(" where ? between t0.first_name and t0.last_name");
  }

  @Test
  void betweenProperties_notFirstPredicate() {
    var query = new QContact()
      .lastName.isNotNull()
      .firstName.betweenProperties(lastName, "B")
      .email.isNotNull();

    query.findList();
    assertThat(query.getGeneratedSql()).contains(" where t0.last_name is not null and ? between t0.first_name and t0.last_name and t0.email is not null");
  }

  @Test
  void query_inRangeWithOtherProperties() {
    var c = QCustomer.alias();

    Query<Customer> query = new QCustomer()
      .select(c.id)
      .name.inRangeWith(c.contacts.firstName, c.contacts.lastName)
      .query();

    // select distinct t0.id from be_customer t0
    // left join be_contact t1 on t1.customer_id = t0.id
    // where t1.first_name <= t0.name and (t0.name < t1.last_name or t1.last_name is null)
    query.findList();
    assertThat(query.getGeneratedSql()).contains(" where t1.first_name <= t0.name and (t0.name < t1.last_name or t1.last_name is null)");
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
  void checkingPropertyTypesToEQOperator() {

    QCustomer c = QCustomer.alias();

    new QCustomer()
      .select(c.version, count(c.id))
      .version.gt(0)
      .having()
      .add(gt(count(c.id), 1))
      .findList();

    new QCustomer()
      .add(in(name, List.of("foo", "bar")))
      .add(eq(currentInet, Inet.of("127.0.0.1")))
      .findList();

    new QCustomer()
      //.add(gt(sum(QCustomer.Alias.version), 45))
      .add(eq(version, 45L))
      .add(eq(name, "junk"))
      .add(eq(registered, new Date()))
      .add(eq(whenUpdated, new Timestamp(System.currentTimeMillis())))
      .findList();
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
    assertNotNull(customerSet);
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

    java.util.Date maxDate = new QCustomer()
      .select("max(registered)")
      .findSingleAttribute();

    assertThat(maxDate).isNotNull();
  }

  @Test
  public void findSingleAttributeOrEmpty() {

    Customer cust = new Customer();
    cust.setName("MaybeIExist yeah");
    cust.setStatus(Customer.Status.GOOD);
    cust.setRegistered(new Date());
    cust.save();

    Optional<String> customerName = new QCustomer()
      .select(name)
      .status.eq(Customer.Status.GOOD)
      .name.startsWith("MaybeIExist")
      .findSingleAttributeOrEmpty();

    assertThat(customerName).isPresent();

    Optional<String> customerName2 = new QCustomer()
      .select(name)
      .status.eq(Customer.Status.GOOD)
      .name.eq("NahIDoNotExist")
      .findSingleAttributeOrEmpty();

    assertThat(customerName2).isEmpty();
  }


  @Test
  public void testFetchByScalarValue() {
    Customer cust = new Customer();
    cust.setName("testFetchByScalarValue");
    cust.setPhoneNumber(new PhoneNumber("+18005555555"));
    cust.save();

    var customer = new QCustomer()
      .name.eq("testFetchByScalarValue")
      .phoneNumber.eq(new PhoneNumber("+18005555555"))
      .findOne();

    assertThat(customer).isNotNull();

    var customers = new QCustomer()
      .name.eq("testFetchByScalarValue")
      .phoneNumber.eq(new PhoneNumber("+18005555555"))
      .findList();

    assertThat(customers).hasSize(1);
  }


  @Test
  public void testFetchByComparableScalarValue() {
    Customer cust = new Customer();
    cust.setName("testFetchByComparableScalarValue");
    cust.setEmail(new ValidEmail("foo2@example.org"));
    cust.save();
    assertThat(new QCustomer()
      .name.eq("testFetchByComparableScalarValue")
      .email.eq(new ValidEmail("foo2@example.org"))
      .findOne()).isNotNull();
    assertThat(new QCustomer()
      .name.eq("testFetchByComparableScalarValue")
      .email.gt(new ValidEmail("foo2@example.org"))
      .findOne()).isNull();
    assertThat(new QCustomer()
      .name.eq("testFetchByComparableScalarValue")
      .email.gt(new ValidEmail("foo1@example.org"))
      .findOne()).isNotNull();
    assertThat(new QCustomer()
      .name.eq("testFetchByComparableScalarValue")
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
