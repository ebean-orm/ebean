package org.tests.inheritance;

import io.ebean.DB;
import io.ebean.plugin.ExpressionPath;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Roland Praml, FOCONIS AG
 */
public class TestPropertyAccess {


  private ExpressionPath custCretime = DB.getDefault().pluginApi().beanType(Customer.class).expressionPath("cretime");
  private ExpressionPath custName = DB.getDefault().pluginApi().beanType(Customer.class).expressionPath("name");
  // Note: Animal.name is only present in "Cat" not in "Dog"
  private ExpressionPath animalName = DB.getDefault().pluginApi().beanType(Animal.class).expressionPath("name");
  private ExpressionPath productName = DB.getDefault().pluginApi().beanType(Product.class).expressionPath("name");
  private ExpressionPath animalSpecies = DB.getDefault().pluginApi().beanType(Animal.class).expressionPath("species");

  @Test
  void testOnInheritance() {
    Cat cat = new Cat();
    cat.setName("Tom");
    DB.save(cat);

    Dog dog = new Dog();
    dog.setRegistrationNumber("FOO");
    DB.save(dog);


    Animal animal = DB.find(Animal.class, cat.getId());
    assertThat(animalName.pathGet(animal)).isEqualTo("Tom");

    animal = DB.find(Animal.class, dog.getId());
    assertThat(animalName.pathGet(animal)).isNull();

    animalName.pathSet(cat, "Jerry");
    assertThat(cat.getName()).isEqualTo("Jerry");

    assertThatThrownBy(() -> animalName.pathSet(dog, "Jerry"))
      .isInstanceOf(IllegalArgumentException.class);

    animalSpecies.pathSet(cat, "Angora");
    animalSpecies.pathSet(dog, "Bulldog");

    assertThat(cat.getSpecies()).isEqualTo("Angora");
    assertThat(dog.getSpecies()).isEqualTo("Bulldog");
  }

  @Test
  void testOnMappedSuperClass() {
    Customer cust = new Customer();

    Timestamp ts = new Timestamp(123);
    custCretime.pathSet(cust, ts);
    assertThat(custCretime.pathGet(cust)).isEqualTo(ts);

    custName.pathSet(cust, "Roland");
    assertThat(custName.pathGet(cust)).isEqualTo("Roland");

  }

  @Test
  void testOnPlainBean() {
    Product product = new Product();

    productName.pathSet(product, "Roland");
    assertThat(productName.pathGet(product)).isEqualTo("Roland");
  }

  @Test
  void testOnContactGroup() {
    ContactGroup cg = new ContactGroup();

    // CHECKEM: Ist it OK to us the "custCretime" on "contactGroup"
    assertThatThrownBy(() -> custCretime.pathGet(cg)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void testOnCrossUsage() {
    Product product = new Product();
    Customer cust = new Customer();
    Cat cat = new Cat();
    Dog dog = new Dog();


    assertThatThrownBy(() -> custCretime.pathGet(product)).isInstanceOf(IllegalArgumentException.class);
    custCretime.pathGet(cust);
    assertThatThrownBy(() -> custCretime.pathGet(cat)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> custCretime.pathGet(dog)).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> custName.pathGet(product)).isInstanceOf(IllegalArgumentException.class);
    custName.pathGet(cust);
    assertThatThrownBy(() -> custName.pathGet(cat)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> custName.pathGet(dog)).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> animalName.pathGet(product)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> animalName.pathGet(cust)).isInstanceOf(IllegalArgumentException.class);
    animalName.pathGet(cat);
    animalName.pathGet(dog);

    productName.pathGet(product);
    assertThatThrownBy(() -> productName.pathGet(cust)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> productName.pathGet(cat)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> productName.pathGet(dog)).isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> animalSpecies.pathGet(product)).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> animalSpecies.pathGet(cust)).isInstanceOf(IllegalArgumentException.class);
    animalSpecies.pathGet(cat);
    animalSpecies.pathGet(dog);
  }
}
