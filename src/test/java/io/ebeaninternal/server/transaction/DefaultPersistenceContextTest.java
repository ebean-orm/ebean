package io.ebeaninternal.server.transaction;

import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.PersistenceContextUtil;
import org.junit.Test;
import org.tests.model.basic.Car;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Product;
import org.tests.model.basic.Vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultPersistenceContextTest {

  private final Customer customer42;

  private final Car car1;

  public DefaultPersistenceContextTest() {
    customer42 = new Customer();
    customer42.setId(42);
    car1 = new Car();
    car1.setId(1);
  }

  private DefaultPersistenceContext pc() {
    return new DefaultPersistenceContext();
  }

  private DefaultPersistenceContext pcWith42() {
    DefaultPersistenceContext pc = pc();
    pc.put(Customer.class, 42, customer42);
    return pc;
  }

  Class<?> root(Class<?> cls) {
    return PersistenceContextUtil.root(cls);
  }

  @Test
  public void put_get_withInheritance() {

    PersistenceContext pc = pc();
    pc.put(root(Vehicle.class), 1, car1);

    Object found = pc.get(root(Car.class), 1);
    assertThat(found).isSameAs(car1);
  }

  @Test
  public void put_get() {

    PersistenceContext pc = pc();
    pc.put(Customer.class, customer42.getId(), customer42);

    Object found = pc.get(Customer.class, 42);
    assertThat(found).isSameAs(customer42);
  }

  @Test
  public void putIfAbsent_when_absent() {

    PersistenceContext pc = pc();
    Object existing = pc.putIfAbsent(Customer.class, customer42.getId(), customer42);

    assertThat(existing).isNull();
  }

  @Test
  public void putIfAbsent_when_notAbsent() {

    PersistenceContext pc = pcWith42();
    Object existing = pc.putIfAbsent(Customer.class, customer42.getId(), new Customer());

    assertThat(existing).isSameAs(customer42);
  }

  @Test
  public void get_when_empty() {
    PersistenceContext pc = pc();
    Object found = pc.get(Customer.class, 42);
    assertThat(found).isNull();
  }

  @Test
  public void get_when_there() {
    PersistenceContext pc = pcWith42();
    Object found = pc.get(Customer.class, 42);
    assertThat(found).isSameAs(customer42);
  }

  @Test
  public void getWithOption_when_empty() {

    PersistenceContext pc = pc();
    PersistenceContext.WithOption withOption = pc.getWithOption(Customer.class, 42);
    assertThat(withOption).isNull();
  }

  @Test
  public void getWithOption_when_there() {

    PersistenceContext pc = pcWith42();

    PersistenceContext.WithOption withOption = pc.getWithOption(Customer.class, 42);
    assertThat(withOption.getBean()).isSameAs(customer42);
  }

  @Test
  public void getWithOption_when_deleted() {

    PersistenceContext pc = pcWith42();
    pc.deleted(Customer.class, 42);

    PersistenceContext.WithOption withOption = pc.getWithOption(Customer.class, 42);
    assertThat(withOption.isDeleted()).isTrue();
    assertThat(withOption.getBean()).isNull();
  }

  @Test
  public void size_when_empty() {

    PersistenceContext pc = pc();
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }

  @Test
  public void size_when_some() {

    PersistenceContext pc = pcWith42();
    assertThat(pc.size(Customer.class)).isEqualTo(1);
  }

  @Test
  public void clear() {

    PersistenceContext pc = pcWith42();
    pc.clear();
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }

  @Test
  public void clearClass() {

    PersistenceContext pc = pcWith42();
    pc.clear(Customer.class);
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }

  @Test
  public void clearClassAndId() {

    PersistenceContext pc = pcWith42();
    pc.put(Customer.class, 43, new Customer());

    pc.clear(Customer.class, 42);
    assertThat(pc.size(Customer.class)).isEqualTo(1);

    pc.clear(Customer.class, 43);
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }

  @Test
  public void forIterate() {
    final DefaultPersistenceContext pc = pcWith42();
    final Object origCustomer42 = pc.get(Customer.class, 42);

    // act
    final PersistenceContext pcIterate = pc.forIterate();
    assertThat(pc).isNotSameAs(pcIterate);
    assertThat(pcIterate.size(Customer.class)).isEqualTo(1);

    // assert same instance (bean effectively transferred to iterator persistence context
    final Object customer42 = pcIterate.get(Customer.class, 42);
    assertThat(customer42).isSameAs(origCustomer42);
    final PersistenceContext.WithOption option = pcIterate.getWithOption(Customer.class, 42);
    assertThat(option.getBean()).isSameAs(origCustomer42);
  }

  @Test
  public void forIterate_many() {
    DefaultPersistenceContext pc = new DefaultPersistenceContext();
    addCustomers(pc, 1, 100);
    addContacts(pc, 1, 1010);
    assertThat(pc.size(Customer.class)).isEqualTo(100);
    assertThat(pc.size(Contact.class)).isEqualTo(1010);

    // act
    final PersistenceContext pcIterate = pc.forIterate();
    assertThat(pcIterate.size(Customer.class)).isEqualTo(100);
    assertThat(pcIterate.size(Contact.class)).isEqualTo(1010);
  }

  @Test
  public void forIterate_resetLimit_forIterateReset() {
    DefaultPersistenceContext initialPc = new DefaultPersistenceContext();
    addCustomers(initialPc, 1, 100);
    addContacts(initialPc, 1, 1010);

    final PersistenceContext pcIterate = initialPc.forIterate();
    assertFalse(pcIterate.resetLimit());

    // added 900 NEW contact beans
    addContacts(pcIterate, 2000, 900);
    assertThat(pcIterate.size(Contact.class)).isEqualTo(1910);
    assertFalse(pcIterate.resetLimit());

    // boundary, added 1000 NEW contact beans (still false)
    addContacts(pcIterate, 3000, 100);
    assertFalse(pcIterate.resetLimit());

    addContacts(pcIterate, 4000, 1);
    addProducts(pcIterate, 1, 100);
    // ACT - over 1000 added beans boundary for contacts so returns true
    assertTrue(pcIterate.resetLimit());

    assertThat(pcIterate.size(Contact.class)).isEqualTo(2011);
    assertThat(pcIterate.size(Customer.class)).isEqualTo(100);
    assertThat(pcIterate.size(Product.class)).isEqualTo(100);

    // ACT - obtain new PC forIterateReset
    PersistenceContext pcReset = pcIterate.forIterateReset();

    // keeps original customer beans as no new added beans there
    assertThat(pcReset.size(Customer.class)).isEqualTo(100); // customers didn't change
    // added beans to contacts and products so those where reset
    assertThat(pcReset.size(Contact.class)).isEqualTo(0);
    assertThat(pcReset.size(Product.class)).isEqualTo(0);
  }

  @Test
  public void toString_sillyTest() {
    DefaultPersistenceContext pc = pcWith42();
    assertThat(pc.toString()).contains("org.tests.model.basic.Customer");
  }

  private void addCustomers(PersistenceContext pc, int start, int loop) {
    for (int i = start; i < start + loop; i++) {
      Customer bean = new Customer();
      bean.setId(i);
      pc.put(Customer.class, i, bean);
    }
  }

  private void addContacts(PersistenceContext pc, int start, int loop) {
    for (int i = start; i < start + loop; i++) {
      Contact bean = new Contact();
      bean.setId(i);
      pc.put(Contact.class, i, bean);
    }
  }

  private void addProducts(PersistenceContext pc, int start, int loop) {
    for (int i = start; i < start + loop; i++) {
      Product bean = new Product();
      bean.setId(i);
      pc.put(Product.class, i, bean);
    }
  }
}
