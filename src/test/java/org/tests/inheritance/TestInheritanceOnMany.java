package org.tests.inheritance;

import io.ebean.BaseTestCase;
import io.ebean.Ebean;
import io.ebean.Query;
import io.ebean.bean.BeanCollection.ModifyListenMode;
import io.ebean.common.BeanList;
import org.tests.model.basic.Animal;
import org.tests.model.basic.AnimalShelter;
import org.tests.model.basic.BigDog;
import org.tests.model.basic.Cat;
import org.tests.model.basic.Dog;
import org.tests.model.basic.Zoo;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

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
    
    Zoo zoo = new Zoo();
    zoo.setAnyAnimal(bigDog);
    zoo.setCat(cat);
    zoo.setDog(dog);
    zoo.setBigDog(bigDog);
    
    Ebean.save(zoo);
    
    
    Query<Zoo> zooQuery = Ebean.createQuery(Zoo.class).setId(zoo.getId());
    
    zooQuery.findOne();
    Assertions.assertThat(sqlOf(zooQuery)).contains("select t0.id, t0.version, "
        + "t1.species, t0.any_animal_id, "
        + "t2.species, t0.dog_id, "
        + "t3.species, t0.big_dog_id, "
        + "t4.species, t0.cat_id from zoo t0 "
        + "left join animal t1 on t1.id = t0.any_animal_id  "
        + "left join animal t2 on t2.id = t0.dog_id  "
        + "left join animal t3 on t3.id = t0.big_dog_id  "
        + "left join animal t4 on t4.id = t0.cat_id  where t0.id = ?  ");
    
    
    
    Query<AnimalShelter> asQuery = Ebean.createQuery(AnimalShelter.class).setId(zoo.getId());
    asQuery.fetch("dogs");
    asQuery.findOne();
    Assertions.assertThat(sqlOf(asQuery)).contains("left join animal t1 on t1.shelter_id = t0.id and t1.species in ('DOG','BDG')  where t0.id = ?");

    asQuery = Ebean.createQuery(AnimalShelter.class).setId(zoo.getId());
    asQuery.fetch("animals");
    asQuery.findOne();
    //Assertions.assertThat(sqlOf(asQuery)).contains("left join animal t1 on t1.shelter_id = t0.id and t1.species in ('CAT','DOG','BDG')"); not optimal
    Assertions.assertThat(sqlOf(asQuery)).contains("left join animal t1 on t1.shelter_id = t0.id  where t0.id = ?");

  }

}
