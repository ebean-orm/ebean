package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.common.BeanList;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Animal;
import org.tests.model.basic.AnimalShelter;
import org.tests.model.basic.BigDog;
import org.tests.model.basic.Cat;
import org.tests.model.basic.Dog;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestInheritanceOnMany extends BaseTestCase {

  @Test
  public void test() {

    Cat cat = new Cat();
    cat.setName("Puss");
    Ebean.save(cat);

    Dog dog = new Dog();
    dog.setRegistrationNumber("DOGGIE");
    Ebean.save(dog);

    BigDog bd = new BigDog();
    bd.setDogSize("large");
    bd.setRegistrationNumber("BG1");
    Ebean.save(bd);

    AnimalShelter shelter = new AnimalShelter();
    shelter.setName("My Animal Shelter");
    shelter.getAnimals().add(cat);
    shelter.getAnimals().add(dog);

    Ebean.save(shelter);

    AnimalShelter shelter2 = Ebean.find(AnimalShelter.class, shelter.getId());
    List<Animal> animals = shelter2.getAnimals();

    BeanList<?> beanList = (BeanList<?>) animals;
    ModifyListenMode modifyListenMode = beanList.getModifyListenMode();

    assertNotNull(modifyListenMode);

    assertNotNull(Ebean.find(Animal.class).findList());
  }

  @Test
  public void testGetReferenceOnConcrete() {

    Dog dog = new Dog();
    dog.setRegistrationNumber("D2");
    Ebean.save(dog);

    LoggedSqlCollector.start();
    // Dog is concrete so we return as Dog even though
    // it could be a BigDog (so we are trusting the caller)
    Dog ref = Ebean.getReference(Dog.class, dog.getId());

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).isEmpty();
    assertNotNull(ref);

    // invoke lazy loading
    assertEquals("D2", ref.getRegistrationNumber());
  }

  @Test
  public void testGetReferenceOnLeaf() {

    BigDog bd = new BigDog();
    bd.setDogSize("large");
    bd.setRegistrationNumber("BG2");
    Ebean.save(bd);

    LoggedSqlCollector.start();
    BigDog bigDog = Ebean.getReference(BigDog.class, bd.getId());

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).isEmpty();
    assertNotNull(bigDog);

    // invoke lazy loading
    assertEquals("BG2", bigDog.getRegistrationNumber());


    LoggedSqlCollector.start();
    // Animal is abstract so we hit the DB
    Animal animal = Ebean.getReference(Animal.class, bd.getId());

    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertThat(trimSql(sql.get(0), 2)).contains("select t0.species, t0.id from animal t0 where t0.id = ?");
    assertNotNull(animal);

    // invoke lazy loading
    assertNotNull(animal.getVersion());
  }
}
