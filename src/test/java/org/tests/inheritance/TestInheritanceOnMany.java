package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.common.BeanList;
import org.ebeantest.LoggedSqlCollector;
import org.junit.Test;
import org.tests.model.basic.Address;
import org.tests.model.basic.Animal;
import org.tests.model.basic.AnimalShelter;
import org.tests.model.basic.BigDog;
import org.tests.model.basic.Cat;
import org.tests.model.basic.Dog;
import org.tests.model.basic.Zoo;

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
  public void testManyToOne() {

    AnimalShelter as = new AnimalShelter();
    Ebean.save(as);

    Cat cat = new Cat();
    cat.setShelter(as);
    Dog dog = new Dog();
    dog.setShelter(as);
    BigDog bigDog = new BigDog();
    bigDog.setShelter(as);

    Address addr = new Address();
    addr.setLine1("Line1");
    Ebean.save(addr);

    Zoo zoo = new Zoo();
    zoo.setAddress(addr);
    zoo.setAnyAnimal(bigDog);
    zoo.setCat(cat);
    zoo.setDog(dog);
    zoo.setBigDog(bigDog);

    Ebean.save(zoo);


    Query<Zoo> zooQuery = Ebean.createQuery(Zoo.class).setId(zoo.getId());

    zooQuery.findOne();

    assertThat(sqlOf(zooQuery)).contains("left join animal t1 on t1.id = t0.any_animal_id ");
    assertThat(sqlOf(zooQuery)).contains(" join animal t2 on t2.id = t0.dog_id ");
    assertThat(sqlOf(zooQuery)).doesNotContain("= t0.big_dog_id");
    assertThat(sqlOf(zooQuery)).doesNotContain("= t0.cat_id");



    Query<AnimalShelter> asQuery = Ebean.createQuery(AnimalShelter.class).setId(zoo.getId());
    asQuery.fetch("dogs");
    asQuery.findOne();
    assertThat(sqlOf(asQuery)).contains("left join animal t1 on t1.shelter_id = t0.id and t1.species in ('DOG','BDG')  where t0.id = ?");

    asQuery = Ebean.createQuery(AnimalShelter.class).setId(zoo.getId());
    asQuery.fetch("animals");
    asQuery.findOne();
    //Assertions.assertThat(sqlOf(asQuery)).contains("left join animal t1 on t1.shelter_id = t0.id and t1.species in ('CAT','DOG','BDG')"); not optimal
    assertThat(sqlOf(asQuery)).contains("left join animal t1 on t1.shelter_id = t0.id  where t0.id = ?");

  }

  @Test
  public void testGetReferenceOnConcrete() {

    Dog dog = new Dog();
    dog.setRegistrationNumber("D2");
    Ebean.save(dog);

    LoggedSqlCollector.start();
    // We must hit the database here and load the bean,
    // as Dog could be a BigDog.
    Dog ref = Ebean.getReference(Dog.class, dog.getId());

    List<String> sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(1);
    assertNotNull(ref);

    // not longer lazy loading
    LoggedSqlCollector.start();
    assertEquals("D2", ref.getRegistrationNumber());
    sql = LoggedSqlCollector.stop();
    assertThat(sql).isEmpty();
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
    assertThat(trimSql(sql.get(0), 2)).contains("select t0.species, t0.id, t0.species, t0.version, t0.shelter_id, t0.name, t0.species, t0.registration_number, t0.date_of_birth, t0.dog_size from animal t0 where t0.id = ?");
    assertNotNull(animal);

    // not longer lazy loading
    LoggedSqlCollector.start();
    assertNotNull(animal.getVersion());
    sql = LoggedSqlCollector.stop();
    assertThat(sql).hasSize(0);
  }
}
