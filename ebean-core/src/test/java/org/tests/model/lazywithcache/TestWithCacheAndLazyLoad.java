package org.tests.model.lazywithcache;

import org.junit.Test;

import io.ebean.BaseTestCase;
import io.ebean.DB;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test with bean cache and lazy loaded property.
 *
 * @author Noemi Szemenyei, FOCONIS AG
 *
 */
public class TestWithCacheAndLazyLoad extends BaseTestCase{

  @Test
  public void testGetters() {

    ChildWithCache child = new ChildWithCache();
    child.setId(1L);
    child.setName("Child With Cache");
    child.setAddress("Address");
    DB.save(child);

    ParentA parentA = new ParentA();
    parentA.setId(1L);
    parentA.setName("Parent A");
    parentA.setChild(child);
    DB.save(parentA);

    ParentB parentB = new ParentB();
    parentB.setId(1L);
    parentB.setChild(child);
    DB.save(parentB);


    ParentA tempA = DB.find(ParentA.class, 1L);
    tempA.getChild().getName(); // load name

    ParentB tempB = DB.find(ParentB.class, 1L);

    ChildWithCache temp = tempB.getChild();
    // if the next line is commented out, the test passes
    temp.getName(); // load name from cache --> ebean_intercept.loadedFromCache = true

    assertThat(temp.getAddress()).isEqualTo("Address");
    assertThat(temp.getName()).isEqualTo("Child With Cache");
  }

}
