package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.common.BeanList;
import io.ebean.test.LoggedSql;
import org.junit.jupiter.api.Test;
import org.tests.model.basic.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestInheritanceOnMany extends BaseTestCase {

  @Test
  public void test() {

    Cat cat = new Cat();
    cat.setName("Puss");
    DB.save(cat);

    Dog dog = new Dog();
    dog.setRegistrationNumber("DOGGIE");
    DB.save(dog);

    BigDog bd = new BigDog();
    bd.setDogSize("large");
    bd.setRegistrationNumber("BG1");
    DB.save(bd);

    AnimalShelter shelter = new AnimalShelter();
    shelter.setName("My Animal Shelter");
    shelter.getAnimals().add(cat);
    shelter.getAnimals().add(dog);

    DB.save(shelter);

    AnimalShelter shelter2 = DB.find(AnimalShelter.class, shelter.getId());
    List<Animal> animals = shelter2.getAnimals();

    BeanList<?> beanList = (BeanList<?>) animals;
    ModifyListenMode modifyListenMode = beanList.getModifyListenMode();

    assertNotNull(modifyListenMode);

    assertNotNull(DB.find(Animal.class).findList());
  }

  @Test
  public void testGetReferenceOnConcrete() {

    Dog dog = new Dog();
    dog.setRegistrationNumber("D2");
    DB.save(dog);

    LoggedSql.start();
    // Dog is concrete so we return as Dog even though
    // it could be a BigDog (so we are trusting the caller)
    Dog ref = DB.reference(Dog.class, dog.getId());

    List<String> sql = LoggedSql.stop();
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
    DB.save(bd);

    LoggedSql.start();
    BigDog bigDog = DB.reference(BigDog.class, bd.getId());

    List<String> sql = LoggedSql.stop();
    assertThat(sql).isEmpty();
    assertNotNull(bigDog);

    // invoke lazy loading
    assertEquals("BG2", bigDog.getRegistrationNumber());


    LoggedSql.start();
    // Animal is abstract so we hit the DB
    Animal animal = DB.reference(Animal.class, bd.getId());

    sql = LoggedSql.stop();
    assertThat(sql).hasSize(1);
    assertThat(trimSql(sql.get(0), 2)).contains("select t0.species, t0.id from animal t0 where t0.id = ?");
    assertNotNull(animal);

    // invoke lazy loading
    assertNotNull(animal.getVersion());
  }
}
