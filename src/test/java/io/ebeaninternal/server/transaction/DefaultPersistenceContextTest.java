package io.ebeaninternal.server.transaction;

import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.server.deploy.PersistenceContextUtil;
import org.tests.model.basic.Car;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Vehicle;
import org.junit.Test;

import static org.assertj.core.api.StrictAssertions.assertThat;

public class DefaultPersistenceContextTest {

  Customer customer42;

  Car car1;

  public DefaultPersistenceContextTest() {
    customer42 = new Customer();
    customer42.setId(42);

    car1 = new Car();
    car1.setId(1);
  }

  PersistenceContext pc() {
    return new DefaultPersistenceContext();
  }

  PersistenceContext pcWith42() {
    PersistenceContext pc = pc();
    pc.put(Customer.class, 42, customer42);
    return pc;
  }

  Class<?> root(Class<?> cls) {
    return PersistenceContextUtil.root(cls);
  }

  @Test
  public void put_get_withInheritance() throws Exception {

    PersistenceContext pc = pc();
    pc.put(root(Vehicle.class), 1, car1);

    Object found = pc.get(root(Car.class), 1);
    assertThat(found).isSameAs(car1);
  }

  @Test
  public void put_get() throws Exception {

    PersistenceContext pc = pc();
    pc.put(Customer.class, customer42.getId(), customer42);

    Object found = pc.get(Customer.class, 42);
    assertThat(found).isSameAs(customer42);
  }

  @Test
  public void putIfAbsent_when_absent() throws Exception {

    PersistenceContext pc = pc();
    Object existing = pc.putIfAbsent(Customer.class, customer42.getId(), customer42);

    assertThat(existing).isNull();
  }

  @Test
  public void putIfAbsent_when_notAbsent() throws Exception {

    PersistenceContext pc = pcWith42();
    Object existing = pc.putIfAbsent(Customer.class, customer42.getId(), new Customer());

    assertThat(existing).isSameAs(customer42);
  }

  @Test
  public void get_when_empty() throws Exception {
    PersistenceContext pc = pc();
    Object found = pc.get(Customer.class, 42);
    assertThat(found).isNull();
  }

  @Test
  public void get_when_there() throws Exception {
    PersistenceContext pc = pcWith42();
    Object found = pc.get(Customer.class, 42);
    assertThat(found).isSameAs(customer42);
  }

  @Test
  public void getWithOption_when_empty() throws Exception {

    PersistenceContext pc = pc();
    PersistenceContext.WithOption withOption = pc.getWithOption(Customer.class, 42);
    assertThat(withOption).isNull();
  }

  @Test
  public void getWithOption_when_there() throws Exception {

    PersistenceContext pc = pcWith42();

    PersistenceContext.WithOption withOption = pc.getWithOption(Customer.class, 42);
    assertThat(withOption.getBean()).isSameAs(customer42);
  }

  @Test
  public void getWithOption_when_deleted() throws Exception {

    PersistenceContext pc = pcWith42();
    pc.deleted(Customer.class, 42);

    PersistenceContext.WithOption withOption = pc.getWithOption(Customer.class, 42);
    assertThat(withOption.isDeleted()).isTrue();
    assertThat(withOption.getBean()).isNull();
  }

  @Test
  public void size_when_empty() throws Exception {

    PersistenceContext pc = pc();
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }

  @Test
  public void size_when_some() throws Exception {

    PersistenceContext pc = pcWith42();
    assertThat(pc.size(Customer.class)).isEqualTo(1);
  }

  @Test
  public void clear() throws Exception {

    PersistenceContext pc = pcWith42();
    pc.clear();
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }

  @Test
  public void clearClass() throws Exception {

    PersistenceContext pc = pcWith42();
    pc.clear(Customer.class);
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }

  @Test
  public void clearClassAndId() throws Exception {

    PersistenceContext pc = pcWith42();
    pc.put(Customer.class, 43, new Customer());

    pc.clear(Customer.class, 42);
    assertThat(pc.size(Customer.class)).isEqualTo(1);

    pc.clear(Customer.class, 43);
    assertThat(pc.size(Customer.class)).isEqualTo(0);
  }
}
