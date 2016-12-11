package io.ebeaninternal.server.cache;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.tests.model.basic.Address;
import org.tests.model.basic.Car;
import org.tests.model.basic.Customer;
import org.junit.Test;

import java.sql.Date;

import static org.junit.Assert.assertEquals;

public class CachedBeanDataFromBeanTest extends BaseTestCase {

  SpiEbeanServer server = spiEbeanServer();

  @Test
  public void extract() throws Exception {

    BeanDescriptor<Customer> desc = server.getBeanDescriptor(Customer.class);

    Date largeDate = new Date(9223372036825200000L);

    Customer customer = new Customer();
    customer.setId(42);
    customer.setName("Rob");
    customer.setAnniversary(largeDate);

    Address billingAddress = new Address();
    billingAddress.setId(Short.valueOf("12"));
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
}
