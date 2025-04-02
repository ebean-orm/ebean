package io.ebeaninternal.server.deploy;

import io.ebean.bean.EntityBean;
import io.ebean.plugin.Property;
import io.ebeaninternal.server.core.CacheOptions;
import io.ebeaninternal.server.deploy.meta.DeployBeanDescriptor;
import io.ebeaninternal.server.deploy.meta.DeployIdentityMode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Order;
import org.tests.model.bridge.BSite;
import org.tests.model.bridge.BUser;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BeanDescriptorTest extends BaseTest {

  private BeanDescriptor<Customer> customerDesc = spiEbeanServer().descriptor(Customer.class);

  @Test
  public void createReference() {

    Customer bean = customerDesc.createReference(false, false, 42, null);
    assertThat(bean.getId()).isEqualTo(42);
    assertThat(server().beanState(bean).isUnmodifiable()).isFalse();
  }

  @Test
  public void createReference_whenReadOnly() {

    Customer bean = customerDesc.createReference(true, false, 42, null);
    assertThat(server().beanState(bean).isUnmodifiable()).isTrue();
  }

  @Test
  public void createReference_whenNotReadOnly() {

    Customer bean = customerDesc.createReference(false, false, 42, null);
    assertThat(server().beanState(bean).isUnmodifiable()).isFalse();

    bean = customerDesc.createReference(42, null);
    assertThat(server().beanState(bean).isUnmodifiable()).isFalse();
  }

  @Test
  public void createReference_when_disabledLazyLoad() {

    Customer bean = customerDesc.createReference(false, true, 42, null);
    assertThat(server().beanState(bean).isDisableLazyLoad()).isTrue();
  }

  @Test
  public void allProperties() {

    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    Collection<? extends Property> props = desc.allProperties();

    assertThat(props).extracting("name").contains("id", "status", "orderDate", "shipDate");
  }

  @Test
  public void matchBaseTable() {
    BeanDescriptor<Customer> desc = getBeanDescriptor(Customer.class);
    assertTrue(desc.matchBaseTable("o_customer"));
  }

  @Test
  public void matchBaseTable_whenTableHasSchema_expect_matchRegardlessOfSchema() {

    DeployBeanDescriptor<Customer> deploy = mockDeployCustomer();

    when(deploy.getBaseTable()).thenReturn("foo.o_customer");
    BeanDescriptor<?> desc1 = new BeanDescriptor<>(mockOwner(), deploy);
    assertTrue(desc1.matchBaseTable("o_customer"));

    when(deploy.getBaseTable()).thenReturn("bar.o_customer");
    BeanDescriptor<?> desc2 = new BeanDescriptor<>(mockOwner(), deploy);
    assertTrue(desc2.matchBaseTable("o_customer"));
  }

  @SuppressWarnings("unchecked")
  private DeployBeanDescriptor<Customer> mockDeployCustomer() {
    DeployBeanDescriptor<Customer> deploy = mock(DeployBeanDescriptor.class);
    when(deploy.getBeanType()).thenReturn(Customer.class);
    when(deploy.getIdentityMode()).thenReturn(DeployIdentityMode.auto());
    when(deploy.buildIdentityMode()).thenReturn(IdentityMode.NONE);
    when(deploy.getCacheOptions()).thenReturn(CacheOptions.NO_CACHING);
    return deploy;
  }

  private BeanDescriptorMap mockOwner() {
    BeanDescriptorMap owner = mock(BeanDescriptorMap.class);
    return owner;
  }

  @Test
  public void merge_when_empty() {

    Customer from = new Customer();
    from.setId(42);
    from.setName("rob");

    Customer to = new Customer();
    customerDesc.merge((EntityBean) from, (EntityBean) to);

    assertThat(to.getId()).isEqualTo(42);
    assertThat(to.getName()).isEqualTo("rob");
  }

  @Test
  public void isIdTypeExternal_when_externalId() {

    BeanDescriptor<Country> countryDesc = spiEbeanServer().descriptor(Country.class);
    assertThat(countryDesc.isIdGeneratedValue()).isFalse();
  }

  @Test
  public void isIdTypeExternal_when_platformGenerator_noGeneratedValueAnnotation() {

    assertThat(customerDesc.isIdGeneratedValue()).isFalse();
  }

  @Test
  public void isIdTypeExternal_when_explicitGeneratedValue() {

    BeanDescriptor<Contact> desc = spiEbeanServer().descriptor(Contact.class);
    assertThat(desc.isIdGeneratedValue()).isTrue();
  }

  @Test
  public void isIdTypeExternal_when_uuidGenerator_and_generatedValue() {

    BeanDescriptor<BSite> desc = spiEbeanServer().descriptor(BSite.class);
    assertThat(desc.isIdGeneratedValue()).isTrue();
  }

  @Test
  public void isIdTypeExternal_when_uuidGenerator_and_noGeneratedValue() {

    BeanDescriptor<BUser> desc = spiEbeanServer().descriptor(BUser.class);
    assertThat(desc.isIdGeneratedValue()).isFalse();
  }
}
