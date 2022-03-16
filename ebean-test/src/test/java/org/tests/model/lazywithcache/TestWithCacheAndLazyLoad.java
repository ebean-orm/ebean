package org.tests.model.lazywithcache;

import io.ebean.BaseTestCase;
import io.ebean.DB;
import io.ebean.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test with bean cache and lazy loaded property.
 *
 * @author Noemi Szemenyei, FOCONIS AG
 */
class TestWithCacheAndLazyLoad extends BaseTestCase {

  @Test
  void testGetters() {

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

  @Test
  void testBatch() {

    for (int i = 0; i < 2; i++) {
      ChildWithCache child = new ChildWithCache();
      child.setId(1000L + i);
      child.setName("Child With Cache " + i);
      child.setAddress("Address");
      DB.save(child);

      ParentA parentA = new ParentA();
      parentA.setId(1000L + i);
      parentA.setName("Parent A");
      parentA.setChild(child);
      DB.save(parentA);
    }
    // 0. load cache
    ParentA temp = DB.find(ParentA.class, 1000L);
    temp.getChild().getName();

    try (Transaction txn = DB.beginTransaction()) {
      // 1. FindList
      List<ParentA> list = DB.find(ParentA.class).where().ge("id", 1000L).orderBy("id").findList();
      // 2. FindById (first element from list)
      temp = DB.find(ParentA.class, 1000L);
      assertNotNull(temp.getChild().getName());
      // 3. access elements in list
      assertNotNull(list.get(0).getChild().getAddress());
      assertNotNull(list.get(1).getChild().getAddress());
    }
  }

}
