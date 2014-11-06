package com.avaje.tests.text.json;

import java.sql.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.text.json.JsonContext;
import com.avaje.tests.model.basic.Animal;
import com.avaje.tests.model.basic.Cat;
import com.avaje.tests.model.basic.Dog;

public class TestJsonInheritanceDiscriminator extends BaseTestCase {

  @Test
  public void testNoDiscriminator() {

    Cat cat = new Cat();
    cat.setName("Gemma");

    Ebean.save(cat);

    JsonContext json = Ebean.createJsonContext();
    String jsonContent = json.toJsonString(cat);

    Cat cat2 = json.toBean(Cat.class, jsonContent);

    Assert.assertEquals(cat.getId(), cat2.getId());
    Assert.assertEquals(cat.getName(), cat2.getName());
    Assert.assertEquals(cat.getVersion(), cat2.getVersion());

    String noDiscriminator = "{\"id\":1,\"name\":\"Gemma\",\"version\":1}";

    Cat cat3 = json.toBean(Cat.class, noDiscriminator);

    Assert.assertEquals(cat.getId(), cat3.getId());
    Assert.assertEquals(cat.getName(), cat3.getName());
    Assert.assertEquals(cat.getVersion(), cat3.getVersion());

    Dog dog = new Dog();
    dog.setRegistrationNumber("ABC123");
    dog.setDateOfBirth(new Date(System.currentTimeMillis()));

    Ebean.save(dog);

    List<Animal> animals = Ebean.find(Animal.class).findList();

    String listJson = json.toJsonString(animals);

    List<Animal> animals2 = json.toList(Animal.class, listJson);
    Assert.assertEquals(animals.size(), animals2.size());

    String noDiscList = "[{\"id\":1,\"name\":\"Gemma\",\"version\":1},{\"name\":\"PussCat\",\"version\":1},{\"species\":\"CAT\",\"name\":\"PussCat\",\"version\":1}]";
    List<Cat> cats = json.toList(Cat.class, noDiscList);
    Assert.assertEquals(cats.size(), 3);

  }

}
