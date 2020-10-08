package io.ebean.bean;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import org.tests.compositekeys.db.AuditInfo;
import org.tests.model.basic.Customer;
import org.tests.model.basic.EBasic;
import org.tests.model.basic.ResetBasicData;
import org.junit.Test;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityBeanInterceptTest extends BaseTestCase {

  @Test
  public void testHasDirtyProperty() {

    ResetBasicData.reset();

    List<Customer> list = DB.find(Customer.class).findList();

    Set<String> propertyNames = new HashSet<>();
    propertyNames.add("name");
    propertyNames.add("status");


    Customer customer = list.get(0);
    EntityBeanIntercept ebi = ebi(customer);

    assertFalse(ebi.hasDirtyProperty(propertyNames));

    customer.setAnniversary(new Date(System.currentTimeMillis()));
    assertFalse(ebi.hasDirtyProperty(propertyNames));

    customer.setStatus(Customer.Status.ACTIVE);
    assertTrue(ebi.hasDirtyProperty(propertyNames));

  }

  @Test
  public void isPartial_when_new() {

    EBasic basic = new EBasic();
    EntityBeanIntercept ebi = ebi(basic);
    assertThat(ebi.isPartial()).isTrue();
  }

  @Test
  public void isPartial_when_partial() {

    EBasic basic = new EBasic();
    basic.setId(42);
    basic.setName("some");
    EntityBeanIntercept ebi = ebi(basic);
    assertThat(ebi.isPartial()).isTrue();
  }

  @Test
  public void isPartial_when_full() {

    EBasic basic = new EBasic();
    basic.setId(42);
    basic.setName("some");
    basic.setDescription("asd");
    basic.setSomeDate(null);
    basic.setStatus(EBasic.Status.ACTIVE);

    EntityBeanIntercept ebi = ebi(basic);
    assertThat(ebi.isPartial()).isFalse();
  }

  @Test
  public void isEmbeddedNewOrDirty() {

    AuditInfo auditInfo = new AuditInfo();
    EntityBeanIntercept ebi = ebi(auditInfo);
    assertTrue(ebi.isNew());
    assertTrue(ebi.isEmbeddedNewOrDirty(auditInfo));

    auditInfo.setUpdatedBy("initial");
    ebi.setLoaded();
    assertTrue(ebi.isLoaded());
    assertFalse(ebi.isEmbeddedNewOrDirty(auditInfo));

    auditInfo.setUpdatedBy("nowDirty");
    assertTrue(ebi.isDirty());
    assertTrue(ebi.isEmbeddedNewOrDirty(auditInfo));
    assertFalse(ebi.isEmbeddedNewOrDirty(null));
  }

  @Test
  public void setEmbeddedLoaded() {
    AuditInfo auditInfo = new AuditInfo();
    EntityBeanIntercept ebi = ebi(auditInfo);
    assertFalse(ebi.isLoaded());

    ebi.setEmbeddedLoaded(auditInfo);
    assertTrue(ebi.isLoaded());
  }


  @Test
  public void initialisedMany() {
    Customer customer = new Customer();
    EntityBeanIntercept ebi = ebi(customer);
    final int contactsPos = findProperty("contacts", ebi);
    assertFalse(ebi.isLoadedProperty(contactsPos));

    ebi.initialisedMany(contactsPos);
    assertTrue(ebi.isLoadedProperty(contactsPos));
  }

  private int findProperty(String name, EntityBeanIntercept eb) {
    final String[] names = eb.getOwner()._ebean_getPropertyNames();
    for (int i = 0; i < names.length; i++) {
      if (names[i].equals(name)) {
        return i;
      }
    }

    throw new RuntimeException("property not found");
  }

  @SuppressWarnings("unchecked")
  private EntityBeanIntercept ebi(Object bean) {
    return ((EntityBean)bean)._ebean_getIntercept();
  }
}
