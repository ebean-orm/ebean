package org.tests.text.json;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.text.json.JsonContext;
import org.junit.Test;
import org.tests.model.basic.Animal;
import org.tests.model.basic.Cat;
import org.tests.model.basic.Dog;

import java.sql.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestJsonInheritanceDiscriminator extends BaseTestCase {

  @Test
  public void testNoDiscriminator() {

    Cat cat = new Cat();
    cat.setName("Gemma");

    Ebean.save(cat);

    JsonContext json = Ebean.json();
    String jsonContent = json.toJson(cat);

    Cat cat2 = json.toBean(Cat.class, jsonContent);

    assertEquals(cat.getId(), cat2.getId());
    assertEquals(cat.getName(), cat2.getName());
    assertEquals(cat.getVersion(), cat2.getVersion());

    String noDiscriminator = "{\"id\":1,\"name\":\"Gemma\",\"version\":1}";

    Cat cat3 = json.toBean(Cat.class, noDiscriminator);

    assertEquals(1L, cat3.getId().longValue());
    assertEquals("Gemma", cat3.getName());
    assertEquals(1L, cat3.getVersion().longValue());

    Dog dog = new Dog();
    dog.setRegistrationNumber("ABC123");
    dog.setDateOfBirth(new Date(System.currentTimeMillis()));

    Ebean.save(dog);

    List<Animal> animals = Ebean.find(Animal.class).findList();

    String listJson = json.toJson(animals);

    List<Animal> animals2 = json.toList(Animal.class, listJson);
    assertEquals(animals.size(), animals2.size());

    String noDiscList = "[{\"id\":1,\"name\":\"Gemma\",\"version\":1},{\"name\":\"PussCat\",\"version\":1},{\"species\":\"CAT\",\"name\":\"PussCat\",\"version\":1}]";
    List<Cat> cats = json.toList(Cat.class, noDiscList);
    assertEquals(cats.size(), 3);

  }

}
