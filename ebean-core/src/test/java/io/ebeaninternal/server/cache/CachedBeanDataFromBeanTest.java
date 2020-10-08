package io.ebeaninternal.server.cache;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Car;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Customer;

import java.sql.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CachedBeanDataFromBeanTest extends BaseTestCase {

  private final SpiEbeanServer server = spiEbeanServer();

  @Test
  public void extract() {

    BeanDescriptor<Customer> desc = server.getBeanDescriptor(Customer.class);

    Date largeDate = new Date(9223372036825200000L);

    Customer customer = new Customer();
    customer.setId(42);
    customer.setName("Rob");
    customer.setAnniversary(largeDate);

    Address billingAddress = new Address();
    billingAddress.setId(12);
    billingAddress.setCity("SomePlace");

    customer.setBillingAddress(billingAddress);

    CachedBeanData cacheData = CachedBeanDataFromBean.extract(desc, (EntityBean) customer);

    assertEquals(cacheData.getData("id"), "42");
    assertEquals(cacheData.getData("name"), "Rob");
    assertEquals(cacheData.getData("billingAddress"), "12");
    assertEquals(cacheData.getData("anniversary"), "9223372036825200000");
  }


  @Test
  public void inheritance() {

    Car car = new Car();
    car.setId(42);
    car.setDriver("Jimmy");
    car.setNotes("some notes");

    BeanDescriptor<Car> carDesc = server.getBeanDescriptor(Car.class);
    CachedBeanData cacheData = CachedBeanDataFromBean.extract(carDesc, (EntityBean) car);

    Car newCar = new Car();
    EntityBean entityBean = (EntityBean) newCar;
    CachedBeanDataToBean.load(carDesc, entityBean, cacheData, new DefaultPersistenceContext());

    assertEquals(newCar.getId(), car.getId());
    assertEquals(newCar.getDriver(), car.getDriver());
    assertEquals(newCar.getNotes(), car.getNotes());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void dirtyScalar_expect_originalValueUsed() {

    Contact contact = new Contact();
    contact.setId(42);
    contact.setLastName("Bygrave");
    contact.setFirstName("Foo");
    contact.setEmail("rob@email.com");

    EntityBean entityBean = (EntityBean)contact;
    entityBean._ebean_getIntercept().setLoaded();

    // mutate, dirty
    contact.setLastName("Banana");

    final BeanDescriptor<Contact> desc = getBeanDescriptor(Contact.class);
    CachedBeanData cacheData = CachedBeanDataFromBean.extract(desc, entityBean);

    final Map<String, Object> data = cacheData.getData();
    assertThat(data.get("id")).isEqualTo("42");
    assertThat(data.get("lastName")).isEqualTo("Bygrave"); // ORIGINAL VALUE
    assertThat(data.get("firstName")).isEqualTo(contact.getFirstName());
    assertThat(data.get("email")).isEqualTo(contact.getEmail());
  }

  @Test
  public void dirtyManyToOne_expect_originalValueUsed() {

    Customer customer = new Customer();
    customer.setId(99);

    Contact contact = new Contact();
    contact.setFirstName("Foo");
    contact.setLastName("Bygrave");
    contact.setEmail("rob@email.com");
    contact.setCustomer(customer);

    EntityBean entityBean = (EntityBean)contact;
    entityBean._ebean_getIntercept().setLoaded();

    // mutate, dirty
    Customer customer2 = new Customer();
    customer2.setId(108);
    contact.setCustomer(customer2);
    contact.setLastName("Banana");

    final BeanDescriptor<Contact> desc = getBeanDescriptor(Contact.class);
    CachedBeanData cacheData = CachedBeanDataFromBean.extract(desc, entityBean);

    final Map<String, Object> data = cacheData.getData();
    assertThat(data.get("lastName")).isEqualTo("Bygrave"); // Original value
    assertThat(data.get("customer")).isEqualTo("99");  // Original value
    assertThat(data.get("firstName")).isEqualTo(contact.getFirstName());
    assertThat(data.get("email")).isEqualTo(contact.getEmail());
  }
}
