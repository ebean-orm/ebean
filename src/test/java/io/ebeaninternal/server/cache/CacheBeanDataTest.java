package io.ebeaninternal.server.cache;

import io.ebean.BaseTestCase;
import io.ebean.bean.EntityBean;
import io.ebean.bean.PersistenceContext;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;
import io.ebeaninternal.server.transaction.DefaultPersistenceContext;
import org.tests.model.basic.Address;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Customer.Status;
import org.tests.model.embedded.EAddress;
import org.tests.model.embedded.EPerson;
import org.junit.Test;

import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CacheBeanDataTest extends BaseTestCase {

  @Test
  public void extract_load_on_customer() {

    SpiEbeanServer server = spiEbeanServer();
    BeanDescriptor<Customer> desc = server.getBeanDescriptor(Customer.class);

    Customer c = new Customer();
    c.setId(98989);
    c.setName("Rob");
    c.setCretime(new Timestamp(System.currentTimeMillis()));
    c.setUpdtime(new Timestamp(System.currentTimeMillis()));
    c.setStatus(Status.ACTIVE);
    c.setSmallnote("somenote");

    Address billingAddress = new Address();
    billingAddress.setId((short) 12);
    billingAddress.setCity("Auckland");
    billingAddress.setCountry(server.getReference(Country.class, "NZ"));
    billingAddress.setLine1("92 Someplace Else");
    c.setBillingAddress(billingAddress);

    ((EntityBean) c)._ebean_getIntercept().setNewBeanForUpdate();

    CachedBeanData cacheData = CachedBeanDataFromBean.extract(desc, (EntityBean) c);


    assertNotNull(cacheData);

    Customer newCustomer = new Customer();
    newCustomer.setId(c.getId());
    CachedBeanDataToBean.load(desc, (EntityBean) newCustomer, cacheData, new DefaultPersistenceContext());

    assertEquals(c.getId(), newCustomer.getId());
    assertEquals(c.getName(), newCustomer.getName());
    assertEquals(c.getStatus(), newCustomer.getStatus());
    assertEquals(c.getSmallnote(), newCustomer.getSmallnote());
    assertEquals(c.getCretime(), newCustomer.getCretime());
    assertEquals(c.getUpdtime(), newCustomer.getUpdtime());
    assertEquals(c.getBillingAddress().getId(), newCustomer.getBillingAddress().getId());

    assertNotNull(newCustomer.getId());
    assertNotNull(newCustomer.getName());
    assertNotNull(newCustomer.getStatus());
    assertNotNull(newCustomer.getSmallnote());
    assertNotNull(newCustomer.getCretime());
    assertNotNull(newCustomer.getUpdtime());
    assertNotNull(newCustomer.getBillingAddress());
    assertNotNull(newCustomer.getBillingAddress().getId());

  }


  @Test
  public void extract_load_withEmbeddedBean() {

    SpiEbeanServer server = spiEbeanServer();
    BeanDescriptor<EPerson> desc = server.getBeanDescriptor(EPerson.class);
    BeanPropertyAssocOne<?> addressBeanProperty = (BeanPropertyAssocOne<?>) desc.getBeanProperty("address");

    EAddress address = new EAddress();
    address.setStreet("92 Someplace Else");
    address.setSuburb("Sandringham");
    address.setCity("Auckland");

    EPerson person = new EPerson();
    person.setId(98989L);
    person.setName("Rob");
    person.setAddress(address);

    CachedBeanData addressCacheData = (CachedBeanData) addressBeanProperty.getCacheDataValue((EntityBean) person);

    PersistenceContext context = new DefaultPersistenceContext();

    EPerson newPersonCheck = new EPerson();
    newPersonCheck.setId(98989L);
    addressBeanProperty.setCacheDataValue((EntityBean) newPersonCheck, addressCacheData, context);

    EAddress newAddress = newPersonCheck.getAddress();
    assertEquals(address.getStreet(), newAddress.getStreet());
    assertEquals(address.getCity(), newAddress.getCity());
    assertEquals(address.getSuburb(), newAddress.getSuburb());


    CachedBeanData cacheData = desc.cacheEmbeddedBeanExtract((EntityBean) person);

    assertNotNull(cacheData);

    EPerson newPerson = (EPerson) desc.cacheEmbeddedBeanLoad(cacheData, context);

    assertNotNull(newPerson.getId());
    assertNotNull(newPerson.getName());
    assertNotNull(newPerson.getAddress());

    assertEquals(person.getId(), newPerson.getId());
    assertEquals(person.getName(), newPerson.getName());
    assertEquals(person.getAddress().getStreet(), newPerson.getAddress().getStreet());
    assertEquals(person.getAddress().getCity(), newPerson.getAddress().getCity());
  }
}
