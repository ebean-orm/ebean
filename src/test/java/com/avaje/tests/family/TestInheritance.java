
package com.avaje.tests.family;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.avaje.tests.lib.EbeanTestCase;
import com.avaje.tests.model.family.ChildPerson;
import com.avaje.tests.model.family.GrandParentPerson;
import com.avaje.tests.model.family.ParentPerson;



public class TestInheritance extends EbeanTestCase {


  @Test
  public void testInheritance() {

    ChildPerson child1 = new ChildPerson();
    child1.setAge(13);
    child1.setName("Fred");

    ChildPerson child2 = new ChildPerson();
    child2.setAge(8);
    child2.setFamilyName("Baz");
    child2.setName("Julia");

    ChildPerson child3 = new ChildPerson();
    child3.setAge(36);
    child3.setName("Roland");

    ParentPerson parent1 = new ParentPerson();
    parent1.setAge(40);
    parent1.setName("Maria");
    parent1.setFamilyName("Bar");
    parent1.getChildren().add(child1);
    parent1.getChildren().add(child2);

    ParentPerson parent2 = new ParentPerson();
    parent2.setAge(50);
    parent2.setName("Sandra");
    parent2.getChildren().add(child3);
    parent2.setAddress("Berlin");


    ParentPerson parent3 = new ParentPerson();
    parent3.setAge(60);
    parent3.setName("Michael");

    GrandParentPerson grandparent1 = new GrandParentPerson();
    grandparent1.setAge(65);
    grandparent1.setName("Josef");
    grandparent1.setFamilyName("Foo");
    grandparent1.setAddress("Munich");

    grandparent1.getChildren().add(parent1);
    grandparent1.getChildren().add(parent2);
    grandparent1.getChildren().add(parent3);

    getServer().save(grandparent1);
    // Test setup complete, so retrieve bean from db
    grandparent1 = getServer().find(GrandParentPerson.class).setId(grandparent1.getIdentifier()).findUnique();
    
    assertNotNull(grandparent1);
    // check if aggregation works
    assertEquals(3, grandparent1.getChildCount().intValue());
    assertEquals(150, grandparent1.getTotalAge().intValue());
    
    // check normal properties
    assertEquals("Josef", grandparent1.getName());
    assertEquals("Foo", grandparent1.getFamilyName());
    assertEquals("Munich", grandparent1.getAddress());
    
    // now check children of grandparent
    parent1 = grandparent1.getChildren().get(0);
    parent2 = grandparent1.getChildren().get(1);
    parent3 = grandparent1.getChildren().get(2);
    
    assertNotNull(parent1);
    assertNotNull(parent2);
    assertNotNull(parent3);
    // parent1
    assertEquals("Maria", parent1.getName());
    assertEquals("Bar", parent1.getFamilyName()); // overwritten family name
    assertEquals("Bar", parent1.getEffectiveFamilyName()); // test inheritance
    assertNull(parent1.getAddress()); // no alternative address set 
    assertEquals("Munich", parent1.getEffectiveAddress()); // -> inherit munich
    assertEquals(2, parent1.getChildCount().intValue());
    assertEquals(21, parent1.getTotalAge().intValue());
    
    // parent2
    assertEquals("Sandra", parent2.getName());
    assertNull(parent2.getFamilyName());
    assertEquals("Foo", parent2.getEffectiveFamilyName());
    assertEquals("Berlin", parent2.getAddress());
    assertEquals("Berlin", parent2.getEffectiveAddress());
    assertEquals(1, parent2.getChildCount().intValue());
    assertEquals(36, parent2.getTotalAge().intValue());
    
    // parent3
    assertEquals("Michael", parent3.getName());
    assertNull(parent3.getFamilyName());
    assertEquals("Foo", parent3.getEffectiveFamilyName());
    assertNull(parent3.getAddress());
    assertEquals("Munich", parent3.getEffectiveAddress());
    assertEquals(0, parent3.getChildCount().intValue());
    assertEquals(0, parent3.getTotalAge().intValue());
    
    
    child1 = parent1.getChildren().get(0);
    child2 = parent1.getChildren().get(1);
    
    child3 = parent2.getChildren().get(0);
    
    assertNotNull(child1);
    assertNotNull(child2);
    assertNotNull(child3);
    
    assertEquals("Bar", child1.getEffectiveFamilyName());
    assertEquals("Munich", child1.getEffectiveAddress());
    
    assertEquals("Baz", child2.getEffectiveFamilyName());
    assertEquals("Munich", child2.getEffectiveAddress());
    
    assertEquals("Foo", child3.getEffectiveFamilyName());
    assertEquals("Berlin", child3.getEffectiveAddress());
    
  }
}
