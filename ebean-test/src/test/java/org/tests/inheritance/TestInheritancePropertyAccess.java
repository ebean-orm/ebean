package org.tests.inheritance;

import io.ebean.DB;
import io.ebean.plugin.Property;
import io.ebeaninternal.server.el.ElPropertyValue;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.Animal;
import org.tests.model.basic.Cat;
import org.tests.model.basic.Dog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Roland Praml, FOCONIS AG
 */
public class TestInheritancePropertyAccess {

  @Test
  void test() {
    Cat cat = new Cat();
    cat.setName("Tom");
    DB.save(cat);

    Dog dog = new Dog();
    dog.setRegistrationNumber("FOO");
    DB.save(dog);

    Property prop = DB.getDefault().pluginApi().beanType(Animal.class).property("name");

    Animal animal = DB.find(Animal.class, cat.getId());
    assertThat(prop.value(animal)).isEqualTo("Tom");

    animal = DB.find(Animal.class, dog.getId());
    assertThat(prop.value(animal)).isNull();

    ((ElPropertyValue )prop).pathSet(cat, "Jerry");
    assertThat(cat.getName()).isEqualTo("Jerry");

    assertThatThrownBy(()->((ElPropertyValue )prop).pathSet(dog, "Jerry"))
      .isInstanceOf(RuntimeException.class);

  }
}
