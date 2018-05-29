package io.ebeaninternal.server.deploy;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.EntityBean;
import io.ebean.plugin.Property;
import org.junit.Test;
import org.tests.model.basic.Animal;
import org.tests.model.basic.AnimalShelter;
import org.tests.model.basic.Cat;
import org.tests.model.basic.Contact;
import org.tests.model.basic.Country;
import org.tests.model.basic.Customer;
import org.tests.model.basic.Dog;
import org.tests.model.basic.Order;
import org.tests.model.bridge.BSite;
import org.tests.model.bridge.BUser;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanDescriptorTest extends BaseTestCase {

  private BeanDescriptor<Customer> customerDesc = spiEbeanServer().getBeanDescriptor(Customer.class);

  @Test
  public void createReference() {

    Customer bean = customerDesc.createReference(null, false, 42, null);
    assertThat(bean.getId()).isEqualTo(42);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();
  }

  @Test
  public void createReference_whenReadOnly() {

    Customer bean = customerDesc.createReference(Boolean.TRUE, false, 42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isTrue();
  }

  @Test
  public void createReference_whenNotReadOnly() {

    Customer bean = customerDesc.createReference(Boolean.FALSE, false, 42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();

    bean = customerDesc.createReference(42, null);
    assertThat(server().getBeanState(bean).isReadOnly()).isFalse();
  }

  @Test
  public void createReference_when_disabledLazyLoad() {

    Customer bean = customerDesc.createReference(Boolean.FALSE, true, 42, null);
    assertThat(server().getBeanState(bean).isDisableLazyLoad()).isTrue();
  }

  @Test
  public void createReference_with_inheritance() {
    Cat cat = new Cat();
    cat.setName("Puss");
    Ebean.save(cat);

    Dog dog = new Dog();
    dog.setRegistrationNumber("DOGGIE");
    Ebean.save(dog);

    AnimalShelter shelter = new AnimalShelter();
    shelter.setName("My Animal Shelter");
    shelter.getAnimals().add(cat);
    shelter.getAnimals().add(dog);

    Ebean.save(shelter);

    BeanDescriptor<Animal> animalDesc = spiEbeanServer().getBeanDescriptor(Animal.class);

    Animal bean = animalDesc.createReference(Boolean.FALSE, false, dog.getId(), null);
    assertThat(bean.getId()).isEqualTo(dog.getId());
  }

  @Test
  public void allProperties() {

    BeanDescriptor<Order> desc = getBeanDescriptor(Order.class);
    Collection<? extends Property> props = desc.allProperties();

    assertThat(props).extracting("name").contains("id", "status", "orderDate", "shipDate");
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

    BeanDescriptor<Country> countryDesc = spiEbeanServer().getBeanDescriptor(Country.class);
    assertThat(countryDesc.isIdGeneratedValue()).isFalse();
  }

  @Test
  public void isIdTypeExternal_when_platformGenerator_noGeneratedValueAnnotation() {

    assertThat(customerDesc.isIdGeneratedValue()).isFalse();
  }

  @Test
  public void isIdTypeExternal_when_explicitGeneratedValue() {

    BeanDescriptor<Contact> desc = spiEbeanServer().getBeanDescriptor(Contact.class);
    assertThat(desc.isIdGeneratedValue()).isTrue();
  }

  @Test
  public void isIdTypeExternal_when_uuidGenerator_and_generatedValue() {

    BeanDescriptor<BSite> desc = spiEbeanServer().getBeanDescriptor(BSite.class);
    assertThat(desc.isIdGeneratedValue()).isTrue();
  }

  @Test
  public void isIdTypeExternal_when_uuidGenerator_and_noGeneratedValue() {

    BeanDescriptor<BUser> desc = spiEbeanServer().getBeanDescriptor(BUser.class);
    assertThat(desc.isIdGeneratedValue()).isFalse();
  }
}
