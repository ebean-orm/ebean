package com.avaje.tests.inheritance;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.avaje.ebean.BaseTestCase;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.bean.BeanCollection.ModifyListenMode;
import com.avaje.ebean.common.BeanList;
import com.avaje.tests.model.basic.Animal;
import com.avaje.tests.model.basic.AnimalShelter;
import com.avaje.tests.model.basic.Cat;
import com.avaje.tests.model.basic.Dog;

public class TestInheritanceOnMany extends BaseTestCase {

  @Test
  public void test() {
    
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
    
    AnimalShelter shelter2 = Ebean.find(AnimalShelter.class, shelter.getId());
    List<Animal> animals = shelter2.getAnimals();
    
    BeanList<?> beanList = (BeanList<?>)animals;
    ModifyListenMode modifyListenMode = beanList.getModifyListenMode();
    
    Assert.assertNotNull(modifyListenMode);
    
  }
  
}
